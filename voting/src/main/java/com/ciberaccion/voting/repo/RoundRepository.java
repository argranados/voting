package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.Round;
import com.ciberaccion.voting.domain.RoundStatus;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundRepository extends JpaRepository<Round, Long> {
    Optional<Round> findFirstByStatusOrderByIdDesc(RoundStatus status); 
}
