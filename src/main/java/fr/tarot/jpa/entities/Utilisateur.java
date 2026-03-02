package fr.tarot.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
public class Utilisateur {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (creeLe == null) creeLe = Instant.now();
    }
}
