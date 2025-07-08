package com.lfg.lfg_backend.controller;

import com.lfg.lfg_backend.dto.NotificationDTO;
import com.lfg.lfg_backend.dto.PaginatedResponse;
import com.lfg.lfg_backend.mapper.NotificationMapper;
import com.lfg.lfg_backend.model.Notification;
import com.lfg.lfg_backend.security.UserDetailsImpl;
import com.lfg.lfg_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // === 🟩 GET NOTIFICHE PAGINATE ===

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<NotificationDTO>> getUserNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> pageResult = notificationService.getUserNotificationsPaginated(userDetails.getUser(), pageable);

        List<NotificationDTO> dtos = pageResult.getContent().stream()
                .map(NotificationMapper::toDTO)
                .toList();

        PaginatedResponse<NotificationDTO> response = new PaginatedResponse<>(
                dtos,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }
    // === 🟦 SEGNARE COME LETTE ===

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        notificationService.markAsRead(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.markAllAsRead(userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
