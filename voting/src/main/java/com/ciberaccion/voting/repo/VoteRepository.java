package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    long countByRoundIdAndContestantId(Long roundId, Long contestantId);

    @Query("""
                select v.contestantId, c.name, count(v.id)
                from Vote v, Contestant c
                where v.roundId = :roundId
                  and v.status = com.ciberaccion.voting.domain.VoteStatus.ACCEPTED
                  and c.id = v.contestantId
                group by v.contestantId, c.name
                order by count(v.id) desc
            """)
    List<Object[]> countVotesByContestant(Long roundId);

}