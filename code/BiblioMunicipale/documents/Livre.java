package documents;

import enums.TypeEmprunt;

public class Livre extends Documents {
    private String auteur;
    private String isbn;
    private int dureeMaxEnJours;
    private double fraisLocation;
    private boolean fortementDemande;




    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getDureeMaxEnJours() {
        return dureeMaxEnJours;
    }

    public void setDureeMaxEnJours(int dureeenjr) {
        this.dureeMaxEnJours = dureeenjr ;
    }

    public double getFraisLocation() {
        return fraisLocation;
    }

    public void setFraisLocation(double fraisLocation) {
            this.fraisLocation = fraisLocation ;
    }
    public boolean isFortementDemande() {
        return fortementDemande;
    }


    // constucteurs et methode

    public Livre(String titre, int id, int nombreExemplaires,String auteur,String isbn,boolean fortementDemande
    ){

        super(titre,id,nombreExemplaires,fortementDemande ? TypeEmprunt.LOCATION : TypeEmprunt.PRET);
        this.auteur = auteur ;
        this.isbn = isbn ;
        this.dureeMaxEnJours = fortementDemande ? 7 : 21; // Règle métier
        this.fraisLocation = (this.getTypeEmprunt() == TypeEmprunt.LOCATION) ? 50.0 : 0.0;
    }
}