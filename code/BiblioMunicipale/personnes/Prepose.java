package personnes;

import DAO.transactions.EmpruntDAO;
import DAO.transactions.ReservationDAO;
import documents.Documents;
import transactions.Emprunt;

import java.time.LocalDate;

public class Prepose extends Personne {

    public Prepose(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse, "PREPOSE");
    }


}