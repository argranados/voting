package com.ciberaccion.voting.api.controller;

import com.ciberaccion.voting.domain.Round;
import com.ciberaccion.voting.service.RoundService;
import org.springframework.web.bind.annotation.*;
import com.ciberaccion.voting.api.dto.CreateRoundRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminRoundController {

    private final RoundService roundService;

    public AdminRoundController(RoundService roundService) {
        this.roundService = roundService;
    }

    @PostMapping("/rounds/{roundId}/open")
    public Map<String, Object> open(@PathVariable Long roundId) {
        Round round = roundService.openRound(roundId);
        return Map.of("roundId", round.getId(), "status", round.getStatus().name());
    }

    @PostMapping("/rounds/{roundId}/close")
    public Map<String, Object> close(@PathVariable Long roundId) {
        Round round = roundService.closeRound(roundId);
        return Map.of("roundId", round.getId(), "status", round.getStatus().name());
    }

    @PostMapping("/rounds")
    @ResponseStatus(HttpStatus.CREATED)
    public Round createRound(@Valid @RequestBody CreateRoundRequest request) {
        return roundService.createRound(request);
    }
}