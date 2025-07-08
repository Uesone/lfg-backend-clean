package com.lfg.lfg_backend.model;

import com.lfg.lfg_backend.model.enums.ReportStatus;
import lombok.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser; // opzionale

    @ManyToOne
    @JoinColumn(name = "reported_event_id")
    private Event reportedEvent; // opzionale

    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate createdAt;

    private String adminResponse;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

}
