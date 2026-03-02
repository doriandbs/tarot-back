package fr.tarot.jpa.repositories;

import fr.tarot.jpa.entities.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JoueurRepository extends JpaRepository<Joueur, UUID> {
    List<Joueur> findByUtilisateurIdOrderByNomAsc(UUID utilisateurId);
    boolean existsByUtilisateurIdAndNomIgnoreCase(UUID utilisateurId, String nom);
}
