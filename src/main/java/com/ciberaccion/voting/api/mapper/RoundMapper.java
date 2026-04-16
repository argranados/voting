package com.ciberaccion.voting.api.mapper;

import com.ciberaccion.voting.api.dto.RoundResponse;
import com.ciberaccion.voting.domain.Round;

public final class RoundMapper {

    private RoundMapper() {}

    public static RoundResponse toResponse(Round r) {
        return new RoundResponse(
                r.getId(),
                r.getSeasonId(),
                r.getName(),
                r.getStartsAt(),
                r.getEndsAt(),
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getRuleType(),
                r.getCreatedAt()
        );
    }
}