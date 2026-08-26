-- ============================================================
-- SAVEUR 221 — Script de création de la base de données
-- Partagée par l'application Java Console et l'application PHP Web
-- Moteur : PostgreSQL 13+
-- Converti depuis la version MySQL 8 / InnoDB.
-- Fidèle au diagramme de classes fourni (client hérite de UTILISATEUR,
-- statut_paiement calculé côté application / vue SQL, pas de colonne dédiée).
-- ============================================================

DROP TABLE IF EXISTS avis CASCADE;
DROP TABLE IF EXISTS paiements CASCADE;
DROP TABLE IF EXISTS ligne_commandes CASCADE;
DROP TABLE IF EXISTS commandes CASCADE;
DROP TABLE IF EXISTS produits CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS utilisateurs CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

DROP TRIGGER IF EXISTS trg_produits_statut_stock_insert ON produits;
DROP TRIGGER IF EXISTS trg_produits_statut_stock_update ON produits;
DROP FUNCTION IF EXISTS fn_produits_statut_stock_insert();
DROP FUNCTION IF EXISTS fn_produits_statut_stock_update();


CREATE DATABASE IF NOT EXISTS Restaurant_Saveur_221;
USE Restaurant_Saveur_221;

-- ------------------------------------------------------------
-- ROLES
-- ------------------------------------------------------------
CREATE TABLE roles (
    id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE   -- ex : ADMIN, GERANT
);

-- ------------------------------------------------------------
-- UTILISATEURS (superclasse : porte les champs communs à tout
-- compte du système — personnel interne ET clients, puisque
-- CLIENT hérite de UTILISATEUR dans le diagramme de classes)
-- ------------------------------------------------------------
CREATE TABLE utilisateurs (
    id             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,   -- règle métier : email unique
    mdp            VARCHAR(255) NOT NULL,          -- mot de passe haché (bcrypt/password_hash) ; la contrainte
                                                    -- "6 caractères minimum" se vérifie côté application, AVANT hachage
    actif          BOOLEAN NOT NULL DEFAULT TRUE,  -- règle métier : compte désactivé => connexion refusée
    date_creation  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    role_id        INTEGER NULL,                    -- NULL pour un client, renseigné (ADMIN/GERANT) pour le personnel interne
    CONSTRAINT fk_utilisateur_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX idx_utilisateurs_role ON utilisateurs(role_id);

-- ------------------------------------------------------------
-- CLIENTS (sous-classe de UTILISATEUR — héritage par table :
-- clients.id partage la même clé primaire que utilisateurs.id)
-- ------------------------------------------------------------
CREATE TABLE clients (
    id         INTEGER PRIMARY KEY,
    telephone  VARCHAR(20)  NOT NULL,
    adresse    VARCHAR(255) NOT NULL,
    CONSTRAINT fk_client_utilisateur
        FOREIGN KEY (id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- CATEGORIES
-- ------------------------------------------------------------
CREATE TABLE categories (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- ------------------------------------------------------------
-- PRODUITS
-- Ajout de seuil_alerte (absent du script d'origine) pour
-- couvrir "définir un seuil de stock" / "voir les produits à stock faible".
-- ------------------------------------------------------------
CREATE TABLE produits (
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom           VARCHAR(150) NOT NULL,
    description   TEXT,
    prix          NUMERIC(10,2) NOT NULL CHECK (prix >= 0),
    stock         INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    seuil_alerte  INTEGER NOT NULL DEFAULT 5 CHECK (seuil_alerte >= 0),
    image         VARCHAR(255),
    statut        VARCHAR(20) NOT NULL DEFAULT 'disponible'
                  CHECK (statut IN ('disponible','en_rupture')),  -- «enumeration» statutproduit
    categorie_id  INTEGER NOT NULL,
    CONSTRAINT fk_produit_categorie
        FOREIGN KEY (categorie_id) REFERENCES categories(id)
);

CREATE INDEX idx_produits_categorie ON produits(categorie_id);
CREATE INDEX idx_produits_statut ON produits(statut);

-- Règle métier : si le stock tombe à 0, le produit passe automatiquement "en_rupture" ;
-- s'il redevient > 0, il repasse "disponible".
CREATE FUNCTION fn_produits_statut_stock_insert() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.stock = 0 THEN
        NEW.statut := 'en_rupture';
    ELSIF NEW.statut IS NULL THEN
        NEW.statut := 'disponible';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_produits_statut_stock_insert
BEFORE INSERT ON produits
FOR EACH ROW EXECUTE FUNCTION fn_produits_statut_stock_insert();

CREATE FUNCTION fn_produits_statut_stock_update() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.stock = 0 THEN
        NEW.statut := 'en_rupture';
    ELSIF OLD.stock = 0 AND NEW.stock > 0 THEN
        NEW.statut := 'disponible';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_produits_statut_stock_update
BEFORE UPDATE ON produits
FOR EACH ROW EXECUTE FUNCTION fn_produits_statut_stock_update();

-- ------------------------------------------------------------
-- COMMANDES
-- ------------------------------------------------------------
CREATE TABLE commandes (
    id             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date_commande  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total  NUMERIC(10,2) NOT NULL DEFAULT 0,
    statut         VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                   CHECK (statut IN ('EN_ATTENTE','EN_PREPARATION','PRETE','RETIREE','ANNULEE')),  -- «enumeration» statutCommande
    client_id      INTEGER NOT NULL,
    CONSTRAINT fk_commande_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE INDEX idx_commandes_client ON commandes(client_id);
CREATE INDEX idx_commandes_statut ON commandes(statut);
CREATE INDEX idx_commandes_date ON commandes(date_commande);

-- ------------------------------------------------------------
-- LIGNE_COMMANDES
-- ------------------------------------------------------------
CREATE TABLE ligne_commandes (
    id             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    quantite       INTEGER NOT NULL CHECK (quantite > 0),
    prix_unitaire  NUMERIC(10,2) NOT NULL,
    montant_ligne  NUMERIC(10,2) NOT NULL,   -- = quantite * prix_unitaire (calculé côté service)
    commande_id    INTEGER NOT NULL,
    produit_id     INTEGER NOT NULL,
    CONSTRAINT fk_ligne_commande
        FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE CASCADE,
    CONSTRAINT fk_ligne_produit
        FOREIGN KEY (produit_id) REFERENCES produits(id)
);

CREATE INDEX idx_lignes_commande ON ligne_commandes(commande_id);
CREATE INDEX idx_lignes_produit ON ligne_commandes(produit_id);

-- ------------------------------------------------------------
-- PAIEMENTS
-- (pas de colonne "statut_paiement" : impayée / partielle / soldée
--  est calculé à la volée en comparant SUM(paiements.montant) au
--  montant_total de la commande — voir CommandeService.getStatutPaiement()
--  et la vue vue_statut_paiement ci-dessous)
-- ------------------------------------------------------------
CREATE TABLE paiements (
    id             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    montant        NUMERIC(10,2) NOT NULL CHECK (montant > 0),
    date_paiement  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commande_id    INTEGER NOT NULL,
    CONSTRAINT fk_paiement_commande
        FOREIGN KEY (commande_id) REFERENCES commandes(id)
);

CREATE INDEX idx_paiements_commande ON paiements(commande_id);

-- ------------------------------------------------------------
-- AVIS
-- (commande_id UNIQUE => impose la règle métier "un seul avis par commande")
-- ------------------------------------------------------------
CREATE TABLE avis (
    id           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    note         INTEGER NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire  TEXT,
    date_avis    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_id    INTEGER NOT NULL,
    commande_id  INTEGER NOT NULL UNIQUE,
    CONSTRAINT fk_avis_client
        FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_avis_commande
        FOREIGN KEY (commande_id) REFERENCES commandes(id)
);

CREATE INDEX idx_avis_client ON avis(client_id);

-- ============================================================
-- (Optionnel) Vue utilitaire pour visualiser rapidement le statut de
-- paiement calculé d'une commande — utile en debug SQL, sans dupliquer
-- la logique métier qui reste dans CommandeService.getStatutPaiement()
-- ============================================================
CREATE OR REPLACE VIEW vue_statut_paiement AS
SELECT
    c.id AS commande_id,
    c.montant_total,
    COALESCE(SUM(p.montant), 0) AS total_paye,
    CASE
        WHEN COALESCE(SUM(p.montant), 0) = 0 THEN 'IMPAYEE'
        WHEN COALESCE(SUM(p.montant), 0) < c.montant_total THEN 'PARTIELLEMENT_PAYEE'
        ELSE 'SOLDEE'
    END AS statut_paiement
FROM commandes c
LEFT JOIN paiements p ON p.commande_id = c.id
GROUP BY c.id, c.montant_total;

-- ============================================================
-- Données de référence minimales
-- ============================================================
INSERT INTO roles (libelle) VALUES ('ADMIN'), ('GERANT');