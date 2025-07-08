package com.lfg.lfg_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private long totalUsers;
    private long totalEvents;
    private long upcomingEvents;
    private long totalFeedbacks;
    private long totalReports;
    private String topUserByXp;

    // QUICK STATS
    private List<UserAdminDTO> lastRegisteredUsers;
    private List<EventAdminDTO> lastCreatedEvents;
    private List<ReportResponseDTO> recentReports;
}
