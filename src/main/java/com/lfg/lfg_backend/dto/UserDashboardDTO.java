package com.lfg.lfg_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardDTO {
    private UUID id;
    private String username;
    private String email;
    private String city;
    private String profileImage;   // URL immagine profilo
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
