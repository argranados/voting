package com.ciberaccion.voting.api.dto;

import jakarta.validation.constraints.NotNull;

public class CastVoteRequest {

    @NotNull
    private Long roundId;

    @NotNull
    private Long contestantId;

    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }

    public Long getContestantId() { return contestantId; }
    public void setContestantId(Long contestantId) { this.contestantId = contestantId; }
}