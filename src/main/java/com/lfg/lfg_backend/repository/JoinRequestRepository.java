package com.lfg.lfg_backend.repository;

import com.lfg.lfg_backend.model.JoinRequest;
import com.lfg.lfg_backend.model.enums.JoinStatus;
import com.lfg.lfg_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository per le richieste di partecipazione agli eventi (JoinRequest).
 * Utile anche per verificare se un utente è partecipante approvato (privacy sugli indirizzi evento).
 */
public interface JoinRequestRepository extends JpaRepository<JoinRequest, UUID> {

    // Trova tutte le richieste inviate da uno specifico utente
    List<JoinRequest> findByUserId(UUID userId);

    // Trova tutte le richieste relative a un evento specifico
    List<JoinRequest> findByEventId(UUID eventId);

    // Trova tutte le richieste di un utente con uno specifico stato (APPROVED, PENDING, REJECTED)
    List<JoinRequest> findByUserIdAndStatus(UUID userId, JoinStatus status);

    /**
     * Verifica se un utente è un partecipante APPROVATO a un evento.
     * Serve per privacy: solo chi è approvato vede l'indirizzo evento.
     */
    @Query("SELECT COUNT(j) > 0 FROM JoinRequest j WHERE j.user.id = :userId AND j.event.id = :eventId AND j.status = 'APPROVED'")
    boolean existsByUserIdAndEventIdAndStatusAccepted(@Param("userId") UUID userId, @Param("eventId") UUID eventId);

    /**
     * Ritorna la lista degli utenti partecipanti APPROVATI a un evento.
     */
    @Query("SELECT j.user FROM JoinRequest j WHERE j.event.id = :eventId AND j.status = 'APPROVED'")
    List<User> findApprovedParticipantsByEventId(@Param("eventId") UUID eventId);

    // Conta quanti partecipanti con uno stato specifico ha un evento
    long countByEventIdAndStatus(UUID eventId, JoinStatus status);
}
