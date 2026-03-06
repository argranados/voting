package com.ciberaccion.voting.api.mapper;

import com.ciberaccion.voting.api.dto.NomineeResponse;
import com.ciberaccion.voting.domain.Nomination;

public final class NominationMapper {

    private NominationMapper() {}

    public static NomineeResponse toResponse(Nomination n) {
        return new NomineeResponse(n.getContestantId());
    }
}