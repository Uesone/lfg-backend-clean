package com.lfg.lfg_backend.mapper;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.model.Event;

public class EventMapper {

    public static EventSummaryDTO toSummaryDTO(Event event) {
        if (event == null) return null;
        return EventSummaryDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .activityType(event.getActivityType())
                .city(event.getCity())
                .date(event.getDate())
                .build();
    }

    public static EventDetailsDTO toDetailsDTO(Event event) {
        if (event == null) return null;
        return EventDetailsDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .activityType(event.getActivityType())
                .location(event.getLocation())
                .city(event.getCity())
                .notes(event.getNotes())
                .date(event.getDate())
                .maxParticipants(event.getMaxParticipants())
                .joinMode(event.getJoinMode() != null ? event.getJoinMode().name() : null)
                .creatorUsername(event.getCreator() != null ? event.getCreator().getUsername() : null)
                .build();
    }

    public static EventFeedDTO toFeedDTO(Event event, int creatorLevel) {
        if (event == null) return null;
        return new EventFeedDTO(
                event.getId(),
                event.getTitle(),
                event.getActivityType(),
                event.getLocation(),
                event.getDate(),
                event.getMaxParticipants(),
                event.getCreator() != null ? event.getCreator().getId() : null,
                event.getCreator() != null ? event.getCreator().getUsername() : null,
                creatorLevel,
                event.getLatitude(),        // <--- NEW
                event.getLongitude()        // <--- NEW
        );
    }

}
