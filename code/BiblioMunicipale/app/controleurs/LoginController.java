package app.controleurs;

import DAO.personnes.PersonneDAO;
import app.controleurs.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import personnes.Personne;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private PersonneDAO userService = new PersonneDAO();

    @FXML
    private void initialize() {
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez saisir vos identifiants");
            return;
        }

        Personne user = userService.login(username,password);
        if (user != null) {
            openMainInterface(user);
        } else {
            showError("Identifiants incorrects");
        }
    }

    private void openMainInterface(Personne user) {
        try {
            String fxmlFile = switch (user.getTypePersonne()) {
                case "ADMIN" -> "/app/interfaces/AdminDashboard.fxml";
                case "BIBLIO" -> "/app/interfaces/BibliothecaireDashboard.fxml";
                case "PREPOSE" -> "/app/interfaces/PreposeDashboard.fxml";
                case "CLIENT" -> "/app/interfaces/ClientDashboard.fxml";
                default -> "/app/interfaces/Login.fxml";
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/app/css/style.css").toExternalForm());

            if (loader.getController() instanceof BaseController) {
                ((BaseController) loader.getController()).setCurrentUser(user);
          //  } else if (loader.getController() instanceof ClientController) {
            //    ((ClientController) loader.getController()).setCurrentUser(user);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Bibliothèque - " + user.getTypePersonne());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
