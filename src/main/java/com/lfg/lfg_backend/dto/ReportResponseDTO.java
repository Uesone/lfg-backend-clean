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
public class ReportResponseDTO {
    private UUID id;
    private String reporterUsername;
    private String reportedUserUsername;
    private String reportedEventTitle;
    private String reason;
    private String description;
    private LocalDate createdAt;
    private String status;

}
