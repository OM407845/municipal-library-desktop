package app.controleurs;

import DAO.*;
import DAO.documents.DocumentDAO;
import DAO.personnes.PersonneDAO;
import documents.*;
import personnes.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ClientController extends BaseController implements Initializable {

    // Onglet Catalogue - COLONNE AUTEUR SUPPRIMÉE
    @FXML private TableView<DocumentInfo> catalogueTable;
    @FXML private TableColumn<DocumentInfo, String> titreColumn;
    @FXML private TableColumn<DocumentInfo, String> typeColumn;
    @FXML private TableColumn<DocumentInfo, String> disponibleColumn;
    @FXML private TableColumn<DocumentInfo, Void> actionColumn;

    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> critereRechercheCombo;

    // Onglet Mes Prêts
    @FXML private TableView<EmpruntInfo> mesPretsTable;
    @FXML private TableColumn<EmpruntInfo, String> pretTitreColumn;
    @FXML private TableColumn<EmpruntInfo, String> pretDateColumn;
    @FXML private TableColumn<EmpruntInfo, String> pretRetourColumn;
    @FXML private TableColumn<EmpruntInfo, String> pretStatutColumn;
    @FXML private TableColumn<EmpruntInfo, Double> pretAmendeColumn;

    // Onglet Réservations
    @FXML private TableView<ReservationInfo> reservationsTable;
    @FXML private TableColumn<ReservationInfo, String> reservationTitreColumn;
    @FXML private TableColumn<ReservationInfo, String> reservationDateColumn;
    @FXML private TableColumn<ReservationInfo, Integer> reservationPositionColumn;
    @FXML private TableColumn<ReservationInfo, String> reservationStatutColumn;
    @FXML private TableColumn<ReservationInfo, Void> reservationActionColumn;

    private DocumentDAO documentDAO;
    private PersonneDAO personneDAO;
    private Client currentClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INITIALISATION ClientController ===");

        try {
            // Test de connexion à la base de données
            testDatabaseConnection();

            // Initialiser les DAOs
            documentDAO = new DocumentDAO();
            personneDAO = new PersonneDAO();

            // Configurer les tables
            setupCatalogueTable();
            setupPretsTable();
            setupReservationsTable();
            setupSearchControls();

            // Charger le catalogue immédiatement
            loadCatalogue();

            // Initialiser le client depuis BaseController
            initializeCurrentClient();

            System.out.println("✓ ClientController initialisé avec succès");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du ClientController: " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, charger des données de test
            loadTestData();
        }
    }

    /**
     * Initialise le client connecté depuis BaseController
     */
    private void initializeCurrentClient() {
        try {
            // Récupérer le client depuis BaseController
            if (currentUser instanceof Client) {
                this.currentClient = (Client) currentUser;
                System.out.println("Client récupéré depuis BaseController: " + currentClient.getNom() + " " + currentClient.getPrenom());

                // Charger les données spécifiques au client
                loadMesPrets();
                loadMesReservations();
            } else {
                System.out.println("Aucun client connecté trouvé dans BaseController");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void setCurrentUser(Personne user) {
        super.setCurrentUser(user);
        if (user instanceof Client) {
            this.currentClient = (Client) user;
            System.out.println("Client défini via setCurrentUser: " + currentClient.getNom() + " " + currentClient.getPrenom());

            // Charger les données spécifiques au client
            loadMesPrets();
            loadMesReservations();
        }
    }

    private void testDatabaseConnection() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Connexion à la base de données OK");

                // Test de la table documents
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM documents");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("✓ Nombre de documents dans la base: " + count);
                }
                rs.close();
                stmt.close();

                // Test de la table emprunts
                stmt = conn.prepareStatement("SELECT COUNT(*) FROM emprunts");
                rs = stmt.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("✓ Nombre d'emprunts dans la base: " + count);
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

    private void setupCatalogueTable() {
        if (catalogueTable == null) {
            System.err.println("ERREUR: catalogueTable est null!");
            return;
        }

        System.out.println("Configuration du tableau catalogue (sans colonne auteur)...");

        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeDocument"));
        disponibleColumn.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));

        // Bouton Réserver
        actionColumn.setCellFactory(param -> new TableCell<DocumentInfo, Void>() {
            private final Button reserverBtn = new Button("Réserver");
            private final Button consultBtn = new Button("Consult...");

            {
                reserverBtn.getStyleClass().add("primary-button");
                consultBtn.getStyleClass().add("secondary-button");

                reserverBtn.setOnAction(event -> {
                    DocumentInfo document = getTableView().getItems().get(getIndex());
                    handleReserver(document);
                });

                consultBtn.setOnAction(event -> {
                    showAlert("Information", "Ce document est disponible uniquement en consultation sur place.");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DocumentInfo doc = getTableView().getItems().get(getIndex());
                    if (!doc.isEmpruntable()) {
                        setGraphic(consultBtn);
                    } else {
                        setGraphic(reserverBtn);
                    }
                }
            }
        });

        System.out.println("✓ Configuration du tableau catalogue terminée (sans colonne auteur)");
    }

    private void setupPretsTable() {
        if (mesPretsTable == null) {
            System.err.println("ERREUR: mesPretsTable est null!");
            return;
        }

        System.out.println("Configuration du tableau des prêts...");

        pretTitreColumn.setCellValueFactory(new PropertyValueFactory<>("titreDocument"));
        pretDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
        pretRetourColumn.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        pretStatutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        pretAmendeColumn.setCellValueFactory(new PropertyValueFactory<>("amende"));

        // Formatter l'affichage des amendes
        pretAmendeColumn.setCellFactory(column -> new TableCell<EmpruntInfo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("-");
                } else {
                    setText(String.format("%.2f DH", item));
                    setStyle("-fx-text-fill: red;");
                }
            }
        });

        System.out.println("✓ Configuration du tableau des prêts terminée");
    }

    private void setupReservationsTable() {
        if (reservationsTable == null) {
            System.err.println("ERREUR: reservationsTable est null!");
            return;
        }

        System.out.println("Configuration du tableau des réservations...");

        reservationTitreColumn.setCellValueFactory(new PropertyValueFactory<>("titreDocument"));
        reservationDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));
        reservationPositionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        reservationStatutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Bouton Annuler
        reservationActionColumn.setCellFactory(param -> new TableCell<ReservationInfo, Void>() {
            private final Button annulerBtn = new Button("Annuler");

            {
                annulerBtn.getStyleClass().add("warning-button");
                annulerBtn.setOnAction(event -> {
                    ReservationInfo reservation = getTableView().getItems().get(getIndex());
                    handleAnnulerReservation(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ReservationInfo res = getTableView().getItems().get(getIndex());
                    if ("ACTIVE".equals(res.getStatut()) || "EN_ATTENTE".equals(res.getStatut())) {
                        annulerBtn.setDisable(false);
                    } else {
                        annulerBtn.setDisable(true);
                    }
                    setGraphic(annulerBtn);
                }
            }
        });

        System.out.println("✓ Configuration du tableau des réservations terminée");
    }

    private void setupSearchControls() {
        if (critereRechercheCombo == null) {
            System.err.println("ERREUR: critereRechercheCombo est null!");
            return;
        }

        System.out.println("Configuration des contrôles de recherche...");

        critereRechercheCombo.setItems(FXCollections.observableArrayList(
                "Titre", "Type"
        ));
        critereRechercheCombo.setValue("Titre");

        System.out.println("✓ Configuration des contrôles de recherche terminée");
    }

    /**
     * Charge des données de test pour le développement
     */
    private void loadTestData() {
        System.out.println("Chargement des données de test...");

        // Données de test pour le catalogue
        ObservableList<DocumentInfo> testDocuments = FXCollections.observableArrayList();
        testDocuments.add(new DocumentInfo(1, "Le Petit Prince", "LIVRE", 5, "PRET", true));
        testDocuments.add(new DocumentInfo(2, "1984", "LIVRE", 0, "PRET", true));
        testDocuments.add(new DocumentInfo(3, "L'Étranger", "LIVRE", 2, "PRET", true));
        testDocuments.add(new DocumentInfo(4, "Harry Potter", "LIVRE", 5, "LOCATION", true));
        testDocuments.add(new DocumentInfo(5, "Le Figaro", "JOURNAL", 2, "CONSULTATION", false));

        Platform.runLater(() -> {
            if (catalogueTable != null) {
                catalogueTable.setItems(testDocuments);
                catalogueTable.refresh();
                System.out.println("✓ Données de test chargées dans le catalogue: " + testDocuments.size() + " documents");
            }
        });
    }

    private void loadCatalogue() {
        if (catalogueTable == null) {
            System.err.println("ERREUR: catalogueTable est null dans loadCatalogue()!");
            return;
        }

        System.out.println("Chargement du catalogue depuis la base de données...");

        try {
            // Requête adaptée à votre vraie structure de table
            String query = """
                SELECT d.id, d.titre, d.type_document, d.nombre_exemplaires, 
                       d.type_emprunt, d.empruntable
                FROM documents d 
                ORDER BY d.titre
            """;

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            ObservableList<DocumentInfo> documents = FXCollections.observableArrayList();
            int compteur = 0;

            while (rs.next()) {
                compteur++;
                DocumentInfo doc = new DocumentInfo(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("type_document"),
                        rs.getInt("nombre_exemplaires"),
                        rs.getString("type_emprunt"),
                        rs.getBoolean("empruntable")
                );
                documents.add(doc);

                System.out.println("Document " + compteur + ": " + doc.getTitre() +
                        " (" + doc.getTypeDocument() + ") - " +
                        doc.getNombreExemplaires() + " exemplaires");
            }

            System.out.println("Nombre de documents récupérés: " + compteur);

            Platform.runLater(() -> {
                catalogueTable.setItems(documents);
                catalogueTable.refresh();
                System.out.println("✓ Catalogue mis à jour avec " + documents.size() + " documents");
            });

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement du catalogue: " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, charger les données de test
            System.out.println("Chargement des données de test suite à l'erreur SQL");
            loadTestData();

        } catch (Exception e) {
            System.err.println("Erreur générale lors du chargement du catalogue: " + e.getMessage());
            e.printStackTrace();
            loadTestData();
        }
    }

    private void loadMesPrets() {
        if (mesPretsTable == null) {
            System.err.println("ERREUR: mesPretsTable est null dans loadMesPrets()!");
            return;
        }

        if (currentClient == null) {
            System.err.println("ERREUR: currentClient est null dans loadMesPrets()!");
            return;
        }

        System.out.println("=== CHARGEMENT DES PRÊTS ===");
        System.out.println("Client ID: " + currentClient.getId());
        System.out.println("Client: " + currentClient.getNom() + " " + currentClient.getPrenom());

        try {
            String query = """
                SELECT e.*, d.titre, d.type_document
                FROM emprunts e 
                JOIN documents d ON e.id_document = d.id 
                WHERE e.id_client = ? AND e.statut = 'EN_COURS'
                ORDER BY e.date_emprunt DESC
            """;

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(query);
            stmt.setInt(1, currentClient.getId());
            ResultSet rs = stmt.executeQuery();

            ObservableList<EmpruntInfo> emprunts = FXCollections.observableArrayList();
            int compteur = 0;

            while (rs.next()) {
                compteur++;
                // Calculer l'amende si en retard
                LocalDate dateRetourPrevue = rs.getDate("date_retour_supposee").toLocalDate();
                LocalDate dateActuelle = LocalDate.now();
                double amende = 0.0;

                if (dateActuelle.isAfter(dateRetourPrevue)) {
                    long joursRetard = ChronoUnit.DAYS.between(dateRetourPrevue, dateActuelle);
                    String typeDocument = rs.getString("type_document");

                    // Calculer l'amende selon les règles
                    if ("DISQUE".equals(typeDocument)) {
                        amende = joursRetard * 10.0; // 10 DH par jour pour les locations
                    } else {
                        amende = joursRetard * 5.0;  // 5 DH par jour pour les prêts réguliers
                    }
                }

                EmpruntInfo emprunt = new EmpruntInfo(
                        rs.getString("titre"),
                        rs.getDate("date_emprunt").toString(),
                        rs.getDate("date_retour_supposee").toString(),
                        rs.getString("statut"),
                        amende
                );
                emprunts.add(emprunt);

                System.out.println("Emprunt " + compteur + ": " + rs.getString("titre"));
            }

            System.out.println("Nombre de prêts en cours récupérés: " + compteur);

            Platform.runLater(() -> {
                mesPretsTable.setItems(emprunts);
                mesPretsTable.refresh();
                System.out.println("✓ Prêts mis à jour avec " + emprunts.size() + " emprunts");
            });

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des prêts: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur générale lors du chargement des prêts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMesReservations() {
        if (reservationsTable == null || currentClient == null) {
            System.err.println("ERREUR: reservationsTable est null ou currentClient est null dans loadMesReservations()!");
            return;
        }

        System.out.println("Chargement des réservations depuis la base de données...");

        try {
            String query = """
                SELECT r.id, r.date_reservation, r.statut, d.titre,
                   ROW_NUMBER() OVER (PARTITION BY r.id_document ORDER BY r.date_reservation) as position_file
                FROM reservations r 
                JOIN documents d ON r.id_document = d.id 
                WHERE r.id_client = ? AND r.statut IN ('ACTIVE', 'EN_ATTENTE')
                ORDER BY r.date_reservation DESC
            """;

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(query);
            stmt.setInt(1, currentClient.getId());
            ResultSet rs = stmt.executeQuery();

            ObservableList<ReservationInfo> reservations = FXCollections.observableArrayList();
            int compteur = 0;

            while (rs.next()) {
                compteur++;
                ReservationInfo reservation = new ReservationInfo(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getDate("date_reservation").toString(),
                        rs.getInt("position_file"),
                        rs.getString("statut")
                );
                reservations.add(reservation);
            }

            System.out.println("Nombre de réservations récupérées: " + compteur);

            Platform.runLater(() -> {
                reservationsTable.setItems(reservations);
                reservationsTable.refresh();
                System.out.println("✓ Réservations mises à jour avec " + reservations.size() + " réservations");
            });

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des réservations: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur générale lors du chargement des réservations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String critere = critereRechercheCombo.getValue();
        String terme = rechercheField.getText().trim();

        if (terme.isEmpty()) {
            loadCatalogue();
            return;
        }

        System.out.println("Recherche de documents avec critère: " + critere + ", terme: " + terme);

        try {
            String query = "SELECT d.id, d.titre, d.type_document, d.nombre_exemplaires, " +
                    "d.type_emprunt, d.empruntable " +
                    "FROM documents d WHERE ";

            switch (critere) {
                case "Titre":
                    query += "LOWER(d.titre) LIKE LOWER(?)";
                    break;
                case "Type":
                    query += "LOWER(d.type_document) LIKE LOWER(?)";
                    break;
                default:
                    query += "LOWER(d.titre) LIKE LOWER(?)";
            }

            query += " ORDER BY d.titre";

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(query);
            stmt.setString(1, "%" + terme + "%");
            ResultSet rs = stmt.executeQuery();

            ObservableList<DocumentInfo> documents = FXCollections.observableArrayList();
            int compteur = 0;

            while (rs.next()) {
                compteur++;
                DocumentInfo doc = new DocumentInfo(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("type_document"),
                        rs.getInt("nombre_exemplaires"),
                        rs.getString("type_emprunt"),
                        rs.getBoolean("empruntable")
                );
                documents.add(doc);
            }

            System.out.println("Nombre de documents trouvés: " + compteur);

            Platform.runLater(() -> {
                catalogueTable.setItems(documents);
                catalogueTable.refresh();
                System.out.println("✓ Résultats de recherche affichés: " + documents.size() + " documents");
            });

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la recherche: " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur générale lors de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowAll() {
        rechercheField.clear();
        loadCatalogue();
    }

    private void handleReserver(DocumentInfo document) {
        System.out.println("=== DÉBUT RÉSERVATION ===");
        System.out.println("Document: " + document.getTitre());
        System.out.println("Client actuel: " + (currentClient != null ? currentClient.getNom() + " " + currentClient.getPrenom() : "NULL"));

        if (currentClient == null) {
            System.err.println("ERREUR: currentClient est null!");
            showAlert("Erreur", "Aucun client connecté. Veuillez vous reconnecter.");
            return;
        }

        if (!document.isEmpruntable()) {
            showAlert("Document non empruntable", "Ce document est uniquement disponible en consultation sur place");
            return;
        }

        // Vérifier la limite de 5 documents
        int nombreDocuments = getNombreDocumentsClient();
        System.out.println("Nombre de documents actuels du client: " + nombreDocuments);

        if (nombreDocuments >= 5) {
            showAlert("Limite atteinte", "Vous avez atteint la limite de 5 documents (prêts + réservations)");
            return;
        }

        try {
            // Vérifier si le document est déjà réservé par ce client
            String checkQuery = """
                SELECT COUNT(*) FROM reservations 
                WHERE id_client = ? AND id_document = ? AND statut IN ('ACTIVE', 'EN_ATTENTE')
            """;

            PreparedStatement checkStmt = DBConnection.getConnection().prepareStatement(checkQuery);
            checkStmt.setInt(1, currentClient.getId());
            checkStmt.setInt(2, document.getId());
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) > 0) {
                showAlert("Déjà réservé", "Vous avez déjà réservé ce document");
                checkRs.close();
                checkStmt.close();
                return;
            }
            checkRs.close();
            checkStmt.close();

            // Déterminer le statut de la réservation
            String statut = "ACTIVE";
            if (document.getNombreExemplaires() <= 0) {
                statut = "EN_ATTENTE"; // Document non disponible, en attente
            }

            System.out.println("Statut de la réservation: " + statut);

            // Insérer la réservation
            String insertQuery = """
                INSERT INTO reservations (id_client, id_document, date_reservation, statut) 
                VALUES (?, ?, CURRENT_DATE, ?)
            """;

            PreparedStatement insertStmt = DBConnection.getConnection().prepareStatement(insertQuery);
            insertStmt.setInt(1, currentClient.getId());
            insertStmt.setInt(2, document.getId());
            insertStmt.setString(3, statut);

            int result = insertStmt.executeUpdate();
            insertStmt.close();

            System.out.println("Résultat de l'insertion: " + result);

            if (result > 0) {
                // Calculer la position dans la file
                String positionQuery = """
                    SELECT COUNT(*) as position FROM reservations 
                    WHERE id_document = ? AND statut IN ('ACTIVE', 'EN_ATTENTE') 
                    AND date_reservation <= CURRENT_DATE
                """;

                PreparedStatement posStmt = DBConnection.getConnection().prepareStatement(positionQuery);
                posStmt.setInt(1, document.getId());
                ResultSet posRs = posStmt.executeQuery();

                int position = 1;
                if (posRs.next()) {
                    position = posRs.getInt("position");
                }
                posRs.close();
                posStmt.close();

                String message = "Réservation effectuée pour: " + document.getTitre();
                if ("EN_ATTENTE".equals(statut)) {
                    message += "\nStatut: En attente (document non disponible)";
                    message += "\nPosition dans la file: " + position;
                } else {
                    message += "\nStatut: Active (document disponible)";
                }

                showAlert("Réservation effectuée", message);
                loadMesReservations();
                loadCatalogue(); // Recharger le catalogue pour refléter les changements
            } else {
                showAlert("Erreur", "Erreur lors de la réservation");
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la réservation: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la réservation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur générale lors de la réservation: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur inattendue lors de la réservation");
        }

        System.out.println("=== FIN RÉSERVATION ===");
    }

    private void handleAnnulerReservation(ReservationInfo reservation) {
        try {
            String deleteQuery = "DELETE FROM reservations WHERE id = ?";
            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(deleteQuery);
            stmt.setInt(1, reservation.getId());

            int result = stmt.executeUpdate();
            stmt.close();

            if (result > 0) {
                showAlert("Réservation annulée", "Réservation annulée avec succès");
                loadMesReservations();
                loadCatalogue(); // Recharger le catalogue pour refléter les changements
            } else {
                showAlert("Erreur", "Erreur lors de l'annulation");
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'annulation: " + e.getMessage());
            showAlert("Erreur", "Erreur lors de l'annulation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur générale lors de l'annulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getNombreDocumentsClient() {
        if (currentClient == null) {
            System.err.println("ERREUR: currentClient est null dans getNombreDocumentsClient()");
            return 0;
        }

        try {
            String query = """
                SELECT 
                    (SELECT COUNT(*) FROM emprunts WHERE id_client = ? AND statut = 'EN_COURS') +
                    (SELECT COUNT(*) FROM reservations WHERE id_client = ? AND statut IN ('ACTIVE', 'EN_ATTENTE'))
                AS total
            """;

            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(query);
            stmt.setInt(1, currentClient.getId());
            stmt.setInt(2, currentClient.getId());
            ResultSet rs = stmt.executeQuery();

            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            stmt.close();
            return total;

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du calcul du nombre de documents: " + e.getMessage());
            return 0;
        } catch (Exception e) {
            System.err.println("Erreur générale lors du calcul du nombre de documents: " + e.getMessage());
            return 0;
        }
    }

    @FXML
    @Override
    protected void handleLogout() {
        System.out.println("=== DÉBUT DÉCONNEXION ===");

        try {
            // Utiliser la méthode de BaseController
            super.handleLogout();
            System.out.println("✓ Déconnexion réussie avec BaseController.handleLogout()");

        } catch (Exception e) {
            System.err.println("Erreur lors de la déconnexion avec BaseController: " + e.getMessage());
            e.printStackTrace();

            // Plan B: Utiliser SceneManager
            try {
                Stage currentStage = (Stage) userInfoLabel.getScene().getWindow();
                SceneManager.changeScene(currentStage, "/app/interfaces/Login.fxml", "Connexion - Bibliothèque", null);
                System.out.println("✓ Déconnexion réussie avec SceneManager");

            } catch (Exception ex) {
                System.err.println("Erreur lors de la déconnexion avec SceneManager: " + ex.getMessage());
                ex.printStackTrace();
                showAlert("Erreur", "Impossible de se déconnecter. Veuillez redémarrer l'application.");
            }
        }

        System.out.println("=== FIN DÉCONNEXION ===");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classes internes pour les données des tables - adaptées à votre structure

    public static class DocumentInfo {
        private int id;
        private String titre;
        private String typeDocument;
        private int nombreExemplaires;
        private String typeEmprunt;
        private boolean empruntable;

        public DocumentInfo(int id, String titre, String typeDocument,
                            int nombreExemplaires, String typeEmprunt, boolean empruntable) {
            this.id = id;
            this.titre = titre;
            this.typeDocument = typeDocument;
            this.nombreExemplaires = nombreExemplaires;
            this.typeEmprunt = typeEmprunt;
            this.empruntable = empruntable;
        }

        // Getters
        public int getId() { return id; }
        public String getTitre() { return titre; }
        public String getTypeDocument() { return typeDocument; }
        public String getTypeEmprunt() { return typeEmprunt; }
        public int getNombreExemplaires() { return nombreExemplaires; }
        public boolean isEmpruntable() { return empruntable; }

        public String getDisponibilite() {
            if (!empruntable) {
                return "Consultation uniquement";
            } else if (nombreExemplaires > 0) {
                return nombreExemplaires + " disponible(s)";
            } else {
                return "Non disponible";
            }
        }
    }

    public static class EmpruntInfo {
        private String titreDocument;
        private String dateEmprunt;
        private String dateRetourPrevue;
        private String statut;
        private Double amende;

        public EmpruntInfo(String titreDocument, String dateEmprunt, String dateRetourPrevue,
                           String statut, Double amende) {
            this.titreDocument = titreDocument;
            this.dateEmprunt = dateEmprunt;
            this.dateRetourPrevue = dateRetourPrevue;
            this.statut = statut;
            this.amende = amende;
        }

        // Getters
        public String getTitreDocument() { return titreDocument; }
        public String getDateEmprunt() { return dateEmprunt; }
        public String getDateRetourPrevue() { return dateRetourPrevue; }
        public String getStatut() { return statut; }
        public Double getAmende() { return amende; }
    }

    public static class ReservationInfo {
        private int id;
        private String titreDocument;
        private String dateReservation;
        private int position;
        private String statut;

        public ReservationInfo(int id, String titreDocument, String dateReservation,
                               int position, String statut) {
            this.id = id;
            this.titreDocument = titreDocument;
            this.dateReservation = dateReservation;
            this.position = position;
            this.statut = statut;
        }

        // Getters
        public int getId() { return id; }
        public String getTitreDocument() { return titreDocument; }
        public String getDateReservation() { return dateReservation; }
        public int getPosition() { return position; }
        public String getStatut() { return statut; }
    }
}
