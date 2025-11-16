package proj.app.controllers;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
// Removed: import javafx.scene.image.ImageView;
import javafx.scene.layout.Region; // Added import for Region
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import proj.app.ActiveSimulationRegistry;
import proj.app.AppConstants; // Use constants
import proj.app.ConfigManager;
import proj.app.SimulationInitializer;
import proj.app.services.IAlertService;
import proj.app.services.IMessageService;
import proj.simulation.SimulationProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller for the main application window (MainWindow.fxml).
 * This window allows users to select, create, or delete simulation configurations
 * and launch new simulation instances. It interacts with {@link ConfigManager} for configuration persistence,
 * {@link ActiveSimulationRegistry} to manage running simulations, and uses injected services
 * like {@link IAlertService}, {@link IMessageService}, and {@link SimulationInitializer}.
 * Uses constants from {@link AppConstants}. The parchment background is handled via CSS on a Region.
 */
public class MainWindowController {

    //<editor-fold desc="FXML Fields">
    // Define FXML variables matching fx:id attributes in MainWindow.fxml
    @FXML private ComboBox<String> configSelect;
    @FXML private Button startSimulationButton;
    @FXML private Button newConfigButton;
    @FXML private Button deleteConfigButton;
    @FXML private ListView<String> recentSimulations;
    @FXML private Label selectConfigLabel;
    @FXML private Label recentSimulationsLabel;
    @FXML private Label titleLabel;

    // FXML fields for layout with background
    @FXML private StackPane contentAreaPane;
    @FXML private VBox controlsContainer;
    @FXML private Region parchmentRegion; // Reference to the Region with CSS background
    // Removed: @FXML private ImageView parchmentImageView;
    //</editor-fold>

    //<editor-fold desc="Dependencies (Injected)">
    private final IAlertService alertService;
    private final ActiveSimulationRegistry activeSimulationRegistry;
    private final SimulationInitializer simulationInitializer;
    private final Gson gson;
    private final IMessageService messageService;
    //</editor-fold>

    private SimulationProperties currentConfig;

    /**
     * Constructs the MainWindowController with injected dependencies.
     *
     * @param alertService             The {@link IAlertService} instance.
     * @param activeSimulationRegistry The singleton {@link ActiveSimulationRegistry} instance.
     * @param simulationInitializer    The {@link SimulationInitializer} instance.
     * @param gson                     The shared {@link Gson} instance.
     * @param messageService           The {@link IMessageService} instance.
     * @throws NullPointerException if any injected dependency is null.
     */
    public MainWindowController(IAlertService alertService,
                                ActiveSimulationRegistry activeSimulationRegistry,
                                SimulationInitializer simulationInitializer,
                                Gson gson,
                                IMessageService messageService) {
        this.alertService = Objects.requireNonNull(alertService, "AlertService cannot be null");
        this.activeSimulationRegistry = Objects.requireNonNull(activeSimulationRegistry, "ActiveSimulationRegistry cannot be null");
        this.simulationInitializer = Objects.requireNonNull(simulationInitializer, "SimulationInitializer cannot be null");
        this.gson = Objects.requireNonNull(gson, "Gson instance cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");
    }

    /**
     * Initializes the controller after FXML loading and dependency injection.
     * Loads configurations, sets initial UI state, binds listeners, and sets text.
     * No longer binds image size in Java.
     */
    @FXML
    public void initialize() {
        loadAvailableConfigs(); // Load data first
        // Set initial state
        startSimulationButton.setDisable(true);
        deleteConfigButton.setDisable(true);
        // Setup listeners
        configSelect.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> handleConfigSelectionChange(newVal));
        // Configure list view appearance/behavior
        recentSimulations.setFocusTraversable(false);
        recentSimulations.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // Set localized text
        setUIText();
        // Removed call to bindParchmentSize()
    }

    private void setUIText() {
        titleLabel.setText(messageService.getMessage("app.title"));
        // Usuń tę linię:
        // selectConfigLabel.setText(messageService.getMessage("main.label.selectConfig"));
        recentSimulationsLabel.setText(messageService.getMessage("main.window.recent.simulations.label"));
        startSimulationButton.setText(messageService.getMessage("main.button.start"));
        newConfigButton.setText(messageService.getMessage("main.button.newConfig"));
        deleteConfigButton.setText(messageService.getMessage("main.button.deleteConfig"));
    }

    // Removed the bindParchmentSize() method as it's no longer needed

    // --- Event Handlers ---

    /**
     * Handles the action triggered by clicking the 'New Configuration' button.
     * Opens the configuration editor in a modal window.
     */
    @FXML
    private void handleNewConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfigEditor.fxml"));
            if (loader.getLocation() == null) { throw new IOException("Cannot find FXML file: /fxml/ConfigEditor.fxml"); }

            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == ConfigEditorController.class) {
                    return new ConfigEditorController(this.alertService, this.messageService);
                } else {
                    try { return controllerClass.getDeclaredConstructor().newInstance(); } catch (Exception e) { throw new RuntimeException(e); }
                }
            });

            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(messageService.getMessage("config.editor.title.new"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getOwnWindow());

            ConfigEditorController controller = loader.getController();
            controller.setOnConfigSaved(() -> {
                loadAvailableConfigs();
                String lastSaved = controller.getLastSavedConfigName();
                if (lastSaved != null) { configSelect.getSelectionModel().select(lastSaved); }
            });

            stage.showAndWait();

        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.open.configEditor", e.getMessage()));
            e.printStackTrace();
        } catch (Exception e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.unexpected", e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * Handles the action triggered by clicking the 'Delete Configuration' button.
     * Confirms with the user, stops associated simulations, deletes the config file,
     * and updates the UI.
     */
    @FXML
    private void handleDeleteConfig() {
        String selectedConfigName = configSelect.getSelectionModel().getSelectedItem();
        if (selectedConfigName == null) {
            alertService.showAlert(IAlertService.AlertType.WARNING,
                    messageService.getMessage("warning.title"),
                    messageService.getMessage("warning.delete.select"));
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(messageService.getMessage("dialog.deleteConfig.title"));
        confirmation.setHeaderText(messageService.getFormattedMessage("dialog.deleteConfig.header", selectedConfigName));
        confirmation.setContentText(messageService.getFormattedMessage("dialog.deleteConfig.content", selectedConfigName));
        confirmation.initOwner(getOwnWindow());
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                System.out.println("Attempting to stop active simulations using config: " + selectedConfigName);
                int stoppedCount = this.activeSimulationRegistry.stopSimulations(selectedConfigName);
                System.out.println("Registry stopped " + stoppedCount + " simulation(s).");

                ConfigManager.deleteConfig(selectedConfigName);

                String simsStoppedMsg = (stoppedCount > 0)
                        ? messageService.getFormattedMessage("info.delete.simsStopped", stoppedCount)
                        : messageService.getMessage("info.delete.noSimsStopped");
                alertService.showAlert(IAlertService.AlertType.INFO,
                        messageService.getMessage("info.title"),
                        messageService.getFormattedMessage("info.delete.success", selectedConfigName, simsStoppedMsg));

                recentSimulations.getItems().removeIf(title -> selectedConfigName.equals(extractConfigNameFromTitle(title)));
                loadAvailableConfigs();

            } catch (FileNotFoundException e) {
                alertService.showAlert(IAlertService.AlertType.ERROR,
                        messageService.getMessage("error.title"),
                        messageService.getFormattedMessage("error.delete.config.nf", selectedConfigName));
            } catch (IOException e) {
                alertService.showAlert(IAlertService.AlertType.ERROR,
                        messageService.getMessage("error.title"),
                        messageService.getFormattedMessage("error.delete.config.io", selectedConfigName, e.getMessage()));
                e.printStackTrace();
            } catch (Exception e) {
                alertService.showAlert(IAlertService.AlertType.ERROR,
                        messageService.getMessage("error.title"),
                        messageService.getFormattedMessage("error.delete.config.unexpected", e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the action triggered by clicking the 'Start Simulation' button.
     * Prompts for a simulation run name and launches the simulation window.
     */
    @FXML
    private void handleStartSimulation() {
        if (currentConfig == null) {
            alertService.showAlert(IAlertService.AlertType.WARNING,
                    messageService.getMessage("warning.title"),
                    messageService.getMessage("warning.start.select"));
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Sim_" + currentConfig.getConfigName() + "_" + (System.currentTimeMillis() % 1000));
        dialog.setTitle(messageService.getMessage("dialog.startSim.title"));
        dialog.setHeaderText(messageService.getFormattedMessage("dialog.startSim.header", currentConfig.getConfigName()));
        dialog.setContentText(messageService.getMessage("dialog.startSim.content"));
        dialog.initOwner(getOwnWindow());
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(nameInput -> {
            String simulationName = nameInput.trim();
            if (!simulationName.isEmpty()) {
                startSimulation(simulationName); // Pass only the run name
            } else {
                alertService.showAlert(IAlertService.AlertType.WARNING,
                        messageService.getMessage("warning.title"),
                        messageService.getMessage("warning.start.nameEmpty"));
            }
        });
    }

    // --- Private Helper Methods ---

    /**
     * Called when the ComboBox selection changes. Loads the selected configuration.
     * @param selectedConfigName The name of the newly selected configuration.
     */
    private void handleConfigSelectionChange(String selectedConfigName) {
        if (selectedConfigName != null) {
            try {
                currentConfig = ConfigManager.loadConfig(selectedConfigName);
                startSimulationButton.setDisable(false);
                deleteConfigButton.setDisable(false);
            } catch (IOException e) {
                alertService.showAlert(IAlertService.AlertType.ERROR,
                        messageService.getMessage("error.title"),
                        messageService.getFormattedMessage("error.load.config", selectedConfigName, e.getMessage()));
                startSimulationButton.setDisable(true);
                deleteConfigButton.setDisable(true);
                currentConfig = null;
                configSelect.getSelectionModel().clearSelection();
            }
        } else {
            startSimulationButton.setDisable(true);
            deleteConfigButton.setDisable(true);
            currentConfig = null;
        }
    }

    /**
     * Loads and displays the simulation window (SimulationWindow.fxml).
     * @param simulationRunName The name provided by the user for this specific simulation run.
     */
    private void startSimulation(String simulationRunName) {
        if (currentConfig == null) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Internal Error", "Cannot start simulation: No configuration loaded.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SimulationWindow.fxml"));
            if (loader.getLocation() == null) { throw new IOException("Cannot find FXML file: /fxml/SimulationWindow.fxml"); }

            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == SimulationWindowController.class) {
                    return new SimulationWindowController(
                            this.alertService,
                            this.simulationInitializer,
                            this.activeSimulationRegistry,
                            this.messageService
                    );
                } else {
                    try { return controllerClass.getDeclaredConstructor().newInstance(); } catch (Exception e) { throw new RuntimeException(e); }
                }
            });

            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMinWidth(900); stage.setMinHeight(700);

            SimulationWindowController controller = loader.getController();
            // Pass the config, stage, and run name to the simulation controller
            controller.setupAndRunSimulation(currentConfig, stage, simulationRunName);

            // Create display title for recent list
            String displayTitle = messageService.getFormattedMessage("simulation.window.title.format", simulationRunName, currentConfig.getConfigName());
            updateRecentSimulationsList(displayTitle);

            stage.show();

        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.open.simWindow", e.getMessage()));
            e.printStackTrace();
        } catch (Exception e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.sim.setup.unexpected", e.getMessage()));
            e.printStackTrace();
        }
    }


    /**
     * Updates the list of recent simulations displayed in the ListView.
     * @param simulationDisplayTitle The formatted title to display (RunName - (ConfigName)).
     */
    private void updateRecentSimulationsList(String simulationDisplayTitle) {
        recentSimulations.getItems().remove(simulationDisplayTitle);
        recentSimulations.getItems().addFirst(simulationDisplayTitle);
        if (recentSimulations.getItems().size() > AppConstants.MAX_RECENT_SIMULATIONS) {
            recentSimulations.getItems().remove(AppConstants.MAX_RECENT_SIMULATIONS, recentSimulations.getItems().size());
        }
    }

    /**
     * Extracts the configuration name from a simulation window title string.
     * Assumes the format from "simulation.window.title.format" key.
     * @param simulationTitle The title string from the recentSimulations list.
     * @return The extracted configuration name or a fallback string.
     */
    private String extractConfigNameFromTitle(String simulationTitle) {
        if (simulationTitle == null) return "Unknown Config";
        int separatorIndex = simulationTitle.lastIndexOf(" - (");
        if (separatorIndex != -1 && simulationTitle.endsWith(")")) {
            return simulationTitle.substring(separatorIndex + 4, simulationTitle.length() - 1);
        }
        return "Unknown Config";
    }

    /**
     * Reloads the list of available configurations from ConfigManager.
     */
    private void loadAvailableConfigs() {
        String previouslySelected = configSelect.getSelectionModel().getSelectedItem();
        try {
            List<String> configs = ConfigManager.getAvailableConfigs();
            configSelect.getItems().setAll(configs);

            if (previouslySelected != null && configs.contains(previouslySelected)) {
                configSelect.getSelectionModel().select(previouslySelected);
            } else {
                configSelect.getSelectionModel().clearSelection();
            }
            handleConfigSelectionChange(configSelect.getSelectionModel().getSelectedItem());

        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.load.configs", e.getMessage()));
            configSelect.getItems().clear();
            handleConfigSelectionChange(null);
        }
    }

    /**
     * Helper method to get the Window containing this controller's scene.
     * Uses a reliable element within the scene graph.
     * @return The owning Window, or null if not available.
     */
    private Window getOwnWindow() {
        if (contentAreaPane != null && contentAreaPane.getScene() != null) {
            return contentAreaPane.getScene().getWindow();
        }
        if (configSelect != null && configSelect.getScene() != null) {
            return configSelect.getScene().getWindow();
        }
        System.err.println("Warning: Could not determine owner window in MainWindowController.");
        return null;
    }
}