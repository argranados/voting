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
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class VotingService {

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

        // 1) La ronda debe existir
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("Round no existe: " + roundId));

        // 2) La ronda debe estar OPEN
        if (round.getStatus() != RoundStatus.OPEN) {
            throw new BadRequestException("La ronda no está abierta para votar. Estado actual: " + round.getStatus());
        }

        // 3) El participante debe estar nominado en esa ronda
        boolean nominated = nominationRepository.existsByRoundIdAndContestantId(roundId, contestantId);
        if (!nominated) {
            throw new BadRequestException("El participante " + contestantId + " no está nominado en la ronda " + roundId);
        }

        // 4) Guardar voto (Nivel 1: sin voter)
        Vote vote = new Vote();
        vote.setRoundId(roundId);
        vote.setContestantId(contestantId);
        vote.setVoterId(null); // Nivel 1
        vote.setCreatedAt(Instant.now());
        vote.setStatus(VoteStatus.ACCEPTED);
        vote.setRejectReason(null);

        voteRepository.save(vote);
    }
}