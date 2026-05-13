package com.unz.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "emprunts")
@Data
@NoArgsConstructor
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur etudiant;

    @ManyToOne
    @JoinColumn(name = "exemplaire_id", nullable = false)
    private Exemplaire exemplaire;

    @Column(nullable = false)
    private LocalDate dateEmprunt;

    @Column(nullable = false)
    private LocalDate dateRetourPrevue;

    private LocalDate dateRetourEffective;

    @Enumerated(EnumType.STRING)
    private StatutEmprunt statut = StatutEmprunt.EN_COURS;

    // ── Méthodes métier ───────────────────────────────────────────────────────
    public boolean estEnRetard() {
        return LocalDate.now().isAfter(this.dateRetourPrevue)
            && this.statut == StatutEmprunt.EN_COURS;
    }

    public long calculerJoursRetard() {
        if (!estEnRetard()) return 0;
        return ChronoUnit.DAYS.between(this.dateRetourPrevue, LocalDate.now());
    }

    public double calculerPenalite() {
        // 10 FCFA par jour de retard
        return calculerJoursRetard() * 10.0;
    }

    public enum StatutEmprunt {
        EN_COURS, RETOURNE, EN_RETARD
    }
}
