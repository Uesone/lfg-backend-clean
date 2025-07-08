package com.lfg.lfg_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDetailsDTO {
    private UUID id;
    private UUID creatorID;
    private String title;
    private String activityType;
    private String location;
    private String city;
    private String notes;
    private LocalDate date;
    private int maxParticipants;
    private String joinMode;
    private String creatorUsername;
}
