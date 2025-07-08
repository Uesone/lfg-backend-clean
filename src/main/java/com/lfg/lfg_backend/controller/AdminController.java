package com.lfg.lfg_backend.controller;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.service.EventService;
import com.lfg.lfg_backend.service.ReportService;
import com.lfg.lfg_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final EventService eventService;
    private final ReportService reportService;

    // 🟦 Quick Dashboard Overview
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboardOverview() {
        return ResponseEntity.ok(userService.getAdminDashboard());
    }

    // 🟩 Users (admin view)
    @GetMapping("/users")
    public List<UserAdminDTO> getAllUsers() {
        return userService.getAllAdminUsers();
    }

    // 🟦 Events (admin view)
    @GetMapping("/events")
    public List<EventAdminDTO> getAllEvents() {
        return eventService.getAllAdminEvents();
    }

    // 🟧 Reports
    @GetMapping("/reports")
    public List<ReportResponseDTO> getAllReports(@RequestParam(required = false) String status) {
        return (status == null) ? reportService.getAllReports() : reportService.getReportsByStatus(status.toUpperCase());
    }

    @PatchMapping("/reports/{id}/status")
    public ResponseEntity<String> updateReportStatus(
            @PathVariable UUID id,
            @RequestBody ReportStatusUpdateDTO dto
    ) {
        reportService.updateReportStatus(id, dto.getStatus(), dto.getAdminResponse());
        return ResponseEntity.ok("Stato e risposta della segnalazione aggiornati con successo.");
    }

    @PatchMapping("/reports/{id}/assign")
    public ResponseEntity<String> assignReportToAdmin(@PathVariable UUID id) {
        reportService.markReportAsInReview(id);
        return ResponseEntity.ok("Segnalazione segnata come IN_REVIEW.");
    }

    @GetMapping("/reports/dismissed-count/{userId}")
    public ResponseEntity<Integer> getDismissedReportsCount(@PathVariable UUID userId) {
        int count = reportService.countDismissedReportsOfUser(userId);
        return ResponseEntity.ok(count);
    }

    // 🟩 CRUD USER/EVENT (admin)
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Utente eliminato con successo.");
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEventById(id);
        return ResponseEntity.ok("Evento eliminato con successo.");
    }

    // 🟥 BAN/UNBAN
    @PatchMapping("/users/{id}/ban")
    public ResponseEntity<String> banUser(
            @PathVariable UUID id,
            @RequestParam("until") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime until
    ) {
        if (until.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("La data di ban deve essere futura.");
        }
        userService.banUserUntil(id, until);
        return ResponseEntity.ok("Utente bannato fino a " + until);
    }

    @PatchMapping("/users/{id}/unban")
    public ResponseEntity<String> unbanUser(@PathVariable UUID id) {
        userService.unbanUser(id);
        return ResponseEntity.ok("Utente sbannato con successo.");
    }
}
