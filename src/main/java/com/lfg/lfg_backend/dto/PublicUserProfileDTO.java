package com.lfg.lfg_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicUserProfileDTO {
    private UUID id;            // UUID!
    private String username;
    private String city;
    private String bio;
    private String profileImage;
    private int level;
    private Double latitude;
    private Double longitude;
}
