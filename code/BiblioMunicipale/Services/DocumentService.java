package Services;
import java.sql.*;

import DAO.DBConnection;
import DAO.documents.*;
import documents.*;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class DocumentService {
    private DocumentDAO documentDAO = new DocumentDAO();
    private LivreDAO livreDAO = new LivreDAO();
    private JournalDAO journalDAO = new JournalDAO();
    private DisqueCompactDAO disqueDAO = new DisqueCompactDAO();
    private PeriodiqueDAO periodiqueDAO = new PeriodiqueDAO();

    public List<Documents> getAllDocuments() {
        return documentDAO.getAllDocuments();
    }

    public boolean addDocument(Documents document) {
        try {
            // Logique pour ajouter selon le type
            if (document instanceof Livre) {
                livreDAO.insert((Livre) document);
            } else if (document instanceof Journal) {
                journalDAO.insert((Journal) document);
            } else if (document instanceof DisqueCompact) {
                disqueDAO.insert((DisqueCompact) document);
            } else if (document instanceof Periodique) {
                periodiqueDAO.insert((Periodique) document);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDocument(Documents document) {
        // Implémentez la logique de mise à jour
        return true;
    }

    public boolean deleteDocument(int id) {
        try {
            String type = DocumentDAO.getTypeDocumentById(id);
            if (type == null) return false;

            switch (type) {
                case "LIVRE":
                    livreDAO.delete(id);
                    break;
                case "JOURNAL":
                    journalDAO.delete(id);
                    break;
                case "DISQUE":
                    disqueDAO.delete(id);
                    break;
                case "PERIODIQUE":
                    periodiqueDAO.delete(id);
                    break;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Documents> searchDocuments(String critere, String terme) {
        List<Documents> documents = new ArrayList<>();
        String sql = "";

        switch (critere) {
            case "Titre":
                sql = "SELECT id, titre, type_document, nombre_exemplaires, type_emprunt, empruntable " +
                        "FROM documents WHERE LOWER(titre) LIKE LOWER(?)";
                break;

            case "Auteur":
                sql = "SELECT DISTINCT d.id, d.titre, d.type_document, d.nombre_exemplaires, d.type_emprunt, d.empruntable " +
                        "FROM documents d " +
                        "LEFT JOIN livres l ON d.id = l.document_id " +
                        "LEFT JOIN disques_compacts dc ON d.id = dc.document_id " +
                        "LEFT JOIN journaux j ON d.id = j.document_id " +
                        "LEFT JOIN periodiques p ON d.id = p.document_id " +
                        "WHERE LOWER(l.auteur) LIKE LOWER(?) " +
                        "OR LOWER(dc.artiste) LIKE LOWER(?) " +
                        "OR LOWER(j.editeur) LIKE LOWER(?) " +
                        "OR LOWER(p.editeur) LIKE LOWER(?)";
                break;

            case "Code-barre":
                sql = "SELECT d.id, d.titre, d.type_document, d.nombre_exemplaires, d.type_emprunt, d.empruntable " +
                        "FROM documents d " +
                        "JOIN livres l ON d.id = l.document_id " +
                        "WHERE LOWER(l.isbn) LIKE LOWER(?)";
                break;

            default:
                return getAllDocuments();
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchTerm = "%" + terme + "%";

            if (critere.equals("Auteur")) {
                // Pour la recherche d'auteur, définir le paramètre pour chaque table
                ps.setString(1, searchTerm);
                ps.setString(2, searchTerm);
                ps.setString(3, searchTerm);
                ps.setString(4, searchTerm);
            } else {
                ps.setString(1, searchTerm);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String typeDocument = rs.getString("type_document");
                int id = rs.getInt("id");

                Documents doc = null;

                // Créer le bon type d'objet
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
                    documents.add(doc);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return documents;
    }
}