package com.lfg.lfg_backend.dto;

import java.util.UUID;

public record LeaderboardEntryDTO(
        UUID userId,
        String username,
        int xp,
        int level
) {}
