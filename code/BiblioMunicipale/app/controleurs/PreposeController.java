package app.controleurs;

import DAO.*;
import DAO.documents.DocumentDAO;
import DAO.personnes.PersonneDAO;
import documents.*;
import personnes.*;
import Services.DocumentService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.ResourceBundle;

public class PreposeController extends BaseController implements Initializable {

    // Onglet Prêt/Retour
    @FXML private TextField scanDocumentField;
    @FXML private TextField scanClientField;
    @FXML private Label documentInfoLabel;
    @FXML private Label clientInfoLabel;
    @FXML private Button pretButton;
    @FXML private Button retourButton;

    // Onglet Gestion Amendes
    @FXML private TextField amendeClientField;
    @FXML private TextField amendeMontantField;
    @FXML private Label amendeInfoLabel;

    // Tableau des prêts
    @FXML private TableView<EmpruntInfo> pretsTable;
    @FXML private TableColumn<EmpruntInfo, String> clientColumn;
    @FXML private TableColumn<EmpruntInfo, String> documentColumn;
    @FXML private TableColumn<EmpruntInfo, String> dateEmpruntColumn;
    @FXML private TableColumn<EmpruntInfo, String> dateRetourColumn;
    @FXML private TableColumn<EmpruntInfo, String> statutColumn;
    @FXML private TableColumn<EmpruntInfo, Double> amendeColumn;

    private DocumentService documentService;
    private PersonneDAO personneDAO;
    private DocumentDAO documentDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== DÉBUT INITIALISATION PreposeController ===");

        try {
            // Test de la connexion à la base de données
            testDatabaseConnection();

            // Découvrir la structure des tables
            discoverTableStructure();

            // Initialiser les DAOs
            System.out.println("Initialisation des DAOs...");
            documentService = new DocumentService();
            personneDAO = new PersonneDAO();
            documentDAO = new DocumentDAO();
            System.out.println("DAOs initialisés avec succès");

            // Configurer les colonnes du tableau
            setupTableColumns();

            // Charger les données
            loadPrets();

            // Configurer les event handlers
            setupEventHandlers();

            System.out.println("=== FIN INITIALISATION PreposeController ===");

        } catch (Exception e) {
            System.err.println("ERREUR LORS DE L'INITIALISATION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testDatabaseConnection() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Connexion à la base de données OK");

                // Test simple de requête
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM emprunts");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("✓ Nombre total d'emprunts dans la base: " + count);
                }
                rs.close();
                stmt.close();
            } else {
                System.err.println("✗ Connexion à la base de données ÉCHOUÉE");
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void discoverTableStructure() {
        try {
            Connection conn = DBConnection.getConnection();

            // Découvrir la structure de la table documents
            System.out.println("=== STRUCTURE DE LA TABLE DOCUMENTS ===");
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'documents' ORDER BY ordinal_position"
            );
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                System.out.println("Colonne: " + columnName + " (" + dataType + ")");
            }
            rs.close();
            stmt.close();

            // Découvrir la structure de la table personnes
            System.out.println("=== STRUCTURE DE LA TABLE PERSONNES ===");
            stmt = conn.prepareStatement(
                    "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'personnes' ORDER BY ordinal_position"
            );
            rs = stmt.executeQuery();

            while (rs.next()) {
                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                System.out.println("Colonne: " + columnName + " (" + dataType + ")");
            }
            rs.close();
            stmt.close();

            // Découvrir la structure de la table emprunts
            System.out.println("=== STRUCTURE DE LA TABLE EMPRUNTS ===");
            stmt = conn.prepareStatement(
                    "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'emprunts' ORDER BY ordinal_position"
            );
            rs = stmt.executeQuery();

            while (rs.next()) {
                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                System.out.println("Colonne: " + columnName + " (" + dataType + ")");
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la découverte de la structure: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        if (pretsTable == null) {
            System.err.println("✗ ERREUR: pretsTable est null!");
            return;
        }

        System.out.println("Configuration des colonnes du tableau...");

        try {
            if (clientColumn != null) {
                clientColumn.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
                System.out.println("✓ clientColumn configurée");
            }

            if (documentColumn != null) {
                documentColumn.setCellValueFactory(new PropertyValueFactory<>("titreDocument"));
                System.out.println("✓ documentColumn configurée");
            }

            if (dateEmpruntColumn != null) {
                dateEmpruntColumn.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
                System.out.println("✓ dateEmpruntColumn configurée");
            }

            if (dateRetourColumn != null) {
                dateRetourColumn.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
                System.out.println("✓ dateRetourColumn configurée");
            }

            if (statutColumn != null) {
                statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
                System.out.println("✓ statutColumn configurée");
            }

            if (amendeColumn != null) {
                amendeColumn.setCellValueFactory(new PropertyValueFactory<>("amende"));
                amendeColumn.setCellFactory(column -> new TableCell<EmpruntInfo, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(String.format("%.2f DH", item));
                        }
                    }
                });
                System.out.println("✓ amendeColumn configurée");
            }

            System.out.println("✓ Configuration des colonnes terminée");

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la configuration des colonnes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        if (scanDocumentField != null) {
            scanDocumentField.setOnAction(e -> handleScanDocument());
        }
        if (scanClientField != null) {
            scanClientField.setOnAction(e -> handleScanClient());
        }
    }

    private void loadPrets() {
        if (pretsTable == null) {
            System.err.println("✗ ERREUR: pretsTable est null dans loadPrets()!");
            return;
        }

        System.out.println("=== DÉBUT CHARGEMENT DES PRÊTS ===");

        try {
            // Requête adaptée selon la structure réelle de votre base
            // Je vais utiliser une requête plus simple d'abord
            String query = """
                SELECT e.id, e.date_emprunt, e.date_retour_supposee, e.statut, 
                       COALESCE(e.amende, 0) as amende,
                       p.nom, p.prenom, 
                       d.titre,
                       COALESCE(d.auteur, d.nom_auteur, d.auteur_nom, 'Auteur inconnu') as auteur_document
                FROM emprunts e 
                LEFT JOIN personnes p ON e.id_client = p.id 
                LEFT JOIN documents d ON e.id_document = d.id 
                ORDER BY e.date_emprunt DESC
            """;

            System.out.println("Tentative avec requête adaptée...");

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            ObservableList<EmpruntInfo> emprunts = FXCollections.observableArrayList();
            int compteur = 0;

            while (rs.next()) {
                compteur++;

                String nomClient = rs.getString("nom");
                String prenomClient = rs.getString("prenom");
                String titreDocument = rs.getString("titre");
                String auteurDocument = rs.getString("auteur_document");
                String dateEmprunt = rs.getDate("date_emprunt") != null ? rs.getDate("date_emprunt").toString() : "N/A";
                String dateRetour = rs.getDate("date_retour_supposee") != null ? rs.getDate("date_retour_supposee").toString() : "N/A";
                String statut = rs.getString("statut");
                Double amende = rs.getDouble("amende");

                System.out.println("Emprunt " + compteur + ":");
                System.out.println("  - Client: " + nomClient + " " + prenomClient);
                System.out.println("  - Document: " + titreDocument + " - " + auteurDocument);
                System.out.println("  - Statut: " + statut);

                EmpruntInfo emprunt = new EmpruntInfo(
                        (nomClient != null && prenomClient != null) ? nomClient + " " + prenomClient : "Client inconnu",
                        (titreDocument != null) ? titreDocument + " - " + (auteurDocument != null ? auteurDocument : "Auteur inconnu") : "Document inconnu",
                        dateEmprunt,
                        dateRetour,
                        statut != null ? statut : "INCONNU",
                        amende != null ? amende : 0.0
                );
                emprunts.add(emprunt);
            }

            System.out.println("Nombre total d'emprunts récupérés: " + emprunts.size());

            Platform.runLater(() -> {
                try {
                    pretsTable.setItems(emprunts);
                    pretsTable.refresh();
                    System.out.println("✓ Tableau mis à jour avec " + emprunts.size() + " éléments");
                } catch (Exception e) {
                    System.err.println("✗ Erreur lors de la mise à jour du tableau: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL lors du chargement des prêts: " + e.getMessage());

            // Si la première requête échoue, essayer une requête plus simple
            loadPretsSimple();

        } catch (Exception e) {
            System.err.println("✗ Erreur générale lors du chargement des prêts: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== FIN CHARGEMENT DES PRÊTS ===");
    }

    private void loadPretsSimple() {
        System.out.println("=== TENTATIVE AVEC REQUÊTE SIMPLIFIÉE ===");

        try {
            // Requête très simple sans jointures d'abord
            String simpleQuery = """
                SELECT e.id, e.date_emprunt, e.date_retour_supposee, e.statut, 
                       COALESCE(e.amende, 0) as amende, e.id_client, e.id_document
                FROM emprunts e 
                ORDER BY e.date_emprunt DESC
            """;

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(simpleQuery);
            ResultSet rs = stmt.executeQuery();

            ObservableList<EmpruntInfo> emprunts = FXCollections.observableArrayList();
            int compteur = 0;

            while (rs.next()) {
                compteur++;

                int idClient = rs.getInt("id_client");
                int idDocument = rs.getInt("id_document");
                String dateEmprunt = rs.getDate("date_emprunt") != null ? rs.getDate("date_emprunt").toString() : "N/A";
                String dateRetour = rs.getDate("date_retour_supposee") != null ? rs.getDate("date_retour_supposee").toString() : "N/A";
                String statut = rs.getString("statut");
                Double amende = rs.getDouble("amende");

                EmpruntInfo emprunt = new EmpruntInfo(
                        "Client ID: " + idClient,
                        "Document ID: " + idDocument,
                        dateEmprunt,
                        dateRetour,
                        statut != null ? statut : "INCONNU",
                        amende != null ? amende : 0.0
                );
                emprunts.add(emprunt);
            }

            System.out.println("Nombre d'emprunts avec requête simple: " + emprunts.size());

            Platform.runLater(() -> {
                pretsTable.setItems(emprunts);
                pretsTable.refresh();
                System.out.println("✓ Tableau mis à jour avec requête simple: " + emprunts.size() + " éléments");
            });

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("✗ Même la requête simple échoue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour forcer le rechargement
    @FXML
    private void handleRefreshTable() {
        System.out.println("=== RECHARGEMENT MANUEL DU TABLEAU ===");
        loadPrets();
    }
    @FXML
    private void handleScanDocument() {
        String codeDocument = scanDocumentField.getText().trim();
        if (codeDocument.isEmpty()) {
            documentInfoLabel.setText("Veuillez scanner un document");
            documentInfoLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            int idDocument = Integer.parseInt(codeDocument);
            Documents document = documentDAO.getDocumentById(idDocument);

            if (document != null) {
                boolean disponible = document.getExemplairesDisponibles() > 0;
                String typeEmprunt = getTypeEmprunt(document);

                documentInfoLabel.setText(String.format(
                        "Document: %s - %s (%s) - %s",
                        document.getTitre(),
                        document.getAuteur(),
                        typeEmprunt,
                        disponible ? "Disponible" : "Non disponible"
                ));
                documentInfoLabel.setStyle(disponible ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            } else {
                documentInfoLabel.setText("Document non trouvé");
                documentInfoLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            documentInfoLabel.setText("Code document invalide");
            documentInfoLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            documentInfoLabel.setText("Erreur lors de la recherche du document");
            documentInfoLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleScanClient() {
        String carteClient = scanClientField.getText().trim();
        if (carteClient.isEmpty()) {
            clientInfoLabel.setText("Veuillez scanner une carte client");
            clientInfoLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Client client = personneDAO.rechercherClientParCarte(carteClient);
            if (client != null) {
                double amendesImpayees = calculerTotalAmendes(client.getId());

                if (amendesImpayees > 0) {
                    clientInfoLabel.setText(String.format(
                            "Client: %s %s - AMENDES IMPAYÉES: %.2f DH",
                            client.getNom(), client.getPrenom(), amendesImpayees
                    ));
                    clientInfoLabel.setStyle("-fx-text-fill: red;");
                } else {
                    clientInfoLabel.setText(String.format(
                            "Client: %s %s - Compte en règle",
                            client.getNom(), client.getPrenom()
                    ));
                    clientInfoLabel.setStyle("-fx-text-fill: green;");
                }
            } else {
                clientInfoLabel.setText("Client non trouvé");
                clientInfoLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            clientInfoLabel.setText("Erreur lors de la recherche du client");
            clientInfoLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePret() {
        String codeDocument = scanDocumentField.getText().trim();
        String carteClient = scanClientField.getText().trim();

        if (codeDocument.isEmpty() || carteClient.isEmpty()) {
            showAlert("Erreur", "Veuillez scanner le document et la carte client");
            return;
        }

        try {
            int idDocument = Integer.parseInt(codeDocument);
            Client client = personneDAO.rechercherClientParCarte(carteClient);
            Documents document = documentDAO.getDocumentById(idDocument);

            if (client == null) {
                showAlert("Erreur", "Client non trouvé");
                return;
            }

            if (document == null) {
                showAlert("Erreur", "Document non trouvé");
                return;
            }

            // Vérifier si le document est empruntable
            if (!document.isEmpruntable()) {
                showAlert("Erreur", "Ce document n'est pas empruntable (consultation uniquement)");
                return;
            }

            // Vérifier la disponibilité
            if (document.getExemplairesDisponibles() <= 0) {
                showAlert("Erreur", "Document non disponible");
                return;
            }

            // Vérifier les amendes impayées
            double amendesImpayees = calculerTotalAmendes(client.getId());
            if (amendesImpayees > 0) {
                showAlert("Erreur", String.format(
                        "Le client a des amendes impayées: %.2f DH. Veuillez régler avant tout nouvel emprunt.",
                        amendesImpayees
                ));
                return;
            }

            // Effectuer le prêt
            if (effectuerPret(client.getId(), idDocument)) {
                showAlert("Succès", "Prêt effectué avec succès");
                clearFields();
                loadPrets(); // Recharger la liste
            } else {
                showAlert("Erreur", "Erreur lors du prêt");
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Code document invalide");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du prêt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        String codeDocument = scanDocumentField.getText().trim();
        String carteClient = scanClientField.getText().trim();

        if (codeDocument.isEmpty() || carteClient.isEmpty()) {
            showAlert("Erreur", "Veuillez scanner le document et la carte client");
            return;
        }

        try {
            int idDocument = Integer.parseInt(codeDocument);
            Client client = personneDAO.rechercherClientParCarte(carteClient);

            if (client == null) {
                showAlert("Erreur", "Client non trouvé");
                return;
            }

            // Effectuer le retour
            double amende = effectuerRetour(client.getId(), idDocument);

            if (amende >= 0) {
                if (amende > 0) {
                    showAlert("Retour effectué", String.format(
                            "Retour effectué. Amende à payer: %.2f DH", amende
                    ));
                } else {
                    showAlert("Succès", "Retour effectué sans amende");
                }

                clearFields();
                loadPrets(); // Recharger la liste
            } else {
                showAlert("Erreur", "Emprunt non trouvé ou déjà retourné");
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Code document invalide");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du retour: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRechercherAmendes() {
        String carteClient = amendeClientField.getText().trim();
        if (carteClient.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir le numéro de carte du client");
            return;
        }

        try {
            Client client = personneDAO.rechercherClientParCarte(carteClient);
            if (client == null) {
                amendeInfoLabel.setText("Client non trouvé");
                amendeInfoLabel.setStyle("-fx-text-fill: red;");
                amendeMontantField.clear();
                return;
            }

            // Calculer le total des amendes
            double totalAmendes = calculerTotalAmendes(client.getId());

            if (totalAmendes > 0) {
                amendeInfoLabel.setText(String.format("Client: %s %s - Total amendes: %.2f DH",
                        client.getNom(), client.getPrenom(), totalAmendes));
                amendeInfoLabel.setStyle("-fx-text-fill: red;");
                amendeMontantField.setText(String.valueOf(totalAmendes));
            } else {
                amendeInfoLabel.setText(String.format("Client: %s %s - Aucune amende",
                        client.getNom(), client.getPrenom()));
                amendeInfoLabel.setStyle("-fx-text-fill: green;");
                amendeMontantField.setText("0.00");
            }

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePayerAmende() {
        String carteClient = amendeClientField.getText().trim();
        String montantStr = amendeMontantField.getText().trim();

        if (carteClient.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir le numéro de carte du client");
            return;
        }

        if (montantStr.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir le montant à payer");
            return;
        }

        try {
            double montant = Double.parseDouble(montantStr);
            if (montant <= 0) {
                showAlert("Erreur", "Le montant doit être positif");
                return;
            }

            Client client = personneDAO.rechercherClientParCarte(carteClient);
            if (client == null) {
                showAlert("Erreur", "Client non trouvé");
                return;
            }

            // Effectuer le paiement
            boolean paiementReussi = payerAmendes(client.getId(), montant);

            if (paiementReussi) {
                showAlert("Succès", String.format("Paiement de %.2f DH effectué avec succès pour %s %s",
                        montant, client.getNom(), client.getPrenom()));

                // Vider les champs
                amendeClientField.clear();
                amendeMontantField.clear();
                amendeInfoLabel.setText("");

                // Recharger la liste des prêts
                loadPrets();
            } else {
                showAlert("Erreur", "Erreur lors du paiement des amendes");
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Montant invalide. Utilisez le format: 25.50");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du paiement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthodes utilitaires manquantes

    private void clearFields() {
        scanDocumentField.clear();
        scanClientField.clear();
        documentInfoLabel.setText("");
        clientInfoLabel.setText("");
    }

    private boolean effectuerPret(int idClient, int idDocument) {
        try {
            Documents document = documentDAO.getDocumentById(idDocument);
            if (document == null) return false;

            // Calculer la date de retour selon le type
            LocalDate dateEmprunt = LocalDate.now();
            LocalDate dateRetour;

            if (document.getTypeDocument().equals("DISQUE") ||
                    (document.getTypeDocument().equals("LIVRE") && document.getTypeEmprunt().equals("LOCATION"))) {
                dateRetour = dateEmprunt.plusWeeks(1); // 1 semaine pour locations
            } else {
                dateRetour = dateEmprunt.plusWeeks(3); // 3 semaines pour prêts réguliers
            }

            String insertEmprunt = """
                INSERT INTO emprunts (id_document, id_client, date_emprunt, date_retour_supposee, statut) 
                VALUES (?, ?, ?, ?, 'EN_COURS')
            """;

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(insertEmprunt);
            stmt.setInt(1, idDocument);
            stmt.setInt(2, idClient);
            stmt.setDate(3, java.sql.Date.valueOf(dateEmprunt));
            stmt.setDate(4, java.sql.Date.valueOf(dateRetour));

            int result = stmt.executeUpdate();
            stmt.close();

            // Après l'insertion de l'emprunt réussie (après le stmt.executeUpdate() et avant le return)
            if (result > 0) {
                // Mettre à jour le nombre d'exemplaires disponibles
                String updateDocument = "UPDATE documents SET nombre_exemplaires = nombre_exemplaires - 1 WHERE id = ?";
                PreparedStatement updateStmt = DBConnection.getConnection().prepareStatement(updateDocument);
                updateStmt.setInt(1, idDocument);
                updateStmt.executeUpdate();
                updateStmt.close();

                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private double effectuerRetour(int idClient, int idDocument) {
        try {
            // Trouver l'emprunt en cours
            String findEmprunt = """
                SELECT e.*, d.type_document, d.type_emprunt 
                FROM emprunts e 
                JOIN documents d ON e.id_document = d.id 
                WHERE e.id_client = ? AND e.id_document = ? AND e.date_retour_reel IS NULL
            """;

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(findEmprunt);
            stmt.setInt(1, idClient);
            stmt.setInt(2, idDocument);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate dateRetourSupposee = rs.getDate("date_retour_supposee").toLocalDate();
                LocalDate dateRetourReel = LocalDate.now();
                String typeDocument = rs.getString("type_document");
                String typeEmprunt = rs.getString("type_emprunt");
                int idEmprunt = rs.getInt("id");

                // Calculer l'amende selon l'énoncé
                double amende = 0.0;
                if (dateRetourReel.isAfter(dateRetourSupposee)) {
                    long joursRetard = ChronoUnit.DAYS.between(dateRetourSupposee, dateRetourReel);

                    // Amende selon le type d'emprunt
                    if (typeDocument.equals("DISQUE") ||
                            (typeDocument.equals("LIVRE") && typeEmprunt.equals("LOCATION"))) {
                        amende = joursRetard * 10.0; // 10 DH par jour pour les locations
                    } else {
                        amende = joursRetard * 5.0;  // 5 DH par jour pour les prêts réguliers
                    }
                }

                // Mettre à jour l'emprunt
                String updateEmprunt = """
                    UPDATE emprunts 
                    SET date_retour_reel = ?, statut = 'RETOURNE', amende = ? 
                    WHERE id = ?
                """;

                PreparedStatement updateStmt = DBConnection.getConnection().prepareStatement(updateEmprunt);
                updateStmt.setDate(1, java.sql.Date.valueOf(dateRetourReel));
                updateStmt.setDouble(2, amende);
                updateStmt.setInt(3, idEmprunt);
                updateStmt.executeUpdate();
                updateStmt.close();

                // Après la mise à jour de l'emprunt réussie (après updateStmt.executeUpdate() et avant le return)
                // Mettre à jour le nombre d'exemplaires disponibles
                String updateDocument = "UPDATE documents SET nombre_exemplaires = nombre_exemplaires + 1 WHERE id = ?";
                PreparedStatement docStmt = DBConnection.getConnection().prepareStatement(updateDocument);
                docStmt.setInt(1, idDocument);
                docStmt.executeUpdate();
                docStmt.close();

                rs.close();
                stmt.close();
                return amende;
            }

            rs.close();
            stmt.close();
            return -1; // Emprunt non trouvé

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean payerAmendes(int idClient, double montantPaye) {
        try {
            // Marquer les documents en retard comme rendus avec amende payée
            String updateEmprunts = """
                UPDATE emprunts 
                SET date_retour_reel = CURRENT_DATE, statut = 'RETOURNE', amende = 0 
                WHERE id_client = ? AND date_retour_reel IS NULL 
                AND date_retour_supposee < CURRENT_DATE
            """;

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(updateEmprunts);
            stmt.setInt(1, idClient);
            int rowsUpdated = stmt.executeUpdate();
            stmt.close();

            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors du paiement des amendes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getTypeEmprunt(Documents document) {
        String typeDoc = document.getTypeDocument();
        switch (typeDoc) {
            case "LIVRE":
                return document.getTypeEmprunt().equals("LOCATION") ? "Location (1 semaine)" : "Prêt (3 semaines)";
            case "DISQUE":
                return "Location (1 semaine)";
            case "JOURNAL":
            case "PERIODIQUE":
                return "Consultation uniquement";
            default:
                return "Inconnu";
        }
    }

    private double calculerTotalAmendes(int idClient) {
        double totalAmendes = 0.0;

        try {
            // Récupérer tous les emprunts en retard du client
            String query = """
                SELECT e.*, d.type_document, d.type_emprunt 
                FROM emprunts e 
                JOIN documents d ON e.id_document = d.id 
                WHERE e.id_client = ? AND e.date_retour_reel IS NULL 
                AND e.date_retour_supposee < CURRENT_DATE
            """;

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(query);
            stmt.setInt(1, idClient);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDate dateRetourSupposee = rs.getDate("date_retour_supposee").toLocalDate();
                LocalDate dateActuelle = LocalDate.now();
                String typeDocument = rs.getString("type_document");
                String typeEmprunt = rs.getString("type_emprunt");

                // Calculer le nombre de jours de retard
                long joursRetard = ChronoUnit.DAYS.between(dateRetourSupposee, dateActuelle);

                if (joursRetard > 0) {
                    // Amende selon l'énoncé
                    double amendeDocument;
                    if (typeDocument.equals("DISQUE") ||
                            (typeDocument.equals("LIVRE") && typeEmprunt.equals("LOCATION"))) {
                        amendeDocument = joursRetard * 10.0; // 10 DH par jour pour les locations
                    } else {
                        amendeDocument = joursRetard * 5.0;  // 5 DH par jour pour les prêts réguliers
                    }
                    totalAmendes += amendeDocument;
                }
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des amendes: " + e.getMessage());
            e.printStackTrace();
        }

        return totalAmendes;
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe interne pour les informations d'emprunt
    public static class EmpruntInfo {
        private String nomClient;
        private String titreDocument;
        private String dateEmprunt;
        private String dateRetourPrevue;
        private String statut;
        private Double amende;

        public EmpruntInfo(String nomClient, String titreDocument, String dateEmprunt,
                           String dateRetourPrevue, String statut, Double amende) {
            this.nomClient = nomClient;
            this.titreDocument = titreDocument;
            this.dateEmprunt = dateEmprunt;
            this.dateRetourPrevue = dateRetourPrevue;
            this.statut = statut;
            this.amende = amende;
        }

        // Getters
        public String getNomClient() { return nomClient; }
        public String getTitreDocument() { return titreDocument; }
        public String getDateEmprunt() { return dateEmprunt; }
        public String getDateRetourPrevue() { return dateRetourPrevue; }
        public String getStatut() { return statut; }
        public Double getAmende() { return amende; }
    }
}
