package com.unz.bibliotheque.service;

import com.unz.bibliotheque.model.*;
import com.unz.bibliotheque.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * Service de gestion des emprunts.
 * Applique les règles métier : disponibilité, durée, pénalités.
 */
@Service
@Transactional
public class EmpruntService {

    private static final int DUREE_EMPRUNT_JOURS = 14;
    private static final double SEUIL_BLOCAGE_FCFA = 500.0;

    @Autowired private EmpruntRepository empruntRepository;
    @Autowired private OuvrageRepository ouvrageRepository;
    @Autowired private ExemplaireRepository exemplaireRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private PenaliteRepository penaliteRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private PenaliteStrategy penaliteStrategy; // PATTERN STRATEGY

    /**
     * Crée un emprunt pour un étudiant.
     * Vérifie la disponibilité et que l'étudiant n'est pas bloqué.
     */
    public Emprunt creerEmprunt(Long ouvrageId, Long etudiantId) {
        Ouvrage ouvrage = ouvrageRepository.findById(ouvrageId)
            .orElseThrow(() -> new RuntimeException("Ouvrage non trouvé : " + ouvrageId));

        if (!ouvrage.estDisponible()) {
            throw new RuntimeException("Aucun exemplaire disponible pour : " + ouvrage.getTitre());
        }

        Utilisateur etudiant = utilisateurRepository.findById(etudiantId)
            .orElseThrow(() -> new RuntimeException("Étudiant non trouvé : " + etudiantId));

        // Vérifier si l'étudiant est bloqué (pénalités impayées > seuil)
        Double totalPenalites = penaliteRepository.sumMontantImpayeByEtudiantId(etudiantId);
        if (totalPenalites != null && totalPenalites >= SEUIL_BLOCAGE_FCFA) {
            throw new RuntimeException("Compte bloqué : pénalités impayées = " + totalPenalites + " FCFA");
        }

        // Trouver un exemplaire disponible
        Exemplaire exemplaire = exemplaireRepository.findByOuvrageIdAndDisponibleTrue(ouvrageId)
            .orElseThrow(() -> new RuntimeException("Aucun exemplaire disponible"));

        // Créer l'emprunt
        Emprunt emprunt = new Emprunt();
        emprunt.setEtudiant(etudiant);
        emprunt.setExemplaire(exemplaire);
        emprunt.setDateEmprunt(LocalDate.now());
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(DUREE_EMPRUNT_JOURS));
        emprunt.setStatut(Emprunt.StatutEmprunt.EN_COURS);

        // Mettre à jour la disponibilité
        ouvrage.decrementerDisponibles();
        exemplaire.setDisponible(false);

        ouvrageRepository.save(ouvrage);
        exemplaireRepository.save(exemplaire);
        Emprunt empruntSauvegarde = empruntRepository.save(emprunt);

        // PATTERN OBSERVER : publier l'événement pour planifier le rappel
        notificationService.planifierRappelRetour(empruntSauvegarde);

        return empruntSauvegarde;
    }

    /**
     * Enregistre le retour d'un ouvrage et calcule la pénalité si retard.
     */
    public Emprunt enregistrerRetour(Long empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
            .orElseThrow(() -> new RuntimeException("Emprunt non trouvé : " + empruntId));

        if (emprunt.getStatut() == Emprunt.StatutEmprunt.RETOURNE) {
            throw new RuntimeException("Cet ouvrage a déjà été retourné");
        }

        // Créer une pénalité si retard (PATTERN STRATEGY pour le calcul)
        if (emprunt.estEnRetard()) {
            long joursRetard = emprunt.calculerJoursRetard();
            double montant = penaliteStrategy.calculerPenalite(joursRetard);

            Penalite penalite = new Penalite();
            penalite.setEtudiant(emprunt.getEtudiant());
            penalite.setEmprunt(emprunt);
            penalite.setMontant(montant);
            penalite.setJoursRetard(joursRetard);
            penalite.setDateCalcul(LocalDate.now());
            penaliteRepository.save(penalite);
        }

        emprunt.setDateRetourEffective(LocalDate.now());
        emprunt.setStatut(Emprunt.StatutEmprunt.RETOURNE);

        // Remettre l'exemplaire en disponible
        Exemplaire exemplaire = emprunt.getExemplaire();
        exemplaire.setDisponible(true);
        Ouvrage ouvrage = exemplaire.getOuvrage();
        ouvrage.incrementerDisponibles();

        exemplaireRepository.save(exemplaire);
        ouvrageRepository.save(ouvrage);

        Emprunt retour = empruntRepository.save(emprunt);

        // Notifier le premier étudiant en liste d'attente si réservation existe
        notificationService.notifierProchainReservataire(ouvrage.getId());

        return retour;
    }

    public List<Emprunt> getEmpruntsEtudiant(Long etudiantId) {
        return empruntRepository.findByEtudiantId(etudiantId);
    }

    public List<Emprunt> getTousLesEmprunts() {
        return empruntRepository.findAll();
    }

    public List<Emprunt> getEmpruntsEnRetard() {
        return empruntRepository.findEmpruntsEnRetard(LocalDate.now());
    }
}
