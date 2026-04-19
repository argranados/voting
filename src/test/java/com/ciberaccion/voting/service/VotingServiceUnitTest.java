// src/test/java/com/ciberaccion/voting/service/VotingServiceUnitTest.java
package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.error.BadRequestException;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.*;
import com.ciberaccion.voting.repo.NominationRepository;
import com.ciberaccion.voting.repo.RoundRepository;
import com.ciberaccion.voting.repo.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Activa Mockito sin levantar Spring
class VotingServiceUnitTest {

    @Mock
    RoundRepository roundRepository;

    @Mock
    NominationRepository nominationRepository;

    @Mock
    VoteRepository voteRepository;

    @InjectMocks // Crea VotingService inyectando los mocks anteriores
    VotingService votingService;

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Round makeRound(RoundStatus status) {
        Round r = new Round();
        r.setId(1L);
        r.setSeasonId(1L);
        r.setName("Semana Test");
        r.setStartsAt(Instant.now());
        r.setEndsAt(Instant.now().plusSeconds(3600));
        r.setStatus(status);
        r.setRuleType("ELIMINATE_LOWEST");
        r.setCreatedAt(Instant.now());
        return r;
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    @Test
    void castVote_success() {
        // arrange
        Round round = makeRound(RoundStatus.OPEN);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(nominationRepository.existsByRoundIdAndContestantId(1L, 2L)).thenReturn(true);

        // act
        votingService.castVote(1L, 2L);

        // assert — verificamos que se guardó exactamente un voto
        verify(voteRepository).save(any(Vote.class));
    }

    @Test
    void castVote_fails_when_round_not_found() {
        // arrange
        when(roundRepository.findById(99L)).thenReturn(Optional.empty());

        // act + assert
        assertThrows(NotFoundException.class,
                () -> votingService.castVote(99L, 1L));

        // nunca debe guardar un voto
        verify(voteRepository, never()).save(any());
    }

    @Test
    void castVote_fails_when_round_is_closed() {
        // arrange
        Round round = makeRound(RoundStatus.CLOSED);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));

        // act + assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> votingService.castVote(1L, 2L));

        assertTrue(ex.getMessage().contains("no está abierta"));
        verify(voteRepository, never()).save(any());
    }

    @Test
    void castVote_fails_when_round_is_scheduled() {
        // arrange
        Round round = makeRound(RoundStatus.SCHEDULED);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));

        // act + assert
        assertThrows(BadRequestException.class,
                () -> votingService.castVote(1L, 2L));

        verify(voteRepository, never()).save(any());
    }

    @Test
    void castVote_fails_when_contestant_not_nominated() {
        // arrange
        Round round = makeRound(RoundStatus.OPEN);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(nominationRepository.existsByRoundIdAndContestantId(1L, 99L)).thenReturn(false);

        // act + assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> votingService.castVote(1L, 99L));

        assertTrue(ex.getMessage().contains("no está nominado"));
        verify(voteRepository, never()).save(any());
    }
}