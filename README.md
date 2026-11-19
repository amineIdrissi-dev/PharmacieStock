# Gestion de stock pour pharmacie (Java + SQL Server)

Application de bureau Java (Swing), orientée objet, reliée à SQL Server par JDBC.
Fonctions : authentification, tableau de bord, stocks disponibles, produits vendus,
produits indisponibles (quantité 0), acheteurs, facture avec impression, gestion des
utilisateurs, catégories et fournisseurs.

## Contenu

| Dossier / fichier | Rôle |
| --- | --- |
| `database/01_creation.sql` | Création de la base `PharmacieDB` (9 tables + 2 vues) |
| `database/02_donnees_test.sql` | Données de test et comptes de démonstration |
| `src/main/java/pharmacie/model` | Classes métier (Personne, Utilisateur, Medicament, Vente, Facture...) |
| `src/main/java/pharmacie/dao` | Interface `CrudDAO<T>` et une classe DAO (CRUD) par table |
| `src/main/java/pharmacie/service` | Règles métier : connexion, vente, facture, stock, impression |
| `src/main/java/pharmacie/util` | Connexion SQL Server, configuration, hachage SHA-256 |
| `src/main/java/pharmacie/view` | Fenêtres Swing |
| `config.exemple.properties` | Modèle de configuration à copier en `config.properties` (connexion, pharmacie, TVA) |
| `docs/` | Cahier des charges |

## 1. Préparer la base SQL Server

1. Ouvrez SQL Server Management Studio (SSMS).
2. Exécutez `database/01_creation.sql`, puis `database/02_donnees_test.sql`.
3. Vérifiez que SQL Server accepte les connexions :
   - **Authentification SQL Server** activée (Propriétés du serveur > Sécurité > « Mode d'authentification SQL Server et Windows »), avec un compte comme `sa` qui a un mot de passe ;
   - **TCP/IP** activé sur le port **1433** (SQL Server Configuration Manager > Configuration du réseau SQL Server > Protocoles), puis redémarrez le service SQL Server.
4. Copiez `config.exemple.properties` en `config.properties` (ce fichier, qui contient le mot de passe, n'est pas envoyé sur GitHub), puis renseignez-le : `db.utilisateur`, `db.motdepasse`, et si besoin l'adresse dans `db.url`
   (pour une instance nommée, par exemple `localhost\\SQLEXPRESS`, utilisez `jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=PharmacieDB;encrypt=true;trustServerCertificate=true`).

## 2. Lancer l'application

### Avec Visual Studio Code (recommandé)

1. Installez un JDK 17 ou plus récent.
2. Dans VS Code, installez l'extension **Extension Pack for Java** (proposée automatiquement à l'ouverture du dossier).
3. Ouvrez le dossier `PharmacieStock` (Fichier > Ouvrir le dossier). VS Code lit `pom.xml` et télécharge le pilote SQL Server.
4. Ouvrez l'onglet « Exécuter et déboguer » et lancez **Lancer PharmacieStock** (ou ouvrez `Main.java` et cliquez sur « Run »).

Visual Studio (la version violette, pour C# / C++) ne permet pas de développer en Java : utilisez Visual Studio Code.

### Avec NetBeans, IntelliJ IDEA ou Eclipse

Ouvrez le dossier comme un projet Maven (`pom.xml`) et lancez la classe `pharmacie.Main`.
Le dossier de travail doit être le dossier du projet (pour lire `config.properties`).

### Sans IDE

1. Copiez le pilote `mssql-jdbc-...jre11.jar` dans `lib/` (voir `lib/LISEZ-MOI.txt`).
2. Double-cliquez sur `compiler.bat`, puis sur `lancer.bat`.

## 3. Comptes de test

| Login | Mot de passe | Rôle |
| --- | --- | --- |
| admin | admin123 | Administrateur |
| employe | employe123 | Employé |

Changez ces mots de passe (rubrique Utilisateurs) avant une utilisation réelle.

## Impression des factures

Le bouton « Imprimer » de la facture ouvre la fenêtre d'impression de Windows.
Choisissez l'imprimante « Microsoft Print to PDF » pour obtenir un fichier PDF.
