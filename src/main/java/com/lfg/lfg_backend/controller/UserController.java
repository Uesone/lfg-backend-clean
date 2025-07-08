package com.lfg.lfg_backend.controller;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.mapper.UserMapper;
import com.lfg.lfg_backend.model.User;
import com.lfg.lfg_backend.repository.UserRepository;
import com.lfg.lfg_backend.service.CloudinaryService;
import com.lfg.lfg_backend.service.FeedbackService;
import com.lfg.lfg_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final FeedbackService feedbackService;
    private final CloudinaryService cloudinaryService;

    // === CRUD BASE (solo admin) ===

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserMapper.toDTO(saved));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(value -> ResponseEntity.ok(UserMapper.toDTO(value)))
                .orElse(ResponseEntity.notFound().build());
    }

    // === UTENTE LOGGATO ===

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserDTO getCurrentUser() {
        return userService.getCurrentUser();
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateUserProfile(@RequestBody @Valid UserProfileUpdateDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updateUserProfile(email, request);
        return ResponseEntity.ok("Profilo aggiornato con successo.");
    }

    // PATCH SUPPORT
    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> patchUserProfile(@RequestBody UserProfileUpdateDTO updates) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User updated = userService.patchUserProfile(email, updates);
        return ResponseEntity.ok(UserMapper.toDTO(updated));
    }

    @PostMapping("/me/upload-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("file") MultipartFile file) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String imageUrl = cloudinaryService.uploadImage(file);
        userService.updateUserImage(email, imageUrl);
        return ResponseEntity.ok(imageUrl);
    }

    @GetMapping("/me/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDashboardDTO> getUserDashboard() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDashboardDTO dashboard = userService.getUserDashboard(email);
        return ResponseEntity.ok(dashboard);
    }

    // === REPUTAZIONE & CLASSIFICA ===

    @GetMapping("/{id}/reputation")
    public ResponseEntity<ReputationDTO> getUserReputation(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        return ResponseEntity.ok(feedbackService.calculateReputation(user));
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryDTO> getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        return feedbackService.getLeaderboard(limit);
    }

    // === PROFILO PUBBLICO ===

    @GetMapping("/{id}/profile")
    public ResponseEntity<PublicUserProfileDTO> getUserPublicProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }
}
