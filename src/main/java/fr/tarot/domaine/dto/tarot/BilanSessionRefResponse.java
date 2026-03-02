package fr.tarot.domaine.dto.tarot;

import java.time.LocalDate;
import java.util.UUID;

public record BilanSessionRefResponse(
        UUID sessionId,
        String sessionNom,
        LocalDate dateSession
) {}
