package com.ciberaccion.voting.api.controller;

import com.ciberaccion.voting.api.dto.CastVoteRequest;
import com.ciberaccion.voting.service.VotingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicVotingController {

    private final VotingService votingService;

    public PublicVotingController(VotingService votingService) {
        this.votingService = votingService;
    }

    @PostMapping("/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> castVote(@Valid @RequestBody CastVoteRequest request) {
        votingService.castVote(request.getRoundId(), request.getContestantId());
        return Map.of("message", "Vote accepted");
    }
}