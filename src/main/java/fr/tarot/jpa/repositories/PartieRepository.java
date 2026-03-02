package fr.tarot.jpa.repositories;

import fr.tarot.jpa.entities.Partie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PartieRepository extends JpaRepository<Partie, UUID> {
    List<Partie> findBySessionIdOrderByNumeroAsc(UUID sessionId);
    java.util.Optional<Partie> findByIdAndSessionId(UUID id, UUID sessionId);

    @Query("select coalesce(max(p.numero), 0) from Partie p where p.session.id = :sessionId")
    Integer findMaxNumeroBySessionId(UUID sessionId);
}
