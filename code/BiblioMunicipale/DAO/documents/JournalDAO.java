package DAO.documents;

import documents.Journal;
import DAO.DBConnection;

import java.sql.*;

public class JournalDAO {

    public void insert(Journal journal) {
        String insertDocSQL = "INSERT INTO documents (titre, type_document, nombre_exemplaires, type_emprunt, empruntable) VALUES (?, 'JOURNAL', ?, ?, ?) RETURNING id";
        String insertSQL = "INSERT INTO journaux (document_id, date_publication, editeur) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement psDoc = conn.prepareStatement(insertDocSQL);
            psDoc.setString(1, journal.getTitre());
            psDoc.setInt(2, journal.getNombreExemplaires());
            psDoc.setString(3, journal.getTypeEmprunt().name());
            psDoc.setBoolean(4, journal.isEmpruntable());

            ResultSet rs = psDoc.executeQuery();
            int docId = -1;
            if (rs.next()) {
                docId = rs.getInt(1);
            }

            PreparedStatement ps = conn.prepareStatement(insertSQL);
            ps.setInt(1, docId);
            ps.setDate(2, Date.valueOf(journal.getDatePub()));
            ps.setString(3, journal.getEditeur());

            ps.executeUpdate();
            conn.commit();
            System.out.println("✅ Journal inséré.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'insertion du Journal.");
        }
    }

    public Journal findById(int id) {
        String sql = "SELECT d.titre, d.nombre_exemplaires, d.type_emprunt, d.empruntable, " +
                "j.date_publication, j.editeur " +
                "FROM documents d JOIN journaux j ON d.id = j.document_id WHERE d.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Journal journal = new Journal(
                        id,
                        rs.getString("titre"),
                        rs.getInt("nombre_exemplaires"),
                        rs.getDate("date_publication").toLocalDate(),
                        rs.getString("editeur")
                );

                // AJOUTER CETTE LIGNE :
                journal.setTypeDocument("JOURNAL");

                return journal;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(int id) {
        String deleteJournalSql = "DELETE FROM journaux WHERE document_id = ?" ;
        String deleteDocSQL = "DELETE FROM documents WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement psJo = conn.prepareStatement(deleteJournalSql);
            psJo.setInt(1,id);
            psJo.executeUpdate();

            PreparedStatement psDoc = conn.prepareStatement(deleteDocSQL);
            psDoc.setInt(1, id);
            psDoc.executeUpdate();


            conn.commit();
                System.out.println("Journal supprimé.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la suppression.");

        }
    }
}

