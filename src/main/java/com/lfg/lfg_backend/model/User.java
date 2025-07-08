package com.lfg.lfg_backend.model;

import com.lfg.lfg_backend.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    private int reputation;

    private LocalDate createdAt;

    private String city;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "banned_until")
    private LocalDateTime bannedUntil;

    //geolocalizzazione
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}
