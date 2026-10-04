package DAO.documents;
import documents.*;
import DAO.DBConnection;
import enums.TypeEmprunt;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class DocumentDAO {

    public static String getTypeDocumentById(int id) {
        String sql = "SELECT type_document FROM documents WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("type_document");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Récupérer tous les documents
    public static List<Documents> getAllDocuments() {
        List<Documents> documents = new ArrayList<>();
        String sql = "SELECT d.id, d.titre, d.type_document, d.nombre_exemplaires, d.type_emprunt, d.empruntable, " +
                "COALESCE(d.nombre_exemplaires - COUNT(e.id), d.nombre_exemplaires) as exemplaires_disponibles " +
                "FROM documents d " +
                "LEFT JOIN emprunts e ON d.id = e.id_document AND e.statut = 'EN_COURS' " +
                "GROUP BY d.id, d.titre, d.type_document, d.nombre_exemplaires, d.type_emprunt, d.empruntable";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String typeDocument = rs.getString("type_document");
                int id = rs.getInt("id");

                Documents doc = null;

                // Créer le bon type d'objet selon type_document
                switch (typeDocument) {
                    case "LIVRE":
                        LivreDAO livreDAO = new LivreDAO();
                        doc = livreDAO.findById(id);
                        break;
                    case "DISQUE":
                        DisqueCompactDAO disqueDAO = new DisqueCompactDAO();
                        doc = disqueDAO.findById(id);
                        break;
                    case "JOURNAL":
                        JournalDAO journalDAO = new JournalDAO();
                        doc = journalDAO.findById(id);
                        break;
                    case "PERIODIQUE":
                        PeriodiqueDAO periodiqueDAO = new PeriodiqueDAO();
                        doc = periodiqueDAO.findById(id);
                        break;
                }

                if (doc != null) {
                    // Mettre à jour les exemplaires disponibles
                    doc.setExemplairesDisponibles(rs.getInt("exemplaires_disponibles"));
                    documents.add(doc);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return documents;
    }
    // Ajouter cette méthode dans votre classe DocumentDAO
    public static Documents getDocumentById(int id) {
        String sql = "SELECT d.id, d.titre, d.type_document, d.nombre_exemplaires, d.type_emprunt, d.empruntable, " +
                "COALESCE(d.nombre_exemplaires - COUNT(e.id), d.nombre_exemplaires) as exemplaires_disponibles " +
                "FROM documents d " +
                "LEFT JOIN emprunts e ON d.id = e.id_document AND e.statut = 'EN_COURS' " +
                "WHERE d.id = ? " +
                "GROUP BY d.id, d.titre, d.type_document, d.nombre_exemplaires, d.type_emprunt, d.empruntable";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String typeDocument = rs.getString("type_document");
                Documents doc = null;

                // Créer le bon type d'objet selon type_document
                switch (typeDocument) {
                    case "LIVRE":
                        LivreDAO livreDAO = new LivreDAO();
                        doc = livreDAO.findById(id);
                        break;
                    case "DISQUE":
                        DisqueCompactDAO disqueDAO = new DisqueCompactDAO();
                        doc = disqueDAO.findById(id);
                        break;
                    case "JOURNAL":
                        JournalDAO journalDAO = new JournalDAO();
                        doc = journalDAO.findById(id);
                        break;
                    case "PERIODIQUE":
                        PeriodiqueDAO periodiqueDAO = new PeriodiqueDAO();
                        doc = periodiqueDAO.findById(id);
                        break;
                }

                if (doc != null) {
                    // Mettre à jour les exemplaires disponibles
                    doc.setExemplairesDisponibles(rs.getInt("exemplaires_disponibles"));
                }

                return doc;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    }

