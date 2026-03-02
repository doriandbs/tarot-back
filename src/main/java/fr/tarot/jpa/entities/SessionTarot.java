package fr.tarot.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "session_tarot")
@Getter
@Setter
@NoArgsConstructor
public class SessionTarot {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(length = 150)
    private String nom;

    @Column(name = "date_session", nullable = false)
    private LocalDate dateSession;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (creeLe == null) creeLe = Instant.now();
    }
}
