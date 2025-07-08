package com.lfg.lfg_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryDTO {
    private UUID id;
    private String title;
    private String activityType;
    private String city;
    private LocalDate date;
}
