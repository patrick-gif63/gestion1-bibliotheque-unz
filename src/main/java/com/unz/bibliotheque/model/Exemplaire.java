package com.unz.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exemplaires")
@Data
@NoArgsConstructor
public class Exemplaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cote; // ex: "INF-001-A"

    @Enumerated(EnumType.STRING)
    private EtatExemplaire etat = EtatExemplaire.BON;

    private boolean disponible = true;

    @ManyToOne
    @JoinColumn(name = "ouvrage_id", nullable = false)
    private Ouvrage ouvrage;

    public enum EtatExemplaire {
        BON, ABIME, PERDU
    }
}
