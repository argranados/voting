package com.ciberaccion.voting.api.mapper;

import com.ciberaccion.voting.api.dto.NomineeResponse;

public final class NominationMapper {

    private NominationMapper() {}

    public static NomineeResponse toResponse(Long contestantId, String contestantName) {
        return new NomineeResponse(contestantId, contestantName);
    }
}