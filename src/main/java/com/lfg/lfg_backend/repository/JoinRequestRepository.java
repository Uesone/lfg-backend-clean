package com.lfg.lfg_backend.repository;

import com.lfg.lfg_backend.model.JoinRequest;
import com.lfg.lfg_backend.model.enums.JoinStatus;
import com.lfg.lfg_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, UUID> {

    List<JoinRequest> findByUserId(UUID userId);

    List<JoinRequest> findByEventId(UUID eventId);

    List<JoinRequest> findByUserIdAndStatus(UUID userId, JoinStatus status);

    @Query("SELECT COUNT(j) > 0 FROM JoinRequest j WHERE j.user.id = :userId AND j.event.id = :eventId AND j.status = 'APPROVED'")
    boolean existsByUserIdAndEventIdAndStatusAccepted(@Param("userId") UUID userId, @Param("eventId") UUID eventId);

    @Query("SELECT j.user FROM JoinRequest j WHERE j.event.id = :eventId AND j.status = 'APPROVED'")
    List<User> findApprovedParticipantsByEventId(@Param("eventId") UUID eventId);

    long countByEventIdAndStatus(UUID eventId, JoinStatus status);
}
