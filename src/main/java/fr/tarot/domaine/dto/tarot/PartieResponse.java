package fr.tarot.domaine.dto.tarot;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PartieResponse(
        UUID id,
        int numero,
        Instant joueLe,
        PreneurType typePreneur,
        BigDecimal multiplicateurPreneur,
        Integer boutsGagnants,
        Integer pointsFaitsGagnants,
        Integer pointsAFaire,
        Integer pointsDeBase,
        BigDecimal pointsContrat,
        List<BonusPartieResponse> bonus,
        List<PartieScoreResponse> scores
) {}
