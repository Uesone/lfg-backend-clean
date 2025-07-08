package com.lfg.lfg_backend.mapper;

import com.lfg.lfg_backend.dto.JoinRequestDTO;
import com.lfg.lfg_backend.model.JoinRequest;

public class JoinRequestMapper {

    public static JoinRequestDTO toDTO(JoinRequest joinRequest) {
        if (joinRequest == null) return null;
        return new JoinRequestDTO(
                joinRequest.getId(),
                joinRequest.getEvent() != null ? joinRequest.getEvent().getId() : null,
                joinRequest.getUser() != null ? joinRequest.getUser().getId() : null,
                joinRequest.getUser() != null ? joinRequest.getUser().getUsername() : null,
                joinRequest.getRequestMessage(),
                joinRequest.getStatus(),
                joinRequest.getCreatedAt()
        );
    }
}
