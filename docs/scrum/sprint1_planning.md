# Sprint 1 - Sprint Planning

**Date :** 10 Mai 2026
**Durée :** 1 semaine (10 Mai - 17 Mai 2026)
**Équipe :** 6 membres

## Objectif du Sprint
Mettre en place la base du système : authentification, 
gestion du catalogue et recherche d'ouvrages.

## User Stories sélectionnées

| ID | User Story | Points |
|----|-----------|--------|
| US01 | S'inscrire avec email | 5 |
| US02 | Se connecter | 3 |
| US03 | Rechercher un ouvrage | 8 |
| US04 | Voir détail ouvrage | 3 |
| US10 | Ajouter un ouvrage | 8 |
| US11 | Modifier un ouvrage | 3 |
| US12 | Supprimer un ouvrage | 2 |

**Total points Sprint 1 : 32 points**

## Décomposition en tâches

### US01 - Inscription
- [ ] Créer le formulaire HTML d'inscription
- [ ] Créer UtilisateurService.inscrire()
- [ ] Valider l'unicité de l'email
- [ ] Tester l'inscription

### US02 - Connexion
- [ ] Configurer Spring Security
- [ ] Créer la page login.html
- [ ] Gérer les erreurs de connexion
- [ ] Tester la connexion

### US10 - Ajouter ouvrage
- [ ] Créer le formulaire ajout ouvrage
- [ ] Créer OuvrageService.ajouterOuvrage()
- [ ] Valider l'ISBN unique
- [ ] Tester l'ajout
