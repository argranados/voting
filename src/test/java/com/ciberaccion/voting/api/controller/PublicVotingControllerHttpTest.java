package com.ciberaccion.voting.api.controller;

import com.ciberaccion.voting.domain.*;
import com.ciberaccion.voting.repo.ContestantRepository;
import com.ciberaccion.voting.repo.NominationRepository;
import com.ciberaccion.voting.repo.RoundRepository;
import com.ciberaccion.voting.repo.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// @Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PublicVotingControllerHttpTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    RoundRepository roundRepository;
    @Autowired
    ContestantRepository contestantRepository;
    @Autowired
    NominationRepository nominationRepository;
    @Autowired
    VoteRepository voteRepository;

    @BeforeEach
    void setup() {
    voteRepository.deleteAll();
    nominationRepository.deleteAll();
    roundRepository.deleteAll();
    contestantRepository.deleteAll();
    }

    @Test
    void postVotes_returns201_and_savesVote() {
        // contestant
        Contestant c = new Contestant();
        c.setSeasonId(1L);
        c.setName("Famoso");
        c.setStatus(ContestantStatus.ACTIVE);
        c.setCreatedAt(Instant.now());
        c = contestantRepository.save(c);

        // round OPEN
        Round r = new Round();
        r.setSeasonId(1L);
        r.setName("Semana");
        r.setStartsAt(Instant.now());
        r.setEndsAt(Instant.now().plusSeconds(3600));
        r.setStatus(RoundStatus.OPEN);
        r.setRuleType("ELIMINATE_LOWEST");
        r.setCreatedAt(Instant.now());
        r = roundRepository.save(r);

        // nomination
        Nomination n = new Nomination();
        n.setRoundId(r.getId());
        n.setContestantId(c.getId());
        n.setCreatedAt(Instant.now());
        nominationRepository.save(n);

        var response = restTemplate.postForEntity(
                "/api/v1/public/votes",
                Map.of("roundId", r.getId(), "contestantId", c.getId()),
                String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, voteRepository.count());
    }

    @Test
    void postVote_returns400_when_round_closed() {

        // contestant
        Contestant c = new Contestant();
        c.setSeasonId(1L);
        c.setName("Famoso");
        c.setStatus(ContestantStatus.ACTIVE);
        c.setCreatedAt(Instant.now());
        c = contestantRepository.save(c);

        // round CLOSED
        Round r = new Round();
        r.setSeasonId(1L);
        r.setName("Semana Test");
        r.setStartsAt(Instant.now());
        r.setEndsAt(Instant.now().plusSeconds(3600));
        r.setStatus(RoundStatus.CLOSED);
        r.setRuleType("ELIMINATE_LOWEST");
        r.setCreatedAt(Instant.now());
        r = roundRepository.save(r);

        // nomination
        Nomination n = new Nomination();
        n.setRoundId(r.getId());
        n.setContestantId(c.getId());
        n.setCreatedAt(Instant.now());
        nominationRepository.save(n);

        var response = restTemplate.postForEntity(                
                "/api/v1/public/votes",
                Map.of("roundId", r.getId(), "contestantId", c.getId()),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}