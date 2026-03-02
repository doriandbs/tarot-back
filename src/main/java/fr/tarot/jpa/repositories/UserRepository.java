package fr.tarot.jpa.repositories;

import fr.tarot.jpa.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Utilisateur, UUID> {
    Optional<Utilisateur> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}