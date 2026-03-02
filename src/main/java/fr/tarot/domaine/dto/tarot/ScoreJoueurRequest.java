package fr.tarot.domaine.dto.tarot;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ScoreJoueurRequest(
        @NotNull
        UUID joueurId,
        @NotNull
        Integer points
) {}
