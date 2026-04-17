package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.error.BadRequestException;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.Round;
import com.ciberaccion.voting.domain.RoundStatus;
import com.ciberaccion.voting.domain.Vote;
import com.ciberaccion.voting.domain.VoteStatus;
import com.ciberaccion.voting.repo.NominationRepository;
import com.ciberaccion.voting.repo.RoundRepository;
import com.ciberaccion.voting.repo.VoteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class VotingService {

    private static final Logger log = LoggerFactory.getLogger(VotingService.class);

    private final RoundRepository roundRepository;
    private final NominationRepository nominationRepository;
    private final VoteRepository voteRepository;

    public VotingService(RoundRepository roundRepository,
                         NominationRepository nominationRepository,
                         VoteRepository voteRepository) {
        this.roundRepository = roundRepository;
        this.nominationRepository = nominationRepository;
        this.voteRepository = voteRepository;
    }

    public void castVote(Long roundId, Long contestantId) {

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> {
                    log.warn("Intento de voto en ronda inexistente: roundId={}", roundId);
                    return new NotFoundException("Round no existe: " + roundId);
                });

        if (round.getStatus() != RoundStatus.OPEN) {
            log.warn("Intento de voto en ronda no abierta: roundId={}, status={}", roundId, round.getStatus());
            throw new BadRequestException("La ronda no está abierta para votar. Estado actual: " + round.getStatus());
        }

        boolean nominated = nominationRepository.existsByRoundIdAndContestantId(roundId, contestantId);
        if (!nominated) {
            log.warn("Intento de voto por contestant no nominado: roundId={}, contestantId={}", roundId, contestantId);
            throw new BadRequestException("El participante " + contestantId + " no está nominado en la ronda " + roundId);
        }

        Vote vote = new Vote();
        vote.setRoundId(roundId);
        vote.setContestantId(contestantId);
        vote.setVoterId(null);
        vote.setCreatedAt(Instant.now());
        vote.setStatus(VoteStatus.ACCEPTED);
        vote.setRejectReason(null);

        voteRepository.save(vote);
        log.info("Voto registrado: roundId={}, contestantId={}", roundId, contestantId);
    }
}