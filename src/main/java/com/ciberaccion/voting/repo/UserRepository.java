// src/main/java/com/ciberaccion/voting/repo/UserRepository.java
package com.ciberaccion.voting.repo;

import com.ciberaccion.voting.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}