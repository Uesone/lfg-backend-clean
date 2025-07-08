package com.lfg.lfg_backend.controller;

import com.lfg.lfg_backend.dto.JoinRequestCreateDTO;
import com.lfg.lfg_backend.dto.JoinRequestDTO;
import com.lfg.lfg_backend.dto.ParticipantDTO;
import com.lfg.lfg_backend.model.enums.JoinStatus;
import com.lfg.lfg_backend.security.UserDetailsImpl;
import com.lfg.lfg_backend.service.JoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/join-requests")
@RequiredArgsConstructor
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    // === CREAZIONE ===
    @PostMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JoinRequestDTO> sendJoinRequest(
            @PathVariable UUID eventId,
            @RequestBody JoinRequestCreateDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        JoinRequestDTO response = joinRequestService.createJoinRequest(
                userDetails.getUser(),
                eventId,
                request.getRequestMessage()
        );
        return ResponseEntity.ok(response);
    }

    // === RICHIESTE DELL'UTENTE LOGGATO ===
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<JoinRequestDTO>> getMyJoinRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<JoinRequestDTO> requests = joinRequestService.getJoinRequestsOfUser(userDetails.getUser());
        return ResponseEntity.ok(requests);
    }

    // === RICHIESTE PER EVENTO (solo creator/admin) ===
    @GetMapping("/event/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<JoinRequestDTO>> getRequestsForEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<JoinRequestDTO> requests = joinRequestService.getJoinRequestsForEvent(eventId, userDetails.getUser());
        return ResponseEntity.ok(requests);
    }

    // === APPROVAZIONE / RIFIUTO ===
    @PostMapping("/{requestId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JoinRequestDTO> approveRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        JoinRequestDTO updated = joinRequestService.updateRequestStatus(requestId, JoinStatus.APPROVED, userDetails.getUser());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JoinRequestDTO> rejectRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        JoinRequestDTO updated = joinRequestService.updateRequestStatus(requestId, JoinStatus.REJECTED, userDetails.getUser());
        return ResponseEntity.ok(updated);
    }

    // === PARTECIPANTI APPROVATI (solo creator o partecipanti) ===
    @GetMapping("/participants/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParticipantDTO>> getApprovedParticipants(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<ParticipantDTO> participants = joinRequestService.getApprovedParticipants(eventId, userDetails.getUser());
        return ResponseEntity.ok(participants);
    }
}
