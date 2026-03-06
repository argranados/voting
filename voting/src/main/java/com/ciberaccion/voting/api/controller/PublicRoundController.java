package com.ciberaccion.voting.api.controller;

import com.ciberaccion.voting.api.dto.NomineeResponse;
import com.ciberaccion.voting.api.dto.RoundResponse;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.api.mapper.NominationMapper;
import com.ciberaccion.voting.api.mapper.RoundMapper;
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
    public RoundResponse getCurrentRound() {
        return roundRepository.findFirstByStatusOrderByIdDesc(RoundStatus.OPEN)
                .map(RoundMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("No hay ronda OPEN en este momento"));
    }

    @GetMapping("/rounds/{roundId}/nominees")
    public List<NomineeResponse> nominees(@PathVariable Long roundId) {
        return nominationRepository.findByRoundId(roundId)
                .stream()
                .map(NominationMapper::toResponse)
                .toList();
    }
}