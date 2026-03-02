package fr.tarot.jpa.repositories;

import fr.tarot.jpa.entities.ScorePartie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScorePartieRepository extends JpaRepository<ScorePartie, UUID> {
    List<ScorePartie> findByPartieIdOrderBySessionParticipantJoueurNomAsc(UUID partieId);
    List<ScorePartie> findByPartieIdIn(List<UUID> partieIds);
}
