package com.lfg.lfg_backend.mapper;

import com.lfg.lfg_backend.dto.ReportResponseDTO;
import com.lfg.lfg_backend.model.Report;

public class ReportMapper {

    public static ReportResponseDTO toDTO(Report report) {
        if (report == null) return null;
        return ReportResponseDTO.builder()
                .id(report.getId())
                .reporterUsername(report.getReporter() != null ? report.getReporter().getUsername() : null)
                .reportedUserUsername(report.getReportedUser() != null ? report.getReportedUser().getUsername() : null)
                .reportedEventTitle(report.getReportedEvent() != null ? report.getReportedEvent().getTitle() : null)
                .reason(report.getReason())
                .description(report.getDescription())
                .createdAt(report.getCreatedAt())
                .status(report.getStatus() != null ? report.getStatus().name() : null)
                .build();
    }
}
