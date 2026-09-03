-- ============================================================================
-- SAVEUR 221 — Script de base de données
-- Généré à partir du diagramme de classes (héritage Client -> UTILISATEUR)
-- Compatible PostgreSQL 13+
-- Partagé par les deux applications : Module A (Java Console) et Module B (PHP Web)
-- ============================================================================
-- À exécuter avec psql, par exemple :
--   psql -U postgres -f database.sql
-- (les commandes \c ci-dessous ne fonctionnent que dans le client psql)
-- ============================================================================

DROP DATABASE IF EXISTS saveur221;
CREATE DATABASE saveur221 WITH ENCODING 'UTF8';

\c saveur221

-- ============================================================================
-- TYPES ÉNUMÉRÉS (correspondent aux «enumeration» du diagramme de classes)
-- ============================================================================
CREATE TYPE statut_produit_enum   AS ENUM ('disponible', 'en_rupture');
CREATE TYPE statut_commande_enum  AS ENUM ('EN_ATTENTE', 'EN_PREPARATION', 'PRETE', 'RETIREE', 'ANNULEE');

-- ============================================================================
-- 1. ROLES
-- ============================================================================
CREATE TABLE roles (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(30) NOT NULL UNIQUE
);

-- ============================================================================
-- 2. UTILISATEUR (classe mère : personnel interne ET clients)
-- ============================================================================
CREATE TABLE utilisateurs (
    id             SERIAL PRIMARY KEY,
    nom            VARCHAR(60)  NOT NULL,
    prenom         VARCHAR(60)  NOT NULL,
    email          VARCHAR(120) NOT NULL UNIQUE,
    mdp            VARCHAR(255) NOT NULL,
    actif          BOOLEAN      NOT NULL DEFAULT TRUE,
    date_creation  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    role_id        INT          NOT NULL REFERENCES roles(id)
);

-- ============================================================================
-- 3. CLIENT (hérite de UTILISATEUR — Class Table Inheritance)
-- ============================================================================
CREATE TABLE clients (
    id         INT PRIMARY KEY REFERENCES utilisateurs(id) ON DELETE CASCADE,
    telephone  VARCHAR(20)  NOT NULL,
    adresse    VARCHAR(255) NOT NULL
);

-- ============================================================================
-- 4. CATEGORIE
-- ============================================================================
CREATE TABLE categories (
    id          SERIAL PRIMARY KEY,
    libelle         VARCHAR(60) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- ============================================================================
-- 5. PRODUIT
-- ============================================================================
CREATE TABLE produits (
    id           SERIAL PRIMARY KEY,
    nom          VARCHAR(100)  NOT NULL,
    description  VARCHAR(255),
    prix         NUMERIC(10,2) NOT NULL CHECK (prix >= 0),
    stock        INT           NOT NULL DEFAULT 0 CHECK (stock >= 0),
    image        VARCHAR(255),
    statut       statut_produit_enum NOT NULL DEFAULT 'disponible',
    categorie_id INT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT
);

CREATE INDEX idx_produit_categorie ON produits(categorie_id);

-- ============================================================================
-- 6. COMMANDE
-- ============================================================================
CREATE TABLE commandes (
    id             SERIAL PRIMARY KEY,
    date_commande  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total  NUMERIC(10,2) NOT NULL DEFAULT 0,
    statut         statut_commande_enum NOT NULL DEFAULT 'EN_ATTENTE',
    client_id      INT NOT NULL REFERENCES clients(id)
);

CREATE INDEX idx_commande_client ON commandes(client_id);
CREATE INDEX idx_commande_statut ON commandes(statut);

-- ============================================================================
-- 7. LIGNE_DE_COMMANDE
-- ============================================================================
CREATE TABLE lignes_commande (
    id             SERIAL PRIMARY KEY,
    quantite       INT           NOT NULL CHECK (quantite > 0),
    prix_unitaire  NUMERIC(10,2) NOT NULL,
    montant_ligne  NUMERIC(10,2) NOT NULL,
    commande_id    INT NOT NULL REFERENCES commandes(id) ON DELETE CASCADE,
    produit_id     INT NOT NULL REFERENCES produits(id)
);

CREATE INDEX idx_ligne_commande ON lignes_commande(commande_id);
CREATE INDEX idx_ligne_produit  ON lignes_commande(produit_id);

-- ============================================================================
-- 8. PAIEMENT (statut calculé, jamais stocké)
-- ============================================================================
CREATE TABLE paiements (
    id             SERIAL PRIMARY KEY,
    montant        NUMERIC(10,2) NOT NULL CHECK (montant > 0),
    date_paiement  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commande_id    INT NOT NULL REFERENCES commandes(id)
);

CREATE INDEX idx_paiement_commande ON paiements(commande_id);

-- ============================================================================
-- 9. AVIS (1 seul avis par commande -> UNIQUE(commande_id))
-- ============================================================================
CREATE TABLE avis (
    id           SERIAL PRIMARY KEY,
    note         INT NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire  VARCHAR(500),
    date_avis    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_id    INT NOT NULL REFERENCES clients(id),
    commande_id  INT NOT NULL UNIQUE REFERENCES commandes(id)
);

-- ============================================================================
-- VUE PARTAGÉE : statut de paiement calculé (utilisable par Java ET PHP)
-- ============================================================================
CREATE VIEW vue_statut_paiement AS
SELECT
    c.id AS commande_id,
    c.montant_total,
    COALESCE(SUM(p.montant), 0) AS montant_paye,
    c.montant_total - COALESCE(SUM(p.montant), 0) AS montant_restant,
    CASE
        WHEN COALESCE(SUM(p.montant), 0) = 0 THEN 'IMPAYEE'
        WHEN COALESCE(SUM(p.montant), 0) < c.montant_total THEN 'PARTIELLEMENT_PAYEE'
        ELSE 'TOTALEMENT_PAYEE'
    END AS statut_paiement
FROM commandes c
LEFT JOIN paiements p ON p.commande_id = c.id
GROUP BY c.id, c.montant_total;

-- ============================================================================
-- TRIGGERS — règles métier appliquées au niveau base de données
-- ============================================================================

CREATE FUNCTION fn_ligne_commande_before_insert() RETURNS TRIGGER AS $$
DECLARE
    stock_dispo INT;
BEGIN
    SELECT stock INTO stock_dispo FROM produits WHERE id = NEW.produit_id FOR UPDATE;
    IF stock_dispo < NEW.quantite THEN
        RAISE EXCEPTION 'Quantité commandée supérieure au stock disponible';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ligne_commande_before_insert
BEFORE INSERT ON lignes_commande
FOR EACH ROW EXECUTE FUNCTION fn_ligne_commande_before_insert();

CREATE FUNCTION fn_ligne_commande_after_insert() RETURNS TRIGGER AS $$
BEGIN
    UPDATE produits SET stock = stock - NEW.quantite WHERE id = NEW.produit_id;
    UPDATE produits SET statut = 'en_rupture' WHERE id = NEW.produit_id AND stock = 0;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ligne_commande_after_insert
AFTER INSERT ON lignes_commande
FOR EACH ROW EXECUTE FUNCTION fn_ligne_commande_after_insert();

CREATE FUNCTION fn_commande_annulation() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.statut = 'ANNULEE' AND OLD.statut <> 'ANNULEE' THEN
        UPDATE produits
        SET stock = produits.stock + lc.quantite,
            statut = 'disponible'
        FROM lignes_commande lc
        WHERE produits.id = lc.produit_id AND lc.commande_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_commande_annulation
AFTER UPDATE ON commandes
FOR EACH ROW EXECUTE FUNCTION fn_commande_annulation();

CREATE FUNCTION fn_paiement_before_insert() RETURNS TRIGGER AS $$
DECLARE
    total_commande NUMERIC(10,2);
    deja_paye      NUMERIC(10,2);
BEGIN
    SELECT montant_total INTO total_commande FROM commandes WHERE id = NEW.commande_id;
    SELECT COALESCE(SUM(montant),0) INTO deja_paye FROM paiements WHERE commande_id = NEW.commande_id;
    IF NEW.montant > (total_commande - deja_paye) THEN
        RAISE EXCEPTION 'Le paiement dépasse le montant restant de la commande';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_paiement_before_insert
BEFORE INSERT ON paiements
FOR EACH ROW EXECUTE FUNCTION fn_paiement_before_insert();

CREATE FUNCTION fn_avis_before_insert() RETURNS TRIGGER AS $$
DECLARE
    statut_cmd statut_commande_enum;
BEGIN
    SELECT statut INTO statut_cmd FROM commandes WHERE id = NEW.commande_id;
    IF statut_cmd <> 'RETIREE' THEN
        RAISE EXCEPTION 'Un avis ne peut être laissé que sur une commande retirée';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_avis_before_insert
BEFORE INSERT ON avis
FOR EACH ROW EXECUTE FUNCTION fn_avis_before_insert();

-- ============================================================================
-- DONNÉES DE BASE
-- ============================================================================
INSERT INTO roles (libelle) VALUES ('ADMIN'), ('GERANT'), ('CLIENT');

INSERT INTO categories (libelle, description) VALUES
    ('Entrées', 'Accras, salades, feuilletés'),
    ('Plats', 'Plats principaux du menu'),
    ('Desserts', 'Douceurs et pâtisseries'),
    ('Boissons', 'Jus locaux et sodas');

-- ============================================================================
-- COMPTES DE TEST — PERSONNEL INTERNE
-- Mots de passe déjà hashés en bcrypt (compatibles AuthService.java) :
--   ADMIN  : a.diallo@saveur221.sn  / mot de passe : motdepasse1
--   GERANT : mariama.g@saveur221.sn / mot de passe : motdepasse2
-- ============================================================================
INSERT INTO utilisateurs (nom, prenom, email, mdp, actif, role_id) VALUES
    ('Diallo', 'Amadou', 'a.diallo@saveur221.sn',
     '$2a$10$8d0WxVONRnwhi79y.qSgoenzbcBbt6pSV4LTPi/11iHraRHFoe64O', TRUE, 1),
    ('Gerant', 'Mariama', 'mariama.g@saveur221.sn',
     '$2a$10$JNi55LpdGP/3WYiZu0lpJ.GHrlnxZio7A/LSVvAwPUCJZwSf3Zuju', TRUE, 2);

-- ============================================================================
-- DONNÉES DE TEST — CLIENTS (reproduisent les diagrammes d'objets)
-- ============================================================================
INSERT INTO utilisateurs (id, nom, prenom, email, mdp, actif, role_id) VALUES
    (10, 'Diagne', 'Ndeye', 'ndeye@email.com', '$2y$10$ObArOfXlJpVRcmujcP5gl.VpSoWHz2hYh8vbrSTRtfpJHYrvcubnK', TRUE, 3);
INSERT INTO clients (id, telephone, adresse) VALUES (10, '771001010', 'yoff');

INSERT INTO utilisateurs (id, nom, prenom, email, mdp, actif, role_id) VALUES
    (11, 'Ndiaye', 'Omar', 'omar@email.com', '$2y$10$LkbncjE48aDxux/edbSXt.cRJJ2pqWCt7Rra88O2SKJO4wGVCwqYO', TRUE, 3);
INSERT INTO clients (id, telephone, adresse) VALUES (11, '771111111', 'parcelle');

SELECT setval(pg_get_serial_sequence('utilisateurs','id'), (SELECT MAX(id) FROM utilisateurs));

-- ============================================================================
-- DONNÉES DE TEST — PRODUITS
-- ============================================================================
INSERT INTO produits (id, nom, prix, stock, statut, categorie_id) VALUES
    (1, 'Thiéboudienne au poisson', 3500, 8, 'disponible', 2),
    (2, 'Accras de poisson (x6)', 1500, 5, 'disponible', 2),
    (3, 'Yassa poulet', 3000, 1, 'disponible', 2);
SELECT setval(pg_get_serial_sequence('produits','id'), (SELECT MAX(id) FROM produits));
SELECT setval(pg_get_serial_sequence('produits','id'), (SELECT MAX(id) FROM produits));

-- ============================================================================
-- DONNÉES DE TEST — COMMANDES
-- ============================================================================
-- ============================================================================
-- DONNÉES DE TEST — COMMANDES
-- ============================================================================
-- ============================================================================
-- DONNÉES DE TEST — COMMANDES
-- ============================================================================
INSERT INTO commandes (id, date_commande, montant_total, statut, client_id) VALUES
    (1, '2026-08-20', 8500, 'RETIREE', 10);
SELECT setval(pg_get_serial_sequence('commandes','id'), (SELECT MAX(id) FROM commandes));

INSERT INTO lignes_commande (id, quantite, prix_unitaire, montant_ligne, commande_id, produit_id) VALUES
    (1, 2, 3500, 7000, 1, 1),
    (2, 1, 1500, 1500, 1, 2);
SELECT setval(pg_get_serial_sequence('lignes_commande','id'), (SELECT MAX(id) FROM lignes_commande));

INSERT INTO paiements (id, montant, date_paiement, commande_id) VALUES
    (1, 8500, '2026-08-21', 1);
SELECT setval(pg_get_serial_sequence('paiements','id'), (SELECT MAX(id) FROM paiements));
INSERT INTO avis (id, note, commentaire, date_avis, client_id, commande_id) VALUES
    (1, 5, 'Service rapide', '2026-08-21', 10, 1);
SELECT setval(pg_get_serial_sequence('avis','id'), (SELECT MAX(id) FROM avis));


UPDATE utilisateurs
SET mdp = '$2y$10$LkbncjE48aDxux/edbSXt.cRJJ2pqWCt7Rra88O2SKJO4wGVCwqYO'
WHERE id = 11;

UPDATE utilisateurs
SET mdp = '$2y$10$ObArOfXlJpVRcmujcP5gl.VpSoWHz2hYh8vbrSTRtfpJHYrvcubnK'
WHERE id = 10;