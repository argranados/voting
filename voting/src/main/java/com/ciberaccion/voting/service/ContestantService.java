package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.dto.CreateContestantRequest;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.Contestant;
import com.ciberaccion.voting.domain.ContestantStatus;
import com.ciberaccion.voting.repo.ContestantRepository;
import com.ciberaccion.voting.repo.SeasonRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ContestantService {

    private final ContestantRepository contestantRepository;
    private final SeasonRepository seasonRepository;

    public ContestantService(ContestantRepository contestantRepository,
                             SeasonRepository seasonRepository) {
        this.contestantRepository = contestantRepository;
        this.seasonRepository = seasonRepository;
    }

    public Contestant createContestant(CreateContestantRequest request) {
        if (!seasonRepository.existsById(request.getSeasonId())) {
            throw new NotFoundException("Season no existe: " + request.getSeasonId());
        }

        Contestant contestant = new Contestant();
        contestant.setSeasonId(request.getSeasonId());
        contestant.setName(request.getName());
        contestant.setStatus(ContestantStatus.ACTIVE);
        contestant.setCreatedAt(Instant.now());

        return contestantRepository.save(contestant);
    }

    public List<Contestant> getAllContestants() {
        return contestantRepository.findAll();
    }
}