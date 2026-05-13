package com.unz.bibliotheque.repository;

import com.unz.bibliotheque.model.Exemplaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExemplaireRepository extends JpaRepository<Exemplaire, Long> {
    List<Exemplaire> findByOuvrageId(Long ouvrageId);
    Optional<Exemplaire> findByOuvrageIdAndDisponibleTrue(Long ouvrageId);
    Optional<Exemplaire> findByCote(String cote);
}
