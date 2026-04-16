package com.ciberaccion.voting.api.controller;

import com.ciberaccion.voting.api.dto.CreateSeasonRequest;
import com.ciberaccion.voting.api.dto.SeasonResponse;
import com.ciberaccion.voting.api.mapper.SeasonMapper;
import com.ciberaccion.voting.service.SeasonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/seasons")
public class AdminSeasonController {

    private final SeasonService seasonService;

    public AdminSeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SeasonResponse createSeason(@Valid @RequestBody CreateSeasonRequest request) {
        return SeasonMapper.toResponse(seasonService.createSeason(request));
    }

    @GetMapping
    public List<SeasonResponse> getAllSeasons() {
        return seasonService.getAllSeasons()
                .stream()
                .map(SeasonMapper::toResponse)
                .toList();
    }
}