package com.lfg.lfg_backend.mapper;

import com.lfg.lfg_backend.dto.*;
import com.lfg.lfg_backend.model.Event;

/**
 * Mapper per conversione tra entità Event e i vari DTO usati per API e privacy.
 * - Feed: SOLO città (mai location)
 * - Dettaglio: location visibile solo se autorizzato (gestito a livello service/controller)
 */
public class EventMapper {

    /**
     * Mapping base per il feed pubblico o lista eventi.
     * Mostra solo i dati pubblici e la città.
     */
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

    /**
     * Mapping per il dettaglio evento.
     * La visibilità del campo "location" è gestita dal parametro includeLocation.
     * Se vuoi nascondere l'indirizzo, passa false.
     */
    public static EventDetailsDTO toDetailsDTO(Event event, boolean includeLocation) {
        if (event == null) return null;
        return EventDetailsDTO.builder()
                .id(event.getId())
                .creatorID(event.getCreator() != null ? event.getCreator().getId() : null)
                .title(event.getTitle())
                .activityType(event.getActivityType())
                .location(includeLocation ? event.getLocation() : null) // location solo se autorizzato!
                .city(event.getCity())
                .notes(event.getNotes())
                .date(event.getDate())
                .maxParticipants(event.getMaxParticipants())
                .joinMode(event.getJoinMode() != null ? event.getJoinMode().name() : null)
                .creatorUsername(event.getCreator() != null ? event.getCreator().getUsername() : null)
                .build();
    }

    /**
     * Mapper classico per EventFeedDTO - versione compatibilità.
     * Mostra SOLO città, mai location né lat/lon nel feed.
     */
    public static EventFeedDTO toFeedDTO(Event event, int creatorLevel) {
        if (event == null) return null;
        return new EventFeedDTO(
                event.getId(),
                event.getTitle(),
                event.getActivityType(),
                event.getCity(), // <-- SOLO città!
                event.getDate(),
                event.getMaxParticipants(),
                event.getCreator() != null ? event.getCreator().getId() : null,
                event.getCreator() != null ? event.getCreator().getUsername() : null,
                creatorLevel,
                null // distanzaFromUser non presente in questa variante
        );
    }

    /**
     * Mapper con distanza calcolata (distanceFromUser in km).
     * Mostra SOLO città nel feed.
     */
    public static EventFeedDTO toFeedDTO(Event event, int creatorLevel, Double distanceFromUser) {
        if (event == null) return null;
        return new EventFeedDTO(
                event.getId(),
                event.getTitle(),
                event.getActivityType(),
                event.getCity(), // <-- SOLO città!
                event.getDate(),
                event.getMaxParticipants(),
                event.getCreator() != null ? event.getCreator().getId() : null,
                event.getCreator() != null ? event.getCreator().getUsername() : null,
                creatorLevel,
                distanceFromUser // distanza in km, opzionale, solo se geo attiva
        );
    }
}
