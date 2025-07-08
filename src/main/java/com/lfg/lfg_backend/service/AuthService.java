package com.lfg.lfg_backend.service;

import com.lfg.lfg_backend.dto.LoginRequest;
import com.lfg.lfg_backend.model.User;
import com.lfg.lfg_backend.model.enums.Role;
import com.lfg.lfg_backend.repository.UserRepository;
import com.lfg.lfg_backend.security.JwtUtil;
import com.lfg.lfg_backend.dto.RegisterRequest;
import com.lfg.lfg_backend.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email già registrata");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username già in uso");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .createdAt(LocalDate.now())
                .reputation(0)
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        // RESTITUISCI anche userId e username!
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    public AuthResponse authenticate(LoginRequest request) {
        String identifier = request.getUsernameOrEmail();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
            );

            User user = userRepository.findByEmail(identifier)
                    .or(() -> userRepository.findByUsername(identifier))
                    .orElse(null);

            if (user == null) {
                throw new RuntimeException("Credenziali non valide");
            }

            String token = jwtUtil.generateToken(user.getEmail());
            // RESTITUISCI anche userId e username!
            return new AuthResponse(token, user.getId(), user.getUsername());

        } catch (Exception e) {
            throw new RuntimeException("Credenziali non valide");
        }
    }
}
