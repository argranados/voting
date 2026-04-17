package com.ciberaccion.voting.service;

import com.ciberaccion.voting.api.dto.NomineeResponse;
import com.ciberaccion.voting.api.error.BadRequestException;
import com.ciberaccion.voting.api.error.NotFoundException;
import com.ciberaccion.voting.domain.Nomination;
import com.ciberaccion.voting.repo.ContestantRepository;
import com.ciberaccion.voting.repo.NominationRepository;
import com.ciberaccion.voting.repo.RoundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class NominationService {

    private static final Logger log = LoggerFactory.getLogger(NominationService.class);

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

        roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("Round no existe: " + roundId));

        if (contestantIds.stream().distinct().count() != contestantIds.size()) {
            throw new BadRequestException("La lista de contestantIds contiene duplicados");
        }

        for (Long cid : contestantIds) {
            if (!contestantRepository.existsById(cid)) {
                throw new NotFoundException("Contestant no existe: " + cid);
            }
        }

        List<Nomination> created = new ArrayList<>();
        for (Long cid : contestantIds) {
            if (nominationRepository.existsByRoundIdAndContestantId(roundId, cid)) {
                log.warn("Contestant ya nominado, se ignora: roundId={}, contestantId={}", roundId, cid);
                continue;
            }
            Nomination n = new Nomination();
            n.setRoundId(roundId);
            n.setContestantId(cid);
            n.setCreatedAt(Instant.now());
            created.add(nominationRepository.save(n));
        }

        log.info("Nominations creadas: roundId={}, cantidad={}", roundId, created.size());
        return created;
    }

    public List<NomineeResponse> getNominees(Long roundId) {
        roundRepository.findById(roundId)
                .orElseThrow(() -> new NotFoundException("Round no existe: " + roundId));

        List<NomineeResponse> nominees = nominationRepository.findNomineesByRoundId(roundId)
                .stream()
                .map(p -> new NomineeResponse(p.getContestantId(), p.getContestantName()))
                .toList();

        log.debug("Nominees consultados: roundId={}, cantidad={}", roundId, nominees.size());
        return nominees;
    }
}