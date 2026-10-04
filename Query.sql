CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    type_document VARCHAR(20) NOT NULL CHECK (type_document IN ('LIVRE', 'JOURNAL', 'DISQUE', 'PERIODIQUE')),
    nombre_exemplaires INT NOT NULL CHECK (nombre_exemplaires >= 0),
    type_emprunt VARCHAR(20) NOT NULL CHECK (type_emprunt IN ('PRET', 'LOCATION', 'CONSULTATION')),
    empruntable BOOLEAN NOT NULL
);

-- Tables enfants avec ON DELETE CASCADE
CREATE TABLE livres (
    document_id INT PRIMARY KEY REFERENCES documents(id) ON DELETE CASCADE, -- Modification ici
    auteur VARCHAR(255) NOT NULL,
    isbn VARCHAR(13) UNIQUE NOT NULL,
    duree_max_jours INT CHECK (duree_max_jours IN (7, 21)),
    frais_location DECIMAL(5,2) CHECK (frais_location IN (0.0, 50.0)),
    fortement_demande BOOLEAN NOT NULL
);

CREATE TABLE journaux (
    document_id INT PRIMARY KEY REFERENCES documents(id) ON DELETE CASCADE,
    date_publication DATE NOT NULL,
    editeur VARCHAR(255) NOT NULL
);

CREATE TABLE disques_compacts (
    document_id INT PRIMARY KEY REFERENCES documents(id) ON DELETE CASCADE, -- Modification ici
    artiste VARCHAR(255) NOT NULL,
    genre VARCHAR(50),
    duree_max_jours INT DEFAULT 7 CHECK (duree_max_jours = 7),
    frais_location DECIMAL(5,2) DEFAULT 40.0 CHECK (frais_location = 40.0)
);

CREATE TABLE periodiques (
    document_id INT PRIMARY KEY REFERENCES documents(id) ON DELETE CASCADE, -- Modification ici
    numero INT NOT NULL CHECK (numero > 0),
    date_parution DATE NOT NULL,
    editeur VARCHAR(255) NOT NULL
);

CREATE TABLE personnes (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    mot_de_passe VARCHAR(255),
    type_personne VARCHAR(50) CHECK (type_personne IN ('ADMIN', 'BIBLIO', 'PREPOSE', 'CLIENT'))
);

CREATE TABLE carte_client (
    id SERIAL PRIMARY KEY,
    numero_code_barres VARCHAR(50) UNIQUE NOT NULL,
    date_emission DATE NOT NULL,
    date_expiration DATE NOT NULL,
    frais_inscription DECIMAL(6,2) NOT NULL,
    est_resident BOOLEAN NOT NULL,
    id_personne INT REFERENCES personnes(id) ON DELETE CASCADE
);

CREATE TABLE emprunts (
    id SERIAL PRIMARY KEY,
    id_document INT REFERENCES documents(id),
    id_client INT REFERENCES personnes(id),
    date_emprunt DATE NOT NULL,
    date_retour_supposee DATE NOT NULL,
    date_retour_reel DATE,
    statut VARCHAR(20) CHECK (statut IN ('EN_COURS', 'RETOURNE', 'RETARD')),
    amende DOUBLE PRECISION DEFAULT 0.0
);

CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    id_document INTEGER NOT NULL,
    id_client INTEGER NOT NULL,
    date_reservation DATE NOT NULL,
    statut VARCHAR(20) NOT NULL CHECK (statut IN ('EN_ATTENTE', 'ACTIVE', 'EXPIREE')),
    
    CONSTRAINT fk_client_reservation FOREIGN KEY (id_client)
        REFERENCES personnes(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_document_reservation FOREIGN KEY (id_document)
        REFERENCES documents(id)
        ON DELETE CASCADE
);
--pour creer un admin dans la table personnes
INSERT INTO personnes (nom, prenom, email, mot_de_passe, type_personne)
VALUES ('Nassiri', 'Ahmed', 'admin', 'admin', 'ADMIN');








