package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.Contestant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestantRepository extends JpaRepository<Contestant, Long> {
}