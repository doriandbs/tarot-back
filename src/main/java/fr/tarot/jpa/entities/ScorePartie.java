package fr.tarot.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "score_partie")
@Getter
@Setter
@NoArgsConstructor
public class ScorePartie {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partie_id", nullable = false)
    private Partie partie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_participant_id", nullable = false)
    private SessionParticipant sessionParticipant;

    @Column(nullable = false)
    private Boolean gagnant;

    @Column(nullable = false)
    private BigDecimal points;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }
}
