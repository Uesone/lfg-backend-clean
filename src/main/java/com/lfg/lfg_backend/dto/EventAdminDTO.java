package com.lfg.lfg_backend.dto;

import com.lfg.lfg_backend.model.enums.JoinMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAdminDTO {
    private UUID id;
    private String title;
    private String activityType;
    private String city;
    private String location;
    private String notes;
    private LocalDate date;
    private int maxParticipants;
    private JoinMode joinMode;
    private String creatorUsername;
    private UUID creatorId;
    private LocalDateTime createdAt;
}
