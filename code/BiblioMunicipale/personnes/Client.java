package personnes;
import DAO.transactions.EmpruntDAO;
import DAO.transactions.ReservationDAO;
import documents.Documents;
import transactions.Emprunt;
import transactions.Reservation;

import java.time.LocalDate ;
public class Client extends Personne {
    private CarteClient carteClient; // association
    public static final int LIMITE_MAX_DOCUMENTS = 5;
    private boolean estResident;

    public boolean isEstResident() {
        return estResident;
    }

    public void setEstResident(boolean estResident) {
        this.estResident = estResident;
    }
    // constructeur et methode


    public CarteClient getCarteClient() {
        return carteClient;
    }
    public void setCarteClient(CarteClient carte){
        this.carteClient = carte ;
    }

    public Client(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse, "CLIENT");
    }






}