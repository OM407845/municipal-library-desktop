package DAO.documents;

import documents.Livre;
import DAO.DBConnection;
import enums.TypeEmprunt;

import java.sql.*;

public class LivreDAO {
    // fonction d'insertion
    public void insert(Livre livre) {
        //Étape 1 – Préparation de la requête d’insertion dans documents
        String insertDocSQL = "INSERT INTO documents (titre, type_document, nombre_exemplaires, type_emprunt, empruntable) VALUES (?, 'LIVRE', ?, ?, ?) RETURNING id";
        //Étape 2 – Requête pour insérer dans livres
        String insertLivreSQL = "INSERT INTO livres (document_id, auteur, isbn, duree_max_jours, frais_location, fortement_demande) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Insert into documents
            PreparedStatement psDoc = conn.prepareStatement(insertDocSQL);
            psDoc.setString(1, livre.getTitre());
            psDoc.setInt(2, livre.getNombreExemplaires());
            psDoc.setString(3, livre.getTypeEmprunt().name());
            psDoc.setBoolean(4, livre.isEmpruntable());

            ResultSet rs = psDoc.executeQuery();
            int docId = -1;
            if (rs.next()) {
                docId = rs.getInt(1);
            }

            // 2. Insert into livres
            PreparedStatement psLivre = conn.prepareStatement(insertLivreSQL);
            psLivre.setInt(1, docId);
            psLivre.setString(2, livre.getAuteur());
            psLivre.setString(3, livre.getIsbn());
            psLivre.setInt(4, livre.getDureeMaxEnJours());
            psLivre.setDouble(5, livre.getFraisLocation());
            psLivre.setBoolean(6, livre.isFortementDemande());

            psLivre.executeUpdate();

            conn.commit();
            System.out.println("Livre inséré avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'insertion du livre.");
        }
    }
    //suppression d'un livre
    public void delete(int id) {
        String deleteLivreSQL = "DELETE FROM livres WHERE document_id = ?";
        String deleteDocSQL = "DELETE FROM documents WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement psLivre = conn.prepareStatement(deleteLivreSQL);
            psLivre.setInt(1, id);
            psLivre.executeUpdate();

            PreparedStatement psDoc = conn.prepareStatement(deleteDocSQL);
            psDoc.setInt(1, id);
            psDoc.executeUpdate();

            conn.commit();
            System.out.println("Livre supprimé.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la suppression.");
        }
    }
    // recherche d'un livre
    public Livre findById(int id) {
        String sql = "SELECT d.titre, d.nombre_exemplaires, d.type_emprunt, d.empruntable, " +
                "l.auteur, l.isbn, l.duree_max_jours, l.frais_location, l.fortement_demande " +
                "FROM documents d JOIN livres l ON d.id = l.document_id WHERE d.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Livre livre = new Livre(
                        rs.getString("titre"),
                        id,
                        rs.getInt("nombre_exemplaires"),
                        rs.getString("auteur"),
                        rs.getString("isbn"),
                        rs.getBoolean("fortement_demande")
                );

                // AJOUTER CETTE LIGNE :
                livre.setTypeDocument("LIVRE");

                return livre;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Recherche d’un livre par son titre
    public Livre findByTitre(String titre) {
        String sql = "SELECT d.id, d.titre, d.nombre_exemplaires, d.type_emprunt, d.empruntable, " +
                "l.auteur, l.isbn, l.duree_max_jours, l.frais_location, l.fortement_demande " +
                "FROM documents d JOIN livres l ON d.id = l.document_id WHERE d.titre = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, titre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Livre(
                        rs.getString("titre"),
                        rs.getInt("id"),
                        rs.getInt("nombre_exemplaires"),
                        rs.getString("auteur"),
                        rs.getString("isbn"),
                        rs.getBoolean("fortement_demande")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
