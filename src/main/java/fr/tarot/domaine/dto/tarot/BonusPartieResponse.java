package fr.tarot.domaine.dto.tarot;

import java.util.UUID;

public record BonusPartieResponse(
        UUID joueurId,
        String joueurNom,
        BonusType typeBonus,
        int points
) {}
