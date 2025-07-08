package com.lfg.lfg_backend.service;

import com.lfg.lfg_backend.dto.ReportRequestDTO;
import com.lfg.lfg_backend.dto.ReportResponseDTO;
import com.lfg.lfg_backend.model.User;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    void createReport(User reporter, ReportRequestDTO request);
    List<ReportResponseDTO> getAllReports();
    List<ReportResponseDTO> getReportsByStatus(String status);
    void updateReportStatus(UUID reportId, String status, String response);
    void markReportAsInReview(UUID reportId);
    int countDismissedReportsOfUser(UUID reporterId);
}
