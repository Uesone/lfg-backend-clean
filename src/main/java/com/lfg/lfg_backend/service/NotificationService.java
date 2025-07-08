package com.lfg.lfg_backend.service;

import com.lfg.lfg_backend.model.Notification;
import com.lfg.lfg_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    Notification createNotification(User user, String message, String type);

    Notification createNotificationWithDetails(
            User user,
            String message,
            String type,
            String referenceId,
            String description
    );

    List<Notification> getUserNotifications(User user);

    Page<Notification> getUserNotificationsPaginated(User user, Pageable pageable);

    void markAsRead(UUID id, User user);

    void markAllAsRead(User user);
}
