package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.dto.RoundResultsResponse;
import com.ciberaccion.voting.domain.*;
import com.ciberaccion.voting.repo.ContestantRepository;
import com.ciberaccion.voting.repo.RoundRepository;
import com.ciberaccion.voting.repo.VoteRepository;

import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class ResultsServiceTest {

    @Autowired
    ResultsService resultsService;

    @Autowired
    RoundRepository roundRepository;

    @Autowired
    ContestantRepository contestantRepository;

    @Autowired
    VoteRepository voteRepository;

    @Test
    void getResults_counts_votes_correctly() {
        // contestants
        Contestant a = new Contestant();
        a.setSeasonId(1L);
        a.setName("A");
        a.setStatus(ContestantStatus.ACTIVE);
        a.setCreatedAt(Instant.now());
        a = contestantRepository.save(a);

        Contestant b = new Contestant();
        b.setSeasonId(1L);
        b.setName("B");
        b.setStatus(ContestantStatus.ACTIVE);
        b.setCreatedAt(Instant.now());
        b = contestantRepository.save(b);

        // round
        Round r = new Round();
        r.setSeasonId(1L);
        r.setName("Semana Test");
        r.setStartsAt(Instant.now());
        r.setEndsAt(Instant.now().plusSeconds(3600));
        r.setStatus(RoundStatus.OPEN);
        r.setRuleType("ELIMINATE_LOWEST");
        r.setCreatedAt(Instant.now());
        r = roundRepository.save(r);

        // votes: 2 for A, 1 for B
        voteRepository.save(makeVote(r.getId(), a.getId()));
        voteRepository.save(makeVote(r.getId(), a.getId()));
        voteRepository.save(makeVote(r.getId(), b.getId()));

        RoundResultsResponse res = resultsService.getResults(r.getId());

        assertEquals(r.getId(), res.getRoundId());
        assertEquals(2, res.getResults().size());

        assertEquals(a.getId(), res.getResults().get(0).getContestantId());
        assertEquals(2, res.getResults().get(0).getVotes());

        assertEquals(b.getId(), res.getResults().get(1).getContestantId());
        assertEquals(1, res.getResults().get(1).getVotes());
    }

    private Vote makeVote(Long roundId, Long contestantId) {
        Vote v = new Vote();
        v.setRoundId(roundId);
        v.setContestantId(contestantId);
        v.setVoterId(null);
        v.setCreatedAt(Instant.now());
        v.setStatus(VoteStatus.ACCEPTED);
        v.setRejectReason(null);
        return v;
    }
}