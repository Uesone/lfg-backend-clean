package com.lfg.lfg_backend.service;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service per la gestione degli eventi e feed (anche geo-based)
 */
public interface EventService {
    // === BASE CRUD ===

    EventDetailsDTO createEvent(EventCreateDTO eventDTO, String creatorEmail);

    void updateEvent(UUID eventId, EventUpdateDTO updateDTO, String userEmail);

    void deleteEvent(UUID eventId, String userEmail);

    List<EventSummaryDTO> getAllEvents();

    EventDetailsDTO getEventById(UUID id);

    // === FEED EVENTS ===

    List<EventFeedDTO> getSuggestedEventsForUser(
            User user,
            int page,
            int size
    );

    List<EventFeedDTO> getSuggestedEventsForUser(
            User user,
            int page,
            int size,
            Double lat,
            Double lon,
            Double radiusKm
    );

    /**
     * Feed geo-based con filtro raggio dalla posizione dell’utente loggato.
     * Default: usa getSuggestedEventsForUser con parametri da user.
     */
    default List<EventFeedDTO> getEventsWithinRadius(User user, Double radiusKm) {
        return getSuggestedEventsForUser(user, 0, 20, user.getLatitude(), user.getLongitude(), radiusKm != null ? radiusKm : 25.0);
    }

    // === RELAZIONI ===

    List<UserDTO> getEventParticipants(UUID eventId);

    List<JoinRequestDTO> getJoinRequestsForEvent(UUID eventId, User requester);

    Object getEventView(UUID eventId, User currentUser);

    List<EventSummaryDTO> getPublicEvents(
            String city,
            String activity,
            LocalDate after,
            String sortBy,
            int page,
            int size
    );

    // === ADMIN ===

    List<EventAdminDTO> getAllAdminEvents();

    void deleteEventById(UUID id);
}
