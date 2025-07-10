package com.lfg.lfg_backend.dto;
import java.util.Set;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO usato per il feed pubblico/privato degli eventi.
 * Nel feed mostra SOLO informazioni pubbliche (città, NO indirizzo preciso/location).
 * Il campo 'location' qui può essere valorizzato solo come città,
 * oppure sostituito con un campo 'city' se vuoi maggiore chiarezza.
 *
 * Per privacy, l’indirizzo preciso è solo nel dettaglio evento (e solo per utenti approvati o creator).
 */
public record EventFeedDTO(
        UUID id,
        String title,
        String activityType,
        String city,                // <-- CAMBIO: solo città (NON location/indirizzo!)
        LocalDate date,
        int maxParticipants,
        UUID creatorId,
        String creatorUsername,
        int creatorLevel,
        Double distanceFromUser,    // <-- Distanza in km (opzionale, solo se richiesta dal backend)
        Set<String> tags

) {}
