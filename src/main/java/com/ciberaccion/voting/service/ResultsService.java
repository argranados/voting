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

        List<Object[]> rows = voteRepository.countVotesByContestant(roundId);

        List<RoundResultsResponse.ResultItem> items = rows.stream()
                .map(r -> new RoundResultsResponse.ResultItem(
                        (Long) r[0],
                        (String) r[1],
                        (Long) r[2]   // count() en JPA suele venir como Long
                ))
                .toList();

        return new RoundResultsResponse(roundId, items);
    }
}