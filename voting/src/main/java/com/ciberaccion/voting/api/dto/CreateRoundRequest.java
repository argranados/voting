package com.ciberaccion.voting.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CreateRoundRequest {

    @NotNull
    private Long seasonId;

    @NotBlank
    private String name;

    @NotNull
    private Instant startsAt;

    @NotNull
    private Instant endsAt;

    @NotBlank
    private String ruleType;

    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Instant getStartsAt() { return startsAt; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }

    public Instant getEndsAt() { return endsAt; }
    public void setEndsAt(Instant endsAt) { this.endsAt = endsAt; }

    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
}