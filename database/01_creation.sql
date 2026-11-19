-- =============================================================
--  PharmacieDB : création de la base (SQL Server)
--  À exécuter dans SQL Server Management Studio (SSMS)
-- =============================================================

IF DB_ID('PharmacieDB') IS NULL
    CREATE DATABASE PharmacieDB;
GO
USE PharmacieDB;
GO

CREATE TABLE Categorie (
    id_categorie INT IDENTITY(1,1) PRIMARY KEY,
    libelle      NVARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Utilisateur (
    id_utilisateur    INT IDENTITY(1,1) PRIMARY KEY,
    nom               NVARCHAR(100) NOT NULL,
    prenom            NVARCHAR(100) NOT NULL,
    login             NVARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe_hash CHAR(64)      NOT NULL,          -- SHA-256 en hexadécimal
    photo             VARBINARY(MAX) NULL,
    role              NVARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'EMPLOYE')),
    actif             BIT           NOT NULL DEFAULT 1,
    date_creation     DATETIME2     NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE Medicament (
    id_medicament      INT IDENTITY(1,1) PRIMARY KEY,
    code_barre         NVARCHAR(50)  UNIQUE,
    nom                NVARCHAR(150) NOT NULL,
    forme              NVARCHAR(50),                    -- comprimé, sirop, gélule...
    dosage             NVARCHAR(50),                    -- ex. 500 mg
    photo              VARBINARY(MAX) NULL,
    prix_unitaire      DECIMAL(10,2) NOT NULL CHECK (prix_unitaire >= 0),
    quantite           INT           NOT NULL DEFAULT 0 CHECK (quantite >= 0),
    seuil_min          INT           NOT NULL DEFAULT 10,
    date_expiration    DATE,
    ordonnance_requise BIT           NOT NULL DEFAULT 0,
    id_categorie       INT           NOT NULL REFERENCES Categorie(id_categorie)
);

CREATE TABLE Acheteur (
    id_acheteur      INT IDENTITY(1,1) PRIMARY KEY,
    nom              NVARCHAR(100) NOT NULL,
    prenom           NVARCHAR(100) NOT NULL,
    telephone        NVARCHAR(20),
    email            NVARCHAR(150),
    adresse          NVARCHAR(255),
    date_inscription DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE Fournisseur (
    id_fournisseur INT IDENTITY(1,1) PRIMARY KEY,
    nom            NVARCHAR(150) NOT NULL,
    telephone      NVARCHAR(20),
    email          NVARCHAR(150),
    adresse        NVARCHAR(255)
);

CREATE TABLE Vente (
    id_vente       INT IDENTITY(1,1) PRIMARY KEY,
    date_vente     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    montant_total  DECIMAL(10,2) NOT NULL DEFAULT 0,               -- total HT
    id_acheteur    INT NULL     REFERENCES Acheteur(id_acheteur),   -- NULL = client de passage
    id_utilisateur INT NOT NULL REFERENCES Utilisateur(id_utilisateur)
);

CREATE TABLE LigneVente (
    id_ligne      INT IDENTITY(1,1) PRIMARY KEY,
    id_vente      INT NOT NULL REFERENCES Vente(id_vente) ON DELETE CASCADE,
    id_medicament INT NOT NULL REFERENCES Medicament(id_medicament),
    quantite      INT NOT NULL CHECK (quantite > 0),
    prix_unitaire DECIMAL(10,2) NOT NULL
);

CREATE TABLE Approvisionnement (
    id_appro       INT IDENTITY(1,1) PRIMARY KEY,
    date_appro     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    quantite       INT NOT NULL CHECK (quantite > 0),
    prix_achat     DECIMAL(10,2),
    id_medicament  INT NOT NULL REFERENCES Medicament(id_medicament),
    id_fournisseur INT NOT NULL REFERENCES Fournisseur(id_fournisseur),
    id_utilisateur INT NOT NULL REFERENCES Utilisateur(id_utilisateur)
);

CREATE TABLE Facture (
    id_facture     INT IDENTITY(1,1) PRIMARY KEY,
    numero_facture NVARCHAR(20)  NOT NULL UNIQUE,         -- ex. FAC-2026-00001
    date_facture   DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    montant_ht     DECIMAL(10,2) NOT NULL,
    taux_tva       DECIMAL(5,2)  NOT NULL DEFAULT 0,      -- en %
    montant_ttc    DECIMAL(10,2) NOT NULL,
    mode_paiement  NVARCHAR(20)  NOT NULL CHECK (mode_paiement IN ('ESPECES', 'CARTE', 'CHEQUE')),
    statut         NVARCHAR(20)  NOT NULL DEFAULT 'VALIDEE' CHECK (statut IN ('VALIDEE', 'ANNULEE')),
    id_vente       INT NOT NULL UNIQUE REFERENCES Vente(id_vente) ON DELETE CASCADE  -- 1 facture par vente
);
GO

-- Vues utilisées par le menu
CREATE VIEW v_StocksDisponibles AS
    SELECT m.*, c.libelle AS categorie
    FROM Medicament m JOIN Categorie c ON c.id_categorie = m.id_categorie
    WHERE m.quantite > 0;
GO

CREATE VIEW v_ProduitsIndisponibles AS
    SELECT m.*, c.libelle AS categorie
    FROM Medicament m JOIN Categorie c ON c.id_categorie = m.id_categorie
    WHERE m.quantite = 0;
GO
