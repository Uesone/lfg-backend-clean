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
import com.lfg.lfg_backend.util.EventTagWhitelist; // <-- IMPORTANTE: Importa la whitelist dei tag
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementazione di EventService con privacy by design e validazione dei tag predefiniti.
 * - Feed mostra solo la città
 * - Dettaglio evento mostra indirizzo solo se utente autorizzato
 * - Supporto geo-based feed (raggio ricerca e distanza)
 * - Solo i tag della whitelist possono essere salvati/aggiornati su eventi
 */
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final FeedbackService feedbackService;
    private final UserService userService;

    /**
     * Valida che tutti i tag inseriti siano nella whitelist (case-insensitive).
     * Lancia IllegalArgumentException se ne trova uno non valido.
     */
    private void validateTags(Set<String> tags) {
        if (tags == null) return; // Nessun tag da validare
        Set<String> allowedLower = EventTagWhitelist.ALLOWED_TAGS
                .stream().map(String::toLowerCase).collect(Collectors.toSet());
        for (String tag : tags) {
            if (!allowedLower.contains(tag.toLowerCase())) {
                throw new IllegalArgumentException("Tag non valido: " + tag);
            }
        }
    }

    @Override
    public EventDetailsDTO createEvent(EventCreateDTO eventDTO, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (userService.isUserBanned(creator)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + creator.getBannedUntil());
        }

        // === Validazione tag predefiniti ===
        validateTags(eventDTO.getTags());

        Event event = Event.builder()
                .title(eventDTO.getTitle())
                .activityType(eventDTO.getActivityType())
                .location(eventDTO.getLocation())
                .city(eventDTO.getCity())
                .notes(eventDTO.getNotes())
                .date(eventDTO.getDate())
                .maxParticipants(eventDTO.getMaxParticipants())
                .joinMode(eventDTO.getJoinMode() != null
                        ? com.lfg.lfg_backend.model.enums.JoinMode.valueOf(eventDTO.getJoinMode())
                        : com.lfg.lfg_backend.model.enums.JoinMode.AUTO)
                .creator(creator)
                .createdAt(LocalDateTime.now())
                .latitude(eventDTO.getLatitude() != null ? eventDTO.getLatitude() : null)
                .longitude(eventDTO.getLongitude() != null ? eventDTO.getLongitude() : null)
                .tags(eventDTO.getTags()) // <-- Salva i tag
                .build();

        Event saved = eventRepository.save(event);
        // Sei il creatore: location sempre visibile!
        return EventMapper.toDetailsDTO(saved, true);
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

        // === Validazione tag predefiniti SOLO se presenti nell'updateDTO ===
        if (updateDTO.getTags() != null) {
            validateTags(updateDTO.getTags());
            event.setTags(updateDTO.getTags());
        }

        if (updateDTO.getTitle() != null) event.setTitle(updateDTO.getTitle());
        if (updateDTO.getActivityType() != null) event.setActivityType(updateDTO.getActivityType());
        if (updateDTO.getLocation() != null) event.setLocation(updateDTO.getLocation());
        if (updateDTO.getCity() != null) event.setCity(updateDTO.getCity());
        if (updateDTO.getDate() != null) event.setDate(updateDTO.getDate());
        if (updateDTO.getNotes() != null) event.setNotes(updateDTO.getNotes());
        if (updateDTO.getJoinMode() != null) event.setJoinMode(updateDTO.getJoinMode());
        if (updateDTO.getMaxParticipants() > 0) event.setMaxParticipants(updateDTO.getMaxParticipants());
        // Qui puoi gestire anche lat/lon

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
        // By default non mostrare location (privacy): puoi cambiare a piacere
        return EventMapper.toDetailsDTO(event, false);
    }

    /**
     * Feed geo-based consigliato: mostra SOLO città e distanza se geo attiva.
     * - Se lat/lon sono null, mostra TUTTI gli eventi (NO filtro distanza).
     * - Se sono presenti, mostra solo entro il raggio.
     * - EventFeedDTO mostra SOLO città (privacy).
     */
    @Override
    public List<EventFeedDTO> getSuggestedEventsForUser(
            User user,
            int page,
            int size,
            Double lat,
            Double lon,
            Double radiusKm
    ) {
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
                        !event.getDate().isBefore(LocalDate.now())
                )
                .filter(event -> {
                    // --- FILTRO DISTANZA GEO SOLO SE GEO ATTIVA ---
                    if (lat == null || lon == null || event.getLatitude() == null || event.getLongitude() == null)
                        return true; // Nessun filtro se dati non disponibili (feed "aperto")
                    double distance = haversine(lat, lon, event.getLatitude(), event.getLongitude());
                    return distance <= (radiusKm != null ? radiusKm : 25.0);
                })
                .map(event -> {
                    int score = 0;
                    int creatorLevel = feedbackService.calculateReputation(event.getCreator()).level();
                    if (creatorLevel >= 5) score += 20;
                    if (user.getCity() != null && user.getCity().equalsIgnoreCase(event.getCity())) score += 15;
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
                    Double distance = (lat != null && lon != null && e.getLatitude() != null && e.getLongitude() != null)
                            ? haversine(lat, lon, e.getLatitude(), e.getLongitude())
                            : null;
                    return EventMapper.toFeedDTO(e, level, distance); // SOLO città, distanza se geo attiva
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventFeedDTO> getSuggestedEventsForUser(User user, int page, int size) {
        // Feed standard: senza geo, NO filtro raggio, mostra tutti
        return getSuggestedEventsForUser(user, page, size, null, null, 25.0);
    }

    // Haversine formula per calcolo distanza tra due coordinate geografiche (in km)
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raggio terra in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // === RELAZIONI & ADMIN ===

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

    /**
     * Ritorna il dettaglio evento con location solo per creator o partecipanti APPROVATI.
     * Altri utenti vedranno solo info pubbliche (senza indirizzo).
     */
    @Override
    public Object getEventView(UUID eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));
        boolean isCreator = event.getCreator().getId().equals(currentUser.getId());
        boolean isApproved = joinRequestRepository.existsByUserIdAndEventIdAndStatusAccepted(currentUser.getId(), eventId);

        if (isCreator || isApproved) {
            // Mostra TUTTO, compreso indirizzo (location)
            return EventMapper.toDetailsDTO(event, true);
        } else {
            // Mostra dati pubblici, ma SENZA location (privacy)
            return EventMapper.toDetailsDTO(event, false);
        }
    }

    @Override
    public List<EventSummaryDTO> getPublicEvents(String city, String activity, LocalDate after, String sortBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findAll().stream()
                .filter(e -> e.getDate().isAfter(after != null ? after : LocalDate.now()))
                .filter(e -> city == null || e.getCity().equalsIgnoreCase(city))
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
