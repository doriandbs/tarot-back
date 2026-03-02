package fr.tarot.domaine.dto.tarot;

import java.util.UUID;

public record JoueurResponse(
        UUID id,
        String nom
) {}
