package com.lfg.lfg_backend.ServiceImpl;

import com.lfg.lfg_backend.dto.ReportRequestDTO;
import com.lfg.lfg_backend.dto.ReportResponseDTO;
import com.lfg.lfg_backend.mapper.ReportMapper;
import com.lfg.lfg_backend.model.*;
import com.lfg.lfg_backend.model.enums.ReportStatus;
import com.lfg.lfg_backend.repository.*;
import com.lfg.lfg_backend.service.NotificationService;
import com.lfg.lfg_backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    @Override
    public void createReport(User reporter, ReportRequestDTO request) {
        if (request.getReportedUserId() == null && request.getReportedEventId() == null) {
            throw new RuntimeException("È necessario specificare almeno un utente o un evento da segnalare.");
        }

        // 🚨 FLOOD PROTECTION: massimo 5 segnalazioni pendenti giornaliere per utente
        int todayReports = reportRepository.countByReporterIdAndStatusSince(
                reporter.getId(), ReportStatus.PENDING, LocalDate.now()
        );
        if (todayReports >= 5) {
            throw new RuntimeException("Hai già raggiunto il numero massimo di segnalazioni giornaliere.");
        }

        // 🚫 Blacklist motivi proibiti (case-insensitive)
        List<String> blacklistedReasons = List.of("troll", "provocazione", "fake");
        if (request.getReason() != null &&
                blacklistedReasons.contains(request.getReason().toLowerCase())) {
            throw new RuntimeException("Motivo di segnalazione non valido.");
        }

        User reportedUser = null;
        if (request.getReportedUserId() != null) {
            reportedUser = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> new RuntimeException("Utente segnalato non trovato."));
        }

        Event reportedEvent = null;
        if (request.getReportedEventId() != null) {
            reportedEvent = eventRepository.findById(request.getReportedEventId())
                    .orElseThrow(() -> new RuntimeException("Evento segnalato non trovato."));
        }

        // Controllo duplicato (esclude segnalazioni già pendenti o in review)
        boolean alreadyReported = reportRepository.existsActiveByReporterAndTarget(
                reporter.getId(),
                request.getReportedUserId(),
                request.getReportedEventId(),
                List.of(ReportStatus.PENDING, ReportStatus.IN_REVIEW)
        );

        if (alreadyReported) {
            throw new RuntimeException("Hai già una segnalazione attiva per questo utente o evento.");
        }

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reportedEvent(reportedEvent)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .createdAt(LocalDate.now())
                .build();

        reportRepository.save(report);
    }

    @Override
    public List<ReportResponseDTO> getReportsByStatus(String status) {
        ReportStatus enumStatus;
        try {
            enumStatus = ReportStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Stato non valido: " + status);
        }

        return reportRepository.findByStatus(enumStatus).stream()
                .map(ReportMapper::toDTO)
                .toList();
    }

    @Override
    public List<ReportResponseDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(ReportMapper::toDTO)
                .toList();
    }

    @Override
    public void updateReportStatus(UUID reportId, String status, String response) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report non trovato"));

        ReportStatus newStatus;
        try {
            newStatus = ReportStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Stato non valido. Usa APPROVED, DISMISSED, IN_REVIEW...");
        }

        report.setStatus(newStatus);
        report.setAdminResponse(response);
        reportRepository.save(report);

        User reporter = report.getReporter();
        String message = "La tua segnalazione è stata " + newStatus.name().toLowerCase() + ".";
        String description = response != null ? response : "";

        // Notifica sempre
        notificationService.createNotificationWithDetails(
                reporter,
                message,
                "REPORT_RESOLVED",
                report.getId().toString(),
                description
        );

        // Check segnalazioni infondate negli ultimi 30 giorni
        if (newStatus == ReportStatus.DISMISSED) {
            int recentDismissed = reportRepository.countByReporterIdAndStatusSince(
                    reporter.getId(),
                    ReportStatus.DISMISSED,
                    LocalDate.now().minusDays(30)
            );

            if (recentDismissed == 3) {
                notificationService.createNotification(
                        reporter,
                        "Attenzione: hai ricevuto 3 segnalazioni infondate negli ultimi 30 giorni. Se arrivi a 5, verrai temporaneamente sospeso.",
                        "WARNING_ABUSE"
                );
            } else if (recentDismissed >= 5) {
                reporter.setBannedUntil(LocalDate.now().plusDays(2).atStartOfDay());
                userRepository.save(reporter);
                notificationService.createNotification(
                        reporter,
                        "Sei stato temporaneamente sospeso per 2 giorni a causa di segnalazioni infondate ripetute.",
                        "AUTO_BAN"
                );
            }
        }
    }

    @Override
    public void markReportAsInReview(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Segnalazione non trovata"));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new RuntimeException("Solo le segnalazioni in stato PENDING possono essere prese in carico.");
        }

        report.setStatus(ReportStatus.IN_REVIEW);
        reportRepository.save(report);
    }

    @Override
    public int countDismissedReportsOfUser(UUID reporterId) {
        return reportRepository.countDismissedByUserId(reporterId);
    }
}

