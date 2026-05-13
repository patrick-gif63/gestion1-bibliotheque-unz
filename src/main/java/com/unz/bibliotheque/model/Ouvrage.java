package com.unz.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ouvrages")
@Data
@NoArgsConstructor
public class Ouvrage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(unique = true)
    private String isbn;

    private int anneePublication;
    private int nbExemplaires;
    private int nbDisponibles;
    private String description;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToMany
    @JoinTable(
        name = "ouvrage_auteur",
        joinColumns = @JoinColumn(name = "ouvrage_id"),
        inverseJoinColumns = @JoinColumn(name = "auteur_id")
    )
    private List<Auteur> auteurs = new ArrayList<>();

    @OneToMany(mappedBy = "ouvrage", cascade = CascadeType.ALL)
    private List<Exemplaire> exemplaires = new ArrayList<>();

    @OneToMany(mappedBy = "ouvrage", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    // ── Méthodes métier ───────────────────────────────────────────────────────
    public boolean estDisponible() {
        return this.nbDisponibles > 0;
    }

    public void decrementerDisponibles() {
        if (this.nbDisponibles <= 0) {
            throw new IllegalStateException("Aucun exemplaire disponible");
        }
        this.nbDisponibles--;
    }

    public void incrementerDisponibles() {
        if (this.nbDisponibles < this.nbExemplaires) {
            this.nbDisponibles++;
        }
    }
}
