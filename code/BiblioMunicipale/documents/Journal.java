package documents;

import enums.TypeEmprunt;

import java.time.LocalDate;

public class Journal extends Documents {
    private LocalDate datePub;
    private String editeur;

    public LocalDate getDatePub() {
        return datePub;
    }

    public void setDatePub(LocalDate datePub) {
        this.datePub = datePub;
    }

    public String getEditeur() {
        return editeur;
    }

    public void setEditeur(String editeur) {
        this.editeur = editeur;
    }

    // Constructeur et methodes
    public Journal(int id, String titre, int nbexemple , LocalDate datePub, String editeur) {
        super(titre, id, nbexemple,TypeEmprunt.CONSULTATION); // Consultation uniquement
        this.datePub = datePub;
        this.editeur = editeur;
    }
}