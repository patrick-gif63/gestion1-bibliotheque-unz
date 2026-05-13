package com.unz.bibliotheque.repository;

import com.unz.bibliotheque.model.Ouvrage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OuvrageRepository extends JpaRepository<Ouvrage, Long> {

    Optional<Ouvrage> findByIsbn(String isbn);

    // Recherche avancée (titre, auteur, catégorie, mots-clés)
    @Query("SELECT DISTINCT o FROM Ouvrage o " +
           "LEFT JOIN o.auteurs a " +
           "LEFT JOIN o.categorie c " +
           "WHERE LOWER(o.titre) LIKE LOWER(CONCAT('%', :mot, '%')) " +
           "OR LOWER(o.isbn) LIKE LOWER(CONCAT('%', :mot, '%')) " +
           "OR LOWER(a.nom) LIKE LOWER(CONCAT('%', :mot, '%')) " +
           "OR LOWER(a.prenom) LIKE LOWER(CONCAT('%', :mot, '%')) " +
           "OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :mot, '%'))")
    Page<Ouvrage> rechercherParMotCle(@Param("mot") String mot, Pageable pageable);

    List<Ouvrage> findByCategorieId(Long categorieId);

    // Les 10 ouvrages les plus empruntés
    @Query("SELECT o FROM Ouvrage o " +
           "JOIN o.exemplaires e " +
           "JOIN Emprunt emp ON emp.exemplaire = e " +
           "GROUP BY o " +
           "ORDER BY COUNT(emp) DESC")
    List<Ouvrage> findTopEmpruntes(Pageable pageable);
}
