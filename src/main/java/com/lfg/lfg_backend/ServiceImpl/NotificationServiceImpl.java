package com.lfg.lfg_backend.ServiceImpl;

import com.lfg.lfg_backend.model.Notification;
import com.lfg.lfg_backend.model.User;
import com.lfg.lfg_backend.repository.NotificationRepository;
import com.lfg.lfg_backend.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification createNotification(User user, String message, String type) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public Notification createNotificationWithDetails(User user, String message, String type, String referenceId, String description) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .description(description)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Page<Notification> getUserNotificationsPaginated(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    public void markAsRead(UUID id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Not allowed to modify this notification");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .filter(n -> !n.isRead())
                .toList();

        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}
