package com.lfg.lfg_backend.service;

import com.lfg.lfg_backend.dto.JoinRequestDTO;
import com.lfg.lfg_backend.dto.ParticipantDTO;
import com.lfg.lfg_backend.model.User;
import com.lfg.lfg_backend.model.enums.JoinStatus;

import java.util.List;
import java.util.UUID;

public interface JoinRequestService {
    JoinRequestDTO createJoinRequest(User user, UUID eventId, String message);

    List<JoinRequestDTO> getJoinRequestsOfUser(User user);

    List<JoinRequestDTO> getJoinRequestsForEvent(UUID eventId, User user);

    List<ParticipantDTO> getApprovedParticipants(UUID eventId, User requester);

    void approveJoinRequest(UUID requestId, User approver);

    void rejectJoinRequest(UUID requestId, User approver);

    // Metodo unico per aggiornare lo status (consigliato per API "generica")
    JoinRequestDTO updateRequestStatus(UUID requestId, JoinStatus status, User approver);
}
