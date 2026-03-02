package fr.tarot.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "joueur")
@Getter
@Setter
@NoArgsConstructor
public class Joueur {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (creeLe == null) creeLe = Instant.now();
    }
}
