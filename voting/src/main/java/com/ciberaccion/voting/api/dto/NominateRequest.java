package com.ciberaccion.voting.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class NominateRequest {

    @NotNull
    @NotEmpty
    private List<Long> contestantIds;

    public List<Long> getContestantIds() { return contestantIds; }
    public void setContestantIds(List<Long> contestantIds) { this.contestantIds = contestantIds; }
}