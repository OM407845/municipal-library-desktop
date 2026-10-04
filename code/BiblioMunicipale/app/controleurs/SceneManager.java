package app.controleurs;

import app.controleurs.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import personnes.Personne;

import java.io.IOException;

/**
 * Gestionnaire de scènes pour faciliter les transitions entre les différentes interfaces
 * tout en maintenant une taille cohérente.
 */
public class SceneManager {
    
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    
    /**
     * Change la scène actuelle tout en préservant la taille de la fenêtre
     * 
     * @param currentStage La fenêtre actuelle
     * @param fxmlPath Le chemin vers le fichier FXML à charger
     * @param title Le nouveau titre de la fenêtre
     * @param user L'utilisateur connecté (peut être null)
     * @return Le contrôleur de la nouvelle scène
     */
    public static Object changeScene(Stage currentStage, String fxmlPath, String title, Personne user) {
        try {
            // Récupérer la taille actuelle de la fenêtre
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            // Créer une nouvelle scène
            Scene scene = new Scene(root);
            
            // Ajouter le CSS
            String cssPath = SceneManager.class.getResource("/app/css/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            // Appliquer la nouvelle scène
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            
            // Conserver la même taille
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            
            // Passer l'utilisateur au contrôleur si nécessaire
            Object controller = loader.getController();
            if (user != null && controller instanceof BaseController) {
                ((BaseController) controller).setCurrentUser(user);
            }
            
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Crée une nouvelle fenêtre avec la taille standard
     * 
     * @param fxmlPath Le chemin vers le fichier FXML à charger
     * @param title Le titre de la fenêtre
     * @return La nouvelle fenêtre
     */
    public static Stage createStage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // Ajouter le CSS
            String cssPath = SceneManager.class.getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMinWidth(DEFAULT_WIDTH);
            stage.setMinHeight(DEFAULT_HEIGHT);
            
            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
