package DAO.transactions;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import DAO.DBConnection;
import transactions.Emprunt;
import documents.Documents;
import personnes.Client;
import enums.StatutEmprunt;

public class EmpruntDAO {

    public void enregistrerEmprunt(Emprunt emprunt) {
        String sql = "INSERT INTO emprunts (id_document, id_client, date_emprunt, date_retour_supposee, statut, amende) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, emprunt.getDoc().getId());
            ps.setInt(2, emprunt.getClient().getId());
            ps.setDate(3, Date.valueOf(emprunt.getDateEmprunt()));
            ps.setDate(4, Date.valueOf(emprunt.getDateRetourSupposee()));
            ps.setString(5, emprunt.getStatut().name());
            ps.setDouble(6, emprunt.getAmende());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void enregistrerRetour(int idEmprunt, LocalDate dateRetour, double amende, StatutEmprunt statut) {
        String sql = "UPDATE emprunts SET date_retour_reel = ?, amende = ?, statut = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(dateRetour));
            ps.setDouble(2, amende);
            ps.setString(3, statut.name());
            ps.setInt(4, idEmprunt);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int countEmpruntsActifsClient(int idClient) {
        String sql = "SELECT COUNT(*) FROM emprunts WHERE id_client = ? AND statut = 'EN_COURS'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public List<Emprunt> getEmpruntsEnCours() {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT * FROM emprunts WHERE statut = 'EN_COURS'";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Créer les objets Emprunt avec les données de la BD
                // Vous devrez récupérer les objets Documents et Client associés
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emprunts;
    }

    public int countEmpruntsActifsDocument(int idDocument) {
        String sql = "SELECT COUNT(*) FROM emprunts WHERE id_document = ? AND statut = 'EN_COURS'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idDocument);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }



}
