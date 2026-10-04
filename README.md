# Mini-Projet : Gestion des documents d'une Bibliothèque municipale

**Année universitaire :** 2024-2025

## Membres du groupe :
* **Nom:** MOUSSA - **Prénom:** YASSINE
* **Nom:** KERROUM - **Prénom:** ADAM
* **Nom:** MALIKI - **Prénom:** OMAR
* **Nom:** BARHOINE - **Prénom:** AYOUB

---

## Base de données

* **Base de données utilisée :** PostgreSQL
* **Nom de la base :** `BibliothequeMunicipale` (sans espace)
* **Fichier fourni :** `Query.sql` (à importer pour créer la base)

### Étapes d'initialisation :
1. Ouvrir **pgAdmin 4**.
2. Créer une nouvelle base de données nommée `BibliothequeMunicipale`.
3. Ouvrir le **Query Tool** puis charger le fichier `Query.sql` fourni.
4. Exécuter le script pour créer les tables.
5. Une fois les tables créées, exécuter la requête en bas pour ajouter un administrateur dans la table personnes.

---

## Installation et configuration de l'environnement de développement

Ce projet utilise désormais **Maven** pour la gestion des dépendances. Vous n'avez plus besoin d'installer manuellement JavaFX ou le pilote PostgreSQL, Maven s'occupe de tout !

### 1. Prérequis
* **JDK (Java Development Kit) 17 ou supérieur :** Assurez-vous d'avoir Java 17+ installé sur votre machine.
* **IntelliJ IDEA (ou tout autre IDE compatible Maven).**

### 2. Lancement du projet
* Ouvrez le dossier `miniprojetsujet8` dans votre IDE.
* Votre IDE devrait détecter automatiquement le fichier `pom.xml` et télécharger les dépendances (PostgreSQL JDBC et JavaFX).
* **Important :** Dans le fichier `code/BiblioMunicipale/DAO/DBConnection.java`, veuillez modifier le champ `password` selon celui que vous avez défini pour votre base de données PostgreSQL.
* Vous pouvez lancer l'application en exécutant la classe `Main.java` ou via la commande Maven :
  ```bash
  mvn clean javafx:run
  ```

---

## Description des répertoires

* `app/` : contient la classe principale de l'application.
* `DAO/` : classes pour la gestion des accès aux données.
* `documents/` : gestion des documents de la bibliothèque.
* `enums/` : énumérations utilisées dans le projet.
* `lib/` : éventuelles bibliothèques externes.
* `out/` : répertoire de sortie des fichiers compilés.
* `personnes/` : gestion des utilisateurs, membres, bibliothécaires...
* `Services/` : classes métiers / services applicatifs.
* `transactions/` : gestion des emprunts, retours, réservations...