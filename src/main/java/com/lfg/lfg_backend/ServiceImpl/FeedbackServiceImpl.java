package com.lfg.lfg_backend.ServiceImpl;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.mapper.FeedbackMapper;
import com.lfg.lfg_backend.model.Event;
import com.lfg.lfg_backend.model.Feedback;
import com.lfg.lfg_backend.model.User;
import com.lfg.lfg_backend.repository.EventRepository;
import com.lfg.lfg_backend.repository.FeedbackRepository;
import com.lfg.lfg_backend.repository.JoinRequestRepository;
import com.lfg.lfg_backend.repository.UserRepository;
import com.lfg.lfg_backend.service.FeedbackService;
import com.lfg.lfg_backend.service.NotificationService;
import com.lfg.lfg_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    @Override
    public FeedbackResponse createFeedback(User fromUser, FeedbackRequest request) {
        if (fromUser.getId().equals(request.getToUserId())) {
            throw new RuntimeException("Non puoi lasciare feedback a te stesso");
        }

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new RuntimeException("Utente destinatario non trovato"));

        boolean fromUserPartecipante = joinRequestRepository.existsByUserIdAndEventIdAndStatusAccepted(
                fromUser.getId(), event.getId());
        if (!fromUserPartecipante) {
            throw new RuntimeException("Puoi lasciare feedback solo se hai partecipato all’evento");
        }
        if (userService.isUserBanned(fromUser)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + fromUser.getBannedUntil());
        }

        boolean alreadyGiven = feedbackRepository.existsByFromUserIdAndToUserIdAndEventId(
                fromUser.getId(), toUser.getId(), event.getId());
        if (alreadyGiven) {
            throw new RuntimeException("Hai già lasciato feedback a questo utente per questo evento");
        }

        Feedback feedback = Feedback.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .event(event)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDate.now())
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        notificationService.createNotification(
                toUser,
                "Hai ricevuto un nuovo feedback da " + fromUser.getUsername() + "!",
                "FEEDBACK_RECEIVED"
        );

        return mapToResponse(saved);
    }

    @Override
    public Optional<FeedbackResponse> getFeedbackById(UUID id) {
        return feedbackRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Override
    public Page<FeedbackResponse> getAllFeedback(Pageable pageable) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public double getAverageRatingForUser(UUID userId) {
        Double average = feedbackRepository.findAverageRatingByToUserId(userId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    @Override
    public ReputationDTO calculateReputation(User user) {
        UUID userId = user.getId();

        Double average = feedbackRepository.findAverageRatingByToUserId(userId);
        int feedbackCount = feedbackRepository.findByToUserId(userId).size();
        int eventCount = joinRequestRepository.findByUserId(userId).size();

        double averageRating = average != null ? average : 0.0;

        double score = (averageRating / 5.0) * 60 +
                Math.min(feedbackCount * 5, 20) +
                Math.min(eventCount * 2, 20);
        double roundedScore = Math.round(score * 10.0) / 10.0;

        int xp = (int) Math.floor(roundedScore);
        int level = xp / 50 + 1;
        int xpToNextLevel = 50 - (xp % 50);

        return new ReputationDTO(
                userId,
                Math.round(averageRating * 10.0) / 10.0,
                feedbackCount,
                eventCount,
                roundedScore,
                xp,
                level,
                xpToNextLevel
        );
    }

    @Override
    public List<LeaderboardEntryDTO> getLeaderboard(int limit) {
        return userRepository.findAll().stream()
                .map(user -> {
                    ReputationDTO rep = calculateReputation(user);
                    return new LeaderboardEntryDTO(
                            user.getId(),
                            user.getUsername(),
                            rep.xp(),
                            rep.level()
                    );
                })
                .sorted((a, b) -> Integer.compare(b.xp(), a.xp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getFromUser().getId(),
                feedback.getToUser().getId(),
                feedback.getEvent().getId(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getCreatedAt()
        );
    }

    @Override
    public Page<FeedbackResponse> getFeedbacksReceivedByUser(User user, Pageable pageable) {
        return feedbackRepository.findByToUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<FeedbackResponse> getFeedbacksSentByUser(User user, Pageable pageable) {
        return feedbackRepository.findByFromUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<FeedbackResponse> getFeedbacksByFromUser(UUID userId, Pageable pageable) {
        return feedbackRepository.findByFromUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<FeedbackDTO> getFeedbacksForEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        List<Feedback> feedbacks = feedbackRepository.findByEventId(eventId);

        return feedbacks.stream()
                .filter(f -> f.getToUser().getId().equals(event.getCreator().getId())) // solo feedback al creatore
                .map(FeedbackMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public double calculateAverageRatingForEvent(UUID eventId) {
        List<Feedback> feedbacks = feedbackRepository.findByEventId(eventId);
        return feedbacks.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
    }
}
