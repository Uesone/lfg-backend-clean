package com.lfg.lfg_backend.controller;

import com.lfg.lfg_backend.security.UserDetailsImpl;
import com.lfg.lfg_backend.dto.ReportRequestDTO;
import com.lfg.lfg_backend.dto.ReportResponseDTO;
import com.lfg.lfg_backend.dto.ReportStatusUpdateDTO;
import com.lfg.lfg_backend.model.User;
import com.lfg.lfg_backend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> createReport(
            @RequestBody @Valid ReportRequestDTO request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        User reporter = userDetails.getUser();
        reportService.createReport(reporter, request);
        return ResponseEntity.ok("Segnalazione inviata con successo.");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReportResponseDTO> getAllReports() {
        return reportService.getAllReports();
    }

    // PATCH endpoint per aggiornare lo status della segnalazione (solo admin)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateReportStatus(
            @PathVariable UUID id,
            @RequestBody @Valid ReportStatusUpdateDTO update
    ) {
        reportService.updateReportStatus(id, update.getStatus(), update.getAdminResponse());
        return ResponseEntity.noContent().build();
    }
}
