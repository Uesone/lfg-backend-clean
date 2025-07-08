package com.lfg.lfg_backend.controller;

import com.lfg.lfg_backend.dto.FeedbackRequest;
import com.lfg.lfg_backend.dto.FeedbackResponse;
import com.lfg.lfg_backend.dto.PaginatedResponse;
import com.lfg.lfg_backend.security.UserDetailsImpl;
import com.lfg.lfg_backend.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // === 🟩 CREAZIONE ===

    // Solo utenti autenticati possono lasciare feedback
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeedbackResponse> createFeedback(
            @RequestBody @Valid FeedbackRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        FeedbackResponse response = feedbackService.createFeedback(userDetails.getUser(), request);
        return ResponseEntity.ok(response);
    }

    // === 🟦 GET SINGOLO ===

    // Lasciato pubblico: chiunque può vedere un feedback specifico
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable UUID id) {
        return feedbackService.getFeedbackById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === 🟨 GET PAGINATI - UTENTE SPECIFICO ===

    // Pubblica: per vedere tutti i feedback lasciati da un utente (ad esempio per profilo pubblico)
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<PaginatedResponse<FeedbackResponse>> getFeedbacksByUser(
            @PathVariable UUID userId,
            Pageable pageable
    ) {
        Page<FeedbackResponse> page = feedbackService.getFeedbacksByFromUser(userId, pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        ));
    }

    // Solo utenti autenticati possono vedere i feedback ricevuti da loro
    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<FeedbackResponse>> getFeedbacksReceived(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable
    ) {
        Page<FeedbackResponse> page = feedbackService.getFeedbacksReceivedByUser(userDetails.getUser(), pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        ));
    }

    // Solo utenti autenticati possono vedere i feedback inviati da loro
    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<FeedbackResponse>> getFeedbacksSent(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable
    ) {
        Page<FeedbackResponse> page = feedbackService.getFeedbacksSentByUser(userDetails.getUser(), pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        ));
    }

    // === 🟧 GET MEDIA ===

    // Pubblica: mostra la media voti di un utente (utile per profilo pubblico)
    @GetMapping("/average-rating/{userId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID userId) {
        double average = feedbackService.getAverageRatingForUser(userId);
        return ResponseEntity.ok(average);
    }

    // === 🟥 GET TUTTI PAGINATI ===

    // Pubblica: puoi limitarla ad admin in futuro se vuoi
    @GetMapping
    public ResponseEntity<PaginatedResponse<FeedbackResponse>> getAllFeedback(Pageable pageable) {
        Page<FeedbackResponse> page = feedbackService.getAllFeedback(pageable);
        return ResponseEntity.ok(new PaginatedResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        ));
    }
}
