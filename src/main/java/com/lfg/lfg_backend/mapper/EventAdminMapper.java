package com.lfg.lfg_backend.mapper;

import com.lfg.lfg_backend.dto.EventAdminDTO;
import com.lfg.lfg_backend.model.Event;

public class EventAdminMapper {
    public static EventAdminDTO toDTO(Event event) {
        if (event == null) return null;
        return EventAdminDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .activityType(event.getActivityType())
                .city(event.getLocation())
                .location(event.getLocation())
                .notes(event.getNotes())
                .date(event.getDate())
                .maxParticipants(event.getMaxParticipants())
                .joinMode(event.getJoinMode())
                .creatorUsername(event.getCreator() != null ? event.getCreator().getUsername() : null)
                .creatorId(event.getCreator() != null ? event.getCreator().getId() : null)
                .createdAt(event.getCreatedAt())
                .build();
    }
}
