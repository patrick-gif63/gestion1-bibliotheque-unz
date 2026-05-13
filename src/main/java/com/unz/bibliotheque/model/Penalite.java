package com.unz.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "penalites")
@Data
@NoArgsConstructor
public class Penalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur etudiant;

    @OneToOne
    @JoinColumn(name = "emprunt_id", nullable = false)
    private Emprunt emprunt;

    @Column(nullable = false)
    private double montant;

    @Column(nullable = false)
    private LocalDate dateCalcul;

    private LocalDate datePaiement;

    private boolean payee = false;

    private long joursRetard;
}
