package com.lfg.lfg_backend.mapper;

import com.lfg.lfg_backend.dto.NotificationDTO;
import com.lfg.lfg_backend.model.Notification;

public class NotificationMapper {

    public static NotificationDTO toDTO(Notification notification) {
        if (notification == null) return null;
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
