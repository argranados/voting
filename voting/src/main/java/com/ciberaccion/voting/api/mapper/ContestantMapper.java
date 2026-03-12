package com.ciberaccion.voting.api.mapper;

import com.ciberaccion.voting.api.dto.ContestantResponse;
import com.ciberaccion.voting.domain.Contestant;

public final class ContestantMapper {

    private ContestantMapper() {
    }

    public static ContestantResponse toResponse(Contestant contestant) {
        return new ContestantResponse(
                contestant.getId(),
                contestant.getSeasonId(),
                contestant.getName(),
                contestant.getStatus().name(),
                contestant.getCreatedAt()
        );
    }
}