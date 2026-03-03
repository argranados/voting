package com.ciberaccion.voting.api.controller;

import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.Nomination;
import com.ciberaccion.voting.domain.Round;
import com.ciberaccion.voting.domain.RoundStatus;
import com.ciberaccion.voting.repo.NominationRepository;
import com.ciberaccion.voting.repo.RoundRepository;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public")
public class PublicRoundController {

    private final RoundRepository roundRepository;
    private final NominationRepository nominationRepository;

    public PublicRoundController(RoundRepository roundRepository, NominationRepository nominationRepository) {
        this.roundRepository = roundRepository;
        this.nominationRepository = nominationRepository;
    }

    @GetMapping("/rounds/current")
    public Round getCurrentRound() {
        return roundRepository.findFirstByStatusOrderByIdDesc(RoundStatus.OPEN)
                .orElseThrow(() -> new NotFoundException("No hay ronda OPEN en este momento"));
    }

    @GetMapping("/rounds/{roundId}/nominees")
    public List<Nomination> getNominees(@PathVariable Long roundId) {
        return nominationRepository.findByRoundId(roundId);
    }
}