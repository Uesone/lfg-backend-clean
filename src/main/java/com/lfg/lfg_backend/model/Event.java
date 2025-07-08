package com.lfg.lfg_backend.model;

import com.lfg.lfg_backend.model.enums.JoinMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(name = "activity_type")
    private String activityType;

    private String location;

    private String city;

    private LocalDate date;

    private int maxParticipants;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private JoinMode joinMode;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    private LocalDateTime createdAt;
}
