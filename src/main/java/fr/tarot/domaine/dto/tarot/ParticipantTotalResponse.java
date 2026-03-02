package fr.tarot.domaine.dto.tarot;

import java.util.UUID;
import java.math.BigDecimal;

public record ParticipantTotalResponse(
        UUID joueurId,
        String joueurNom,
        BigDecimal totalPoints
) {}
