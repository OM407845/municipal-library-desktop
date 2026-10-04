package DAO.documents;

import documents.Periodique;
import DAO.DBConnection;

import java.sql.*;
import java.time.LocalDate;

public class PeriodiqueDAO {

    public void insert(Periodique periodique) {
        String insertDocSQL = "INSERT INTO documents (titre, type_document, nombre_exemplaires, type_emprunt, empruntable) VALUES (?, 'PERIODIQUE', ?, ?, ?) RETURNING id";
        String insertSQL = "INSERT INTO periodiques (document_id, numero, date_parution, editeur) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement psDoc = conn.prepareStatement(insertDocSQL);
            psDoc.setString(1, periodique.getTitre());
            psDoc.setInt(2, periodique.getNombreExemplaires());
            psDoc.setString(3, periodique.getTypeEmprunt().name());
            psDoc.setBoolean(4, periodique.isEmpruntable());

            ResultSet rs = psDoc.executeQuery();
            int docId = -1;
            if (rs.next()) {
                docId = rs.getInt(1);
            }


            PreparedStatement ps = conn.prepareStatement(insertSQL);
            ps.setInt(1, docId);
            ps.setInt(2, periodique.getNumero());
            ps.setDate(3, Date.valueOf(periodique.getDatePub()));
            ps.setString(4, periodique.getediteur());

            ps.executeUpdate();
            conn.commit();
            System.out.println("Périodique inséré.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'insertion du Périodique.");

        }
    }

    public Periodique findById(int id) {
        String sql = "SELECT d.titre, d.nombre_exemplaires, d.type_emprunt, d.empruntable, " +
                "p.numero, p.date_parution, p.editeur " +
                "FROM documents d JOIN periodiques p ON d.id = p.document_id WHERE d.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Periodique periodique = new Periodique(
                        id,
                        rs.getString("titre"),
                        rs.getInt("nombre_exemplaires"),
                        rs.getString("editeur"),
                        rs.getInt("numero"),
                        rs.getDate("date_parution").toLocalDate()
                );

                // AJOUTER CETTE LIGNE :
                periodique.setTypeDocument("PERIODIQUE");

                return periodique;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(int id) {
        String deletePeriodiqueSQL = "DELETE FROM periodiques WHERE document_id = ?";
        String deleteDocSQL = "DELETE FROM documents WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement psPer = conn.prepareStatement(deletePeriodiqueSQL);
            psPer.setInt(1, id);
            psPer.executeUpdate();

            PreparedStatement psDoc = conn.prepareStatement(deleteDocSQL);
            psDoc.setInt(1, id);
            psDoc.executeUpdate();

            conn.commit();
            System.out.println("Periodique supprimé.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la suppression.");
        }
    }
}

