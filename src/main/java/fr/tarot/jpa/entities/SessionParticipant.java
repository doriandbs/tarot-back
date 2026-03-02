package fr.tarot.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_participant")
@Getter
@Setter
@NoArgsConstructor
public class SessionParticipant {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionTarot session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur joueur;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (creeLe == null) creeLe = Instant.now();
    }
}
