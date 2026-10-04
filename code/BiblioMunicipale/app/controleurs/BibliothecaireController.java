package app.controleurs;

import documents.*;
import Services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class BibliothecaireController extends BaseController {

    @FXML private TableView<Documents> documentsTable;
    @FXML private TableColumn<Documents, String> titreColumn;
    @FXML private TableColumn<Documents, String> auteurColumn;
    @FXML private TableColumn<Documents, String> typeColumn;
    @FXML private TableColumn<Documents, Integer> exemplairesColumn;

    @FXML private TextField codeBarreField;
    @FXML private TextField titreField;
    @FXML private TextField auteurField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField exemplairesField;
    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> critereRechercheCombo;

    private DocumentService documentService = new DocumentService();
    private ObservableList<Documents> documentsList = FXCollections.observableArrayList();
    private Documents selectedDocument = null;

    @FXML
    private void initialize() {
        // Assurez-vous que ces noms correspondent aux getters dans votre classe Documents
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        auteurColumn.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeDocument"));
        exemplairesColumn.setCellValueFactory(new PropertyValueFactory<>("nombreExemplaires"));
        documentsTable.setItems(documentsList);

        typeCombo.setItems(FXCollections.observableArrayList("LIVRE", "JOURNAL", "DISQUE", "PERIODIQUE"));
        critereRechercheCombo.setItems(FXCollections.observableArrayList(
                "Titre", "Auteur", "Code-barre"
        ));
        critereRechercheCombo.setValue("Titre");

        documentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadDocumentInForm(newSelection);
                    }
                }
        );

        loadDocuments();
    }

    private void loadDocuments() {
        documentsList.clear();
        documentsList.addAll(documentService.getAllDocuments());
    }

    private void loadDocumentInForm(Documents document) {
        selectedDocument = document;
        titreField.setText(document.getTitre());
        auteurField.setText(document.getAuteur()); // Utilise votre nouvelle méthode
        typeCombo.setValue(document.getTypeDocument());
        exemplairesField.setText(String.valueOf(document.getNombreExemplaires()));
    }

    @FXML
    private void handleAddDocument() {
        if (!validateForm()) return;

        Documents document = null;
        String type = typeCombo.getValue();

        try {
            int exemplaires = Integer.parseInt(exemplairesField.getText().trim());

            switch (type) {
                case "LIVRE":
                    document = new Livre(
                            titreField.getText().trim(),
                            0, // ID sera généré par la base de données
                            exemplaires,
                            auteurField.getText().trim(),
                            codeBarreField.getText().trim().isEmpty() ?
                                    generateISBN() : codeBarreField.getText().trim(), // Générer ISBN si vide
                            false // fortement_demande par défaut
                    );
                    break;

                case "JOURNAL":
                    document = new Journal(
                            0, // ID sera généré par la base de données
                            titreField.getText().trim(),
                            exemplaires,
                            java.time.LocalDate.now(), // Date de publication actuelle
                            auteurField.getText().trim().isEmpty() ?
                                    "Éditeur inconnu" : auteurField.getText().trim()
                    );
                    break;

                case "DISQUE":
                    document = new DisqueCompact(
                            titreField.getText().trim(),
                            0, // ID sera généré par la base de données
                            exemplaires,
                            auteurField.getText().trim().isEmpty() ?
                                    "Artiste inconnu" : auteurField.getText().trim(),
                            "Musique" // Genre par défaut, vous pourriez ajouter un champ pour cela
                    );
                    break;

                case "PERIODIQUE":
                    document = new Periodique(
                            0, // ID sera généré par la base de données
                            titreField.getText().trim(),
                            exemplaires,
                            auteurField.getText().trim().isEmpty() ?
                                    "Éditeur inconnu" : auteurField.getText().trim(),
                            1, // Numéro par défaut
                            java.time.LocalDate.now() // Date de parution actuelle
                    );
                    break;

                default:
                    showAlert("Erreur", "Type de document non supporté");
                    return;
            }

            if (documentService.addDocument(document)) {
                clearForm();
                loadDocuments();
                showAlert("Succès", "Document ajouté avec succès");
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout du document");
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le nombre d'exemplaires doit être un nombre valide");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    // Méthode utilitaire pour générer un ISBN simple
    private String generateISBN() {
        return "978" + System.currentTimeMillis() % 1000000000L;
    }

    @FXML
    private void handleUpdateDocument() {
        if (selectedDocument == null) {
            showAlert("Erreur", "Sélectionnez un document");
            return;
        }

        // Mettre à jour les propriétés du document sélectionné
        selectedDocument.setTitre(titreField.getText().trim());
        // Autres propriétés à mettre à jour

        try {
            int exemplaires = Integer.parseInt(exemplairesField.getText().trim());
            selectedDocument.setNombreExemplaires(exemplaires);
        } catch (NumberFormatException e) {
            // Garder la valeur actuelle
        }

        if (documentService.updateDocument(selectedDocument)) {
            clearForm();
            loadDocuments();
            showAlert("Succès", "Document modifié");
        }
    }

    @FXML
    private void handleDeleteDocument() {
        if (selectedDocument == null) {
            showAlert("Erreur", "Sélectionnez un document");
            return;
        }

        if (documentService.deleteDocument(selectedDocument.getId())) {
            clearForm();
            loadDocuments();
            showAlert("Succès", "Document supprimé");
        }
    }

    @FXML
    private void handleSearch() {
        String critere = critereRechercheCombo.getValue();
        String terme = rechercheField.getText().trim();

        if (terme.isEmpty()) {
            loadDocuments();
        } else {
            documentsList.clear();
            documentsList.addAll(documentService.searchDocuments(critere, terme));
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private boolean validateForm() {
        return !titreField.getText().trim().isEmpty() &&
                typeCombo.getValue() != null;
    }

    private void clearForm() {
        selectedDocument = null;
        codeBarreField.clear();
        titreField.clear();
        auteurField.clear();
        typeCombo.setValue(null);
        exemplairesField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}