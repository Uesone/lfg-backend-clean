package com.lfg.lfg_backend.dto;

import java.time.LocalDate;
import java.util.UUID;


public record EventFeedDTO(
        UUID id,
        String title,
        String activityType,
        String location,
        LocalDate date,
        int maxParticipants,
        UUID creatorId,
        String creatorUsername,
        int creatorLevel
) {}
