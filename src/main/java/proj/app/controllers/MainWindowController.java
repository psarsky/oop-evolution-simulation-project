package proj.app.controllers;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import proj.app.ActiveSimulationRegistry;
import proj.app.AppConstants; // Use constants
import proj.app.ConfigManager;
import proj.app.SimulationInitializer;
import proj.app.services.IAlertService;
import proj.app.services.IMessageService; // Import message service
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
 * Uses constants from {@link AppConstants}.
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
    //</editor-fold>

    //<editor-fold desc="Dependencies (Injected)">
    private final IAlertService alertService;
    private final ActiveSimulationRegistry activeSimulationRegistry;
    private final SimulationInitializer simulationInitializer;
    private final Gson gson; // Needed by SimulationInitializer implicitly
    private final IMessageService messageService; // Injected message service
    //</editor-fold>

    private SimulationProperties currentConfig;
    // Use constant for max recent items
    // private static final int MAX_RECENT_SIMULATIONS = AppConstants.MAX_RECENT_SIMULATIONS; // Defined in AppConstants

    /**
     * Constructs the MainWindowController with injected dependencies.
     * These dependencies are essential for the controller's operations, such as
     * showing alerts, managing simulation instances, initializing new simulations,
     * and retrieving UI text.
     *
     * @param alertService             The {@link IAlertService} instance used for displaying messages and errors to the user. Must not be null.
     * @param activeSimulationRegistry The singleton {@link ActiveSimulationRegistry} instance used to track and stop active simulation windows. Must not be null.
     * @param simulationInitializer    The {@link SimulationInitializer} instance used when creating and setting up components for a new simulation window. Must not be null.
     * @param gson                     The shared {@link Gson} instance, primarily needed by the {@link SimulationInitializer} for components it creates. Must not be null.
     * @param messageService           The {@link IMessageService} instance used for retrieving localized or configured UI strings. Must not be null.
     * @throws NullPointerException if any injected dependency is null.
     */
    public MainWindowController(IAlertService alertService,
                                ActiveSimulationRegistry activeSimulationRegistry,
                                SimulationInitializer simulationInitializer,
                                Gson gson,
                                IMessageService messageService) { // Added messageService
        this.alertService = Objects.requireNonNull(alertService, "AlertService cannot be null");
        this.activeSimulationRegistry = Objects.requireNonNull(activeSimulationRegistry, "ActiveSimulationRegistry cannot be null");
        this.simulationInitializer = Objects.requireNonNull(simulationInitializer, "SimulationInitializer cannot be null");
        this.gson = Objects.requireNonNull(gson, "Gson instance cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null"); // Store message service
    }

    /**
     * Initializes the controller after FXML loading and dependency injection.
     * This method is automatically called by the FXMLLoader. It loads the list of available
     * configurations into the ComboBox, sets up listeners for selection changes,
     * disables action buttons initially, configures the recent simulations list display,
     * and sets localized text for static UI elements using the injected {@link IMessageService}.
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
        recentSimulations.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // Visual only
        // Set localized text
        setUIText();
    }

    /** Sets static text elements using the MessageService. */
    private void setUIText() {
        selectConfigLabel.setText(messageService.getMessage("main.label.selectConfig"));
        recentSimulationsLabel.setText(messageService.getMessage("main.window.recent.simulations.label"));
        startSimulationButton.setText(messageService.getMessage("main.button.start"));
        newConfigButton.setText(messageService.getMessage("main.button.newConfig"));
        deleteConfigButton.setText(messageService.getMessage("main.button.deleteConfig"));
        // Note: Main window title is set in App.java using messageService
    }

    // --- Event Handlers ---

    /**
     * Handles the action triggered by clicking the 'New Configuration' button.
     * It loads the configuration editor (ConfigEditor.fxml) into a new modal window.
     * A ControllerFactory is used to inject the required {@link IAlertService} and
     * {@link IMessageService} into the {@link ConfigEditorController}. It sets a callback
     * on the editor controller to refresh the configuration list in this window when the
     * new configuration is saved. Uses {@link IMessageService} for window title and errors.
     */
    @FXML
    private void handleNewConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfigEditor.fxml"));
            if (loader.getLocation() == null) { throw new IOException("Cannot find FXML file: /fxml/ConfigEditor.fxml"); }

            // Use ControllerFactory to inject both services into ConfigEditorController
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == ConfigEditorController.class) {
                    return new ConfigEditorController(this.alertService, this.messageService);
                } else { // Default behavior for other potential controllers
                    try { return controllerClass.getDeclaredConstructor().newInstance(); } catch (Exception e) { throw new RuntimeException(e); }
                }
            });

            Parent root = loader.load();
            Stage stage = new Stage();
            // Use message service for title
            stage.setTitle(messageService.getMessage("config.editor.title.new"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Block main window
            stage.initOwner(getOwnWindow()); // Set parent window

            ConfigEditorController controller = loader.getController(); // Get controller instance created by factory
            // Set callback to refresh list on save
            controller.setOnConfigSaved(() -> {
                loadAvailableConfigs();
                String lastSaved = controller.getLastSavedConfigName();
                if (lastSaved != null) { configSelect.getSelectionModel().select(lastSaved); } // Auto-select new config
            });

            stage.showAndWait(); // Show modal editor window and wait

        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.open.configEditor", e.getMessage()));
            e.printStackTrace();
        } catch (Exception e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.unexpected", e.getMessage())); // Use generic unexpected error key
            e.printStackTrace();
        }
    }

    /**
     * Handles the action triggered by clicking the 'Delete Configuration' button.
     * It confirms the action with the user via a localized dialog. If confirmed, it uses the injected
     * {@link ActiveSimulationRegistry} to stop any running simulations associated with
     * the selected configuration, then deletes the configuration file using {@link ConfigManager}.
     * Finally, it updates the UI (ComboBox, recent list) and shows localized feedback using the
     * injected {@link IAlertService} and {@link IMessageService}.
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

        // Confirmation Dialog using localized text
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(messageService.getMessage("dialog.deleteConfig.title"));
        confirmation.setHeaderText(messageService.getFormattedMessage("dialog.deleteConfig.header", selectedConfigName));
        confirmation.setContentText(messageService.getFormattedMessage("dialog.deleteConfig.content", selectedConfigName));
        confirmation.initOwner(getOwnWindow());
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Use injected registry to stop simulations
                System.out.println("Attempting to stop active simulations using config: " + selectedConfigName);
                int stoppedCount = this.activeSimulationRegistry.stopSimulations(selectedConfigName);
                System.out.println("Registry stopped " + stoppedCount + " simulation(s).");

                // Delete config file
                ConfigManager.deleteConfig(selectedConfigName);

                // Format localized success message
                String simsStoppedMsg = (stoppedCount > 0)
                        ? messageService.getFormattedMessage("info.delete.simsStopped", stoppedCount)
                        : messageService.getMessage("info.delete.noSimsStopped");
                alertService.showAlert(IAlertService.AlertType.INFO,
                        messageService.getMessage("info.title"),
                        messageService.getFormattedMessage("info.delete.success", selectedConfigName, simsStoppedMsg));

                // Update UI
                recentSimulations.getItems().removeIf(title -> selectedConfigName.equals(extractConfigNameFromTitle(title)));
                loadAvailableConfigs(); // Refreshes ComboBox and resets selection/buttons if needed

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
     * It first verifies that a configuration has been loaded. If so, it prompts the user
     * to enter a name for this specific simulation run using a localized dialog. If a valid name
     * is provided, it calls {@link #startSimulation(String)} to launch the simulation window.
     * Uses the injected {@link IAlertService} and {@link IMessageService} for warnings or errors.
     */
    @FXML
    private void handleStartSimulation() {
        if (currentConfig == null) {
            alertService.showAlert(IAlertService.AlertType.WARNING,
                    messageService.getMessage("warning.title"),
                    messageService.getMessage("warning.start.select"));
            return;
        }

        // Prompt for a name for this simulation run using localized text
        TextInputDialog dialog = new TextInputDialog("Sim_" + currentConfig.getConfigName() + "_" + (System.currentTimeMillis() % 1000));
        dialog.setTitle(messageService.getMessage("dialog.startSim.title"));
        dialog.setHeaderText(messageService.getFormattedMessage("dialog.startSim.header", currentConfig.getConfigName()));
        dialog.setContentText(messageService.getMessage("dialog.startSim.content"));
        dialog.initOwner(getOwnWindow());
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(nameInput -> {
            String simulationName = nameInput.trim();
            if (!simulationName.isEmpty()) {
                // Construct the initial title; SimWindowController will format it using message service
                String simulationTitle = simulationName + " - (" + currentConfig.getConfigName() + ")";
                startSimulation(simulationTitle); // Launch the simulation window
            } else {
                alertService.showAlert(IAlertService.AlertType.WARNING,
                        messageService.getMessage("warning.title"),
                        messageService.getMessage("warning.start.nameEmpty"));
            }
        });
    }

    // --- Private Helper Methods ---

    /**
     * Called when the ComboBox selection changes. Loads the selected configuration properties
     * using {@link ConfigManager} and enables/disables action buttons accordingly.
     * Uses the injected {@link IAlertService} and {@link IMessageService} to show errors if loading fails.
     *
     * @param selectedConfigName The name of the configuration selected in the ComboBox, or null if the selection is cleared.
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
                // Reset state on error
                startSimulationButton.setDisable(true);
                deleteConfigButton.setDisable(true);
                currentConfig = null;
                configSelect.getSelectionModel().clearSelection();
            }
        } else {
            // No selection, disable buttons and clear loaded config
            startSimulationButton.setDisable(true);
            deleteConfigButton.setDisable(true);
            currentConfig = null;
        }
    }

    /**
     * Loads and displays the simulation window (SimulationWindow.fxml) in a new, non-modal stage.
     * It uses a ControllerFactory to inject the required dependencies ({@link IAlertService},
     * {@link SimulationInitializer}, {@link ActiveSimulationRegistry}, {@link IMessageService}) into the
     * {@link SimulationWindowController} instance. It then calls the simulation controller's
     * {@code setupAndRunSimulation} method to initialize and start the simulation display and logic.
     * Finally, it updates the list of recent simulations displayed in this window. Uses {@link IMessageService} for errors.
     *
     * @param simulationTitle The unique title to be initially set on the simulation window. Should not be null or empty.
     *                        (Note: SimulationWindowController might reformat this title using message service).
     */
    private void startSimulation(String simulationTitle) {
        if (currentConfig == null) { // Should be handled by button state, but defensive check
            alertService.showAlert(IAlertService.AlertType.ERROR, "Internal Error", "Cannot start simulation: No configuration loaded.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SimulationWindow.fxml"));
            if (loader.getLocation() == null) { throw new IOException("Cannot find FXML file: /fxml/SimulationWindow.fxml"); }

            // Use ControllerFactory to inject all necessary dependencies
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == SimulationWindowController.class) {
                    return new SimulationWindowController(
                            this.alertService,
                            this.simulationInitializer,
                            this.activeSimulationRegistry,
                            this.messageService // Pass message service
                    );
                } else { // Default behavior for other controllers
                    try { return controllerClass.getDeclaredConstructor().newInstance(); } catch (Exception e) { throw new RuntimeException(e); }
                }
            });

            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(simulationTitle); // Set initial title
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMinWidth(900); stage.setMinHeight(700);

            SimulationWindowController controller = loader.getController(); // Get the created controller
            // Initialize and start the simulation within the new window
            controller.setupAndRunSimulation(currentConfig, stage);

            updateRecentSimulationsList(simulationTitle); // Add to local recent list
            stage.show(); // Show the independent simulation window

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
     * Adds the title of a newly started simulation to the top of the recentSimulations ListView,
     * removing duplicates and trimming the list if it exceeds the limit defined by {@link AppConstants}.
     *
     * @param simulationTitle The title of the simulation run (used as the display string).
     */
    private void updateRecentSimulationsList(String simulationTitle) {
        recentSimulations.getItems().remove(simulationTitle); // Remove if already present (e.g., rerun)
        recentSimulations.getItems().addFirst(simulationTitle); // Add to the top
        // Limit list size using constant
        if (recentSimulations.getItems().size() > AppConstants.MAX_RECENT_SIMULATIONS) {
            recentSimulations.getItems().remove(AppConstants.MAX_RECENT_SIMULATIONS, recentSimulations.getItems().size());
        }
    }

    /**
     * Extracts the configuration name from a simulation window title string.
     * Assumes the format "Run Name - (ConfigName)". Used only for filtering the recent list display.
     *
     * @param simulationTitle The title string from the recentSimulations list. Can be null.
     * @return The extracted configuration name as a String, or "Unknown Config" if parsing fails or input is null.
     */
    private String extractConfigNameFromTitle(String simulationTitle) {
        if (simulationTitle == null) return "Unknown Config";
        // Find the last occurrence of " - (" to handle potential dashes in run name
        int separatorIndex = simulationTitle.lastIndexOf(" - (");
        // Check if separator was found and the string ends with ")"
        if (separatorIndex != -1 && simulationTitle.endsWith(")")) {
            // Extract the part between " - (" and ")"
            return simulationTitle.substring(separatorIndex + 4, simulationTitle.length() - 1);
        }
        return "Unknown Config"; // Fallback if format doesn't match
    }

    /**
     * Reloads the list of available configuration names from {@link ConfigManager}
     * and updates the ComboBox items. Attempts to preserve the previously selected item if it still exists.
     * Disables action buttons if no configurations are available or if loading fails.
     * Uses the injected {@link IAlertService} and {@link IMessageService} to display loading errors.
     */
    private void loadAvailableConfigs() {
        String previouslySelected = configSelect.getSelectionModel().getSelectedItem();
        try {
            List<String> configs = ConfigManager.getAvailableConfigs();
            configSelect.getItems().setAll(configs); // Replace items in ComboBox

            // Restore selection if possible
            if (previouslySelected != null && configs.contains(previouslySelected)) {
                configSelect.getSelectionModel().select(previouslySelected);
                // No need to call handleConfigSelectionChange here, selection listener will fire
            } else {
                configSelect.getSelectionModel().clearSelection(); // Clear if previous selection gone
                // handleConfigSelectionChange(null) will be called by listener
            }
            // Update button states based on whether anything is selected now
            handleConfigSelectionChange(configSelect.getSelectionModel().getSelectedItem());

        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.load.configs", e.getMessage()));
            configSelect.getItems().clear(); // Clear items on error
            handleConfigSelectionChange(null); // Ensure buttons are disabled
        }
    }

    /**
     * Helper method to get the {@link Window} (Stage) containing this controller's scene.
     * Useful for setting the owner of modal dialogs (like confirmation or input dialogs).
     *
     * @return The owning {@link Window}, or {@code null} if the scene or window is not yet available (e.g., during early initialization).
     */
    private Window getOwnWindow() {
        // Ensure controls are initialized and have a scene/window
        if (configSelect != null && configSelect.getScene() != null) {
            return configSelect.getScene().getWindow();
        }
        return null; // Fallback if called too early or UI is detached
    }
}