package com.lfg.lfg_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username obbligatorio")
    @Size(max = 20, message = "Lo username non può superare 50 caratteri")
    private String username;

    @NotBlank(message = "Email obbligatoria")
    @Email(message = "Email non valida")
    private String email;

    @NotBlank(message = "Password obbligatoria")
    @Size(min = 6, message = "La password deve essere di almeno 6 caratteri")
    private String password;

    @Size(max = 100, message = "La città non può superare 100 caratteri")
    private String city;

    private Double latitude;
    private Double longitude;
}
