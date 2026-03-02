package fr.tarot.jpa.repositories;

import fr.tarot.jpa.entities.BonusPartie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BonusPartieRepository extends JpaRepository<BonusPartie, UUID> {
    List<BonusPartie> findByPartieIdIn(List<UUID> partieIds);
}
