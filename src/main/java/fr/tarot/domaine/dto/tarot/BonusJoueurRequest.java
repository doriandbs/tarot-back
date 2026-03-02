package fr.tarot.domaine.dto.tarot;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BonusJoueurRequest(
        @NotNull
        UUID joueurId,
        @NotNull
        BonusType typeBonus
) {}
