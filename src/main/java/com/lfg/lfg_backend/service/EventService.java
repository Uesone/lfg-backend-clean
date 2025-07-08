package com.lfg.lfg_backend.service;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventService {
    EventDetailsDTO createEvent(EventCreateDTO eventDTO, String creatorEmail);
    void updateEvent(UUID eventId, EventUpdateDTO updateDTO, String userEmail);
    void deleteEvent(UUID eventId, String userEmail);
    List<EventSummaryDTO> getAllEvents();
    EventDetailsDTO getEventById(UUID id);
    List<EventFeedDTO> getSuggestedEventsForUser(User user, int page, int size);
    List<UserDTO> getEventParticipants(UUID eventId);
    List<JoinRequestDTO> getJoinRequestsForEvent(UUID eventId, User requester);
    Object getEventView(UUID eventId, User currentUser);
    List<EventSummaryDTO> getPublicEvents(String city, String activity, LocalDate after, String sortBy, int page, int size);

    // --- ADMIN ---
    List<EventAdminDTO> getAllAdminEvents();
    void deleteEventById(UUID id);
}
