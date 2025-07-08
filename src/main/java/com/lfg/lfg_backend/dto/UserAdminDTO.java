package com.lfg.lfg_backend.dto;

import com.lfg.lfg_backend.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDTO {
    private UUID id;
    private String username;
    private String email;
    private Role role;
    private int reputation;
    private String city;
    private String profileImage;
    private String bio;
    private LocalDateTime bannedUntil;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
}
