package com.lfg.lfg_backend.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Data
public class EventCreateDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String activityType;

    @NotBlank
    private String location;

    private String notes;

    @NotNull
    private LocalDate date;

    @Min(2)
    private int maxParticipants;

    private String joinMode; // "AUTO" o "MANUAL"
}
