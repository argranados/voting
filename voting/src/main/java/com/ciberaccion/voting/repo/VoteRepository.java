package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    long countByRoundIdAndContestantId(Long roundId, Long contestantId);

    @Query("""
                select v.contestantId as contestantId, count(v.id) as votes
                from Vote v
                where v.roundId = :roundId and v.status = com.ciberaccion.voting.domain.VoteStatus.ACCEPTED
                group by v.contestantId
                order by count(v.id) desc
            """)
    List<Object[]> countVotesByContestant(Long roundId);

}