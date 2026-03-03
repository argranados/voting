package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.Round;
import com.ciberaccion.voting.domain.RoundStatus;
import com.ciberaccion.voting.repo.RoundRepository;
import org.springframework.stereotype.Service;

@Service
public class RoundService {

    private final RoundRepository roundRepository;

    public RoundService(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    public Round openRound(Long roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("Round no existe: " + roundId));

        round.setStatus(RoundStatus.OPEN);
        return roundRepository.save(round);
    }

    public Round closeRound(Long roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("Round no existe: " + roundId));

        round.setStatus(RoundStatus.CLOSED);
        return roundRepository.save(round);
    }
}