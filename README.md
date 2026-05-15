# 📚 Application de Gestion de Bibliothèque Universitaire

**Université Norbert Zongo** — Génie Logiciel L3 — Semestre 5  
Enseignant : Dr OUEDRAOGO Moïse 

---

## 🚀 Lancer le projet en local

### Prérequis
- Java 17+ (`java -version`)
- Maven 3.8+ (`mvn -version`)
- MySQL 8.0+

### Étapes

```bash
# 1. Cloner le dépôt
git clone https://github.com/VOTRE_NOM/gestion-bibliotheque-unz.git
cd gestion-bibliotheque-unz

# 2. Créer la base de données MySQL
mysql -u root -p
CREATE DATABASE bibliotheque_unz CHARACTER SET utf8mb4;
EXIT;

# 3. Configurer src/main/resources/application.properties
# Mettre vos identifiants MySQL

# 4. Lancer l'application
mvn spring-boot:run

# 5. Ouvrir dans le navigateur
# http://localhost:8080
```

---

## 🏗️ Architecture

```
Controller (REST/Web) → Service (Logique métier) → Repository (JPA) → MySQL
```

**Technologies :** Java 17, Spring Boot 3.2, MySQL 8, Thymeleaf, Bootstrap 5

---

## 🎨 Design Patterns utilisés

| Pattern | Où | Pourquoi |
|---------|-----|----------|
| **Singleton** | Beans Spring (@Service) | Une instance par service |
| **Strategy** | PenaliteStrategy | Calcul de pénalités interchangeable |
| **Observer** | NotificationService + @Scheduled | Découplage des notifications |
| **Repository** | Spring Data JPA Repositories | Séparation accès données |

---

## 🧪 Tests

```bash
# Lancer les tests
mvn test

# Voir la couverture (rapport HTML)
mvn test jacoco:report
# Rapport dans : target/site/jacoco/index.html
```

---

## 👥 Équipe

| Rôle | Nom |
|------|-----|
| Product Owner | [@patrick-gif63] |
| Scrum Master | [@msebogo15-lang] |
| Développeur Backend 1 | [OUMAR] |
| Développeur Backend 2 | [ZACKARIA] |
| Développeur Frontend | [MOUSSA] |
| Testeur / Rapport | [OUEDRAGO] |

---

## 📦 Livrables

- [x] Code source Spring Boot
- [x] Tests unitaires (Jacoco)
- [ ] Rapport Final (PDF)
- [ ] Dossier de Conception (PDF)
- [ ] Vidéo de démonstration

**Soumission :** moisewedra@gmail.com
