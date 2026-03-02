package fr.tarot.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "bonus_partie")
@Getter
@Setter
@NoArgsConstructor
public class BonusPartie {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partie_id", nullable = false)
    private Partie partie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_participant_id", nullable = false)
    private SessionParticipant sessionParticipant;

    @Column(name = "type_bonus", nullable = false, length = 30)
    private String typeBonus;

    @Column(nullable = false)
    private Integer points;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (points == null) points = 40;
    }
}
