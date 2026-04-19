// src/test/java/com/ciberaccion/voting/service/RoundServiceUnitTest.java
package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.dto.CreateRoundRequest;
import com.ciberaccion.voting.api.error.BadRequestException;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.Round;
import com.ciberaccion.voting.domain.RoundStatus;
import com.ciberaccion.voting.repo.RoundRepository;
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

@ExtendWith(MockitoExtension.class)
class RoundServiceUnitTest {

    @Mock
    RoundRepository roundRepository;

    @InjectMocks
    RoundService roundService;

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Round makeRound(Long id, RoundStatus status) {
        Round r = new Round();
        r.setId(id);
        r.setSeasonId(1L);
        r.setName("Semana Test");
        r.setStartsAt(Instant.now());
        r.setEndsAt(Instant.now().plusSeconds(3600));
        r.setStatus(status);
        r.setRuleType("ELIMINATE_LOWEST");
        r.setCreatedAt(Instant.now());
        return r;
    }

    private CreateRoundRequest makeRequest(Instant startsAt, Instant endsAt) {
        CreateRoundRequest req = new CreateRoundRequest();
        req.setSeasonId(1L);
        req.setName("Semana Test");
        req.setStartsAt(startsAt);
        req.setEndsAt(endsAt);
        req.setRuleType("ELIMINATE_LOWEST");
        return req;
    }

    // ─── createRound ──────────────────────────────────────────────────────────

    @Test
    void createRound_success() {
        // arrange
        Instant start = Instant.now();
        Instant end = start.plusSeconds(3600);
        CreateRoundRequest req = makeRequest(start, end);
        Round saved = makeRound(1L, RoundStatus.SCHEDULED);
        when(roundRepository.save(any(Round.class))).thenReturn(saved);

        // act
        Round result = roundService.createRound(req);

        // assert
        assertEquals(RoundStatus.SCHEDULED, result.getStatus());
        verify(roundRepository).save(any(Round.class));
    }

    @Test
    void createRound_fails_when_endsAt_before_startsAt() {
        // arrange
        Instant start = Instant.now();
        Instant end = start.minusSeconds(3600); // end antes de start
        CreateRoundRequest req = makeRequest(start, end);

        // act + assert
        assertThrows(BadRequestException.class,
                () -> roundService.createRound(req));

        verify(roundRepository, never()).save(any());
    }

    @Test
    void createRound_fails_when_endsAt_equals_startsAt() {
        // arrange
        Instant same = Instant.now();
        CreateRoundRequest req = makeRequest(same, same);

        // act + assert
        assertThrows(BadRequestException.class,
                () -> roundService.createRound(req));

        verify(roundRepository, never()).save(any());
    }

    // ─── openRound ────────────────────────────────────────────────────────────

    @Test
    void openRound_success() {
        // arrange
        Round round = makeRound(1L, RoundStatus.SCHEDULED);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(roundRepository.findBySeasonIdAndStatus(1L, RoundStatus.OPEN))
                .thenReturn(Optional.empty());
        when(roundRepository.save(any(Round.class))).thenReturn(round);

        // act
        Round result = roundService.openRound(1L);

        // assert
        assertEquals(RoundStatus.OPEN, result.getStatus());
        verify(roundRepository).save(any(Round.class));
    }

    @Test
    void openRound_fails_when_round_not_found() {
        when(roundRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> roundService.openRound(99L));
    }

    @Test
    void openRound_fails_when_already_open() {
        Round round = makeRound(1L, RoundStatus.OPEN);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));

        assertThrows(BadRequestException.class,
                () -> roundService.openRound(1L));

        verify(roundRepository, never()).save(any());
    }

    @Test
    void openRound_fails_when_already_closed() {
        Round round = makeRound(1L, RoundStatus.CLOSED);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));

        assertThrows(BadRequestException.class,
                () -> roundService.openRound(1L));

        verify(roundRepository, never()).save(any());
    }

    @Test
    void openRound_fails_when_season_already_has_open_round() {
        Round round = makeRound(1L, RoundStatus.SCHEDULED);
        Round existingOpen = makeRound(2L, RoundStatus.OPEN);

        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(roundRepository.findBySeasonIdAndStatus(1L, RoundStatus.OPEN))
                .thenReturn(Optional.of(existingOpen));

        assertThrows(BadRequestException.class,
                () -> roundService.openRound(1L));

        verify(roundRepository, never()).save(any());
    }

    // ─── closeRound ───────────────────────────────────────────────────────────

    @Test
    void closeRound_success() {
        Round round = makeRound(1L, RoundStatus.OPEN);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));
        when(roundRepository.save(any(Round.class))).thenReturn(round);

        Round result = roundService.closeRound(1L);

        assertEquals(RoundStatus.CLOSED, result.getStatus());
        verify(roundRepository).save(any(Round.class));
    }

    @Test
    void closeRound_fails_when_already_closed() {
        Round round = makeRound(1L, RoundStatus.CLOSED);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));

        assertThrows(BadRequestException.class,
                () -> roundService.closeRound(1L));

        verify(roundRepository, never()).save(any());
    }

    @Test
    void closeRound_fails_when_scheduled() {
        Round round = makeRound(1L, RoundStatus.SCHEDULED);
        when(roundRepository.findById(1L)).thenReturn(Optional.of(round));

        assertThrows(BadRequestException.class,
                () -> roundService.closeRound(1L));

        verify(roundRepository, never()).save(any());
    }
}