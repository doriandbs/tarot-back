package fr.tarot.domaine.dto.tarot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SessionSummaryResponse(
        UUID id,
        String nom,
        LocalDate dateSession,
        List<JoueurResponse> participants,
        List<ParticipantTotalResponse> totaux
) {}
