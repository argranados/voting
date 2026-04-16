package com.ciberaccion.voting.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ciberaccion.voting.domain.Vote;
import com.ciberaccion.voting.repo.projection.VoteCountProjection;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    long countByRoundIdAndContestantId(Long roundId, Long contestantId);

    @Query("""
                select v.contestantId AS contestantId,
                       c.name AS contestantName, 
                       count(v.id) AS voteCount
                from Vote v, Contestant c
                where v.roundId = :roundId
                  and v.status = com.ciberaccion.voting.domain.VoteStatus.ACCEPTED
                  and c.id = v.contestantId
                group by v.contestantId, c.name
                order by count(v.id) desc
            """)
    List<VoteCountProjection> countVotesByContestant(Long roundId);

}