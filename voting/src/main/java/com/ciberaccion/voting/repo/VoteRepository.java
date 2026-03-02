package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    long countByRoundIdAndContestantId(Long roundId, Long contestantId);

}