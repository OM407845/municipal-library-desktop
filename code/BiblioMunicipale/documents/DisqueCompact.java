package documents;

import enums.TypeEmprunt;

public class DisqueCompact extends Documents {
    private String artiste;
    private String genre;
    private int dureeMaxEnJours;
    private double fraisLocation;
    public String getArtiste() {
        return artiste;
    }

    public void setArtiste(String artiste) {
        this.artiste = artiste;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDureeMaxEnJours() {
        return dureeMaxEnJours;
    }

    public void setDureeMaxEnJours(int dureeMaxEnJours) {
        this.dureeMaxEnJours = dureeMaxEnJours;
    }

    public double getFraisLocation() {
        return fraisLocation;
    }

    public void setFraisLocation(double fraisLocation) {
        this.fraisLocation = fraisLocation;
    }

    // constructeur et methode ,
    public DisqueCompact(String titre, int id, int nombreExemplaires,String artiste , String genre){
        super(titre, id, nombreExemplaires, TypeEmprunt.LOCATION);
        this.artiste = artiste ;
        this.genre = genre ;
        this.dureeMaxEnJours = 7 ;
        this.fraisLocation = 40.0 ;
    }




}