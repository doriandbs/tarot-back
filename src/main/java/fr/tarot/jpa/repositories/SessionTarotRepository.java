package fr.tarot.jpa.repositories;

import fr.tarot.jpa.entities.SessionTarot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionTarotRepository extends JpaRepository<SessionTarot, UUID> {
    List<SessionTarot> findByUtilisateurIdOrderByDateSessionDescCreeLeDesc(UUID utilisateurId);
    Optional<SessionTarot> findByIdAndUtilisateurId(UUID id, UUID utilisateurId);
}
