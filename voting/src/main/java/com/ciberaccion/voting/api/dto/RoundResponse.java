package com.ciberaccion.voting.api.dto;

import java.time.Instant;

public record RoundResponse(
        Long id,
        Long seasonId,
        String name,
        Instant startsAt,
        Instant endsAt,
        String status,
        String ruleType,
        Instant createdAt
) {
}