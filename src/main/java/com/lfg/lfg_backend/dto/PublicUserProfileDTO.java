package com.lfg.lfg_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicUserProfileDTO {
    private String username;
    private String city;
    private String bio;
    private String profileImage;
    private int level;
    private Double latitude;
    private Double longitude;
}
