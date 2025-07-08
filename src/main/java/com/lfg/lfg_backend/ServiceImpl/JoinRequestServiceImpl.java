package com.lfg.lfg_backend.ServiceImpl;

import com.lfg.lfg_backend.dto.JoinRequestDTO;
import com.lfg.lfg_backend.dto.ParticipantDTO;
import com.lfg.lfg_backend.mapper.JoinRequestMapper;
import com.lfg.lfg_backend.model.*;
import com.lfg.lfg_backend.model.enums.JoinMode;
import com.lfg.lfg_backend.model.enums.JoinStatus;
import com.lfg.lfg_backend.repository.EventRepository;
import com.lfg.lfg_backend.repository.JoinRequestRepository;
import com.lfg.lfg_backend.service.FeedbackService;
import com.lfg.lfg_backend.service.JoinRequestService;
import com.lfg.lfg_backend.service.NotificationService;
import com.lfg.lfg_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JoinRequestServiceImpl implements JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;
    private final EventRepository eventRepository;
    private final FeedbackService feedbackService;
    private final NotificationService notificationService;
    private final UserService userService;

    @Override
    public JoinRequestDTO createJoinRequest(User user, UUID eventId, String message) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        if (userService.isUserBanned(user)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + user.getBannedUntil());
        }

        long approvedCount = joinRequestRepository.countByEventIdAndStatus(eventId, JoinStatus.APPROVED);
        if (approvedCount >= event.getMaxParticipants()) {
            throw new IllegalStateException("Evento pieno. Non è possibile partecipare.");
        }

        JoinStatus status = event.getJoinMode() == JoinMode.AUTO ? JoinStatus.APPROVED : JoinStatus.PENDING;

        JoinRequest request = JoinRequest.builder()
                .event(event)
                .user(user)
                .requestMessage(message)
                .status(status)
                .createdAt(LocalDate.now())
                .build();

        JoinRequest saved = joinRequestRepository.save(request);

        if (status == JoinStatus.APPROVED) {
            notificationService.createNotification(
                    user,
                    "Sei stato aggiunto automaticamente all'evento '" + event.getTitle() + "'!",
                    "JOIN_REQUEST_AUTO_APPROVED"
            );
        }

        return JoinRequestMapper.toDTO(saved);
    }

    @Override
    public List<JoinRequestDTO> getJoinRequestsOfUser(User user) {
        return joinRequestRepository.findByUserId(user.getId())
                .stream()
                .map(JoinRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<JoinRequestDTO> getJoinRequestsForEvent(UUID eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        boolean isCreator = event.getCreator().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isCreator && !isAdmin) {
            throw new RuntimeException("Non autorizzato a visualizzare le richieste per questo evento");
        }

        return joinRequestRepository.findByEventId(eventId)
                .stream()
                .map(JoinRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipantDTO> getApprovedParticipants(UUID eventId, User requester) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        boolean isCreator = event.getCreator().getId().equals(requester.getId());
        boolean isApproved = joinRequestRepository.findByEventId(eventId).stream()
                .anyMatch(jr -> jr.getUser().getId().equals(requester.getId()) && jr.getStatus() == JoinStatus.APPROVED);

        if (!isCreator && !isApproved) {
            throw new RuntimeException("Non autorizzato a visualizzare i partecipanti");
        }

        return joinRequestRepository.findByEventId(eventId).stream()
                .filter(jr -> jr.getStatus() == JoinStatus.APPROVED)
                .map(jr -> {
                    User participant = jr.getUser();
                    int level = feedbackService.calculateReputation(participant).level();
                    return new ParticipantDTO(participant.getId(), participant.getUsername(), participant.getEmail(), level);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void approveJoinRequest(UUID requestId, User approver) {
        changeRequestStatus(requestId, approver, JoinStatus.APPROVED);
    }

    @Override
    public void rejectJoinRequest(UUID requestId, User approver) {
        changeRequestStatus(requestId, approver, JoinStatus.REJECTED);
    }

    @Override
    public JoinRequestDTO updateRequestStatus(UUID requestId, JoinStatus status, User approver) {
        changeRequestStatus(requestId, approver, status);
        return joinRequestRepository.findById(requestId)
                .map(JoinRequestMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Richiesta non trovata dopo l'aggiornamento"));
    }

    // --- PRIVATE ---
    private void changeRequestStatus(UUID requestId, User approver, JoinStatus newStatus) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Richiesta non trovata"));

        boolean isCreator = request.getEvent().getCreator().getId().equals(approver.getId());
        boolean isAdmin = approver.getRole().name().equals("ADMIN");

        if (!isCreator && !isAdmin) {
            throw new RuntimeException("Non autorizzato ad approvare o rifiutare questa richiesta");
        }

        request.setStatus(newStatus);
        joinRequestRepository.save(request);

        String eventTitle = request.getEvent().getTitle();
        if (newStatus == JoinStatus.APPROVED) {
            notificationService.createNotification(
                    request.getUser(),
                    "La tua richiesta di partecipazione all'evento '" + eventTitle + "' è stata approvata!",
                    "JOIN_REQUEST_APPROVED"
            );
        } else if (newStatus == JoinStatus.REJECTED) {
            notificationService.createNotification(
                    request.getUser(),
                    "La tua richiesta di partecipazione all'evento '" + eventTitle + "' è stata rifiutata.",
                    "JOIN_REQUEST_REJECTED"
            );
        }
    }
}
