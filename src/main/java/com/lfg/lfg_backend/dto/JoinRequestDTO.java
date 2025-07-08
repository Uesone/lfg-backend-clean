package com.lfg.lfg_backend.dto;

import com.lfg.lfg_backend.model.enums.JoinStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestDTO {

    private UUID id;
    private UUID eventId;
    private UUID userId;
    private String username;
    private String requestMessage;
    private JoinStatus status;
    private LocalDate createdAt;
}
//per output