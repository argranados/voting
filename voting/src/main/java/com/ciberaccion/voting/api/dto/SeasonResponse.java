package com.ciberaccion.voting.api.dto;

import java.time.Instant;

public record SeasonResponse(
        Long id,
        String name,
        String status,
        Instant createdAt
) {
}