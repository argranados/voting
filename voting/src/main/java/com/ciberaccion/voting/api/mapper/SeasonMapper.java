package com.ciberaccion.voting.api.mapper;

import com.ciberaccion.voting.api.dto.SeasonResponse;
import com.ciberaccion.voting.domain.Season;

public final class SeasonMapper {

    private SeasonMapper() {
    }

    public static SeasonResponse toResponse(Season season) {
        return new SeasonResponse(
                season.getId(),
                season.getName(),
                season.getStatus().name(),
                season.getCreatedAt()
        );
    }
}