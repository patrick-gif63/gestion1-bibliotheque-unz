package com.unz.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur etudiant;

    @ManyToOne
    @JoinColumn(name = "ouvrage_id", nullable = false)
    private Ouvrage ouvrage;

    @Column(nullable = false)
    private LocalDateTime dateReservation;

    private LocalDate dateExpiration;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut = StatutReservation.ACTIVE;

    public enum StatutReservation {
        ACTIVE, SATISFAITE, EXPIREE, ANNULEE
    }
}
