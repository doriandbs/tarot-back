package fr.tarot.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "partie")
@Getter
@Setter
@NoArgsConstructor
public class Partie {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionTarot session;

    @Column(nullable = false)
    private Integer numero;

    @Column(name = "bouts_gagnants")
    private Integer boutsGagnants;

    @Column(name = "points_faits_gagnants")
    private Integer pointsFaitsGagnants;

    @Column(name = "points_a_faire")
    private Integer pointsAFaire;

    @Column(name = "points_de_base")
    private Integer pointsDeBase;

    @Column(name = "type_preneur", length = 20)
    private String typePreneur;

    @Column(name = "multiplicateur_preneur")
    private BigDecimal multiplicateurPreneur;

    @Column(name = "points_contrat")
    private BigDecimal pointsContrat;

    @Column(name = "joue_le", nullable = false)
    private Instant joueLe;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (joueLe == null) joueLe = Instant.now();
    }
}
