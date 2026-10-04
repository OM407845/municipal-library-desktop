package DAO.transactions;

import transactions.Reservation;
import DAO.DBConnection;

import java.sql.*;
import java.time.LocalDate;

public class ReservationDAO {

    public void enregistrerReservation(Reservation r) {
        String sql = "INSERT INTO reservations (id_document, id_client, date_reservation, statut) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, r.getDocument().getId());
            ps.setInt(2, r.getClient().getId());
            ps.setDate(3, Date.valueOf(r.getDateReservation()));
            ps.setString(4, r.getStatut().name());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int countReservationsEtEmpruntsActifs(int idClient) {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM emprunts WHERE id_client = ? AND statut = 'EN_COURS') + " +
                "(SELECT COUNT(*) FROM reservations WHERE id_client = ? AND statut = 'EN_ATTENTE')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idClient);
            ps.setInt(2, idClient);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
