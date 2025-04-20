package proj.app.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import proj.app.ConfigManager;
import proj.simulation.SimulationProperties;
import proj.app.services.IAlertService;
import proj.app.services.JavaFXAlertService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the main application window (MainWindow.fxml).
 * Handles selection of simulation configurations, starting new simulations,
 * creating new configurations, and deleting existing ones.
 * It also displays a list of recently started simulation runs (for informational purposes only).
 */
public class MainWindowController {

    //<editor-fold desc="FXML Fields">
    @FXML private ComboBox<String> configSelect;
    @FXML private Button startSimulationButton;
    @FXML private Button deleteConfigButton;
    @FXML private ListView<String> recentSimulations; // Displays titles of running/recent simulations
    //</editor-fold>

    private SimulationProperties currentConfig; // Currently loaded configuration
    private IAlertService alertService;
    private static final int MAX_RECENT_SIMULATIONS = 10; // Max items in the recent list

    /**
     * Initializes the controller after FXML loading.
     * Sets up the alert service, loads available configurations into the ComboBox,
     * configures listeners for configuration selection, and disables buttons initially.
     * The recentSimulations list is configured for display purposes only (no interaction).
     */
    @FXML
    public void initialize() {
        this.alertService = new JavaFXAlertService();
        loadAvailableConfigs(); // Populate ComboBox

        // Disable buttons until a config is selected
        startSimulationButton.setDisable(true);
        deleteConfigButton.setDisable(true);

        // Listener for ComboBox selection changes
        configSelect.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            handleConfigSelectionChange(newVal);
        });

        // Configure ListView for display only (no selection actions)
        recentSimulations.setFocusTraversable(false);
        recentSimulations.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // Allow single select visually if needed
        // Do NOT add mouse click listeners for starting simulations from this list.
    }

    // --- Event Handlers ---

    /**
     * Handles the action of clicking the 'New Configuration' button.
     * Opens the configuration editor window (ConfigEditor.fxml) in a new stage.
     * Sets a callback to refresh the config list and select the newly created config upon saving.
     */
    @FXML
    private void handleNewConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfigEditor.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file: /fxml/ConfigEditor.fxml");
            }
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Simulation Configuration");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Block main window while editor is open
            stage.initOwner(configSelect.getScene().getWindow()); // Set owner for the MODAL editor window

            ConfigEditorController controller = loader.getController();

            // Set callback to run after the new config is saved in the editor
            controller.setOnConfigSaved(() -> {
                loadAvailableConfigs(); // Refresh the list in ComboBox
                String lastSaved = controller.getLastSavedConfigName();
                if (lastSaved != null) {
                    configSelect.getSelectionModel().select(lastSaved); // Auto-select the new config
                }
            });

            stage.showAndWait(); // Show editor and wait for it to close

        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Error", "Error opening configuration editor: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Error", "An unexpected error occurred while opening the editor: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Handles the action of clicking the 'Delete Configuration' button.
     * Prompts the user for confirmation before deleting the currently selected configuration file
     * using {@link ConfigManager}. Refreshes the configuration list afterwards.
     */
    @FXML
    private void handleDeleteConfig() {
        String selectedConfigName = configSelect.getSelectionModel().getSelectedItem();
        if (selectedConfigName == null) {
            // Should not happen if button is disabled correctly, but check anyway
            alertService.showAlert(IAlertService.AlertType.WARNING, "Delete Configuration", "Please select a configuration to delete.");
            return;
        }

        // Confirmation Dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Configuration");
        confirmation.setHeaderText("Delete Configuration: " + selectedConfigName);
        confirmation.setContentText("Are you sure you want to permanently delete the configuration file '"
                + selectedConfigName + ".json'? This action cannot be undone.");
        confirmation.initOwner(configSelect.getScene().getWindow()); // Set owner for dialog

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed deletion
            try {
                // 1. Stop and close running simulations using this config FIRST
                System.out.println("Attempting to stop active simulations using config: " + selectedConfigName);
                stopSimulationsUsingConfig(selectedConfigName);

                // 2. Delete the configuration file
                ConfigManager.deleteConfig(selectedConfigName);
                alertService.showAlert(IAlertService.AlertType.INFO, "Success", "Configuration '" + selectedConfigName + "' deleted and associated simulations stopped.");

                // 3. Remove from recent list (optional now, but good practice)
                recentSimulations.getItems().removeIf(title -> selectedConfigName.equals(extractConfigName(title)));

                // 4. Refresh the ComboBox (will clear selection if deleted config was selected)
                loadAvailableConfigs();
            } catch (FileNotFoundException e) {
                alertService.showAlert(IAlertService.AlertType.ERROR, "Error", "Could not delete configuration '" + selectedConfigName + "': File not found.");
            } catch (IOException e) {
                alertService.showAlert(IAlertService.AlertType.ERROR, "Error", "Error deleting configuration '" + selectedConfigName + "': " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                alertService.showAlert(IAlertService.AlertType.ERROR,"Error", "An unexpected error occurred during deletion: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the action of clicking the 'Start Simulation' button.
     * Verifies that a configuration is selected, prompts the user for a unique name for the simulation run,
     * and then calls {@link #startSimulation(String)} to launch the simulation window.
     */
    @FXML
    private void handleStartSimulation() {
        if (currentConfig == null) {
            alertService.showAlert(IAlertService.AlertType.WARNING, "Start Simulation", "Please select a configuration from the list first.");
            return;
        }

        // Prompt user for a name for this specific simulation instance
        TextInputDialog dialog = new TextInputDialog("Simulation_" + currentConfig.getConfigName() + "_" + (System.currentTimeMillis() % 1000));
        dialog.setTitle("Start New Simulation");
        dialog.setHeaderText("Starting simulation with config: " + currentConfig.getConfigName());
        dialog.setContentText("Enter a name for this simulation run:");
        dialog.initOwner(configSelect.getScene().getWindow()); // Set owner for dialog

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(nameInput -> {
            String simulationName = nameInput.trim();
            if (!simulationName.isEmpty()) {
                // Create a unique title for the window including the run name and config name
                String simulationTitle = simulationName + " - (" + currentConfig.getConfigName() + ")";
                startSimulation(simulationTitle); // Launch the simulation window
            } else {
                alertService.showAlert(IAlertService.AlertType.WARNING, "Invalid Name", "Simulation run name cannot be empty.");
            }
        });
        // If user cancels, do nothing.
    }


    // --- Private Helper Methods ---

    /**
     * Loads the currently selected configuration when the ComboBox selection changes.
     * Enables or disables action buttons based on whether a valid configuration is loaded.
     *
     * @param selectedConfigName The name of the newly selected configuration, or null if cleared.
     */
    private void handleConfigSelectionChange(String selectedConfigName) {
        if (selectedConfigName != null) {
            try {
                currentConfig = ConfigManager.loadConfig(selectedConfigName);
                startSimulationButton.setDisable(false);
                deleteConfigButton.setDisable(false); // Enable delete button
            } catch (IOException e) {
                alertService.showAlert(IAlertService.AlertType.ERROR, "Load Error", "Error loading configuration '" + selectedConfigName + "': " + e.getMessage());
                startSimulationButton.setDisable(true);
                deleteConfigButton.setDisable(true);
                currentConfig = null;
                configSelect.getSelectionModel().clearSelection(); // Clear invalid selection
            }
        } else {
            // No selection or cleared selection
            startSimulationButton.setDisable(true);
            deleteConfigButton.setDisable(true);
            currentConfig = null;
        }
    }

    /**
     * Loads the simulation window (SimulationWindow.fxml), initializes its controller
     * with the currently selected configuration (`currentConfig`), and displays the window in a new stage.
     * Adds the simulation title to the 'recent simulations' list.
     * REMOVED the initOwner call to allow independent window layering.
     *
     * @param simulationTitle The unique title to be displayed on the simulation window.
     *                        Should contain the config name parsable by `extractConfigName`.
     */
    private void startSimulation(String simulationTitle) {
        if (currentConfig == null) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Internal Error", "Cannot start simulation: No configuration is loaded.");
            return;
        }

        // Double-check config name consistency (optional but safe)
        String configNameFromTitle = extractConfigName(simulationTitle);
        if (!currentConfig.getConfigName().equals(configNameFromTitle)) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Configuration Mismatch",
                    "Internal Error: Window title implies config '" + configNameFromTitle +
                            "', but the loaded config is '" + currentConfig.getConfigName() + "'.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SimulationWindow.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file: /fxml/SimulationWindow.fxml");
            }
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(simulationTitle);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(700);

            // --- THE CHANGE IS HERE: initOwner is removed ---
            // stage.initOwner(configSelect.getScene().getWindow()); // REMOVED

            SimulationWindowController controller = loader.getController();
            stage.setUserData(controller);
            // Pass the loaded config and the stage itself to the controller
            controller.setupAndRunSimulation(currentConfig, stage);

            // Update the informational 'recent simulations' list
            updateRecentSimulationsList(simulationTitle);

            stage.show(); // Show the simulation window as an independent window

        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Error", "Error opening simulation window: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Error", "An unexpected error occurred during simulation setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds the title of a newly started simulation to the top of the recentSimulations ListView,
     * removing duplicates and trimming the list if it exceeds the maximum size.
     *
     * @param simulationTitle The title of the simulation run to add.
     */
    private void updateRecentSimulationsList(String simulationTitle) {
        recentSimulations.getItems().remove(simulationTitle); // Remove if already exists (e.g., rerun)
        recentSimulations.getItems().addFirst(simulationTitle); // Add to the top

        // Limit the list size
        if (recentSimulations.getItems().size() > MAX_RECENT_SIMULATIONS) {
            recentSimulations.getItems().remove(MAX_RECENT_SIMULATIONS, recentSimulations.getItems().size());
        }
        // Do not select the item, list is informational only
    }

    /**
     * Attempts to extract the configuration name from a simulation window title.
     * Assumes the title format is "Simulation Run Name - (ConfigName)".
     *
     * @param simulationTitle The title string from the recentSimulations list or Stage.
     * @return The extracted configuration name, or a default string ("Unknown Config") if parsing fails.
     */
    private String extractConfigName(String simulationTitle) {
        if (simulationTitle == null) return "Unknown Config";
        // Look for the pattern " - (" near the end
        int separatorIndex = simulationTitle.lastIndexOf(" - (");
        if (separatorIndex != -1 && simulationTitle.endsWith(")")) {
            // Extract the text between " - (" and the closing ")"
            return simulationTitle.substring(separatorIndex + 4, simulationTitle.length() - 1);
        }
        // Log if format is unexpected? Optional, maybe just return default.
        // System.err.println("Could not extract config name from title: " + simulationTitle);
        return "Unknown Config"; // Fallback
    }

    /**
     * Reloads the list of available configuration names from the {@link ConfigManager}
     * and updates the ComboBox. It attempts to preserve the current selection if possible.
     * Disables action buttons if no configurations are available or if loading fails.
     */
    private void loadAvailableConfigs() {
        String previouslySelected = configSelect.getSelectionModel().getSelectedItem();
        try {
            List<String> configs = ConfigManager.getAvailableConfigs();
            configSelect.getItems().setAll(configs); // Update ComboBox items

            if (previouslySelected != null && configs.contains(previouslySelected)) {
                // Restore previous selection if it still exists
                configSelect.getSelectionModel().select(previouslySelected);
                handleConfigSelectionChange(previouslySelected); // Reload config and update buttons
            } else {
                // Clear selection if previous doesn't exist or was null
                configSelect.getSelectionModel().clearSelection();
                handleConfigSelectionChange(null); // Clear loaded config and disable buttons
            }
        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Load Error", "Error loading available configurations: " + e.getMessage());
            configSelect.getItems().clear();
            handleConfigSelectionChange(null); // Clear loaded config and disable buttons
        }
    }

    /**
     * Finds and stops all running simulation windows that were started with the specified configuration name.
     * It iterates through all open JavaFX Stages, checks their titles, and if a match is found,
     * retrieves the stored `SimulationWindowController` from the Stage's UserData to call its stop method.
     * Finally, it closes the Stage.
     *
     * @param configNameToStop The name of the configuration whose associated simulation windows should be stopped.
     */
    private void stopSimulationsUsingConfig(String configNameToStop) {
        int stoppedCount = 0;
        // Iterate over a copy of the windows list to avoid ConcurrentModificationException if closing modifies the list
        List<Window> openWindows = new ArrayList<>(Window.getWindows());

        for (Window window : openWindows) {
            // We are only interested in Stages that might be our simulation windows
            if (window instanceof Stage stage) {
                String title = stage.getTitle();
                if (title != null) {
                    String windowConfigName = extractConfigName(title); // Check if title matches format
                    if (configNameToStop.equals(windowConfigName)) {
                        // This window uses the configuration being deleted
                        Object userData = stage.getUserData(); // Retrieve controller stored earlier
                        if (userData instanceof SimulationWindowController controller) {
                            try {
                                System.out.println("Stopping simulation in window: " + title);
                                controller.stopSimulationThreads(); // Gracefully stop simulation threads
                                // Closing the stage should ideally happen after threads stop,
                                // but closing it here ensures immediate UI feedback.
                                // RunLater might be needed if stopSimulationThreads takes time and blocks UI thread.
                                Platform.runLater(stage::close); // Close the window on the FX thread
                                stoppedCount++;
                            } catch (Exception e) {
                                System.err.println("Error stopping/closing simulation window '" + title + "': " + e.getMessage());
                                e.printStackTrace();
                                // Attempt to close anyway?
                                Platform.runLater(stage::close);
                            }
                        } else {
                            // Log if we found a matching window but couldn't get controller
                            System.err.println("Warning: Window '" + title + "' matched config '" + configNameToStop + "' but controller instance was not found in UserData.");
                        }
                    }
                }
            }
        }
        if (stoppedCount > 0) {
            System.out.println("Stopped and closed " + stoppedCount + " simulation window(s) using config '" + configNameToStop + "'.");
        } else {
            System.out.println("No running simulation windows found using config '" + configNameToStop + "'.");
        }
    }

    /** Helper method to get the current window, useful for setting dialog owners. */
    private Window getOwnWindow() {
        return configSelect.getScene().getWindow();
    }
}