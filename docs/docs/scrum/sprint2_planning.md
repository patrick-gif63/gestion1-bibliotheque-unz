# Sprint 2 - Sprint Planning

**Date :** 16 Mai 2026
**Durée :** 16 Mai - 20 Mai 2026
**Équipe :** 6 membres

## Objectif du Sprint
Implémenter la gestion des emprunts, retours, 
réservations et pénalités automatiques.

## User Stories sélectionnées

| ID | User Story | Points |
|----|-----------|--------|
| US05 | Emprunter un ouvrage | 5 |
| US06 | Réserver un ouvrage | 5 |
| US07 | Historique des emprunts | 5 |
| US13 | Enregistrer un emprunt | 8 |
| US14 | Enregistrer un retour | 5 |
| US15 | Pénalité automatique | 8 |

**Total points Sprint 2 : 36 points**

## Décomposition en tâches

### US05 - Emprunter
- [ ] Bouton emprunter sur page détail ouvrage
- [ ] Vérification disponibilité avant emprunt
- [ ] Mise à jour nbDisponibles après emprunt
- [ ] Test unitaire EmpruntService

### US13 - Enregistrer emprunt
- [ ] Formulaire bibliothécaire
- [ ] Sélection étudiant et ouvrage
- [ ] Génération reçu emprunt
- [ ] Test intégration

### US15 - Pénalité automatique
- [ ] Calcul 10 FCFA par jour de retard
- [ ] Blocage compte si > 500 FCFA
- [ ] Affichage pénalités sur profil étudiant
- [ ] Test PenaliteStrategy
