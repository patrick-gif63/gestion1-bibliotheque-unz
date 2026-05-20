# Sprint 3 - Sprint Planning

**Date :** 21 Mai 2026
**Durée :** 21 Mai - 04 Juin 2026
**Équipe :** 6 membres

---

## Objectif du Sprint

Implémenter le système de notifications automatiques, les statistiques
d'utilisation et la génération de rapports pour la bibliothèque universitaire.

---

## User Stories sélectionnées

| ID   | User Story                                                                 | Points |
|------|----------------------------------------------------------------------------|--------|
| US16 | En tant qu'étudiant, je veux recevoir une notification par email           |        |
|      | avant la date limite de retour, afin d'éviter les pénalités.              | 5      |
| US17 | En tant qu'étudiant, je veux être notifié quand un livre réservé           |        |
|      | est disponible, afin de venir le récupérer rapidement.                    | 3      |
| US18 | En tant que bibliothécaire, je veux consulter les statistiques             |        |
|      | d'emprunts par période, afin de suivre l'activité de la bibliothèque.     | 5      |
| US19 | En tant qu'administrateur, je veux générer un rapport mensuel             |        |
|      | des emprunts par filière, afin d'optimiser les acquisitions.              | 8      |
| US20 | En tant qu'administrateur, je veux exporter les rapports en PDF           |        |
|      | et Excel, afin de les archiver et les partager officiellement.            | 5      |
| US21 | En tant que bibliothécaire, je veux voir la liste des étudiants           |        |
|      | avec des pénalités en cours, afin de les relancer facilement.             | 5      |
| US22 | En tant qu'administrateur, je veux consulter le tableau de bord           |        |
|      | des livres les plus empruntés, afin d'identifier les ouvrages populaires. | 5      |

**Total : 36 points**

---

## Critères d'acceptation par User Story

### US16 — Notification de rappel de retour
- [ ] Une notification email est envoyée 3 jours avant la date limite
- [ ] La notification contient le titre du livre et la date limite
- [ ] Si l'étudiant n'a pas d'email, une alerte s'affiche dans son tableau de bord
- [ ] Aucune notification n'est envoyée si le livre est déjà retourné

### US17 — Notification de disponibilité de réservation
- [ ] L'étudiant reçoit un email dès que le livre réservé est rendu
- [ ] La notification contient le titre du livre et un délai de 48h pour venir le récupérer
- [ ] Si l'étudiant ne vient pas dans les 48h, la réservation est annulée automatiquement

### US18 — Statistiques d'emprunts
- [ ] Le bibliothécaire peut filtrer les statistiques par semaine, mois ou année
- [ ] Un graphique affiche l'évolution des emprunts sur la période sélectionnée
- [ ] Les statistiques incluent : nombre d'emprunts, retours, réservations et pénalités

### US19 — Rapport mensuel par filière
- [ ] L'administrateur sélectionne un mois et une année pour générer le rapport
- [ ] Le rapport affiche le nombre d'emprunts par filière
- [ ] Le rapport affiche les 10 ouvrages les plus empruntés par filière

### US20 — Export des rapports
- [ ] L'administrateur peut exporter tout rapport en PDF d'un seul clic
- [ ] L'administrateur peut exporter tout rapport en Excel (.xlsx)
- [ ] Le fichier exporté est nommé automatiquement (ex: rapport_mai_2026.pdf)

### US21 — Liste des étudiants avec pénalités
- [ ] La liste affiche le nom, prénom, montant dû et nombre de jours de retard
- [ ] Le bibliothécaire peut trier par montant croissant/décroissant
- [ ] Un bouton "Relancer" envoie un email de rappel à l'étudiant concerné

### US22 — Tableau de bord des livres populaires
- [ ] Le tableau affiche le Top 10 des livres les plus empruntés
- [ ] Il est possible de filtrer par période (mois, semestre, année)
- [ ] Chaque livre affiche son nombre total d'emprunts et son taux de disponibilité

---

## Répartition des tâches

| Membre     | User Stories assignées       |
|------------|------------------------------|
| Membre 1   | US16 — Notifications retour  |
| Membre 2   | US17 — Notifications réservation |
| Membre 3   | US18 — Statistiques          |
| Membre 4   | US19 — Rapport mensuel       |
| Membre 5   | US20 — Export PDF/Excel      |
| Membre 6   | US21 + US22 — Pénalités & Dashboard |

---

## Estimation — Planning Poker

| ID   | Estimation | Justification                                      |
|------|------------|----------------------------------------------------|
| US16 | 5 points   | Intégration service email + logique de délai       |
| US17 | 3 points   | Réutilise le service email de US16                 |
| US18 | 5 points   | Requêtes complexes + affichage graphique           |
| US19 | 8 points   | Agrégation de données multi-critères               |
| US20 | 5 points   | Génération PDF + Excel avec mise en forme          |
| US21 | 5 points   | Requête filtrée + intégration email relance        |
| US22 | 5 points   | Dashboard avec filtres dynamiques                  |

**Vélocité de référence (Sprint 2) : 36 points**
**Objectif Sprint 3 : 36 points** ✅

---

## Scrum Board initial

| Backlog | To Do | In Progress | Done |
|---------|-------|-------------|------|
| —       | US16  | —           | —    |
| —       | US17  | —           | —    |
| —       | US18  | —           | —    |
| —       | US19  | —           | —    |
| —       | US20  | —           | —    |
| —       | US21  | —           | —    |
| —       | US22  | —           | —    |

---

## Definition of Done (rappel)

- [ ] Code implémenté et fonctionnel
- [ ] Tests unitaires écrits **avant** le code (TDD — action Sprint 3)
- [ ] Pull Request créée et validée par un membre de l'équipe
- [ ] JavaDoc complétée sur tous les services
- [ ] Issue GitHub fermée et liée au Milestone Sprint 3
- [ ] Démo réalisée lors de la Sprint Review
