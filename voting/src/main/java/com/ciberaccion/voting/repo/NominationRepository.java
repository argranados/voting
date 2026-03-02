package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.Nomination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NominationRepository extends JpaRepository<Nomination, Long> {

    boolean existsByRoundIdAndContestantId(Long roundId, Long contestantId);

}