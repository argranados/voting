package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.dto.CreateSeasonRequest;
import com.ciberaccion.voting.domain.Season;
import com.ciberaccion.voting.domain.SeasonType;
import com.ciberaccion.voting.repo.SeasonRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SeasonService {

    private final SeasonRepository seasonRepository;

    public SeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    public Season createSeason(CreateSeasonRequest request) {
        Season season = new Season();
        season.setName(request.getName());
        season.setStatus(SeasonType.ACTIVE);
        season.setCreatedAt(Instant.now());

        return seasonRepository.save(season);
    }

    public List<Season> getAllSeasons() {
        return seasonRepository.findAll();
    }
}