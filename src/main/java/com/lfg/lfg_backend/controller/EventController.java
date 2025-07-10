package com.lfg.lfg_backend.controller;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.security.UserDetailsImpl;
import com.lfg.lfg_backend.service.EventService;
import com.lfg.lfg_backend.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final FeedbackService feedbackService;

    // === 🟩 BASE CRUD ===

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventDetailsDTO> createEvent(
            @RequestBody @Valid EventCreateDTO eventDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String email = userDetails.getUser().getEmail();
        EventDetailsDTO created = eventService.createEvent(eventDTO, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<EventSummaryDTO> getAllEvents() {
        return eventService.getAllEvents();
    }

    /**
     * Dettaglio evento: location mostrata solo a creator o APPROVATI.
     * Il service restituisce automaticamente il DTO corretto.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getEventView(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Object dto = eventService.getEventView(id, userDetails.getUser());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateEvent(
            @PathVariable UUID id,
            @RequestBody @Valid EventUpdateDTO updateDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String email = userDetails.getUser().getEmail();
        eventService.updateEvent(id, updateDTO, email);
        return ResponseEntity.ok("Evento aggiornato con successo.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String email = userDetails.getUser().getEmail();
        eventService.deleteEvent(id, email);
        return ResponseEntity.ok("Evento eliminato con successo.");
    }

    // === 🟦 RELAZIONI ===

    @GetMapping("/{id}/participants")
    public List<UserDTO> getParticipantsForEvent(@PathVariable UUID id) {
        return eventService.getEventParticipants(id);
    }

    @GetMapping("/{id}/join-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<JoinRequestDTO>> getJoinRequestsForEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<JoinRequestDTO> joinRequests = eventService.getJoinRequestsForEvent(id, userDetails.getUser());
        return ResponseEntity.ok(joinRequests);
    }

    @GetMapping("/{id}/feedbacks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventFeedbackResponseDTO> getFeedbacksForEvent(@PathVariable UUID id) {
        List<FeedbackDTO> feedbacks = feedbackService.getFeedbacksForEvent(id);
        double averageRating = feedbackService.calculateAverageRatingForEvent(id);

        EventFeedbackResponseDTO response = new EventFeedbackResponseDTO();
        response.setAverageRating(averageRating);
        response.setFeedbackList(feedbacks);

        return ResponseEntity.ok(response);
    }

    // === 🟨 FEED GEO-BASED ===

    @GetMapping("/feed")
    @PreAuthorize("isAuthenticated()")
    public List<EventFeedDTO> getEventFeed(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(defaultValue = "25") Double radiusKm
    ) {
        // Usa posizione da parametri se presente, altrimenti prendi dal profilo utente
        if (lat == null || lon == null) {
            lat = userDetails.getUser().getLatitude();
            lon = userDetails.getUser().getLongitude();
        }
        return eventService.getSuggestedEventsForUser(
                userDetails.getUser(), page, size, lat, lon, radiusKm
        );
    }

    @GetMapping("/public")
    public ResponseEntity<List<EventSummaryDTO>> getPublicEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String activity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate after,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<EventSummaryDTO> events = eventService.getPublicEvents(
                city, activity, after, sortBy, page, size
        );
        return ResponseEntity.ok(events);
    }
}
