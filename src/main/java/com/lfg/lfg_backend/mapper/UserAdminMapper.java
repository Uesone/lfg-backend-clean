package com.lfg.lfg_backend.mapper;

import com.lfg.lfg_backend.dto.UserAdminDTO;
import com.lfg.lfg_backend.model.User;

public class UserAdminMapper {
    public static UserAdminDTO toDTO(User user) {
        if (user == null) return null;
        return UserAdminDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .reputation(user.getReputation())
                .city(user.getCity())
                .profileImage(user.getProfileImage())
                .bio(user.getBio())
                .bannedUntil(user.getBannedUntil())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().atStartOfDay() : null)
                .build();
    }
}
