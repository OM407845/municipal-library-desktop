package transactions;

import DAO.transactions.EmpruntDAO;
import documents.Documents;
import enums.StatutEmprunt;
import enums.TypeEmprunt;
import personnes.Client;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Emprunt {
    private int id;
    private Documents doc;
    private Client client;
    private LocalDate dateEmprunt;
    private LocalDate dateRetourSupposee;
    private LocalDate dateRetourReel;
    private StatutEmprunt statut;
    private double amende;

    // 🔹 Constructeur automatique
    public Emprunt(int id, Documents doc, Client client) {
        this.id = id;
        this.doc = doc;
        this.client = client;
        this.dateEmprunt = LocalDate.now();
        this.dateRetourSupposee = calculerDateRetourSupposee();
        this.statut = StatutEmprunt.EN_COURS;
        this.amende = 0.0;
    }

    // 🔹 Calcul automatique de la date de retour
    private LocalDate calculerDateRetourSupposee() {
        if (doc.getTypeEmprunt() == TypeEmprunt.PRET) {
            return dateEmprunt.plusDays(21);
        } else if (doc.getTypeEmprunt() == TypeEmprunt.LOCATION) {
            return dateEmprunt.plusDays(7);
        }
        return dateEmprunt;
    }

    // Calcul de l'amende si retour en retard
    public void calculerAmende() {
        if (dateRetourReel != null && dateRetourSupposee != null && dateRetourReel.isAfter(dateRetourSupposee)) {
            long joursRetard = dateRetourReel.toEpochDay() - dateRetourSupposee.toEpochDay();

            if (doc.getTypeEmprunt() == TypeEmprunt.PRET) {
                this.amende = joursRetard * 5.0;
            } else if (doc.getTypeEmprunt() == TypeEmprunt.LOCATION) {
                this.amende = joursRetard * 10.0;
            }
        } else {
            this.amende = 0.0;
        }
    }


    // 🔹 Mise à jour automatique du statut
    public void mettreAJourStatut() {
        if (dateRetourReel != null) {
            if (dateRetourReel.isAfter(dateRetourSupposee)) {
                statut = StatutEmprunt.RETARD;
            } else {
                statut = StatutEmprunt.RETOURNE;
            }
        } else if (LocalDate.now().isAfter(dateRetourSupposee)) {
            statut = StatutEmprunt.RETARD;
        }
    }


    // on va utiliser cette methode
    public void retourner(LocalDate dateRetour) {
        this.dateRetourReel = dateRetour;
        calculerAmende();
        mettreAJourStatut();

        // Appel à la base de données
        EmpruntDAO dao = new EmpruntDAO();
        dao.enregistrerRetour(this.id, this.dateRetourReel, this.amende, this.statut);
    }

    // 🔹 Getters / Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Documents getDoc() {
        return doc;
    }

    public void setDoc(Documents doc) {
        this.doc = doc;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public LocalDate getDateEmprunt() {
        return dateEmprunt;
    }

    public void setDateEmprunt(LocalDate dateEmprunt) {
        this.dateEmprunt = dateEmprunt;
    }

    public LocalDate getDateRetourSupposee() {
        return dateRetourSupposee;
    }

    public void setDateRetourSupposee(LocalDate dateRetourSupposee) {
        this.dateRetourSupposee = dateRetourSupposee;
    }

    public LocalDate getDateRetourReel() {
        return dateRetourReel;
    }

    public void setDateRetourReel(LocalDate dateRetourReel) {
        this.dateRetourReel = dateRetourReel;
    }

    public StatutEmprunt getStatut() {
        return statut;
    }

    public void setStatut(StatutEmprunt statut) {
        this.statut = statut;
    }

    public double getAmende() {
        return amende;
    }

    public void setAmende(double amende) {
        this.amende = amende;
    }
}
