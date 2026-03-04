package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.error.BadRequestException;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.Nomination;
import com.ciberaccion.voting.repo.ContestantRepository;
import com.ciberaccion.voting.repo.NominationRepository;
import com.ciberaccion.voting.repo.RoundRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class NominationService {

    private final RoundRepository roundRepository;
    private final ContestantRepository contestantRepository;
    private final NominationRepository nominationRepository;

    public NominationService(RoundRepository roundRepository,
                             ContestantRepository contestantRepository,
                             NominationRepository nominationRepository) {
        this.roundRepository = roundRepository;
        this.contestantRepository = contestantRepository;
        this.nominationRepository = nominationRepository;
    }

    public List<Nomination> nominate(Long roundId, List<Long> contestantIds) {

        // 1) validar que exista la ronda
        roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("Round no existe: " + roundId));

        // 2) validar que no haya duplicados en la lista
        if (contestantIds.stream().distinct().count() != contestantIds.size()) {
            throw new BadRequestException("La lista de contestantIds contiene duplicados");
        }

        // 3) validar que existan los contestants
        for (Long cid : contestantIds) {
            if (!contestantRepository.existsById(cid)) {
                throw new NotFoundException("Contestant no existe: " + cid);
            }
        }

        // 4) crear nominaciones (si ya existe, se ignora)
        List<Nomination> created = new ArrayList<>();
        for (Long cid : contestantIds) {

            if (nominationRepository.existsByRoundIdAndContestantId(roundId, cid)) {
                continue;
            }

            Nomination n = new Nomination();
            n.setRoundId(roundId);
            n.setContestantId(cid);
            n.setCreatedAt(Instant.now());

            created.add(nominationRepository.save(n));
        }

        return created;
    }
}