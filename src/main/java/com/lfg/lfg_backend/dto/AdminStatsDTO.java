package com.lfg.lfg_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminStatsDTO {
    private long totalUsers;
    private long totalEvents;
    private long upcomingEvents;
    private long totalFeedbacks;
    private long totalReports;
    private String topUserByXp;
}
