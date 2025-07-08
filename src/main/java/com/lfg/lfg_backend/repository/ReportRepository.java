package com.lfg.lfg_backend.repository;

import com.lfg.lfg_backend.model.Report;
import com.lfg.lfg_backend.model.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByStatus(ReportStatus status);
    List<Report> findTop5ByOrderByCreatedAtDesc();

    @Query("""
        SELECT COUNT(r) > 0 FROM Report r
        WHERE r.reporter.id = :reporterId
        AND (:reportedUserId IS NULL OR r.reportedUser.id = :reportedUserId)
        AND (:reportedEventId IS NULL OR r.reportedEvent.id = :reportedEventId)
        AND r.status IN :activeStatuses
    """)
    boolean existsActiveByReporterAndTarget(
            @Param("reporterId") UUID reporterId,
            @Param("reportedUserId") UUID reportedUserId,
            @Param("reportedEventId") UUID reportedEventId,
            @Param("activeStatuses") List<ReportStatus> activeStatuses
    );

    @Query("""
        SELECT COUNT(r) FROM Report r
        WHERE r.reporter.id = :reporterId AND r.status = 'DISMISSED'
    """)
    int countDismissedByUserId(@Param("reporterId") UUID reporterId);

    @Query("""
        SELECT COUNT(r) FROM Report r
        WHERE r.reporter.id = :reporterId
        AND r.status = :status
        AND r.createdAt >= :since
    """)
    int countByReporterIdAndStatusSince(
            @Param("reporterId") UUID reporterId,
            @Param("status") ReportStatus status,
            @Param("since") LocalDate since
    );
}
