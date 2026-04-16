package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.Nomination;
import com.ciberaccion.voting.repo.projection.NomineeProjection;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NominationRepository extends JpaRepository<Nomination, Long> {

    boolean existsByRoundIdAndContestantId(Long roundId, Long contestantId);

    List<Nomination> findByRoundId(Long roundId);

    // Query con JOIN que reemplaza el N+1
    // Trae contestantId y contestantName en una sola query
    @Query("""
            SELECT c.id AS contestantId, c.name AS contestantName
            FROM Nomination n
            JOIN Contestant c ON c.id = n.contestantId
            WHERE n.roundId = :roundId
            """)
    List<NomineeProjection> findNomineesByRoundId(Long roundId);
}