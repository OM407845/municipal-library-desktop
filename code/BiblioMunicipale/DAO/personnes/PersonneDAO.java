package DAO.personnes;
import DAO.*;
import Services.*;
import personnes.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonneDAO {

    // 🔹 1. Authentification (login)
    public Personne login(String email, String motDePasse) {
        String sql = "SELECT * FROM personnes WHERE email = ? AND mot_de_passe = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, motDePasse);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String type = rs.getString("type_personne");

                switch (type.toUpperCase()) {
                    case "ADMIN":
                        return new Administrateur(id, nom, prenom, email, motDePasse);
                    case "BIBLIO":
                        return new Bibliothecaire(id, nom, prenom, email, motDePasse);
                    case "PREPOSE":
                        return new Prepose(id, nom, prenom, email, motDePasse);
                    case "CLIENT":
                        Client client = new Client(id, nom, prenom, email, motDePasse);
                        // charger carte client ?
                        return client;
                    default:
                        return null;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 🔹 2. Création d’un compte (insertion)
    public boolean creerCompte(Personne p) {
        String sql = "INSERT INTO personnes (nom, prenom, email, mot_de_passe, type_personne) VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getMotDePasse());
            ps.setString(5, p.getTypePersonne());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idPersonne = rs.getInt("id");
                p.setId(idPersonne); // injecte l’ID généré dans l’objet

                if (p instanceof Client client) {
                    // Auto-création carte client
                    CarteClient carte = new CarteClient(client.isEstResident());
                    // ou client.isEstResident()
                    creerCarteClient(carte, idPersonne, conn);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void creerCarteClient(CarteClient carte, int idPersonne, Connection conn) throws SQLException {
        String sqlCarte = "INSERT INTO carte_client (numero_code_barres, date_emission, date_expiration, frais_inscription, est_resident, id_personne) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sqlCarte)) {
            ps.setString(1, carte.getNumeroCodeBarres());
            ps.setDate(2, Date.valueOf(carte.getDateEmission()));
            ps.setDate(3, Date.valueOf(carte.getDateExpiration()));
            ps.setDouble(4, carte.getFraisInscription());
            ps.setBoolean(5, carte.isEstResident());
            ps.setInt(6, idPersonne);
            ps.executeUpdate();
        }
    }


    public boolean supprimerPersonne(int idPersonne) {
        try (Connection conn = DBConnection.getConnection()) {
            // Vérifie si c'est un client
            String typeQuery = "SELECT type_personne FROM personnes WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(typeQuery)) {
                ps.setInt(1, idPersonne);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String type = rs.getString("type_personne");
                    if ("CLIENT".equalsIgnoreCase(type)) {
                        // Supprimer carte client d'abord
                        try (PreparedStatement psCarte = conn.prepareStatement("DELETE FROM carte_client WHERE id_personne = ?")) {
                            psCarte.setInt(1, idPersonne);
                            psCarte.executeUpdate();
                        }
                    }
                }
            }

            // Supprimer la personne
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM personnes WHERE id = ?")) {
                ps.setInt(1, idPersonne);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false ;
        }
        return true;
    }


    public List<Personne> listerPersonnes() {
        List<Personne> personnes = new ArrayList<>();
        String sql = "SELECT * FROM personnes";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String email = rs.getString("email");
                String motDePasse = rs.getString("mot_de_passe");
                String type = rs.getString("type_personne");

                Personne p = switch (type.toUpperCase()) {
                    case "ADMIN" -> new Administrateur(id, nom, prenom, email, motDePasse);
                    case "BIBLIO" -> new Bibliothecaire(id, nom, prenom, email, motDePasse);
                    case "PREPOSE" -> new Prepose(id, nom, prenom, email, motDePasse);
                    case "CLIENT" -> new Client(id, nom, prenom, email, motDePasse);
                    default -> null;
                };
                if (p != null) personnes.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return personnes;
    }




    // Recherche d'un client par son numéro de carte
    public Client rechercherClientParCarte(String numeroCodeBarres) {
        String sql = "SELECT p.* FROM personnes p " +
                "JOIN carte_client cc ON p.id = cc.id_personne " +
                "WHERE cc.numero_code_barres = ? AND p.type_personne = 'CLIENT'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numeroCodeBarres);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String email = rs.getString("email");
                String motDePasse = rs.getString("mot_de_passe");

                return new Client(id, nom, prenom, email, motDePasse);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


}
