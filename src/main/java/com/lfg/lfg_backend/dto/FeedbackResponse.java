package com.lfg.lfg_backend.dto;

import java.time.LocalDate;
import java.util.UUID;

public record FeedbackResponse(
        UUID id,
        UUID fromUserId,
        UUID toUserId,
        UUID eventId,
        int rating,
        String comment,
        LocalDate createdAt
) {}