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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests complets - Couverture 65%+")
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
        exemplaire = new Exemplaire();
        exemplaire.setId(1L);
        exemplaire.setCote("INF-001-A");
        exemplaire.setDisponible(true);
        exemplaire.setEtat(Exemplaire.EtatExemplaire.BON);

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
        etudiant.setActif(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS EMPRUNT SERVICE
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Créer emprunt - ouvrage disponible - doit réussir")
    void creerEmprunt_OuvrageDisponible_RetourneEmprunt() {
        when(ouvrageRepository.findById(1L)).thenReturn(Optional.of(ouvrage));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(etudiant));
        when(penaliteRepository.sumMontantImpayeByEtudiantId(1L)).thenReturn(null);
        when(exemplaireRepository.findByOuvrageIdAndDisponibleTrue(1L)).thenReturn(Optional.of(exemplaire));
        when(empruntRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Emprunt emprunt = empruntService.creerEmprunt(1L, 1L);

        assertNotNull(emprunt);
        assertEquals(etudiant, emprunt.getEtudiant());
        assertEquals(exemplaire, emprunt.getExemplaire());
        assertEquals(LocalDate.now(), emprunt.getDateEmprunt());
        assertEquals(LocalDate.now().plusDays(14), emprunt.getDateRetourPrevue());
        assertEquals(Emprunt.StatutEmprunt.EN_COURS, emprunt.getStatut());
        assertEquals(0, ouvrage.getNbDisponibles());
        assertFalse(exemplaire.isDisponible());
        verify(notificationService, times(1)).planifierRappelRetour(any(Emprunt.class));
    }

    @Test
    @DisplayName("Créer emprunt - ouvrage indisponible - lève exception")
    void creerEmprunt_OuvrageIndisponible_LanceException() {
        ouvrage.setNbDisponibles(0);
        when(ouvrageRepository.findById(1L)).thenReturn(Optional.of(ouvrage));

        assertThrows(RuntimeException.class, () -> empruntService.creerEmprunt(1L, 1L));
        verify(empruntRepository, never()).save(any());
    }

    @Test
    @DisplayName("Créer emprunt - ouvrage non trouvé - lève exception")
    void creerEmprunt_OuvrageNonTrouve_LanceException() {
        when(ouvrageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> empruntService.creerEmprunt(99L, 1L));
    }

    @Test
    @DisplayName("Créer emprunt - étudiant bloqué - lève exception")
    void creerEmprunt_EtudiantBloque_LanceException() {
        when(ouvrageRepository.findById(1L)).thenReturn(Optional.of(ouvrage));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(etudiant));
        when(penaliteRepository.sumMontantImpayeByEtudiantId(1L)).thenReturn(600.0);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> empruntService.creerEmprunt(1L, 1L));
        assertTrue(ex.getMessage().contains("bloqué"));
    }

    @Test
    @DisplayName("Créer emprunt - pénalités sous seuil - doit réussir")
    void creerEmprunt_PenalitesSousSeuil_Reussit() {
        when(ouvrageRepository.findById(1L)).thenReturn(Optional.of(ouvrage));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(etudiant));
        when(penaliteRepository.sumMontantImpayeByEtudiantId(1L)).thenReturn(100.0);
        when(exemplaireRepository.findByOuvrageIdAndDisponibleTrue(1L)).thenReturn(Optional.of(exemplaire));
        when(empruntRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Emprunt emprunt = empruntService.creerEmprunt(1L, 1L);
        assertNotNull(emprunt);
    }

    @Test
    @DisplayName("Enregistrer retour - sans retard - statut retourné")
    void enregistrerRetour_SansRetard_StatutRetourne() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(1L);
        emprunt.setEtudiant(etudiant);
        emprunt.setExemplaire(exemplaire);
        emprunt.setDateEmprunt(LocalDate.now().minusDays(5));
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(9));
        emprunt.setStatut(Emprunt.StatutEmprunt.EN_COURS);

        when(empruntRepository.findById(1L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Emprunt retour = empruntService.enregistrerRetour(1L);

        assertEquals(Emprunt.StatutEmprunt.RETOURNE, retour.getStatut());
        assertEquals(LocalDate.now(), retour.getDateRetourEffective());
        assertTrue(exemplaire.isDisponible());
        assertEquals(2, ouvrage.getNbDisponibles());
        verify(penaliteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Enregistrer retour - emprunt non trouvé - lève exception")
    void enregistrerRetour_EmpruntNonTrouve_LanceException() {
        when(empruntRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> empruntService.enregistrerRetour(99L));
    }

    @Test
    @DisplayName("Enregistrer retour - déjà retourné - lève exception")
    void enregistrerRetour_DejaRetourne_LanceException() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(1L);
        emprunt.setStatut(Emprunt.StatutEmprunt.RETOURNE);

        when(empruntRepository.findById(1L)).thenReturn(Optional.of(emprunt));
        assertThrows(RuntimeException.class, () -> empruntService.enregistrerRetour(1L));
    }

    @Test
    @DisplayName("Get emprunts étudiant - retourne liste")
    void getEmpruntsEtudiant_RetourneListe() {
        Emprunt e1 = new Emprunt();
        Emprunt e2 = new Emprunt();
        when(empruntRepository.findByEtudiantId(1L)).thenReturn(List.of(e1, e2));

        List<Emprunt> result = empruntService.getEmpruntsEtudiant(1L);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Get tous les emprunts - retourne liste")
    void getTousLesEmprunts_RetourneListe() {
        when(empruntRepository.findAll()).thenReturn(List.of(new Emprunt(), new Emprunt()));
        assertEquals(2, empruntService.getTousLesEmprunts().size());
    }

    @Test
    @DisplayName("Get emprunts en retard - retourne liste")
    void getEmpruntsEnRetard_RetourneListe() {
        when(empruntRepository.findEmpruntsEnRetard(any())).thenReturn(List.of(new Emprunt()));
        assertEquals(1, empruntService.getEmpruntsEnRetard().size());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS MODÈLE EMPRUNT
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Emprunt - est en retard quand date dépassée")
    void emprunt_EstEnRetard_QuandDateDepassee() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateRetourPrevue(LocalDate.now().minusDays(3));
        emprunt.setStatut(Emprunt.StatutEmprunt.EN_COURS);

        assertTrue(emprunt.estEnRetard());
        assertEquals(3, emprunt.calculerJoursRetard());
        assertEquals(30.0, emprunt.calculerPenalite());
    }

    @Test
    @DisplayName("Emprunt - pas en retard quand date non dépassée")
    void emprunt_PasEnRetard_QuandDateNonDepassee() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(5));
        emprunt.setStatut(Emprunt.StatutEmprunt.EN_COURS);

        assertFalse(emprunt.estEnRetard());
        assertEquals(0, emprunt.calculerJoursRetard());
        assertEquals(0.0, emprunt.calculerPenalite());
    }

    @Test
    @DisplayName("Emprunt - retourné n'est pas en retard")
    void emprunt_Retourne_PasEnRetard() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateRetourPrevue(LocalDate.now().minusDays(2));
        emprunt.setStatut(Emprunt.StatutEmprunt.RETOURNE);

        assertFalse(emprunt.estEnRetard());
        assertEquals(0, emprunt.calculerJoursRetard());
    }

    @Test
    @DisplayName("Emprunt - constructeur et setters")
    void emprunt_ConstructeurEtSetters() {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(1L);
        emprunt.setDateEmprunt(LocalDate.now());
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(14));
        emprunt.setStatut(Emprunt.StatutEmprunt.EN_COURS);

        assertEquals(1L, emprunt.getId());
        assertEquals(LocalDate.now(), emprunt.getDateEmprunt());
        assertEquals(Emprunt.StatutEmprunt.EN_COURS, emprunt.getStatut());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS MODÈLE OUVRAGE
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Ouvrage - est disponible quand nbDisponibles > 0")
    void ouvrage_EstDisponible_QuandNbDisponiblesSuperieurAZero() {
        ouvrage.setNbDisponibles(2);
        assertTrue(ouvrage.estDisponible());
    }

    @Test
    @DisplayName("Ouvrage - non disponible quand nbDisponibles = 0")
    void ouvrage_NonDisponible_QuandNbDisponiblesZero() {
        ouvrage.setNbDisponibles(0);
        assertFalse(ouvrage.estDisponible());
    }

    @Test
    @DisplayName("Ouvrage - décrémenter réduit le compteur")
    void ouvrage_Decrementer_ReducitLeCompteur() {
        ouvrage.setNbDisponibles(2);
        ouvrage.decrementerDisponibles();
        assertEquals(1, ouvrage.getNbDisponibles());
    }

    @Test
    @DisplayName("Ouvrage - décrémenter à zéro lève exception")
    void ouvrage_Decrementer_QuandZero_LanceException() {
        ouvrage.setNbDisponibles(0);
        assertThrows(IllegalStateException.class, () -> ouvrage.decrementerDisponibles());
    }

    @Test
    @DisplayName("Ouvrage - incrémenter augmente le compteur")
    void ouvrage_Incrementer_AugmenteLeCompteur() {
        ouvrage.setNbDisponibles(1);
        ouvrage.incrementerDisponibles();
        assertEquals(2, ouvrage.getNbDisponibles());
    }

    @Test
    @DisplayName("Ouvrage - incrémenter ne dépasse pas le max")
    void ouvrage_Incrementer_NeDepassePasMax() {
        ouvrage.setNbExemplaires(2);
        ouvrage.setNbDisponibles(2);
        ouvrage.incrementerDisponibles();
        assertEquals(2, ouvrage.getNbDisponibles());
    }

    @Test
    @DisplayName("Ouvrage - getters et setters")
    void ouvrage_GettersEtSetters() {
        Ouvrage o = new Ouvrage();
        o.setId(5L);
        o.setTitre("Test");
        o.setIsbn("123-456");
        o.setAnneePublication(2023);
        o.setDescription("Description test");
        o.setNbExemplaires(3);
        o.setNbDisponibles(3);

        assertEquals(5L, o.getId());
        assertEquals("Test", o.getTitre());
        assertEquals("123-456", o.getIsbn());
        assertEquals(2023, o.getAnneePublication());
        assertTrue(o.estDisponible());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS MODÈLE UTILISATEUR
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Utilisateur - getNomComplet retourne prénom + nom")
    void utilisateur_GetNomComplet_RetournePrenomNom() {
        Utilisateur u = new Utilisateur();
        u.setPrenom("Issa");
        u.setNom("Kaboré");
        assertEquals("Issa Kaboré", u.getNomComplet());
    }

    @Test
    @DisplayName("Utilisateur - rôle par défaut est ETUDIANT")
    void utilisateur_RoleParDefaut_EstEtudiant() {
        Utilisateur u = new Utilisateur();
        assertEquals(Utilisateur.Role.ETUDIANT, u.getRole());
    }

    @Test
    @DisplayName("Utilisateur - actif par défaut")
    void utilisateur_ActifParDefaut() {
        Utilisateur u = new Utilisateur();
        assertTrue(u.isActif());
    }

    @Test
    @DisplayName("Utilisateur - setters et getters")
    void utilisateur_SettersEtGetters() {
        Utilisateur u = new Utilisateur();
        u.setId(1L);
        u.setEmail("test@unz.bf");
        u.setMotDePasse("password123");
        u.setRole(Utilisateur.Role.BIBLIOTHECAIRE);
        u.setActif(false);
        u.setTelephone("00226XXXXXXXX");

        assertEquals(1L, u.getId());
        assertEquals("test@unz.bf", u.getEmail());
        assertEquals(Utilisateur.Role.BIBLIOTHECAIRE, u.getRole());
        assertFalse(u.isActif());
    }

    @Test
    @DisplayName("Utilisateur - tous les rôles existent")
    void utilisateur_TousLesRoles_Existent() {
        assertNotNull(Utilisateur.Role.ETUDIANT);
        assertNotNull(Utilisateur.Role.BIBLIOTHECAIRE);
        assertNotNull(Utilisateur.Role.ADMINISTRATEUR);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS MODÈLE EXEMPLAIRE
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Exemplaire - disponible par défaut")
    void exemplaire_DisponibleParDefaut() {
        Exemplaire e = new Exemplaire();
        assertTrue(e.isDisponible());
    }

    @Test
    @DisplayName("Exemplaire - état BON par défaut")
    void exemplaire_EtatBonParDefaut() {
        Exemplaire e = new Exemplaire();
        assertEquals(Exemplaire.EtatExemplaire.BON, e.getEtat());
    }

    @Test
    @DisplayName("Exemplaire - setters et getters")
    void exemplaire_SettersEtGetters() {
        Exemplaire e = new Exemplaire();
        e.setId(1L);
        e.setCote("INF-002-B");
        e.setDisponible(false);
        e.setEtat(Exemplaire.EtatExemplaire.ABIME);

        assertEquals(1L, e.getId());
        assertEquals("INF-002-B", e.getCote());
        assertFalse(e.isDisponible());
        assertEquals(Exemplaire.EtatExemplaire.ABIME, e.getEtat());
    }

    @Test
    @DisplayName("Exemplaire - tous les états existent")
    void exemplaire_TousLesEtats_Existent() {
        assertNotNull(Exemplaire.EtatExemplaire.BON);
        assertNotNull(Exemplaire.EtatExemplaire.ABIME);
        assertNotNull(Exemplaire.EtatExemplaire.PERDU);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS MODÈLE PENALITE
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Pénalité - non payée par défaut")
    void penalite_NonPayeeParDefaut() {
        Penalite p = new Penalite();
        assertFalse(p.isPayee());
    }

    @Test
    @DisplayName("Pénalité - setters et getters")
    void penalite_SettersEtGetters() {
        Penalite p = new Penalite();
        p.setId(1L);
        p.setMontant(50.0);
        p.setDateCalcul(LocalDate.now());
        p.setJoursRetard(5L);
        p.setPayee(true);
        p.setDatePaiement(LocalDate.now());

        assertEquals(1L, p.getId());
        assertEquals(50.0, p.getMontant());
        assertEquals(5L, p.getJoursRetard());
        assertTrue(p.isPayee());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS MODÈLE CATEGORIE
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Catégorie - setters et getters")
    void categorie_SettersEtGetters() {
        Categorie c = new Categorie();
        c.setId(1L);
        c.setNom("Informatique");
        c.setRayon("Rayon A");
        c.setDescription("Livres d'informatique");

        assertEquals(1L, c.getId());
        assertEquals("Informatique", c.getNom());
        assertEquals("Rayon A", c.getRayon());
        assertNotNull(c.getOuvrages());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS MODÈLE AUTEUR
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Auteur - getNomComplet retourne prénom + nom")
    void auteur_GetNomComplet_RetournePrenomNom() {
        Auteur a = new Auteur();
        a.setPrenom("Donald");
        a.setNom("Knuth");
        assertEquals("Donald Knuth", a.getNomComplet());
    }

    @Test
    @DisplayName("Auteur - setters et getters")
    void auteur_SettersEtGetters() {
        Auteur a = new Auteur();
        a.setId(1L);
        a.setNom("Knuth");
        a.setPrenom("Donald");
        a.setBiographie("Auteur de The Art of Computer Programming");

        assertEquals(1L, a.getId());
        assertEquals("Knuth", a.getNom());
        assertNotNull(a.getOuvrages());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS MODÈLE RESERVATION
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Réservation - statut ACTIVE par défaut")
    void reservation_StatutActifParDefaut() {
        Reservation r = new Reservation();
        assertEquals(Reservation.StatutReservation.ACTIVE, r.getStatut());
    }

    @Test
    @DisplayName("Réservation - setters et getters")
    void reservation_SettersEtGetters() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setDateReservation(LocalDateTime.now());
        r.setDateExpiration(LocalDate.now().plusDays(2));
        r.setStatut(Reservation.StatutReservation.SATISFAITE);

        assertEquals(1L, r.getId());
        assertEquals(Reservation.StatutReservation.SATISFAITE, r.getStatut());
        assertNotNull(r.getDateReservation());
    }

    @Test
    @DisplayName("Réservation - tous les statuts existent")
    void reservation_TousLesStatuts_Existent() {
        assertNotNull(Reservation.StatutReservation.ACTIVE);
        assertNotNull(Reservation.StatutReservation.SATISFAITE);
        assertNotNull(Reservation.StatutReservation.EXPIREE);
        assertNotNull(Reservation.StatutReservation.ANNULEE);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS PATTERN STRATEGY
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("PenaliteJournaliereStrategy - 10 FCFA par jour")
    void penaliteStrategy_10FcfaParJour() {
        PenaliteStrategy strategy = new PenaliteJournaliereStrategy();
        assertEquals(10.0, strategy.calculerPenalite(1));
        assertEquals(20.0, strategy.calculerPenalite(2));
        assertEquals(30.0, strategy.calculerPenalite(3));
        assertEquals(70.0, strategy.calculerPenalite(7));
        assertEquals(140.0, strategy.calculerPenalite(14));
    }

    @Test
    @DisplayName("PenaliteJournaliereStrategy - zéro jour = zéro pénalité")
    void penaliteStrategy_ZeroJour_ZeroPenalite() {
        PenaliteStrategy strategy = new PenaliteJournaliereStrategy();
        assertEquals(0.0, strategy.calculerPenalite(0));
    }

    @Test
    @DisplayName("PenaliteJournaliereStrategy - seuil blocage 500 FCFA = 50 jours")
    void penaliteStrategy_SeuilBlocage_50Jours() {
        PenaliteStrategy strategy = new PenaliteJournaliereStrategy();
        assertEquals(500.0, strategy.calculerPenalite(50));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTS STATUTS EMPRUNT
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Emprunt - tous les statuts existent")
    void emprunt_TousLesStatuts_Existent() {
        assertNotNull(Emprunt.StatutEmprunt.EN_COURS);
        assertNotNull(Emprunt.StatutEmprunt.RETOURNE);
        assertNotNull(Emprunt.StatutEmprunt.EN_RETARD);
    }

    @Test
    @DisplayName("Emprunt - calcul pénalité 10 jours retard = 100 FCFA")
    void emprunt_Penalite_10Jours_100Fcfa() {
        Emprunt emprunt = new Emprunt();
        emprunt.setDateRetourPrevue(LocalDate.now().minusDays(10));
        emprunt.setStatut(Emprunt.StatutEmprunt.EN_COURS);

        assertTrue(emprunt.estEnRetard());
        assertEquals(10, emprunt.calculerJoursRetard());
        assertEquals(100.0, emprunt.calculerPenalite());
    }

    @Test
    @DisplayName("Ouvrage - liste auteurs initialisée vide")
    void ouvrage_ListeAuteurs_InitialiseeVide() {
        Ouvrage o = new Ouvrage();
        assertNotNull(o.getAuteurs());
        assertTrue(o.getAuteurs().isEmpty());
    }

    @Test
    @DisplayName("Ouvrage - liste exemplaires initialisée vide")
    void ouvrage_ListeExemplaires_InitialiseeVide() {
        Ouvrage o = new Ouvrage();
        assertNotNull(o.getExemplaires());
        assertTrue(o.getExemplaires().isEmpty());
    }

    @Test
    @DisplayName("Utilisateur - liste emprunts initialisée vide")
    void utilisateur_ListeEmprunts_InitialiseeVide() {
        Utilisateur u = new Utilisateur();
        assertNotNull(u.getEmprunts());
        assertTrue(u.getEmprunts().isEmpty());
    }

    @Test
    @DisplayName("Utilisateur - liste réservations initialisée vide")
    void utilisateur_ListeReservations_InitialiseeVide() {
        Utilisateur u = new Utilisateur();
        assertNotNull(u.getReservations());
        assertTrue(u.getReservations().isEmpty());
    }
}