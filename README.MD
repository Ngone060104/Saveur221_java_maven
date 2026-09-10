# Saveur 221 — Module A : Application Java Console

Application console destinée au **personnel interne** du restaurant (GERANT, ADMIN), développée dans le cadre du projet de gestion du restaurant Saveur 221. Elle partage la même base de données PostgreSQL que le Module B (application PHP Web), afin que les deux applications respectent exactement les mêmes règles métier.

---

## 1. Technologies utilisées

| Technologie | Version | Rôle |
|---|---|---|
| **Java (JDK)** | 21 (développement/exécution) — compilé en ciblant **Java 17** | Langage de programmation |
| **Maven** | 3.8.7+ | Gestion des dépendances et du build |
| **PostgreSQL** | 16 (compatible 13+) | Base de données relationnelle |
| **JDBC PostgreSQL** (`org.postgresql:postgresql`) | 42.7.7 | Pilote de connexion Java ↔ PostgreSQL |
| **jBCrypt** (`org.mindrot:jbcrypt`) | 0.4 | Hachage sécurisé des mots de passe |
| **maven-compiler-plugin** | 3.14.0 | Compilation du code source |
| **exec-maven-plugin** | 3.5.0 | Exécution de l'application via Maven |
| **Git / GitHub** | — | Gestion de versions, workflow de branches |

### Pourquoi ces choix

- **PostgreSQL** plutôt que MySQL : types `ENUM` natifs (`statut_commande_enum`, `statut_produit_enum`) et triggers `plpgsql` permettant de centraliser les règles métier côté base, partagées entre Java et PHP.
- **jBCrypt** : aucun mot de passe n'est stocké en clair. Chaque mot de passe est haché (`BCrypt.hashpw`) avant insertion, et vérifié via `BCrypt.checkpw` à la connexion.
- **exec-maven-plugin** : permet de lancer l'application directement avec `mvn compile exec:java`, sans avoir à construire un jar à chaque test pendant le développement.

---

## 2. Prérequis

- JDK 17 ou supérieur installé (`java -version`)
- Maven 3.8+ installé (`mvn -version`)
- PostgreSQL 13+ installé et démarré
- La base `saveur221` créée à partir du script `database.sql` fourni à la racine du projet

---

## 3. Installation et configuration

### 3.1 Créer la base de données

```bash
psql -U postgres -f database.sql
```

Ce script crée : les tables, les types `ENUM`, les triggers de règles métier, la vue partagée `vue_statut_paiement`, ainsi que des données de test (rôles, catégories, produits, comptes personnel interne).

### 3.2 Configurer la connexion à la base

```bash
cd src/main/resources
cp config.properties.example config.properties
```

Puis éditer `config.properties` avec les identifiants réels de votre PostgreSQL local :

```properties
db.url=jdbc:postgresql://localhost:5432/saveur221
db.user=postgres
db.password=votre_mot_de_passe
```

`config.properties` est ignoré par Git (voir `.gitignore`) — seul `config.properties.example` (avec des valeurs génériques) est versionné, pour ne jamais exposer d'identifiants réels.

Alternative : définir les variables d'environnement `SAVEUR221_DB_URL`, `SAVEUR221_DB_USER`, `SAVEUR221_DB_PASSWORD` (prioritaires sur le fichier de configuration).

---

## 4. Lancer l'application

### Option A — via Maven (recommandé en développement)

```bash
mvn compile exec:java
```

### Option B — en générant un jar exécutable

```bash
mvn clean package
java -jar target/saveur221-java.jar
```

---

## 5. Comptes de test

| Rôle | Email | Mot de passe |
|---|---|---|
| **ADMIN** | `a.diallo@saveur221.sn` | `motdepasse1` |
| **GERANT** | `kone@gmail.com` | `ModiboKone` |

---

## 6. Architecture du projet

```
src/main/java/com/saveur221/
├── Main.java              → point d'entrée, orchestre les vues et les services
├── entities/               → classes du domaine (Role, Utilisateur, Client, Categorie,
│                              Produit, Commande, LigneCommande, Paiement, Avis)
├── enums/                   → StatutCommande, StatutProduit
├── repository/               → accès JDBC à PostgreSQL (une classe par entité)
├── service/                  → logique métier et règles de validation
├── view/                      → interface console (menus, saisie, affichage)
├── config/                    → connexion base de données (DatabaseConfig), session (Session)
└── exceptions/                 → hiérarchie d'exceptions métier (MetierException et ses sous-classes)
```

### Principe d'architecture : séparation stricte View / Service

Conformément aux consignes du coach pédagogique, **la vue n'appelle jamais directement le service**, et le service ne connaît jamais la vue. C'est `Main.java` qui joue le rôle de chef d'orchestre :

```
View   →  affiche un menu, lit une saisie, construit un objet simple
Service →  applique les règles métier, appelle le repository
Main    →  LE SEUL à connaître à la fois la View ET le Service — il les connecte
```

Cette séparation permet de faire évoluer l'affichage sans toucher à la logique métier (et inversement), et facilite l'écriture de tests unitaires sur les services sans dépendre de la console.

### Héritage Client → Utilisateur

Conformément au diagramme de classes, `Client` hérite de `Utilisateur` (généralisation UML), traduite en base par une stratégie de *Class Table Inheritance* : la table `clients` a son `id` à la fois clé primaire et clé étrangère vers `utilisateurs.id`.

---

## 7. Fonctionnalités implémentées

| Fonctionnalité | Détail |
|---|---|
| **Connexion** | Vérifie existence de l'utilisateur, mot de passe (bcrypt), compte actif, et droits d'accès (rôle ADMIN ou GERANT uniquement — un compte CLIENT ne peut pas se connecter à l'application console) |
| **Catégories** | CRUD complet ; suppression bloquée si des produits sont rattachés |
| **Produits** | CRUD complet, recherche par nom, filtres par catégorie et disponibilité |
| **Stock** | Consultation, approvisionnement, seuil d'alerte configurable, détection des produits en rupture ou à stock faible |
| **Commandes** | Consultation (liste, détail, filtre par statut), changement de statut encadré (impossible de sauter une étape), annulation avec restitution automatique du stock |
| **Paiements** | Consultation des paiements par commande, liste des commandes impayées/partiellement payées, enregistrement d'un paiement (jamais au-delà du montant restant) |
| **Statistiques** | Chiffre d'affaires du jour/de la semaine/du mois, nombre de commandes, commandes en cours, répartition par statut, top 3 des produits les plus vendus |
| **Utilisateurs internes** | CRUD réservé à l'ADMIN : ajout, modification, activation/désactivation, suppression (un administrateur ne peut pas supprimer son propre compte) |

*Le Module A ne crée pas de commandes ni de comptes clients : cette création est le rôle du Module B (site web PHP). Le Module A consulte et gère ce que les clients ont initié.*

---

## 8. Règles métier appliquées

Les règles métier du cahier des charges sont appliquées à **deux niveaux complémentaires** :

1. **Côté Java** (services) — pour un retour d'erreur immédiat et un message clair à l'utilisateur.
2. **Côté base de données** (triggers PostgreSQL, dans `database.sql`) — pour garantir que la règle est respectée **quelle que soit l'application** qui écrit dans la base (Java ou PHP), sans duplication de logique.

| Règle | Application Java | Trigger PostgreSQL |
|---|---|---|
| Utilisateur désactivé ne peut pas se connecter | ✅ `AuthService` | — |
| Email unique | ✅ `UtilisateurService` | contrainte `UNIQUE` |
| Mot de passe ≥ 6 caractères | ✅ `UtilisateurService` | — |
| Commande sans produit impossible | — | (créée côté PHP) |
| Impossible de commander plus que le stock | — | `trg_ligne_commande_before_insert` |
| Stock diminue après une commande | — | `trg_ligne_commande_after_insert` |
| Stock restauré après annulation | ✅ `CommandeService` déclenche l'update | `trg_commande_annulation` |
| Catégorie avec produits non supprimable | ✅ `CategorieService` | contrainte `ON DELETE RESTRICT` |
| Paiement ne dépasse jamais le montant restant | ✅ `PaiementService` | `trg_paiement_before_insert` |
| Statut de paiement jamais stocké, toujours calculé | ✅ lecture via vue partagée | vue `vue_statut_paiement` |

---

## 9. Gestion de projet

- **Versionning** : Git, avec une branche `feature/xxx` par fonctionnalité, mergées dans `develop`.
- **Convention de commits** : `feat:`, `fix:`, `refactor:`, `docs:`.
- **Branches créées** : `feature/entites`, `feature/enum`, `feature/database`, `feature/auth`, `feature/categorie`, `feature/produit`, `feature/stock`, `feature/commande`, `feature/paiement`, `feature/statistique`, `feature/utilisateur`.

---

## 10. Limites connues / pistes d'amélioration

- Le seuil d'alerte de stock est actuellement **global** (un seul seuil pour tous les produits, remis à sa valeur par défaut à chaque redémarrage) plutôt que défini par produit en base.
- Les commandes annulées avec un paiement partiel antérieur restent visibles dans la liste "impayées / partiellement payées" (pas de filtre spécifique sur le statut de la commande à cet endroit).
- Pas encore de tests unitaires automatisés (JUnit) sur la couche service — l'architecture (services indépendants de la vue) est prête à en recevoir.