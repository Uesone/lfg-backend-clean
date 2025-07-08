package com.lfg.lfg_backend.service;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.mapper.EventAdminMapper;
import com.lfg.lfg_backend.mapper.EventMapper;
import com.lfg.lfg_backend.mapper.NotificationMapper;
import com.lfg.lfg_backend.mapper.UserAdminMapper;
import com.lfg.lfg_backend.mapper.UserMapper;
import com.lfg.lfg_backend.model.*;
import com.lfg.lfg_backend.model.enums.JoinStatus;
import com.lfg.lfg_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final NotificationRepository notificationRepository;
    private final CloudinaryService cloudinaryService;
    private final FeedbackRepository feedbackRepository;
    private final ReportRepository reportRepository;

    // Restituisce i dati dell'utente autenticato
    public UserDTO getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));
        return UserMapper.toDTO(user);
    }

    // Dashboard utente completa, con mappatura DTO
    public UserDashboardDTO getUserDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));

        if (isUserBanned(user)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + user.getBannedUntil());
        }

        List<Event> created = eventRepository.findByCreator(user);
        List<JoinRequest> approved = joinRequestRepository.findByUserIdAndStatus(user.getId(), JoinStatus.APPROVED);
        List<Event> joined = approved.stream().map(JoinRequest::getEvent).toList();
        List<Notification> recentNotifications = notificationRepository
                .findByUserOrderByCreatedAtDesc(user).stream().limit(5).toList();

        int xp = user.getReputation();
        int level = xp / 100;
        int xpToNextLevel = ((level + 1) * 100) - xp;

        return UserDashboardDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .city(user.getCity())
                .profileImage(user.getProfileImage())
                .bio(user.getBio())
                .xp(xp)
                .level(level)
                .xpToNextLevel(xpToNextLevel)
                .createdEvents(created.stream().map(EventMapper::toSummaryDTO).toList())
                .joinedEvents(joined.stream().map(EventMapper::toSummaryDTO).toList())
                .recentNotifications(recentNotifications.stream().map(NotificationMapper::toDTO).toList())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .build();
    }

    public void updateUserProfile(String email, UserProfileUpdateDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));

        if (isUserBanned(user)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + user.getBannedUntil());
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            user.setCity(request.getCity());
        }
        if (request.getProfileImage() != null && !request.getProfileImage().isBlank()) {
            user.setProfileImage(request.getProfileImage());
        }
        if (request.getBio() != null && !request.getBio().isBlank()) {
            user.setBio(request.getBio());
        }
        if (request.getLatitude() != null) {
            user.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            user.setLongitude(request.getLongitude());
        }

        userRepository.save(user);
    }

    public void updateUserImage(String email, String imageUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));
        if (isUserBanned(user)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + user.getBannedUntil());
        }

        user.setProfileImage(imageUrl);
        userRepository.save(user);
    }

    // Restituisce il profilo pubblico
    public PublicUserProfileDTO getPublicProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        int xp = user.getReputation();
        int level = xp / 100;
        return UserMapper.toPublicDTO(user, level);
    }

    public boolean isUserBanned(User user) {
        return user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now());
    }

    public void banUser(UUID userId, int days) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));
        user.setBannedUntil(LocalDateTime.now().plusDays(days));
        userRepository.save(user);
    }

    public void banUserUntil(UUID userId, LocalDateTime until) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));
        user.setBannedUntil(until);
        userRepository.save(user);
    }

    public void unbanUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));
        user.setBannedUntil(null);
        userRepository.save(user);
    }

    public void uploadProfileImage(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));

        if (isUserBanned(user)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + user.getBannedUntil());
        }

        String imageUrl = cloudinaryService.uploadImage(file);
        user.setProfileImage(imageUrl);
        userRepository.save(user);
    }

    // ADMIN - restituisce tutti gli utenti (DTO admin)
    public List<UserAdminDTO> getAllAdminUsers() {
        return userRepository.findAll().stream()
                .map(UserAdminMapper::toDTO)
                .toList();
    }

    // ADMIN - elimina utente
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    // ADMIN - dashboard stats semplici
    public AdminStatsDTO getAdminStats() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();
        long upcomingEvents = eventRepository.countByDateAfter(LocalDate.now());
        long totalFeedbacks = feedbackRepository.count();
        long totalReports = reportRepository.count();

        String topUser = userRepository.findAll().stream()
                .max((u1, u2) -> Integer.compare(u1.getReputation(), u2.getReputation()))
                .map(User::getUsername)
                .orElse("N/A");

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalEvents(totalEvents)
                .upcomingEvents(upcomingEvents)
                .totalFeedbacks(totalFeedbacks)
                .totalReports(totalReports)
                .topUserByXp(topUser)
                .build();
    }

    // ADMIN - dashboard avanzata con quick stats e highlights
    public AdminDashboardDTO getAdminDashboard() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();
        long upcomingEvents = eventRepository.countByDateAfter(LocalDate.now());
        long totalFeedbacks = feedbackRepository.count();
        long totalReports = reportRepository.count();

        String topUser = userRepository.findAll().stream()
                .max((u1, u2) -> Integer.compare(u1.getReputation(), u2.getReputation()))
                .map(User::getUsername)
                .orElse("N/A");

        // Richiede che tu abbia definito i metodi nelle repository per recuperare ultimi 5...
        List<UserAdminDTO> lastRegisteredUsers = userRepository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(UserAdminMapper::toDTO).toList();

        List<EventAdminDTO> lastCreatedEvents = eventRepository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(EventAdminMapper::toDTO).toList();

        List<ReportResponseDTO> recentReports = reportRepository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(com.lfg.lfg_backend.mapper.ReportMapper::toDTO).toList();

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalEvents(totalEvents)
                .upcomingEvents(upcomingEvents)
                .totalFeedbacks(totalFeedbacks)
                .totalReports(totalReports)
                .topUserByXp(topUser)
                .lastRegisteredUsers(lastRegisteredUsers)
                .lastCreatedEvents(lastCreatedEvents)
                .recentReports(recentReports)
                .build();
    } // ... resto della classe invariato

    // PATCH: aggiorna solo i campi non null
    public User patchUserProfile(String email, UserProfileUpdateDTO updates) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));

        if (isUserBanned(user)) {
            throw new SecurityException("Il tuo account è sospeso fino al " + user.getBannedUntil());
        }

        if (updates.getUsername() != null && !updates.getUsername().isBlank()) {
            user.setUsername(updates.getUsername());
        }
        if (updates.getCity() != null && !updates.getCity().isBlank()) {
            user.setCity(updates.getCity());
        }
        if (updates.getProfileImage() != null && !updates.getProfileImage().isBlank()) {
            user.setProfileImage(updates.getProfileImage());
        }
        if (updates.getBio() != null && !updates.getBio().isBlank()) {
            user.setBio(updates.getBio());
        }
        if (updates.getLatitude() != null) {
            user.setLatitude(updates.getLatitude());
        }
        if (updates.getLongitude() != null) {
            user.setLongitude(updates.getLongitude());
        }

        return userRepository.save(user);
    }


}
