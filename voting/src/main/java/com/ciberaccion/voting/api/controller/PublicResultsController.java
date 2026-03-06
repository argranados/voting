package com.ciberaccion.voting.api.controller;

import com.ciberaccion.voting.api.dto.RoundResultsResponse;
import com.ciberaccion.voting.service.ResultsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public")
public class PublicResultsController {

    private final ResultsService resultsService;

    public PublicResultsController(ResultsService resultsService) {
        this.resultsService = resultsService;
    }

    @GetMapping("/rounds/{roundId}/results")
    public RoundResultsResponse getResults(@PathVariable Long roundId) {
        return resultsService.getResults(roundId);
    }
}