package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.dto.RoundResultsResponse;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.repo.RoundRepository;
import com.ciberaccion.voting.repo.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultsService {

    private final RoundRepository roundRepository;
    private final VoteRepository voteRepository;

    public ResultsService(RoundRepository roundRepository, VoteRepository voteRepository) {
        this.roundRepository = roundRepository;
        this.voteRepository = voteRepository;
    }

    public RoundResultsResponse getResults(Long roundId) {

        // valida que exista la ronda (para no dar resultados de una ronda inexistente)
        roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("Round no existe: " + roundId));

        List<RoundResultsResponse.ResultItem> items = voteRepository
                .countVotesByContestant(roundId)
                .stream()
                // Acceso por nombre en lugar de por índice r[0], r[1], r[2]
                .map(p -> new RoundResultsResponse.ResultItem(
                        p.getContestantId(),
                        p.getContestantName(),
                        p.getVoteCount()))
                .toList();

        return new RoundResultsResponse(roundId, items);
    }
}