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
    @FXML private Spinner<Integer> genotypeSizeSpinner;
    @FXML private Spinner<Integer> equatorHeightSpinner;
    @FXML private Spinner<Integer> plantCountSpinner;
    @FXML private Spinner<Integer> plantsPerDaySpinner;
    @FXML private Spinner<Integer> plantEnergySpinner;
    @FXML private Spinner<Integer> energyNeededToReproduceSpinner;
    @FXML private Spinner<Integer> energyToPassToChildSpinner;
    @FXML private Spinner<Integer> energyCostToMoveSpinner;
    @FXML private Spinner<Integer> simulationStepSpinner;
    @FXML private Spinner<Integer> minimumNumberOfMutationsSpinner;
    @FXML private Spinner<Integer> maximumNumberOfMutationsSpinner;
    @FXML private Spinner<Integer> waterViolenceSpinner;
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
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 200, 50));
        animalCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 50));
        startEnergySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 20));
        genotypeSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 50));
        equatorHeightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 4));
        plantCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100));
        plantsPerDaySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));
        plantEnergySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));
        energyNeededToReproduceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));
        energyToPassToChildSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 5));
        energyCostToMoveSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        simulationStepSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 400, 100));
        minimumNumberOfMutationsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 10));
        maximumNumberOfMutationsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 50));
        waterViolenceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 50));

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
                genotypeSizeSpinner.getValue(),
                movementVariant.getValue(),
                mutationVariant.getValue(),
                mapVariant.getValue(),
                vegetationVariant.getValue(),
                widthSpinner.getValue(),
                heightSpinner.getValue(),
                equatorHeightSpinner.getValue(),
                animalCountSpinner.getValue(),
                plantCountSpinner.getValue(),
                plantsPerDaySpinner.getValue(),
                startEnergySpinner.getValue(),
                plantEnergySpinner.getValue(),
                energyNeededToReproduceSpinner.getValue(),
                energyToPassToChildSpinner.getValue(),
                energyCostToMoveSpinner.getValue(),
                simulationStepSpinner.getValue(),
                minimumNumberOfMutationsSpinner.getValue(),
                maximumNumberOfMutationsSpinner.getValue(),
                waterViolenceSpinner.getValue(),
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