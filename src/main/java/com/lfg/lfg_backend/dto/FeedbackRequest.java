package com.lfg.lfg_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class FeedbackRequest {

    @NotNull(message = "L'utente da valutare è obbligatorio")
    private UUID toUserId;

    @NotNull(message = "L'evento è obbligatorio")
    private UUID eventId;

    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    private int rating;

    @Size(max = 500, message = "Il commento non può superare 500 caratteri")
    private String comment;
}
