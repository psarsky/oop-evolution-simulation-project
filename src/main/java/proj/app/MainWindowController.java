package proj.app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import proj.app.SimulationWindowController;
import java.io.IOException;
import java.util.Optional;

public class MainWindowController {
    @FXML private ComboBox<String> configSelect;
    @FXML private Button newConfigButton;
    @FXML private Button startSimulationButton;
    @FXML private ListView<String> recentSimulations;

    private SimulationConfig currentConfig;

    @FXML
    public void initialize() {
        loadAvailableConfigs();
        configSelect.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    currentConfig = ConfigManager.loadConfig(newVal);
                    startSimulationButton.setDisable(false);
                } catch (IOException e) {
                    showError("Error loading configuration", e.getMessage());
                }
            }
        });

        // Initialize recent simulations list
        recentSimulations.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double click
                String selected = recentSimulations.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadAndStartSimulation(selected);
                }
            }
        });
    }

    @FXML
    private void handleNewConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfigEditor.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Configuration");
            stage.setScene(new Scene(root));

            ConfigEditorController controller = loader.getController();
            controller.setOnConfigSaved(() -> {
                loadAvailableConfigs();
                // Optionally select the newly created config
                configSelect.getSelectionModel().select(controller.getLastSavedConfigName());
            });

            stage.show();
        } catch (IOException e) {
            showError("Error opening configuration editor", e.getMessage());
        }
    }

    @FXML
    private void handleStartSimulation() {
        if (currentConfig == null) {
            showError("Configuration Error", "Please select a configuration first.");
            return;
        }

        // Ask for simulation name
        TextInputDialog dialog = new TextInputDialog("Simulation " + System.currentTimeMillis());
        dialog.setTitle("New Simulation");
        dialog.setHeaderText("Enter a name for this simulation:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String simulationName = result.get();
            startSimulation(simulationName);
        }
    }

    private void startSimulation(String simulationName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SimulationWindow.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(simulationName + " - " + currentConfig.getConfigName());
            stage.setScene(new Scene(root));

            SimulationWindowController controller = loader.getController();
            controller.initializeSimulation(currentConfig);

            // Add to recent simulations
            recentSimulations.getItems().add(0, simulationName);
            // Keep only last 10 simulations
            if (recentSimulations.getItems().size() > 10) {
                recentSimulations.getItems().remove(10, recentSimulations.getItems().size());
            }

            stage.show();
        } catch (IOException e) {
            showError("Error starting simulation", e.getMessage());
        }
    }

    private void loadAndStartSimulation(String simulationName) {
        if (currentConfig != null) {
            startSimulation(simulationName);
        } else {
            showError("Configuration Error", "Please select a configuration first.");
        }
    }

    private void loadAvailableConfigs() {
        try {
            configSelect.getItems().clear();
            configSelect.getItems().addAll(ConfigManager.getAvailableConfigs());
        } catch (IOException e) {
            showError("Error loading configurations", e.getMessage());
        }
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}