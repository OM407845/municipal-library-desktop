package app.controleurs;

import DAO.*;
import DAO.personnes.PersonneDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import personnes.*;
import Services.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminController extends BaseController implements Initializable {

    // Tableau des utilisateurs
    @FXML private TableView<Personne> usersTable;
    @FXML private TableColumn<Personne, String> usernameColumn;
    @FXML private TableColumn<Personne, String> nomColumn;
    @FXML private TableColumn<Personne, String> roleColumn;

    // Champs généraux pour tous les utilisateurs
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField newNomField;
    @FXML private TextField newPrenomField;
    @FXML private ComboBox<String> newRoleCombo;

    // Champs spécifiques pour les clients
    @FXML private CheckBox residentCheckBox;
    @FXML private TextField fraisField;
    @FXML private Label fraisLabel;
    @FXML private Label residentLabel;

    // Champs pour l'onglet "Inscription Client"
    @FXML private TextField clientNomField;
    @FXML private TextField clientPrenomField;
    @FXML private TextField clientEmailField;
    @FXML private PasswordField clientPasswordField;
    @FXML private CheckBox clientResidentCheckBox;
    @FXML private TextField clientFraisField;

    // Champs pour les rapports
    @FXML private TextArea rapportArea;
    @FXML private ComboBox<String> typeRapportCombo;

    private PersonneDAO userService = new PersonneDAO();
    private ObservableList<Personne> usersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INITIALISATION AdminController ===");

        try {
            // Configuration du tableau des utilisateurs
            setupUsersTable();

            // Configuration des ComboBox
            setupComboBoxes();

            // Configuration des champs spécifiques aux clients
            setupClientFields();

            // Configuration de l'onglet inscription client
            setupClientInscriptionTab();

            // Charger les utilisateurs
            loadUsers();

            System.out.println("✓ AdminController initialisé avec succès");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de AdminController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUsersTable() {
        if (usersTable != null && usernameColumn != null && nomColumn != null && roleColumn != null) {
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
            roleColumn.setCellValueFactory(new PropertyValueFactory<>("typePersonne"));
            usersTable.setItems(usersList);
            System.out.println("✓ Tableau des utilisateurs configuré");
        }
    }

    private void setupComboBoxes() {
        if (newRoleCombo != null) {
            newRoleCombo.setItems(FXCollections.observableArrayList("ADMIN", "BIBLIO", "PREPOSE"));
            System.out.println("✓ ComboBox rôles configurée");
        }

        if (typeRapportCombo != null) {
            typeRapportCombo.setItems(FXCollections.observableArrayList(
                    "Statistiques des prêts",
                    "Rapport des retards",
                    "Rapport des amendes",
                    "Documents populaires",
                    "Activité des utilisateurs"
            ));
            typeRapportCombo.setValue("Statistiques des prêts");
            System.out.println("✓ ComboBox rapports configurée");
        }
    }

    private void setupClientFields() {
        // Masquer les champs client par défaut
        toggleClientFields(false);

        // Initialiser les frais par défaut
        if (fraisField != null) {
            fraisField.setText("0.00");
        }

        // Listener pour afficher/masquer les champs selon le rôle sélectionné
        if (newRoleCombo != null) {
            newRoleCombo.setOnAction(e -> {
                String selectedRole = newRoleCombo.getValue();
                toggleClientFields("CLIENT".equals(selectedRole));
            });
        }

        // Listener pour calculer automatiquement les frais selon le statut résident
        if (residentCheckBox != null) {
            residentCheckBox.setOnAction(e -> updateFrais());
        }
    }

    private void toggleClientFields(boolean show) {
        if (residentCheckBox != null) residentCheckBox.setVisible(show);
        if (fraisField != null) fraisField.setVisible(show);
        if (fraisLabel != null) fraisLabel.setVisible(show);
        if (residentLabel != null) residentLabel.setVisible(show);
    }

    private void updateFrais() {
        if (fraisField != null && residentCheckBox != null) {
            if (residentCheckBox.isSelected()) {
                fraisField.setText("0.00");  // Gratuit pour les résidents
            } else {
                fraisField.setText("100.00"); // 100 DH pour les non-résidents
            }
        }
    }

    private void setupClientInscriptionTab() {
        // Initialiser les frais par défaut
        if (clientFraisField != null) {
            clientFraisField.setText("0.00");
        }

        // Listener pour calculer automatiquement les frais selon le statut résident
        if (clientResidentCheckBox != null) {
            clientResidentCheckBox.setOnAction(e -> {
                if (clientFraisField != null) {
                    if (clientResidentCheckBox.isSelected()) {
                        clientFraisField.setText("0.00");  // Gratuit pour les résidents
                    } else {
                        clientFraisField.setText("100.00"); // 100 DH pour les non-résidents
                    }
                }
            });
        }
    }

    private void loadUsers() {
        try {
            usersList.clear();
            usersList.addAll(userService.listerPersonnes());
            System.out.println("✓ " + usersList.size() + " utilisateurs chargés");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddUser() {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText();
        String nom = newNomField.getText().trim();
        String prenom = newPrenomField.getText().trim();
        String role = newRoleCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || nom.isEmpty() || prenom.isEmpty() || role == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        try {
            Personne newUser = null;

            switch (role) {
                case "CLIENT":
                    String fraisStr = fraisField.getText().trim();
                    if (fraisStr.isEmpty()) {
                        showAlert("Erreur", "Veuillez saisir les frais d'inscription");
                        return;
                    }

                    double frais = Double.parseDouble(fraisStr);
                    boolean estResident = residentCheckBox.isSelected();

                    Client client = new Client(0, nom, prenom, username, password);
                    client.setEstResident(estResident);
                    newUser = client;
                    break;

                case "ADMIN":
                    newUser = new Administrateur(0, nom, prenom, username, password);
                    break;

                case "BIBLIO":
                    newUser = new Bibliothecaire(0, nom, prenom, username, password);
                    break;

                case "PREPOSE":
                    newUser = new Prepose(0, nom, prenom, username, password);
                    break;

                default:
                    showAlert("Erreur", "Rôle non reconnu");
                    return;
            }

            if (userService.creerCompte(newUser)) {
                clearForm();
                loadUsers();

                if ("CLIENT".equals(role)) {
                    String message = String.format(
                            "Client inscrit avec succès !\n" +
                                    "Statut: %s\n" +
                                    "Frais: %.2f DH\n" +
                                    "Carte client générée automatiquement",
                            residentCheckBox.isSelected() ? "Résident (gratuit)" : "Non-résident",
                            Double.parseDouble(fraisField.getText())
                    );
                    showAlert("Succès - Client inscrit", message);
                } else {
                    showAlert("Succès", "Utilisateur créé avec succès");
                }
            } else {
                showAlert("Erreur", "Erreur lors de la création (email peut-être déjà existant)");
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Frais d'inscription invalides");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la création: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteUser() {
        Personne selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un utilisateur à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'utilisateur");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " + selected.getNom() + " " + selected.getPrenom() + " ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (userService.supprimerPersonne(selected.getId())) {
                loadUsers();
                showAlert("Succès", "Utilisateur supprimé avec succès");
            } else {
                showAlert("Erreur", "Erreur lors de la suppression");
            }
        }
    }

    @FXML
    private void handleInscriptionClient() {
        String email = clientEmailField.getText().trim();
        String password = clientPasswordField.getText();
        String nom = clientNomField.getText().trim();
        String prenom = clientPrenomField.getText().trim();
        boolean estResident = clientResidentCheckBox.isSelected();

        if (email.isEmpty() || password.isEmpty() || nom.isEmpty() || prenom.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        try {
            Client client = new Client(0, nom, prenom, email, password);
            client.setEstResident(estResident);

            if (userService.creerCompte(client)) {
                String message = String.format(
                        "Client inscrit avec succès !\n" +
                                "Nom: %s %s\n" +
                                "Email: %s\n" +
                                "Statut: %s\n" +
                                "Frais: %.2f DH\n" +
                                "Carte client générée automatiquement",
                        nom, prenom, email,
                        estResident ? "Résident (gratuit)" : "Non-résident",
                        estResident ? 0.00 : 100.00
                );
                showAlert("Succès - Client inscrit", message);

                handleClearClientForm();
                loadUsers();
            } else {
                showAlert("Erreur", "Erreur lors de l'inscription (email peut-être déjà existant)");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearClientForm() {
        if (clientNomField != null) clientNomField.clear();
        if (clientPrenomField != null) clientPrenomField.clear();
        if (clientEmailField != null) clientEmailField.clear();
        if (clientPasswordField != null) clientPasswordField.clear();
        if (clientResidentCheckBox != null) clientResidentCheckBox.setSelected(false);
        if (clientFraisField != null) clientFraisField.setText("0.00");
    }

    @FXML
    private void handleGenerateReport() {
        String typeRapport = typeRapportCombo.getValue();
        if (typeRapport == null) {
            showAlert("Erreur", "Veuillez sélectionner un type de rapport");
            return;
        }

        try {
            String rapport = "";
            switch (typeRapport) {
                case "Statistiques des prêts":
                    rapport = genererRapportStatistiquesPrefs();
                    break;
                case "Rapport des retards":
                    rapport = genererRapportRetards();
                    break;
                case "Rapport des amendes":
                    rapport = genererRapportAmendes();
                    break;
                case "Documents populaires":
                    rapport = genererRapportDocumentsPopulaires();
                    break;
                case "Activité des utilisateurs":
                    rapport = genererRapportActiviteUtilisateurs();
                    break;
                default:
                    rapport = "Type de rapport non reconnu";
            }

            rapportArea.setText(rapport);

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du rapport: " + e.getMessage());
            e.printStackTrace();
            rapportArea.setText("Erreur lors de la génération du rapport: " + e.getMessage());
        }
    }

    // Méthodes de génération de rapports (simplifiées pour éviter les erreurs SQL)
    private String genererRapportStatistiquesPrefs() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== STATISTIQUES DES PRÊTS ===\n");
        rapport.append("Généré le: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        try {
            Connection conn = DBConnection.getConnection();

            // Nombre total d'emprunts
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as total FROM emprunts");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                rapport.append("📊 Nombre total d'emprunts: ").append(rs.getInt("total")).append("\n");
            }
            rs.close();
            stmt.close();

            // Emprunts en cours
            stmt = conn.prepareStatement("SELECT COUNT(*) as en_cours FROM emprunts WHERE statut = 'EN_COURS'");
            rs = stmt.executeQuery();
            if (rs.next()) {
                rapport.append("📖 Emprunts en cours: ").append(rs.getInt("en_cours")).append("\n");
            }
            rs.close();
            stmt.close();

            // Emprunts retournés
            stmt = conn.prepareStatement("SELECT COUNT(*) as retournes FROM emprunts WHERE statut = 'RETOURNE'");
            rs = stmt.executeQuery();
            if (rs.next()) {
                rapport.append("✅ Emprunts retournés: ").append(rs.getInt("retournes")).append("\n");
            }
            rs.close();
            stmt.close();

            // Statistiques par type de document
            rapport.append("\n--- Répartition par type de document ---\n");
            stmt = conn.prepareStatement("""
                SELECT d.type_document, COUNT(*) as nombre
                FROM emprunts e 
                JOIN documents d ON e.id_document = d.id 
                GROUP BY d.type_document 
                ORDER BY nombre DESC
            """);
            rs = stmt.executeQuery();
            while (rs.next()) {
                rapport.append(String.format("• %s: %d emprunts\n",
                        rs.getString("type_document"), rs.getInt("nombre")));
            }
            rs.close();
            stmt.close();

            // Emprunts par mois (derniers 6 mois)
            rapport.append("\n--- Évolution des emprunts (6 derniers mois) ---\n");
            stmt = conn.prepareStatement("""
                SELECT 
                    EXTRACT(YEAR FROM date_emprunt) as annee,
                    EXTRACT(MONTH FROM date_emprunt) as mois,
                    COUNT(*) as nombre
                FROM emprunts 
                WHERE date_emprunt >= CURRENT_DATE - INTERVAL '6 months'
                GROUP BY EXTRACT(YEAR FROM date_emprunt), EXTRACT(MONTH FROM date_emprunt)
                ORDER BY annee DESC, mois DESC
            """);
            rs = stmt.executeQuery();
            while (rs.next()) {
                rapport.append(String.format("• %02d/%d: %d emprunts\n",
                        rs.getInt("mois"), rs.getInt("annee"), rs.getInt("nombre")));
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            rapport.append("Erreur lors de la récupération des statistiques: ").append(e.getMessage());
        }

        return rapport.toString();
    }

    private String genererRapportRetards() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT DES RETARDS ===\n");
        rapport.append("Généré le: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        try {
            Connection conn = DBConnection.getConnection();

            // Emprunts en retard
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT 
                    p.nom, p.prenom, d.titre, e.date_emprunt, e.date_retour_supposee,
                    CURRENT_DATE - e.date_retour_supposee as jours_retard
                FROM emprunts e
                JOIN personnes p ON e.id_client = p.id
                JOIN documents d ON e.id_document = d.id
                WHERE e.statut = 'EN_COURS' AND e.date_retour_supposee < CURRENT_DATE
                ORDER BY jours_retard DESC
            """);
            ResultSet rs = stmt.executeQuery();

            int totalRetards = 0;
            while (rs.next()) {
                totalRetards++;
                rapport.append(String.format("🔴 %s %s\n",
                        rs.getString("nom"), rs.getString("prenom")));
                rapport.append(String.format("   Document: %s\n", rs.getString("titre")));
                rapport.append(String.format("   Emprunté le: %s\n", rs.getDate("date_emprunt")));
                rapport.append(String.format("   Retour prévu: %s\n", rs.getDate("date_retour_supposee")));
                rapport.append(String.format("   Retard: %d jours\n\n", rs.getInt("jours_retard")));
            }

            if (totalRetards == 0) {
                rapport.append("✅ Aucun emprunt en retard actuellement !\n");
            } else {
                rapport.insert(rapport.indexOf("\n\n") + 2,
                        String.format("⚠️ Nombre total d'emprunts en retard: %d\n\n", totalRetards));
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            rapport.append("Erreur lors de la récupération des retards: ").append(e.getMessage());
        }

        return rapport.toString();
    }

    private String genererRapportAmendes() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT DES AMENDES ===\n");
        rapport.append("Généré le: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        try {
            Connection conn = DBConnection.getConnection();

            // Total des amendes
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT 
                    SUM(CASE WHEN amende IS NOT NULL THEN amende ELSE 0 END) as total_amendes,
                    COUNT(CASE WHEN amende > 0 THEN 1 END) as nombre_amendes
                FROM emprunts
            """);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                rapport.append(String.format("💰 Total des amendes: %.2f DH\n", rs.getDouble("total_amendes")));
                rapport.append(String.format("📊 Nombre d'emprunts avec amende: %d\n\n", rs.getInt("nombre_amendes")));
            }
            rs.close();
            stmt.close();

            // Amendes par client
            rapport.append("--- Amendes par client ---\n");
            stmt = conn.prepareStatement("""
                SELECT 
                    p.nom, p.prenom, 
                    SUM(CASE WHEN e.amende IS NOT NULL THEN e.amende ELSE 0 END) as total_client
                FROM emprunts e
                JOIN personnes p ON e.id_client = p.id
                WHERE e.amende > 0
                GROUP BY p.id, p.nom, p.prenom
                ORDER BY total_client DESC
            """);
            ResultSet rs2 = stmt.executeQuery();

            while (rs2.next()) {
                rapport.append(String.format("• %s %s: %.2f DH\n",
                        rs2.getString("nom"), rs2.getString("prenom"), rs2.getDouble("total_client")));
            }
            rs2.close();
            stmt.close();

            // Amendes en cours (emprunts en retard)
            rapport.append("\n--- Amendes potentielles (emprunts en retard) ---\n");
            stmt = conn.prepareStatement("""
                SELECT 
                    p.nom, p.prenom, d.titre, d.type_document, d.type_emprunt,
                    CURRENT_DATE - e.date_retour_supposee as jours_retard
                FROM emprunts e
                JOIN personnes p ON e.id_client = p.id
                JOIN documents d ON e.id_document = d.id
                WHERE e.statut = 'EN_COURS' AND e.date_retour_supposee < CURRENT_DATE
                ORDER BY jours_retard DESC
            """);
            ResultSet rs3 = stmt.executeQuery();

            double totalAmendesPotentielles = 0;
            while (rs3.next()) {
                int joursRetard = rs3.getInt("jours_retard");
                String typeDocument = rs3.getString("type_document");
                String typeEmprunt = rs3.getString("type_emprunt");

                // Calculer l'amende selon les règles
                double amende = 0;
                if ("DISQUE".equals(typeDocument) ||
                        ("LIVRE".equals(typeDocument) && "LOCATION".equals(typeEmprunt))) {
                    amende = joursRetard * 10.0; // 10 DH par jour pour les locations
                } else {
                    amende = joursRetard * 5.0;  // 5 DH par jour pour les prêts réguliers
                }

                totalAmendesPotentielles += amende;

                rapport.append(String.format("• %s %s - %s: %.2f DH (%d jours)\n",
                        rs3.getString("nom"), rs3.getString("prenom"),
                        rs3.getString("titre"), amende, joursRetard));
            }

            rapport.append(String.format("\n💸 Total amendes potentielles: %.2f DH\n", totalAmendesPotentielles));

            rs3.close();
            stmt.close();

        } catch (SQLException e) {
            rapport.append("Erreur lors de la récupération des amendes: ").append(e.getMessage());
        }

        return rapport.toString();
    }

    private String genererRapportDocumentsPopulaires() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== DOCUMENTS POPULAIRES ===\n");
        rapport.append("Généré le: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        try {
            Connection conn = DBConnection.getConnection();

            // Top 10 des documents les plus empruntés (requête simplifiée)
            rapport.append("--- Top 10 des documents les plus empruntés ---\n");
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT d.titre, d.type_document, COUNT(*) as nombre_emprunts
                FROM emprunts e
                JOIN documents d ON e.id_document = d.id
                GROUP BY d.id, d.titre, d.type_document
                ORDER BY nombre_emprunts DESC
                LIMIT 10
            """);
            ResultSet rs = stmt.executeQuery();

            int rang = 1;
            while (rs.next()) {
                rapport.append(String.format("%d. %s\n", rang++, rs.getString("titre")));
                rapport.append(String.format("   Type: %s\n", rs.getString("type_document")));
                rapport.append(String.format("   Emprunts: %d\n\n", rs.getInt("nombre_emprunts")));
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            rapport.append("Erreur lors de la récupération des documents populaires: ").append(e.getMessage());
        }

        return rapport.toString();
    }

    private String genererRapportActiviteUtilisateurs() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== ACTIVITÉ DES UTILISATEURS ===\n");
        rapport.append("Généré le: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        try {
            Connection conn = DBConnection.getConnection();

            // Statistiques générales des utilisateurs
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT 
                    COUNT(CASE WHEN type_personne = 'CLIENT' THEN 1 END) as clients,
                    COUNT(CASE WHEN type_personne = 'ADMIN' THEN 1 END) as admins,
                    COUNT(CASE WHEN type_personne = 'BIBLIO' THEN 1 END) as bibliothecaires,
                    COUNT(CASE WHEN type_personne = 'PREPOSE' THEN 1 END) as preposes
                FROM personnes
            """);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                rapport.append("--- Répartition des utilisateurs ---\n");
                rapport.append(String.format("👥 Clients: %d\n", rs.getInt("clients")));
                rapport.append(String.format("👨‍💼 Administrateurs: %d\n", rs.getInt("admins")));
                rapport.append(String.format("📚 Bibliothécaires: %d\n", rs.getInt("bibliothecaires")));
                rapport.append(String.format("🏢 Préposés: %d\n\n", rs.getInt("preposes")));
            }
            rs.close();
            stmt.close();

            // Clients les plus actifs
            rapport.append("--- Top 10 des clients les plus actifs ---\n");
            stmt = conn.prepareStatement("""
                SELECT 
                    p.nom, p.prenom, 
                    COUNT(*) as nombre_emprunts,
                    MAX(e.date_emprunt) as dernier_emprunt
                FROM emprunts e
                JOIN personnes p ON e.id_client = p.id
                WHERE p.type_personne = 'CLIENT'
                GROUP BY p.id, p.nom, p.prenom
                ORDER BY nombre_emprunts DESC
                LIMIT 10
            """);
            ResultSet rs2 = stmt.executeQuery();

            int rang = 1;
            while (rs2.next()) {
                rapport.append(String.format("%d. %s %s\n", rang++,
                        rs2.getString("nom"), rs2.getString("prenom")));
                rapport.append(String.format("   Emprunts: %d\n", rs2.getInt("nombre_emprunts")));
                rapport.append(String.format("   Dernier emprunt: %s\n\n", rs2.getDate("dernier_emprunt")));
            }
            rs2.close();
            stmt.close();

            // Clients inactifs
            rapport.append("--- Clients inactifs (aucun emprunt depuis 6 mois) ---\n");
            stmt = conn.prepareStatement("""
                SELECT p.nom, p.prenom, MAX(e.date_emprunt) as dernier_emprunt
                FROM personnes p
                LEFT JOIN emprunts e ON p.id = e.id_client
                WHERE p.type_personne = 'CLIENT'
                GROUP BY p.id, p.nom, p.prenom
                HAVING MAX(e.date_emprunt) < CURRENT_DATE - INTERVAL '6 months' 
                    OR MAX(e.date_emprunt) IS NULL
                ORDER BY dernier_emprunt DESC NULLS LAST
            """);
            ResultSet rs3 = stmt.executeQuery();

            int clientsInactifs = 0;
            while (rs3.next()) {
                clientsInactifs++;
                java.sql.Date dernierEmprunt = rs3.getDate("dernier_emprunt");
                rapport.append(String.format("• %s %s",
                        rs3.getString("nom"), rs3.getString("prenom")));
                if (dernierEmprunt != null) {
                    rapport.append(String.format(" (dernier emprunt: %s)", dernierEmprunt));
                } else {
                    rapport.append(" (aucun emprunt)");
                }
                rapport.append("\n");
            }

            if (clientsInactifs == 0) {
                rapport.append("✅ Tous les clients sont actifs !\n");
            } else {
                rapport.append(String.format("\n📊 Total: %d clients inactifs\n", clientsInactifs));
            }

            rs3.close();
            stmt.close();

            // Activité par mois
            rapport.append("\n--- Activité des emprunts par mois (6 derniers mois) ---\n");
            stmt = conn.prepareStatement("""
            SELECT 
                EXTRACT(YEAR FROM date_emprunt) as annee,
                EXTRACT(MONTH FROM date_emprunt) as mois,
                COUNT(*) as nombre_emprunts,
                COUNT(DISTINCT id_client) as clients_actifs
            FROM emprunts 
            WHERE date_emprunt >= CURRENT_DATE - INTERVAL '6 months'
            GROUP BY EXTRACT(YEAR FROM date_emprunt), EXTRACT(MONTH FROM date_emprunt)
            ORDER BY annee DESC, mois DESC
        """);
            ResultSet rs4 = stmt.executeQuery();

            while (rs4.next()) {
                rapport.append(String.format("• %02d/%d: %d emprunts par %d clients\n",
                        rs4.getInt("mois"), rs4.getInt("annee"),
                        rs4.getInt("nombre_emprunts"), rs4.getInt("clients_actifs")));
            }
            rs4.close();
            stmt.close();

        } catch (SQLException e) {
            rapport.append("Erreur lors de la récupération de l'activité des utilisateurs: ").append(e.getMessage());
        }

        return rapport.toString();
    }

    private void clearForm() {
        if (newUsernameField != null) newUsernameField.clear();
        if (newPasswordField != null) newPasswordField.clear();
        if (newNomField != null) newNomField.clear();
        if (newPrenomField != null) newPrenomField.clear();
        if (newRoleCombo != null) newRoleCombo.setValue(null);

        if (residentCheckBox != null) residentCheckBox.setSelected(false);
        if (fraisField != null) fraisField.setText("0.00");

        toggleClientFields(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
