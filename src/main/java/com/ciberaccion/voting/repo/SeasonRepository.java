package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Long> {
}
