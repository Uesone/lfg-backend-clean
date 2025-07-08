package com.lfg.lfg_backend.mapper;

import com.lfg.lfg_backend.dto.UserDTO;
import com.lfg.lfg_backend.dto.PublicUserProfileDTO;
import com.lfg.lfg_backend.dto.UserDashboardDTO;
import com.lfg.lfg_backend.model.User;

import java.util.Collections;
import java.util.List;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .city(user.getCity())
                .bio(user.getBio())
                .role(user.getRole())
                .reputation(user.getReputation())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .build();
    }

    public static PublicUserProfileDTO toPublicDTO(User user, int level) {
        if (user == null) return null;
        return PublicUserProfileDTO.builder()
                .username(user.getUsername())
                .city(user.getCity())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .level(level)
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .build();
    }

    // Se vuoi, puoi aggiungere in futuro anche:
    // public static UserDashboardDTO toDashboardDTO(User user, ...) {...}
}
