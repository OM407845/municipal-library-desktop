package app.controleurs;

import personnes.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public abstract class BaseController {
    @FXML protected Label userInfoLabel;
    protected Personne currentUser;

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (userInfoLabel != null) {
            userInfoLabel.setText("Connecté: " + user.getNom() + " " + user.getPrenom());
        }
    }

    @FXML
    protected void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/interfaces/Login.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/app/css/style.css").toExternalForm());

            Stage stage = (Stage) userInfoLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion - Bibliothèque");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
