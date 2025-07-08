package com.lfg.lfg_backend.dto;

import java.util.UUID;

public record ReputationDTO(
        UUID userId,
        double averageRating,
        int feedbackReceived,
        int eventsParticipated,
        double reputationScore,
        int xp,
        int level,
        int xpToNextLevel
) {}