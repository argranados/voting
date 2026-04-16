package com.ciberaccion.voting.api.controller;

import com.ciberaccion.voting.api.dto.ContestantResponse;
import com.ciberaccion.voting.api.dto.CreateContestantRequest;
import com.ciberaccion.voting.api.mapper.ContestantMapper;
import com.ciberaccion.voting.service.ContestantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/contestants")
public class AdminContestantController {

    private final ContestantService contestantService;

    public AdminContestantController(ContestantService contestantService) {
        this.contestantService = contestantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContestantResponse createContestant(@Valid @RequestBody CreateContestantRequest request) {
        return ContestantMapper.toResponse(contestantService.createContestant(request));
    }

    @GetMapping
    public List<ContestantResponse> getAllContestants() {
        return contestantService.getAllContestants()
                .stream()
                .map(ContestantMapper::toResponse)
                .toList();
    }
}