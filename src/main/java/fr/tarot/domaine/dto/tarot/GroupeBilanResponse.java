package fr.tarot.domaine.dto.tarot;

import java.util.List;

public record GroupeBilanResponse(
        String groupeKey,
        List<JoueurResponse> participants,
        int nombreSessions,
        List<BilanSessionRefResponse> sessions,
        List<ParticipantTotalResponse> cumulPoints
) {}
