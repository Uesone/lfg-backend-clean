package com.lfg.lfg_backend.model;

import com.lfg.lfg_backend.model.enums.JoinStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "join_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private JoinStatus status;

    @Column(columnDefinition = "TEXT")
    private String requestMessage;

    private LocalDate createdAt;
}
