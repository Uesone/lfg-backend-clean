package com.lfg.lfg_backend.dto;

import com.lfg.lfg_backend.model.enums.Role;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private UUID id;
    private String email;
    private String username;
    private String city;
    private String bio;
    private Double latitude;
    private Double longitude;
    private Role role;
    private int reputation;
}
