package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.error.BadRequestException;
import com.ciberaccion.voting.domain.*;
import com.ciberaccion.voting.repo.ContestantRepository;
import com.ciberaccion.voting.repo.NominationRepository;
import com.ciberaccion.voting.repo.RoundRepository;
import com.ciberaccion.voting.repo.VoteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class VotingServiceTest {

    @Autowired
    VotingService votingService;

    @Autowired
    RoundRepository roundRepository;

    @Autowired
    ContestantRepository contestantRepository;

    @Autowired
    NominationRepository nominationRepository;

    @Autowired
    VoteRepository voteRepository;

    // @BeforeEach
    // void setUp() {
    //     voteRepository.deleteAll();
    //     nominationRepository.deleteAll();
    //     roundRepository.deleteAll();
    //     contestantRepository.deleteAll();
    // }

    @Test
    void castVote_fails_when_round_is_closed() {
        // arrange: crea una ronda CLOSED
        Round r = new Round();
        r.setSeasonId(1L);
        r.setName("Semana Test");
        r.setStartsAt(Instant.now());
        r.setEndsAt(Instant.now().plusSeconds(3600));
        r.setStatus(RoundStatus.CLOSED);
        r.setRuleType("ELIMINATE_LOWEST");
        r.setCreatedAt(Instant.now());
        r = roundRepository.save(r);

        // y una nominación para contestant 1
        Nomination n = new Nomination();
        n.setRoundId(r.getId());
        n.setContestantId(1L);
        n.setCreatedAt(Instant.now());
        nominationRepository.save(n);

        // act + assert
        final Round finalRound = r;
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> votingService.castVote(finalRound.getId(), 1L));

        assertTrue(ex.getMessage().contains("no está abierta"));
        assertEquals(0, voteRepository.count());
    }
}