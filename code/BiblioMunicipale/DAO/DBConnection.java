package DAO;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Charger le driver (optionnel avec JDBC 4+ mais conseillé)
            Class.forName("org.postgresql.Driver");
            System.out.println("Le pilote a bien chargee");

        } catch (ClassNotFoundException e) {
            System.out.println("Pilote  non charge !");
        }
        String url = "jdbc:postgresql://localhost:5432/BibliothequeMunicipale";
        String user = "postgres";
        String password = ""; // TODO: Remplacez par votre mot de passe avant de lancer

        try {
            conn = DriverManager.getConnection(url,user,password);
            System.out.println("connecte");
        } catch (SQLException e) {
            System.out.println("non connecte");
        }
        return conn;

    }
}











