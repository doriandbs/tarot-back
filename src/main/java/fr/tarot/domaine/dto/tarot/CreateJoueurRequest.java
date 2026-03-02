package fr.tarot.domaine.dto.tarot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJoueurRequest(
        @NotBlank
        @Size(max = 120)
        String nom
) {}
