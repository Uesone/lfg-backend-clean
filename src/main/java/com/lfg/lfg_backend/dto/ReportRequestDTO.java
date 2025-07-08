package com.lfg.lfg_backend.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {

    private UUID reportedUserId;   // opzionale
    private UUID reportedEventId;  // opzionale

    @NotBlank(message = "Il motivo della segnalazione è obbligatorio.")
    private String reason;

    @Size(max = 500, message = "La descrizione non può superare i 500 caratteri.")
    private String description;
}
