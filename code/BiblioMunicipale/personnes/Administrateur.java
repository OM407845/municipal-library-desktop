package personnes;

import DAO.personnes.PersonneDAO;

public class Administrateur extends Personne {

    public Administrateur(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse, "ADMIN");
    }

}
