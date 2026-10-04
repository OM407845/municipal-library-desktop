package transactions;

import documents.Documents;
import enums.StatutReservation;
import personnes.Client;

import java.time.LocalDate;
import java.util.Locale;
public class Reservation {
    private int id;
    private Client client;
    private Documents document;
    private java.time.LocalDate dateReservation;
    private StatutReservation statut;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Documents getDocument() {
        return document;
    }

    public void setDocument(Documents document) {
        this.document = document;
    }

    public java.time.LocalDate getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(java.time.LocalDate dateReservation) {
        this.dateReservation = dateReservation;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    // Constructeur et methodes

    public Reservation(int id, Client client, Documents document) {
        this.id = id;
        this.client = client;
        this.document = document;
        this.dateReservation = LocalDate.now();
        this.statut = StatutReservation.EN_ATTENTE;
    }

    public void valider() {
        statut = StatutReservation.ACTIVE;
    }

    public void annuler() {
        statut = StatutReservation.EXPIREE;
    }


}