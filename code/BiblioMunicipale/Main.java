import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/interfaces/Login.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);
        
        // Ajouter le CSS à la scène
        String cssPath = getClass().getResource("/app/css/style.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        
        // Configuration de la fenêtre principale avec taille fixe
        primaryStage.setTitle("Système de Gestion - Bibliothèque Municipale");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(800);
        primaryStage.setMaximized(false); // Ne pas maximiser pour garder la taille fixe
        primaryStage.setResizable(true);  // Permettre le redimensionnement mais avec une taille minimale
        primaryStage.centerOnScreen();    // Centrer la fenêtre
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
