package com.unz.bibliotheque.repository;

import com.unz.bibliotheque.model.Emprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    List<Emprunt> findByEtudiantId(Long etudiantId);

    List<Emprunt> findByStatut(Emprunt.StatutEmprunt statut);

    // Emprunts en retard (date dépassée et non retournés)
    @Query("SELECT e FROM Emprunt e WHERE e.dateRetourPrevue < :today AND e.statut = 'EN_COURS'")
    List<Emprunt> findEmpruntsEnRetard(LocalDate today);

    // Rappels à envoyer (date de retour dans 2 jours)
    @Query("SELECT e FROM Emprunt e WHERE e.dateRetourPrevue = :dateRappel AND e.statut = 'EN_COURS'")
    List<Emprunt> findEmpruntsARappeler(LocalDate dateRappel);

    boolean existsByEtudiantIdAndExemplaireIdAndStatut(Long etudiantId, Long exemplaireId, Emprunt.StatutEmprunt statut);
}
