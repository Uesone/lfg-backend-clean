package com.lfg.lfg_backend.repository;

import com.lfg.lfg_backend.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    boolean existsByFromUserIdAndToUserIdAndEventId(UUID fromUserId, UUID toUserId, UUID eventId);

    List<Feedback> findByFromUserId(UUID fromUserId);

    List<Feedback> findByToUserId(UUID toUserId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.toUser.id = :userId")
    Double findAverageRatingByToUserId(@Param("userId") UUID userId);

    // 🆕 Nuovi metodi ordinati
    List<Feedback> findByFromUserIdOrderByCreatedAtDesc(UUID fromUserId);

    List<Feedback> findByToUserIdOrderByCreatedAtDesc(UUID toUserId);

    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Feedback> findByToUserIdOrderByCreatedAtDesc(UUID toUserId, Pageable pageable);
    Page<Feedback> findByFromUserIdOrderByCreatedAtDesc(UUID fromUserId, Pageable pageable);
    List<Feedback> findByEventId(UUID eventId);



}
