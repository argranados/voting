package com.ciberaccion.voting.api.dto;

import java.time.Instant;

public record ContestantResponse(
        Long id,
        Long seasonId,
        String name,
        String status,
        Instant createdAt
) {
}