package documents;

import enums.TypeEmprunt;

public class Documents {
    protected String titre;
    protected int id;
    protected int nombreExemplaires;
    protected int exemplairesDisponibles;
    protected TypeEmprunt typeEmprunt;
    protected boolean empruntable;
    private String typeDocument;

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNombreExemplaires() {
        return nombreExemplaires;
    }

    public void setNombreExemplaires(int nombreExemplaires) {
        this.nombreExemplaires = nombreExemplaires;
    }

    public TypeEmprunt getTypeEmprunt() {
        return typeEmprunt;
    }

    public void setTypeEmprunt(TypeEmprunt typeEmprunt) {
        this.typeEmprunt = typeEmprunt;
    }

    public boolean isEmpruntable() {
        return empruntable;
    }

    public void setEmpruntable(boolean empruntable) {
        this.empruntable = empruntable;
    }
    public String getAuteur() {
        // Retourner une valeur par défaut ou gérer selon le type de document
        if (this instanceof Livre) {
            return ((Livre) this).getAuteur();
        } else if (this instanceof DisqueCompact) {
            return ((DisqueCompact) this).getArtiste();
        } else if (this instanceof Journal) {
            return ((Journal) this).getEditeur();
        } else if (this instanceof Periodique) {
            return ((Periodique) this).getediteur();
        }
        return "Non défini";
    }
    public int getExemplairesDisponibles() {
        return exemplairesDisponibles;
    }

    public void setExemplairesDisponibles(int exemplairesDisponibles) {
        this.exemplairesDisponibles = exemplairesDisponibles;
    }

    // Méthode utilitaire pour vérifier la disponibilité
    public boolean isDisponible() {
        return exemplairesDisponibles > 0 && empruntable;
    }
    // constructeurs et methode

    //constructeurs
    public Documents(String titre, int id, int nombreExemplaires, TypeEmprunt typeEmprunt) {
        this.titre = titre;
        this.id = id;
        this.nombreExemplaires = nombreExemplaires;
        this.exemplairesDisponibles = nombreExemplaires; // Par défaut, tous sont disponibles
        this.typeEmprunt = typeEmprunt;

        // Règle métier : Empruntable uniquement si le type n'est pas CONSULTATION
        this.empruntable = (typeEmprunt != TypeEmprunt.CONSULTATION);
    }



}
