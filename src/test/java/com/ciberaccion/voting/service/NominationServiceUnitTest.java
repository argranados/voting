// src/test/java/com/ciberaccion/voting/service/NominationServiceUnitTest.java
package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.error.BadRequestException;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.Nomination;
import com.ciberaccion.voting.domain.Round;
import com.ciberaccion.voting.domain.RoundStatus;
import com.ciberaccion.voting.repo.ContestantRepository;
import com.ciberaccion.voting.repo.NominationRepository;
import com.ciberaccion.voting.repo.RoundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NominationServiceUnitTest {

    @Mock
    RoundRepository roundRepository;

    @Mock
    ContestantRepository contestantRepository;

    @Mock
    NominationRepository nominationRepository;

    @InjectMocks
    NominationService nominationService;

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Round makeRound() {
        Round r = new Round();
        r.setId(1L);
        r.setSeasonId(1L);
        r.setName("Semana Test");
        r.setStartsAt(Instant.now());
        r.setEndsAt(Instant.now().plusSeconds(3600));
        r.setStatus(RoundStatus.SCHEDULED);
        r.setRuleType("ELIMINATE_LOWEST");
        r.setCreatedAt(Instant.now());
        return r;
    }

    // ─── nominate ─────────────────────────────────────────────────────────────

    @Test
    void nominate_success() {
        // arrange
        when(roundRepository.findById(1L)).thenReturn(Optional.of(makeRound()));
        when(contestantRepository.existsById(1L)).thenReturn(true);
        when(contestantRepository.existsById(2L)).thenReturn(true);
        when(nominationRepository.existsByRoundIdAndContestantId(1L, 1L)).thenReturn(false);
        when(nominationRepository.existsByRoundIdAndContestantId(1L, 2L)).thenReturn(false);
        when(nominationRepository.save(any(Nomination.class))).thenAnswer(i -> i.getArgument(0));

        // act
        List<Nomination> result = nominationService.nominate(1L, List.of(1L, 2L));

        // assert
        assertEquals(2, result.size());
        verify(nominationRepository, times(2)).save(any(Nomination.class));
    }

    @Test
    void nominate_fails_when_round_not_found() {
        when(roundRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> nominationService.nominate(99L, List.of(1L, 2L)));

        verify(nominationRepository, never()).save(any());
    }

    @Test
    void nominate_fails_when_duplicate_contestant_ids() {
        when(roundRepository.findById(1L)).thenReturn(Optional.of(makeRound()));

        // lista con duplicados
        assertThrows(BadRequestException.class,
                () -> nominationService.nominate(1L, List.of(1L, 1L)));

        verify(nominationRepository, never()).save(any());
    }

    @Test
    void nominate_fails_when_contestant_not_found() {
        when(roundRepository.findById(1L)).thenReturn(Optional.of(makeRound()));
        when(contestantRepository.existsById(1L)).thenReturn(true);
        when(contestantRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> nominationService.nominate(1L, List.of(1L, 99L)));

        verify(nominationRepository, never()).save(any());
    }

    @Test
    void nominate_skips_already_nominated_contestant() {
        when(roundRepository.findById(1L)).thenReturn(Optional.of(makeRound()));
        when(contestantRepository.existsById(1L)).thenReturn(true);
        when(contestantRepository.existsById(2L)).thenReturn(true);
        // contestant 1 ya está nominado, contestant 2 no
        when(nominationRepository.existsByRoundIdAndContestantId(1L, 1L)).thenReturn(true);
        when(nominationRepository.existsByRoundIdAndContestantId(1L, 2L)).thenReturn(false);
        when(nominationRepository.save(any(Nomination.class))).thenAnswer(i -> i.getArgument(0));

        List<Nomination> result = nominationService.nominate(1L, List.of(1L, 2L));

        // solo se creó 1 nomination, el otro se ignoró
        assertEquals(1, result.size());
        verify(nominationRepository, times(1)).save(any(Nomination.class));
    }
}