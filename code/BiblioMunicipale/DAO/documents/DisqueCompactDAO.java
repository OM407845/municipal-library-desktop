package DAO.documents;
import DAO.DBConnection;
import documents.DisqueCompact;
import DAO.DBConnection;
import enums.TypeEmprunt;
import java.sql.*;


public class DisqueCompactDAO {

    public void insert(DisqueCompact disque) {
        String insertDocSQL = "INSERT INTO documents (titre, type_document, nombre_exemplaires, type_emprunt, empruntable) VALUES (?, 'DISQUE', ?, ?, ?) RETURNING id";
        String insertSQL = "INSERT INTO disques_compacts (document_id, artiste, genre, duree_max_jours, frais_location) VALUES (?, ?, ?, 7, 40.0)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement psDoc = conn.prepareStatement(insertDocSQL);
            psDoc.setString(1, disque.getTitre());
            psDoc.setInt(2, disque.getNombreExemplaires());
            psDoc.setString(3, disque.getTypeEmprunt().name());
            psDoc.setBoolean(4, disque.isEmpruntable());

            ResultSet rs = psDoc.executeQuery();
            int docId = -1;
            if (rs.next()) {
                docId = rs.getInt(1);
            }

            PreparedStatement ps = conn.prepareStatement(insertSQL);
            ps.setInt(1, docId);
            ps.setString(2, disque.getArtiste());
            ps.setString(3, disque.getGenre());

            ps.executeUpdate();
            conn.commit();
            System.out.println("Disque inséré.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'insertion du Disque.");

        }
    }

    public DisqueCompact findById(int id) {
        String sql = "SELECT d.titre, d.nombre_exemplaires, d.type_emprunt, d.empruntable, " +
                "dc.artiste, dc.genre, dc.duree_max_jours, dc.frais_location " +
                "FROM documents d JOIN disques_compacts dc ON d.id = dc.document_id WHERE d.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                DisqueCompact disque = new DisqueCompact(
                        rs.getString("titre"),
                        id,
                        rs.getInt("nombre_exemplaires"),
                        rs.getString("artiste"),
                        rs.getString("genre")
                );

                // AJOUTER CETTE LIGNE :
                disque.setTypeDocument("DISQUE");

                return disque;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement psDis = conn.prepareStatement("DELETE FROM disques_compacts WHERE document_id = ?");
            PreparedStatement psDoc = conn.prepareStatement("DELETE FROM documents WHERE id = ?");

            psDis.setInt(1,id);
            psDis.executeUpdate() ;

            psDoc.setInt(1,id);
            psDoc.executeUpdate() ;


            conn.commit();
            System.out.println("Disque supprimé.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la suppression.");
        }
    }
}
