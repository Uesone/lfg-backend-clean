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
public class UserDashboardDTO {
    private String username;
    private String email;
    private String city;
    private String profileImage;
    private String bio;

    private int xp;
    private int level;
    private int xpToNextLevel;

    private List<EventSummaryDTO> createdEvents;
    private List<EventSummaryDTO> joinedEvents;
    private List<NotificationDTO> recentNotifications;
    private Double latitude;
    private Double longitude;
}
