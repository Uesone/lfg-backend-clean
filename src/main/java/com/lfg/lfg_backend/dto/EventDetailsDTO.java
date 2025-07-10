package com.lfg.lfg_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO dettagliato di un evento (controlla privacy su location!).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDetailsDTO {
    private UUID id;
    private UUID creatorID;
    private String title;
    private String activityType;
    private String location;   // indirizzo completo (VISIBILE solo se autorizzato)
    private String city;       // città evento (sempre visibile)
    private String notes;
    private LocalDate date;
    private int maxParticipants;
    private String joinMode;
    private String creatorUsername;
    private Set<String> tags;

}
