package com.lfg.lfg_backend.mapper;

import com.lfg.lfg_backend.dto.FeedbackDTO;
import com.lfg.lfg_backend.model.Feedback;

public class FeedbackMapper {

    public static FeedbackDTO toDTO(Feedback feedback) {
        if (feedback == null) return null;
        return FeedbackDTO.builder()
                .id(feedback.getId())
                .comment(feedback.getComment())
                .rating(feedback.getRating())
                .fromUsername(feedback.getFromUser() != null ? feedback.getFromUser().getUsername() : null)
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
