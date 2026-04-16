package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.Round;
import com.ciberaccion.voting.domain.RoundStatus;
import com.ciberaccion.voting.repo.RoundRepository;
import org.springframework.stereotype.Service;
import com.ciberaccion.voting.api.error.BadRequestException;
import com.ciberaccion.voting.api.dto.CreateRoundRequest;

import java.time.Instant;

@Service
public class RoundService {

    private final RoundRepository roundRepository;

    public RoundService(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    public Round createRound(CreateRoundRequest req) {

        if (req.getEndsAt().isBefore(req.getStartsAt()) || req.getEndsAt().equals(req.getStartsAt())) {
            throw new BadRequestException("endsAt debe ser mayor a startsAt");
        }

        Round round = new Round();
        round.setSeasonId(req.getSeasonId());
        round.setName(req.getName());
        round.setStartsAt(req.getStartsAt());
        round.setEndsAt(req.getEndsAt());
        round.setStatus(RoundStatus.SCHEDULED); // clave: siempre se crea SCHEDULED
        round.setRuleType(req.getRuleType());
        round.setCreatedAt(Instant.now());

        return roundRepository.save(round);
    }

    public Round openRound(Long roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("Round no existe: " + roundId));

        if (round.getStatus() == RoundStatus.OPEN) {
            throw new BadRequestException("La ronda ya está OPEN");
        }
        if (round.getStatus() == RoundStatus.CLOSED) {
            throw new BadRequestException("La ronda ya está CLOSED y no se puede reabrir (Nivel 1)");
        }
        roundRepository.findBySeasonIdAndStatus(round.getSeasonId(), RoundStatus.OPEN)
                .ifPresent(r -> {
                    throw new BadRequestException(
                            "Ya existe una ronda OPEN para esta temporada: " + r.getId());
                });
        round.setStatus(RoundStatus.OPEN);
        return roundRepository.save(round);
    }

    public Round closeRound(Long roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("Round no existe: " + roundId));

        if (round.getStatus() == RoundStatus.CLOSED) {
            throw new BadRequestException("La ronda ya está CLOSED");
        }
        if (round.getStatus() != RoundStatus.OPEN) {
            throw new BadRequestException("Solo se puede cerrar una ronda en estado OPEN");
        }

        round.setStatus(RoundStatus.CLOSED);
        return roundRepository.save(round);
    }
}