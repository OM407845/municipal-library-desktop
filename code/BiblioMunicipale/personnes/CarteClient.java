package personnes;
import java.time.*;
public class CarteClient {
    private String CodeBarres;
    private java.time.LocalDate dateEmission;
    private java.time.LocalDate dateExpiration;
    private double fraisInscription;
    private boolean estResident;
    private int clientId; // important pour DAO


    public String getNumeroCodeBarres() {
        return CodeBarres;
    }

    public void setNumeroCodeBarres(String numeroCodeBarres) {
        this.CodeBarres = numeroCodeBarres;
    }

    public java.time.LocalDate getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(java.time.LocalDate dateEmission) {
        this.dateEmission = dateEmission;
    }

    public java.time.LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(java.time.LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public double getFraisInscription() {
        return fraisInscription;
    }

    public void setFraisInscription(double fraisInscription) {
        this.fraisInscription = fraisInscription;
    }

    public boolean isEstResident() {
        return estResident;
    }

    public void setEstResident(boolean estResident) {
        this.estResident = estResident;
    }

    public int getClientId() { return clientId; }

    public void setClientId(int clientId) { this.clientId = clientId; }



    // constructeur et methode

    public CarteClient(boolean estResidant) {
        this.estResident = estResidant;
        this.CodeBarres = genererCodeBarres();
        this.dateEmission = LocalDate.now(); // Date de création
        this.dateExpiration = dateEmission.plusYears(1);
        this.fraisInscription = estResidant ? 0.0 : 100.0;
    }
    private String genererCodeBarres() {
        return "CLT" + System.currentTimeMillis(); // simple exemple
    }

}