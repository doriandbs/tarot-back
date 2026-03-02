package fr.tarot.domaine.dto.tarot;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateSessionRequest(
        @Size(max = 150)
        String nom,
        @NotNull
        LocalDate dateSession,
        @NotEmpty
        List<@NotNull UUID> participantIds
) {}
