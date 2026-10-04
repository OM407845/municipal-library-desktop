package documents;
import enums.TypeEmprunt;

import java.time.LocalDate ;
public class Periodique extends Documents {
    private String editeur;      // Ex: "Elsevier", "National Geographic Society"
    private int numero;          // Ex: 456 (numéro de l'édition)
    private LocalDate dateParution; // Date de publication (ex: 2024-01-15)

    public String getediteur() {
        return editeur;
    }

    public void setEditeur(String editeur) {
        this.editeur = editeur;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
    public LocalDate getDatePub() {
        return dateParution;
    }

    public void setDatePub(LocalDate datePar) {
        this.dateParution = datePar;
    }

    //Constructeurs et methodes

    public Periodique(int id, String titre,int nbexemple  , String editeur, int numero,
                      LocalDate dateParution) {
        super(titre, id, nbexemple , TypeEmprunt.CONSULTATION); // Consultation forcée
        this.editeur = editeur;
        this.numero = numero;
        this.dateParution = dateParution;
    }
}