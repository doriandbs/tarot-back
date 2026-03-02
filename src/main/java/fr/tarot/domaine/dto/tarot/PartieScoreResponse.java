package fr.tarot.domaine.dto.tarot;

import java.util.UUID;
import java.math.BigDecimal;

public record PartieScoreResponse(
        UUID joueurId,
        String joueurNom,
        boolean gagnant,
        BigDecimal points
) {}
