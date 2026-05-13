package com.unz.bibliotheque.service;

import com.unz.bibliotheque.model.Emprunt;
import com.unz.bibliotheque.repository.ReservationRepository;
import com.unz.bibliotheque.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.unz.bibliotheque.repository.EmpruntRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * DESIGN PATTERN : OBSERVER
 * Ce service "observe" les événements du système (emprunts, retours)
 * et envoie des notifications en réaction.
 *
 * Il est aussi utilisé comme Scheduler (tâche planifiée quotidienne).
 */
@Service
public class NotificationService {

    @Autowired private JavaMailSender mailSender;
    @Autowired private EmpruntRepository empruntRepository;
    @Autowired private ReservationRepository reservationRepository;

    /**
     * Appelé après la création d'un emprunt.
     * Envoie un email de confirmation avec la date de retour.
     */
    public void planifierRappelRetour(Emprunt emprunt) {
        String to = emprunt.getEtudiant().getEmail();
        String sujet = "[Bibliothèque UNZ] Confirmation d'emprunt";
        String texte = String.format(
            "Bonjour %s,\n\n" +
            "Vous avez emprunté : %s\n" +
            "Date d'emprunt : %s\n" +
            "Date de retour prévue : %s\n\n" +
            "Merci de ramener l'ouvrage avant la date prévue pour éviter les pénalités (10 FCFA/jour).\n\n" +
            "Bibliothèque Université Norbert Zongo",
            emprunt.getEtudiant().getNomComplet(),
            emprunt.getExemplaire().getOuvrage().getTitre(),
            emprunt.getDateEmprunt(),
            emprunt.getDateRetourPrevue()
        );
        envoyerEmail(to, sujet, texte);
    }

    /**
     * Tâche planifiée : tous les jours à 8h00
     * Envoie des rappels pour les retours prévus dans 2 jours.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void envoyerRappelsQuotidiens() {
        LocalDate dateRappel = LocalDate.now().plusDays(2);
        List<Emprunt> empruntsARappeler = empruntRepository.findEmpruntsARappeler(dateRappel);

        for (Emprunt emprunt : empruntsARappeler) {
            String to = emprunt.getEtudiant().getEmail();
            String sujet = "[Bibliothèque UNZ] Rappel - Retour dans 2 jours";
            String texte = String.format(
                "Bonjour %s,\n\n" +
                "Rappel : vous devez retourner \"%s\" dans 2 jours.\n" +
                "Date limite : %s\n\n" +
                "Passé ce délai, une pénalité de 10 FCFA/jour sera appliquée.\n\n" +
                "Bibliothèque UNZ",
                emprunt.getEtudiant().getNomComplet(),
                emprunt.getExemplaire().getOuvrage().getTitre(),
                emprunt.getDateRetourPrevue()
            );
            envoyerEmail(to, sujet, texte);
        }
    }

    /**
     * Notifie le premier étudiant en réservation quand un ouvrage est rendu.
     */
    public void notifierProchainReservataire(Long ouvrageId) {
        List<Reservation> reservations = reservationRepository
            .findByOuvrageIdAndStatut(ouvrageId, Reservation.StatutReservation.ACTIVE);

        if (!reservations.isEmpty()) {
            Reservation premiere = reservations.get(0);
            String to = premiere.getEtudiant().getEmail();
            String sujet = "[Bibliothèque UNZ] Ouvrage disponible !";
            String texte = String.format(
                "Bonjour %s,\n\n" +
                "Bonne nouvelle ! L'ouvrage \"%s\" que vous avez réservé est maintenant disponible.\n" +
                "Vous avez 48h pour venir le récupérer à la bibliothèque.\n\n" +
                "Bibliothèque UNZ",
                premiere.getEtudiant().getNomComplet(),
                premiere.getOuvrage().getTitre()
            );
            envoyerEmail(to, sujet, texte);
        }
    }

    private void envoyerEmail(String to, String sujet, String texte) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(sujet);
            message.setText(texte);
            mailSender.send(message);
        } catch (Exception e) {
            // En développement, on logue sans crasher l'application
            System.err.println("Erreur envoi email à " + to + " : " + e.getMessage());
        }
    }
}
