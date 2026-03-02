package fr.tarot.domaine.dto.tarot;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FeuillePartieLigneResponse(
        UUID partieId,
        int numero,
        Instant joueLe,
        List<PartieScoreResponse> scoresPartie,
        List<ParticipantTotalResponse> cumulsApresPartie
) {}
