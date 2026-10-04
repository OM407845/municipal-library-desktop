package Services;
import DAO.documents.*;
import documents.*;
import personnes.Personne;

import java.util.List;

public class Bibliothecaire extends Personne {

    // constructeur et methodes
    public Bibliothecaire(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse, "BIBLIO");
    }
}