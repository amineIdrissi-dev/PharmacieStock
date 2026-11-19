-- =============================================================
--  PharmacieDB : données de test
--  Comptes créés :
--    admin   / admin123    (ADMIN)
--    employe / employe123  (EMPLOYE)
--  Changez ces mots de passe avant une utilisation réelle.
-- =============================================================
USE PharmacieDB;
GO

INSERT INTO Utilisateur (nom, prenom, login, mot_de_passe_hash, role) VALUES
 (N'Admin',  N'Pharmacien', N'admin',   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
 (N'Benali', N'Sara',       N'employe', 'c422c301b9f734203ab9cbb82979ca5fed6dc23d858f74fcb6512d1003147ec1', 'EMPLOYE');

INSERT INTO Categorie (libelle) VALUES
 (N'Antalgique'), (N'Antibiotique'), (N'Anti-inflammatoire'), (N'Vitamines'), (N'Dermatologie');

INSERT INTO Fournisseur (nom, telephone, email, adresse) VALUES
 (N'MediDistrib',    N'0522000001', N'contact@medidistrib.test', N'12 rue des Laboratoires'),
 (N'PharmaGrossiste', N'0522000002', N'ventes@pharmagros.test',   N'45 avenue de la Santé');

-- Quantités variées : en stock, stock bas, épuisé (0), expiration proche
INSERT INTO Medicament (code_barre, nom, forme, dosage, prix_unitaire, quantite, seuil_min, date_expiration, ordonnance_requise, id_categorie) VALUES
 (N'3400000000011', N'Paracétamol',   N'Comprimé', N'500 mg', 15.00, 120, 20, DATEADD(MONTH, 18, GETDATE()), 0, 1),
 (N'3400000000028', N'Ibuprofène',    N'Comprimé', N'400 mg', 22.50,   8, 15, DATEADD(MONTH, 12, GETDATE()), 0, 3),
 (N'3400000000035', N'Amoxicilline',  N'Gélule',   N'1 g',    48.00,  40, 10, DATEADD(MONTH,  9, GETDATE()), 1, 2),
 (N'3400000000042', N'Vitamine C',    N'Comprimé', N'1000 mg',30.00,   0, 10, DATEADD(MONTH, 24, GETDATE()), 0, 4),
 (N'3400000000059', N'Crème hydratante', N'Crème', N'50 ml',  65.00,  25,  5, DATEADD(DAY,   20, GETDATE()), 0, 5),
 (N'3400000000066', N'Azithromycine', N'Comprimé', N'250 mg', 72.00,   0,  5, DATEADD(MONTH, 10, GETDATE()), 1, 2);

INSERT INTO Acheteur (nom, prenom, telephone, email, adresse) VALUES
 (N'Alaoui',  N'Youssef', N'0600000001', N'youssef.alaoui@mail.test', N'3 rue des Fleurs'),
 (N'Martin',  N'Claire',  N'0600000002', N'claire.martin@mail.test',  N'18 boulevard Central');
GO
