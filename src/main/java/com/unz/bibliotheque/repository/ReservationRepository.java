package com.unz.bibliotheque.repository;

import com.unz.bibliotheque.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByEtudiantId(Long etudiantId);
    List<Reservation> findByOuvrageIdAndStatut(Long ouvrageId, Reservation.StatutReservation statut);
    boolean existsByEtudiantIdAndOuvrageIdAndStatut(Long etudiantId, Long ouvrageId, Reservation.StatutReservation statut);
}
