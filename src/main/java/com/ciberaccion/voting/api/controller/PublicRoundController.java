package com.ciberaccion.voting.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ciberaccion.voting.api.dto.NomineeResponse;
import com.ciberaccion.voting.api.dto.RoundResponse;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.api.mapper.RoundMapper;
import com.ciberaccion.voting.domain.RoundStatus;
import com.ciberaccion.voting.repo.RoundRepository;
import com.ciberaccion.voting.service.NominationService;

@RestController
@RequestMapping("/api/v1/public")
public class PublicRoundController {

    private final RoundRepository roundRepository;
    private final NominationService nominationService;

    public PublicRoundController(RoundRepository roundRepository, NominationService nominationService) {
        this.roundRepository = roundRepository;
        this.nominationService = nominationService;
    }

    @GetMapping("/rounds/current")
    public RoundResponse getCurrentRound() {
        return roundRepository.findFirstByStatusOrderByIdDesc(RoundStatus.OPEN)
                .map(RoundMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("No hay ronda OPEN en este momento"));
    }

    @GetMapping("/rounds/{roundId}/nominees")
    public List<NomineeResponse> nominees(@PathVariable Long roundId) {
        return nominationService.getNominees(roundId);
    }
}