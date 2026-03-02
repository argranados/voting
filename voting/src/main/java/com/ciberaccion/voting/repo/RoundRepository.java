package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundRepository extends JpaRepository<Round, Long> {
}
