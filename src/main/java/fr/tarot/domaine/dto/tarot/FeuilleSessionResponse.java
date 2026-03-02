package fr.tarot.domaine.dto.tarot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FeuilleSessionResponse(
        UUID sessionId,
        String sessionNom,
        LocalDate dateSession,
        List<JoueurResponse> participants,
        List<FeuillePartieLigneResponse> lignes
) {}
