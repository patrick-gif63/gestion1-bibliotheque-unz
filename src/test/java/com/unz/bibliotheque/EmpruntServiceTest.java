package com.unz.bibliotheque;

import com.unz.bibliotheque.model.*;
import com.unz.bibliotheque.repository.*;
import com.unz.bibliotheque.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service d'emprunt")
class EmpruntServiceTest {

    @Mock private EmpruntRepository empruntRepository;
    @Mock private OuvrageRepository ouvrageRepository;
    @Mock private ExemplaireRepository exemplaireRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private PenaliteRepository penaliteRepository;
    @Mock private NotificationService notificationService;
    @Mock private PenaliteStrategy penaliteStrategy;

    @InjectMocks
    private EmpruntService empruntService;

    private Ouvrage ouvrage;
    private Utilisateur etudiant;
    private Exemplaire exemplaire;

    @BeforeEach
    void setUp() {
        // Préparer les données de test
        exemplaire = new Exemplaire();
        exemplaire.setId(1L);
        exemplaire.setCote("INF-001-A");
        exemplaire.setDisponible(true);

        ouvrage = new Ouvrage();
        ouvrage.setId(1L);
        ouvrage.setTitre("Algorithmes et Programmation");
        ouvrage.setIsbn("978-0-123456-78-9");
        ouvrage.setNbExemplaires(2);
        ouvrage.setNbDisponibles(1);
        ouvrage.setExemplaires(List.of(exemplaire));
        exemplaire.setOuvrage(ouvrage);

        etudiant = new Utilisateur();
        etudiant.setId(1L);
        etudiant.setNom("Kaboré");
        etudiant.setPrenom("Issa");
        etudiant.setEmail("issa.kabore@etudiant.unz.bf");
        etudiant.setRole(Utilisateur.Role.ETUDIANT);
    }

    // ── Tests positifs ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Créer un emprunt avec ouvrage disponible doit réussir")
    void creerEmprunt_OuvrageDisponible_RetourneEmprunt() {
        // ARRANGE
        when(ouvrageRepository.findById(1L)).thenReturn(Optional.of(ouvrage));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(etudiant));
        when(penaliteRepository.sumMontantImpayeByEtudiantId(1L)).thenReturn(null);
        when(exemplaireRepository.findByOuvrageIdAndDisponibleTrue(1L)).thenReturn(Optional.of(exemplaire));
        when(empruntRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // ACT
        Emprunt emprunt = empruntService.creerEmprunt(1L, 1L);

        // ASSERT
        assertNotNull(emprunt);
        assertEquals(etudiant, emprunt.getEtudiant());
        assertEquals(exemplaire, emprunt.getExemplaire());
        assertEquals(LocalDate.now(), emprunt.getDateEmprunt());
        assertEquals(LocalDate.now().plusDays(14), emprunt.getDateRetourPrevue());
        assertEquals(Emprunt.StatutEmprunt.EN_COURS, emprunt.getStatut());

        // Vérifier que la disponibilité est mise à jour
        assertEquals(0, ouvrage.getNbDisponibles());
        assertFalse(exemplaire.isDisponible());

        // Vérifier que la notification est envoyée
        verify(notificationService, times(1)).planifierRappelRetour(any(Emprunt.class));
    }

    @Test
    @DisplayName("Créer un emprunt avec ouvrage indisponible doit lever une exception")
    void creerEmprunt_OuvrageIndisponible_LanceException() {
        // ARRANGE
        ouvrage.setNbDisponibles(0);
        when(ouvrageRepository.findById(1L)).thenReturn(Optional.of(ouvrage));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            empruntService.creerEmprunt(1L, 1L);
        });
        assertTrue(exception.getMessage().contains("disponible"));
        verify(empruntRepository, never()).save(any());
    }

    @Test
    @DisplayName("Créer un emprunt avec étudiant bloqué doit lever une exception")
    void creerEmprunt_EtudiantBloque_LanceException() {
        // ARRANGE
        when(ouvrageRepository.findById(1L)).thenReturn(Optional.of(ouvrage));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(etudiant));
        when(penaliteRepository.sumMontantImpayeByEtudiantId(1L)).thenReturn(600.0); // > 500 FCFA

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            empruntService.creerEmprunt(1L, 1L);
        });
        assertTrue(exception.getMessage().contains("bloqué"));
    }

    @Test
    @DisplayName("Enregistrer retour sans retard doit mettre à jour le statut")
    void enregistrerRetour_SansRetard_StatutRetourne() {
        // ARRANGE
        Emprunt emprunt = new Emprunt();
        emprunt.setId(1L);
        emprunt.setEtudiant(etudiant);
        emprunt.setExemplaire(exemplaire);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(5));
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(9)); // pas encore en retard
        emprunt.setStatut(Emprunt.StatutEmprunt.EN_COURS);

        when(empruntRepository.findById(1L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // ACT
        Emprunt retour = empruntService.enregistrerRetour(1L);

        // ASSERT
        assertEquals(Emprunt.StatutEmprunt.RETOURNE, retour.getStatut());
        assertEquals(LocalDate.now(), retour.getDateRetourEffective());
        assertTrue(exemplaire.isDisponible());
        assertEquals(1, ouvrage.getNbDisponibles());
        verify(penaliteRepository, never()).save(any()); // pas de pénalité
    }

    // ── Tests du modèle Emprunt ───────────────────────────────────────────────

    @Test
    @DisplayName("Un emprunt est en retard quand la date est dépassée")
    void emprunt_EstEnRetard_QuandDateDepassee() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateRetourPrevue(LocalDate.now().minusDays(3));
        emprunt.setStatut(Emprunt.StatutEmprunt.EN_COURS);

        assertTrue(emprunt.estEnRetard());
        assertEquals(3, emprunt.calculerJoursRetard());
        assertEquals(30.0, emprunt.calculerPenalite()); // 3 × 10 FCFA = 30 FCFA
    }

    @Test
    @DisplayName("Un emprunt non dépassé n'est pas en retard")
    void emprunt_PasEnRetard_QuandDateNonDepassee() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(5));
        emprunt.setStatut(Emprunt.StatutEmprunt.EN_COURS);

        assertFalse(emprunt.estEnRetard());
        assertEquals(0, emprunt.calculerJoursRetard());
        assertEquals(0.0, emprunt.calculerPenalite());
    }

    @Test
    @DisplayName("Un ouvrage avec exemplaires disponibles est disponible")
    void ouvrage_EstDisponible_QuandNbDisponiblesSuperieurAZero() {
        ouvrage.setNbDisponibles(2);
        assertTrue(ouvrage.estDisponible());
    }

    @Test
    @DisplayName("Décrémenter les disponibles met à jour le compteur")
    void ouvrage_Decrementer_ReducitLeCompteur() {
        ouvrage.setNbDisponibles(2);
        ouvrage.decrementerDisponibles();
        assertEquals(1, ouvrage.getNbDisponibles());
    }

    @Test
    @DisplayName("Décrémenter à zéro doit lever une exception")
    void ouvrage_Decrementer_QuandZero_LanceException() {
        ouvrage.setNbDisponibles(0);
        assertThrows(IllegalStateException.class, () -> ouvrage.decrementerDisponibles());
    }

    // ── Test du Pattern Strategy ──────────────────────────────────────────────

    @Test
    @DisplayName("PenaliteJournaliereStrategy calcule correctement 10 FCFA/jour")
    void penaliteStrategy_CalculCorrect() {
        PenaliteStrategy strategy = new PenaliteJournaliereStrategy();
        assertEquals(10.0, strategy.calculerPenalite(1));
        assertEquals(30.0, strategy.calculerPenalite(3));
        assertEquals(70.0, strategy.calculerPenalite(7));
        assertEquals(0.0, strategy.calculerPenalite(0));
    }
}
