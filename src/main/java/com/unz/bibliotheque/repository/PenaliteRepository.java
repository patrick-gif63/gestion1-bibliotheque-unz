package com.unz.bibliotheque.repository;

import com.unz.bibliotheque.model.Penalite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PenaliteRepository extends JpaRepository<Penalite, Long> {
    List<Penalite> findByEtudiantId(Long etudiantId);
    List<Penalite> findByEtudiantIdAndPayeeFalse(Long etudiantId);

    @Query("SELECT SUM(p.montant) FROM Penalite p WHERE p.etudiant.id = :etudiantId AND p.payee = false")
    Double sumMontantImpayeByEtudiantId(Long etudiantId);
}
