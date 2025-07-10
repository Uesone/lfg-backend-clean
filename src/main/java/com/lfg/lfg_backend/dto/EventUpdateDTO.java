package com.lfg.lfg_backend.dto;
import java.util.Set;

import com.lfg.lfg_backend.model.enums.JoinMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateDTO {

    @Size(max = 100, message = "Il titolo non può superare 100 caratteri")
    private String title;

    @Size(max = 50, message = "Il tipo di attività non può superare 50 caratteri")
    private String activityType;

    @Size(max = 100, message = "La località non può superare 100 caratteri")
    private String location;

    @Size(max = 100)
    private String city;

    @Size(max = 300, message = "Le note non possono superare 300 caratteri")
    private String notes;

    @Future(message = "La data deve essere nel futuro")
    private LocalDate date;

    @Min(value = 1, message = "I partecipanti devono essere almeno 1")
    private int maxParticipants;

    private JoinMode joinMode;
    private Set<String> tags;
}
