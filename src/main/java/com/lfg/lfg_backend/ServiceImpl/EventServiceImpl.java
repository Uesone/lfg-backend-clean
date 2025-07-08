package com.lfg.lfg_backend.ServiceImpl;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.mapper.EventAdminMapper;
import com.lfg.lfg_backend.mapper.EventMapper;
import com.lfg.lfg_backend.mapper.JoinRequestMapper;
import com.lfg.lfg_backend.mapper.UserMapper;
import com.lfg.lfg_backend.model.Event;
import com.lfg.lfg_backend.model.User;
import com.lfg.lfg_backend.model.enums.JoinStatus;
import com.lfg.lfg_backend.repository.EventRepository;
import com.lfg.lfg_backend.repository.JoinRequestRepository;
import com.lfg.lfg_backend.repository.UserRepository;
import com.lfg.lfg_backend.service.EventService;
import com.lfg.lfg_backend.service.FeedbackService;
import com.lfg.lfg_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final FeedbackService feedbackService;
    private final UserService userService;

    @Override
    public EventDetailsDTO createEvent(EventCreateDTO eventDTO, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (userService.isUserBanned(creator)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + creator.getBannedUntil());
        }

        Event event = Event.builder()
                .title(eventDTO.getTitle())
                .activityType(eventDTO.getActivityType())
                .location(eventDTO.getLocation())
                .notes(eventDTO.getNotes())
                .date(eventDTO.getDate())
                .maxParticipants(eventDTO.getMaxParticipants())
                .joinMode(eventDTO.getJoinMode() != null
                        ? com.lfg.lfg_backend.model.enums.JoinMode.valueOf(eventDTO.getJoinMode())
                        : com.lfg.lfg_backend.model.enums.JoinMode.AUTO)
                .creator(creator)
                .createdAt(LocalDateTime.now())
                .build();

        Event saved = eventRepository.save(event);
        return EventMapper.toDetailsDTO(saved);
    }

    @Override
    public void updateEvent(UUID eventId, EventUpdateDTO updateDTO, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (userService.isUserBanned(user)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + user.getBannedUntil());
        }

        if (!event.getCreator().getId().equals(user.getId())) {
            throw new SecurityException("Non sei il creatore dell'evento");
        }

        if (updateDTO.getTitle() != null) event.setTitle(updateDTO.getTitle());
        if (updateDTO.getActivityType() != null) event.setActivityType(updateDTO.getActivityType());
        if (updateDTO.getLocation() != null) event.setLocation(updateDTO.getLocation());
        if (updateDTO.getDate() != null) event.setDate(updateDTO.getDate());
        if (updateDTO.getNotes() != null) event.setNotes(updateDTO.getNotes());
        if (updateDTO.getJoinMode() != null) event.setJoinMode(updateDTO.getJoinMode());
        if (updateDTO.getMaxParticipants() > 0) event.setMaxParticipants(updateDTO.getMaxParticipants());

        eventRepository.save(event);
    }

    @Override
    public void deleteEvent(UUID eventId, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (userService.isUserBanned(user)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + user.getBannedUntil());
        }

        if (!event.getCreator().getId().equals(user.getId())) {
            throw new SecurityException("Non sei autorizzato a eliminare questo evento.");
        }

        eventRepository.delete(event);
    }

    @Override
    public List<EventSummaryDTO> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(EventMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EventDetailsDTO getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));
        return EventMapper.toDetailsDTO(event);
    }

    @Override
    public List<EventFeedDTO> getSuggestedEventsForUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<UUID> excludedEventIds = joinRequestRepository.findByUserId(user.getId()).stream()
                .map(jr -> jr.getEvent().getId())
                .toList();
        Set<String> userActivities = joinRequestRepository.findByUserId(user.getId()).stream()
                .map(jr -> jr.getEvent().getActivityType())
                .collect(Collectors.toSet());
        return eventRepository.findAll(pageable).getContent().stream()
                .filter(event -> !event.getCreator().getId().equals(user.getId()) &&
                        !excludedEventIds.contains(event.getId()) &&
                        event.getDate().isAfter(LocalDate.now()))
                .map(event -> {
                    int score = 0;
                    int creatorLevel = feedbackService.calculateReputation(event.getCreator()).level();
                    if (creatorLevel >= 5) score += 20;
                    if (user.getCity() != null && user.getCity().equalsIgnoreCase(event.getLocation())) score += 15;
                    if (userActivities.contains(event.getActivityType())) score += 10;
                    long daysToEvent = ChronoUnit.DAYS.between(LocalDate.now(), event.getDate());
                    if (daysToEvent <= 3) score += 10;
                    else if (daysToEvent <= 7) score += 5;
                    return Map.entry(event, score);
                })
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(entry -> {
                    Event e = entry.getKey();
                    int level = feedbackService.calculateReputation(e.getCreator()).level();
                    EventFeedDTO dto = EventMapper.toFeedDTO(e, level);
                    return new EventFeedDTO(
                            dto.id(), dto.title(), dto.activityType(), dto.location(), dto.date(),
                            dto.maxParticipants(), dto.creatorId(), dto.creatorUsername(), level
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getEventParticipants(UUID eventId) {
        return joinRequestRepository.findByEventId(eventId).stream()
                .filter(jr -> jr.getStatus() == JoinStatus.APPROVED)
                .map(jr -> UserMapper.toDTO(jr.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    public List<JoinRequestDTO> getJoinRequestsForEvent(UUID eventId, User requester) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));
        if (!event.getCreator().getId().equals(requester.getId())) {
            throw new RuntimeException("Solo il creatore dell'evento può visualizzare le richieste");
        }
        return joinRequestRepository.findByEventId(eventId).stream()
                .map(JoinRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Object getEventView(UUID eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));
        boolean isCreator = event.getCreator().getId().equals(currentUser.getId());
        boolean isApproved = joinRequestRepository.existsByUserIdAndEventIdAndStatusAccepted(currentUser.getId(), eventId);
        if (isCreator || isApproved) {
            return EventMapper.toDetailsDTO(event);
        } else {
            return EventMapper.toSummaryDTO(event);
        }
    }

    @Override
    public List<EventSummaryDTO> getPublicEvents(String city, String activity, LocalDate after, String sortBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findAll().stream()
                .filter(e -> e.getDate().isAfter(after != null ? after : LocalDate.now()))
                .filter(e -> city == null || e.getLocation().equalsIgnoreCase(city))
                .filter(e -> activity == null || e.getActivityType().equalsIgnoreCase(activity))
                .sorted((a, b) -> {
                    return switch (sortBy) {
                        case "level" -> Integer.compare(
                                feedbackService.calculateReputation(b.getCreator()).level(),
                                feedbackService.calculateReputation(a.getCreator()).level());
                        case "popularity" -> Long.compare(
                                joinRequestRepository.findByEventId(b.getId()).size(),
                                joinRequestRepository.findByEventId(a.getId()).size());
                        default -> a.getDate().compareTo(b.getDate());
                    };
                })
                .skip((long) page * size)
                .limit(size)
                .map(EventMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // --- ADMIN ---
    @Override
    public List<EventAdminDTO> getAllAdminEvents() {
        return eventRepository.findAll().stream()
                .map(EventAdminMapper::toDTO)
                .toList();
    }

    @Override
    public void deleteEventById(UUID id) {
        eventRepository.deleteById(id);
    }
}
