package proj.app.controllers;

import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import proj.app.ConfigManager;
import proj.app.viewmodels.ConfigurationViewModel;
import proj.model.maps.MapVariant;
import proj.model.movement.MovementVariant;
import proj.model.genotype.MutationVariant;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.SimulationProperties;

import java.io.IOException;
import java.util.Objects;

/**
 * Controller for the Configuration Editor window (ConfigEditor.fxml).
 * Handles user input for creating or modifying simulation configurations,
 * validates the input, and saves the configuration using {@link ConfigManager}.
 * Uses {@link ConfigurationViewModel} for data binding and state management.
 */
public class ConfigEditorController {

    //<editor-fold desc="FXML Fields">
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
    //</editor-fold>

    private ConfigurationViewModel viewModel;
    private Runnable onConfigSaved; // Callback executed after successful save
    private String lastSavedConfigName; // To select the saved config in MainWindow

    /**
     * Initializes the controller after FXML loading.
     * Sets up the ViewModel, populates ComboBoxes, binds UI controls to ViewModel properties,
     * and configures Spinners with appropriate value factories and ranges.
     */
    @FXML
    public void initialize() {
        viewModel = new ConfigurationViewModel();

        // Populate ComboBoxes
        mapVariant.getItems().addAll(MapVariant.values());
        movementVariant.getItems().addAll(MovementVariant.values());
        mutationVariant.getItems().addAll(MutationVariant.values());
        vegetationVariant.getItems().addAll(VegetationVariant.values());

        // Set default values in ViewModel (can also be done in ViewModel constructor)
        setViewModelDefaults();

        // Bind UI controls to ViewModel properties
        bindControlsToViewModel();

        // Load initial values from ViewModel into Spinners' value factories
        viewModel.loadInitialSpinnerValues();
    }

    /**
     * Loads an existing configuration into the editor for modification.
     * Populates the ViewModel with the data from the provided {@link SimulationProperties}.
     *
     * @param config The {@link SimulationProperties} object to load. Must not be null.
     */
    public void loadConfiguration(SimulationProperties config) {
        Objects.requireNonNull(config, "Cannot load null configuration");
        viewModel.loadFromSimulationProperties(config);
        // Spinner values are updated automatically via binding and loadInitialSpinnerValues call in loadFrom...
    }

    /**
     * Sets a callback function to be executed when a configuration is successfully saved.
     *
     * @param callback The {@link Runnable} to execute on save.
     */
    public void setOnConfigSaved(Runnable callback) {
        this.onConfigSaved = callback;
    }

    /**
     * Retrieves the name of the last configuration that was successfully saved by this editor instance.
     * Returns null if no configuration has been saved yet.
     *
     * @return The name of the last saved configuration, or null.
     */
    public String getLastSavedConfigName() {
        return lastSavedConfigName;
    }

    // --- Event Handlers ---

    /**
     * Handles the action of clicking the 'Save' button.
     * Validates the inputs, creates a {@link SimulationProperties} object from the ViewModel,
     * saves it using {@link ConfigManager}, executes the save callback, and closes the editor window.
     * Shows error alerts if validation or saving fails.
     */
    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return; // Stop if validation fails
        }

        try {
            // Create SimulationProperties directly from the ViewModel
            SimulationProperties config = viewModel.createSimulationProperties();

            ConfigManager.saveConfig(config);
            this.lastSavedConfigName = config.getConfigName(); // Store name for potential use by caller

            // Execute callback if provided
            if (onConfigSaved != null) {
                onConfigSaved.run();
            }

            closeWindow(); // Close editor on successful save

        } catch (IOException e) {
            showError("Save Error", "Error saving configuration file: " + e.getMessage());
        } catch (NumberFormatException e) {
            // Should be less likely with Spinners, but good practice
            showError("Validation Error", "Invalid number format encountered.");
        } catch (NullPointerException | IllegalArgumentException e) {
            // Catch potential errors from createSimulationProperties or validation
            showError("Validation Error", e.getMessage());
            e.printStackTrace(); // Log for debugging
        } catch (Exception e) {
            // Catch unexpected errors
            showError("Unexpected Error", "An unexpected error occurred during save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the action of clicking the 'Cancel' button.
     * Closes the configuration editor window without saving.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // --- Private Helper Methods ---

    /**
     * Sets default values for the ViewModel properties.
     * These values will be used when creating a new configuration.
     */
    private void setViewModelDefaults() {
        // Sensible defaults
        viewModel.configNameProperty().set("NewConfig");
        viewModel.mapVariantProperty().set(MapVariant.GLOBE);
        viewModel.movementVariantProperty().set(MovementVariant.PREDESTINED);
        viewModel.mutationVariantProperty().set(MutationVariant.RANDOM);
        viewModel.vegetationVariantProperty().set(VegetationVariant.FORESTED_EQUATOR);
        viewModel.widthProperty().set(50);
        viewModel.heightProperty().set(50);
        viewModel.animalCountProperty().set(20);
        viewModel.startEnergyProperty().set(100);
        viewModel.genotypeSizeProperty().set(10);
        viewModel.equatorHeightProperty().set(5);
        viewModel.plantCountProperty().set(50);
        viewModel.plantsPerDayProperty().set(5);
        viewModel.plantEnergyProperty().set(25);
        viewModel.energyNeededToReproduceProperty().set(50);
        viewModel.energyToPassToChildProperty().set(25);
        viewModel.energyCostToMoveProperty().set(1);
        viewModel.simulationStepProperty().set(100); // ms per step
        viewModel.minimumNumberOfMutationsProperty().set(1);
        viewModel.maximumNumberOfMutationsProperty().set(3);
        viewModel.waterViolenceProperty().set(50); // Only relevant for WaterWorld
        viewModel.saveStatisticsProperty().set(false);
    }

    /**
     * Binds the UI controls (TextField, ComboBoxes, Spinners, CheckBox) to their
     * corresponding properties in the {@link ConfigurationViewModel}.
     */
    private void bindControlsToViewModel() {
        // Text Fields and ComboBoxes
        configNameField.textProperty().bindBidirectional(viewModel.configNameProperty());
        mapVariant.valueProperty().bindBidirectional(viewModel.mapVariantProperty());
        movementVariant.valueProperty().bindBidirectional(viewModel.movementVariantProperty());
        mutationVariant.valueProperty().bindBidirectional(viewModel.mutationVariantProperty());
        vegetationVariant.valueProperty().bindBidirectional(viewModel.vegetationVariantProperty());
        saveStatistics.selectedProperty().bindBidirectional(viewModel.saveStatisticsProperty());

        // Spinners (requires setting ValueFactory first, then binding the property)
        setupSpinnerBinding(widthSpinner, 10, 200, viewModel.widthProperty());
        setupSpinnerBinding(heightSpinner, 10, 200, viewModel.heightProperty());
        setupSpinnerBinding(animalCountSpinner, 1, 1000, viewModel.animalCountProperty());
        setupSpinnerBinding(startEnergySpinner, 1, 1000, viewModel.startEnergyProperty());
        setupSpinnerBinding(genotypeSizeSpinner, 5, 100, viewModel.genotypeSizeProperty());
        setupSpinnerBinding(equatorHeightSpinner, 1, 100, viewModel.equatorHeightProperty());
        setupSpinnerBinding(plantCountSpinner, 0, 1000, viewModel.plantCountProperty());
        setupSpinnerBinding(plantsPerDaySpinner, 0, 100, viewModel.plantsPerDayProperty());
        setupSpinnerBinding(plantEnergySpinner, 1, 1000, viewModel.plantEnergyProperty());
        setupSpinnerBinding(energyNeededToReproduceSpinner, 1, 1000, viewModel.energyNeededToReproduceProperty());
        setupSpinnerBinding(energyToPassToChildSpinner, 1, 1000, viewModel.energyToPassToChildProperty());
        setupSpinnerBinding(energyCostToMoveSpinner, 0, 100, viewModel.energyCostToMoveProperty());
        setupSpinnerBinding(simulationStepSpinner, 10, 1000, viewModel.simulationStepProperty()); // ms
        setupSpinnerBinding(minimumNumberOfMutationsSpinner, 0, 100, viewModel.minimumNumberOfMutationsProperty());
        setupSpinnerBinding(maximumNumberOfMutationsSpinner, 0, 100, viewModel.maximumNumberOfMutationsProperty());
        setupSpinnerBinding(waterViolenceSpinner, 0, 100, viewModel.waterViolenceProperty()); // Percentage
    }


    /**
     * Sets up the binding between an Integer Spinner and an IntegerProperty in the ViewModel.
     * Creates an IntegerSpinnerValueFactory with the specified min/max values and binds
     * the factory's value property to the ViewModel's property. Also registers the factory
     * with the ViewModel for later value retrieval.
     *
     * @param spinner  The Spinner<Integer> UI control.
     * @param min      The minimum allowed value for the spinner.
     * @param max      The maximum allowed value for the spinner.
     * @param property The IntegerProperty in the ViewModel to bind to.
     */
    private void setupSpinnerBinding(Spinner<Integer> spinner, int min, int max, IntegerProperty property) {
        // Ensure initial value is within bounds
        int initialValue = Math.max(min, Math.min(max, property.get()));
        property.set(initialValue); // Update property if it was out of bounds

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue);
        spinner.setValueFactory(valueFactory);

        // Bind the ViewModel property *to* the spinner's value factory value.
        // Changes in the spinner update the factory, which then updates the bound property.
        property.bind(valueFactory.valueProperty());

        // Store the value factory in the ViewModel. This allows the ViewModel
        // to set the spinner's value directly when loading data (e.g., editing config).
        viewModel.addValueFactory(property, valueFactory);
    }


    /**
     * Validates the user inputs in the form fields.
     * Checks for required fields and logical consistency (e.g., min mutations <= max mutations).
     * Uses data directly from the ViewModel for validation.
     *
     * @return {@code true} if all inputs are valid, {@code false} otherwise.
     */
    private boolean validateInputs() {
        // Check required fields
        if (viewModel.configNameProperty().get() == null || viewModel.configNameProperty().get().trim().isEmpty()) {
            showError("Validation Error", "Configuration name cannot be empty.");
            return false;
        }
        if (viewModel.mapVariantProperty().get() == null ||
                viewModel.movementVariantProperty().get() == null ||
                viewModel.mutationVariantProperty().get() == null ||
                viewModel.vegetationVariantProperty().get() == null) {
            showError("Validation Error", "Please select a variant for Map, Movement, Mutation, and Vegetation.");
            return false;
        }

        // Check logical consistency
        if (viewModel.minimumNumberOfMutationsProperty().get() > viewModel.maximumNumberOfMutationsProperty().get()) {
            showError("Validation Error", "Minimum number of mutations cannot be greater than the maximum number.");
            return false;
        }
        if (viewModel.energyToPassToChildProperty().get() > viewModel.energyNeededToReproduceProperty().get()) {
            showError("Validation Error", "Energy passed to child cannot exceed the energy required to reproduce.");
            return false;
        }
        // Add more validation rules as needed (e.g., equator height vs map height)
        if (viewModel.equatorHeightProperty().get() > viewModel.heightProperty().get()){
            showError("Validation Error", "Equator height cannot be greater than map height.");
            return false;
        }


        return true; // All checks passed
    }

    /**
     * Displays an error message dialog to the user.
     *
     * @param title   The title of the error dialog window.
     * @param content The main error message content.
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Closes the configuration editor window.
     */
    private void closeWindow() {
        // Get the stage (window) containing any UI element (e.g., configNameField) and close it.
        Stage stage = (Stage) configNameField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        } else {
            System.err.println("Warning: Could not find stage to close ConfigEditor window.");
        }
    }
}