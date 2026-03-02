package fr.tarot.jpa.repositories;

import fr.tarot.jpa.entities.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, UUID> {

    @Query("""
            select sp
            from SessionParticipant sp
            join fetch sp.joueur j
            where sp.session.id = :sessionId
            order by j.nom asc
            """)
    List<SessionParticipant> findBySessionIdOrderByJoueurNom(UUID sessionId);
}
