package com.lfg.lfg_backend.repository;

import com.lfg.lfg_backend.model.Notification;
import com.lfg.lfg_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Restituisce le notifiche di un utente ordinate dalla più recente
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
