package fr.tarot.domaine.dto.tarot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreatePartieRequest(
        @NotEmpty
        List<@NotNull UUID> winnerJoueurIds,
        @NotNull
        PreneurType preneurType,
        @NotNull
        @Min(0)
        @Max(3)
        Integer boutsGagnants,
        @Min(0)
        @Max(91)
        Integer pointsFaitsGagnants,
        @Min(0)
        @Max(91)
        Integer pointsFaitsPerdants,
        @Size(max = 20)
        List<@Valid BonusJoueurRequest> bonusJoueurs
) {}
