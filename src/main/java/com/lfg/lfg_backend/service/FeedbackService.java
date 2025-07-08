package com.lfg.lfg_backend.service;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackService {

    FeedbackResponse createFeedback(User fromUser, FeedbackRequest request);

    Optional<FeedbackResponse> getFeedbackById(UUID id);

    Page<FeedbackResponse> getAllFeedback(Pageable pageable);

    Page<FeedbackResponse> getFeedbacksByFromUser(UUID userId, Pageable pageable);

    double getAverageRatingForUser(UUID userId);

    ReputationDTO calculateReputation(User user);

    List<LeaderboardEntryDTO> getLeaderboard(int limit);

    Page<FeedbackResponse> getFeedbacksReceivedByUser(User user, Pageable pageable);

    Page<FeedbackResponse> getFeedbacksSentByUser(User user, Pageable pageable);

    List<FeedbackDTO> getFeedbacksForEvent(UUID eventId);

    double calculateAverageRatingForEvent(UUID eventId);
}
