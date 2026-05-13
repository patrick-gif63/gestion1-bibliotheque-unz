package com.unz.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auteurs")
@Data
@NoArgsConstructor
public class Auteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String biographie;

    @ManyToMany(mappedBy = "auteurs")
    private List<Ouvrage> ouvrages = new ArrayList<>();

    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }
}
