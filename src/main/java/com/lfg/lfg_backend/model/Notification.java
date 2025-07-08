package com.lfg.lfg_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Notification {
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    // Utente che riceve la notifica
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Testo della notifica
    @Column(nullable = false)
    private String message;

    // Tipo di evento che ha generato la notifica (es. FEEDBACK_RECEIVED, REPORT_RESOLVED)
    @Column(nullable = false)
    private String type;

    // ID di riferimento opzionale (es. ID della segnalazione risolta)
    private String referenceId;

    // Descrizione aggiuntiva opzionale (es. messaggio dell’admin)
    private String description;

    // Stato lettura
    @Column(nullable = false)
    private boolean read = false;

    // Data e ora creazione
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
