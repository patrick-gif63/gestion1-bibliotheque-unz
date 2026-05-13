package com.unz.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ETUDIANT;

    private boolean actif = true;

    private String telephone;

    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL)
    private List<Emprunt> emprunts = new ArrayList<>();

    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    public enum Role {
        ETUDIANT, BIBLIOTHECAIRE, ADMINISTRATEUR
    }

    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }
}
