package proj.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import proj.model.maps.MapVariant;
import proj.model.movement.MovementVariant;
import proj.model.genotype.MutationVariant;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.SimulationProperties;

import java.io.IOException;

public class ConfigEditorController {
    @FXML private TextField configNameField;
    @FXML private ComboBox<MapVariant> mapVariant;
    @FXML private ComboBox<MovementVariant> movementVariant;
    @FXML private ComboBox<MutationVariant> mutationVariant;
    @FXML private ComboBox<VegetationVariant> vegetationVariant;
    @FXML private Spinner<Integer> widthSpinner;
    @FXML private Spinner<Integer> heightSpinner;
    @FXML private Spinner<Integer> animalCountSpinner;
    @FXML private Spinner<Integer> startEnergySpinner;
    @FXML private CheckBox saveStatistics;

    private Runnable onConfigSaved;
    private String lastSavedConfigName;

    @FXML
    public void initialize() {
        // Initialize ComboBoxes
        mapVariant.getItems().addAll(MapVariant.values());
        movementVariant.getItems().addAll(MovementVariant.values());
        mutationVariant.getItems().addAll(MutationVariant.values());
        vegetationVariant.getItems().addAll(VegetationVariant.values());

        // Initialize Spinners
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 200, 50));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 200, 30));
        animalCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 20));
        startEnergySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));

        // Set default values
        mapVariant.setValue(MapVariant.GLOBE);
        movementVariant.setValue(MovementVariant.PREDESTINED);
        mutationVariant.setValue(MutationVariant.RANDOM);
        vegetationVariant.setValue(VegetationVariant.FORESTED_EQUATOR);
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        String configName = configNameField.getText();
        SimulationProperties config = new SimulationProperties(
                configName,
                100, // genotypeSize
                movementVariant.getValue(),
                mutationVariant.getValue(),
                mapVariant.getValue(),
                vegetationVariant.getValue(),
                widthSpinner.getValue(),
                heightSpinner.getValue(),
                4,  // equatorHeight
                animalCountSpinner.getValue(),
                100, // plantCount
                10,  // plantsPerDay
                startEnergySpinner.getValue(),
                5,   // plantEnergy
                10,  // energyNeededToReproduce
                5,   // energyToPassToChild
                1,   // energyCostToMove
                100, // simulationStep
                0,   // minimumNumberOfMutations
                0,   // maximumNumberOfMutations
                50,  // waterViolence
                saveStatistics.isSelected()
        );

        try {
            ConfigManager.saveConfig(config);
            this.lastSavedConfigName = configName;
            if (onConfigSaved != null) {
                onConfigSaved.run();
            }
            closeWindow();
        } catch (IOException e) {
            showError("Error saving configuration", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        if (configNameField.getText().isEmpty()) {
            showError("Validation Error", "Configuration name is required.");
            return false;
        }

        if (mapVariant.getValue() == null ||
                movementVariant.getValue() == null ||
                mutationVariant.getValue() == null ||
                vegetationVariant.getValue() == null) {
            showError("Validation Error", "All variant selections are required.");
            return false;
        }

        return true;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        ((Stage) configNameField.getScene().getWindow()).close();
    }

    public void setOnConfigSaved(Runnable callback) {
        this.onConfigSaved = callback;
    }

    public String getLastSavedConfigName() {
        return lastSavedConfigName;
    }
}