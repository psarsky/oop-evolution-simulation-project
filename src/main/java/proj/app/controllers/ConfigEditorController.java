package proj.app.controllers;

import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
// Removed: import proj.app.AppConstants; // Only needed if spinner bounds were constants
import proj.app.ConfigManager;
import proj.app.services.IAlertService;
import proj.app.services.IMessageService; // Import message service
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
 * Handles user input for creating or modifying simulation configurations via data binding
 * with a {@link ConfigurationViewModel}. It validates the input and uses {@link ConfigManager}
 * to save the configuration. Uses injected {@link IAlertService} and {@link IMessageService}
 * for displaying error messages and setting UI text.
 */
public class ConfigEditorController {

    //<editor-fold desc="FXML Fields">
    // Define FXML variables for controls that need programmatic access (e.g., setting text)
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
    @FXML private Button cancelButton; // Add fx:id="cancelButton" in FXML
    @FXML private Button saveButton;   // Add fx:id="saveButton" in FXML
    // Add Labels if their text needs to be set dynamically via message service
    // @FXML private Label configNameLabel;
    // @FXML private Label mapSettingsLabel;
    // ... etc.
    //</editor-fold>

    private ConfigurationViewModel viewModel;
    private Runnable onConfigSaved;
    private String lastSavedConfigName;

    private final IAlertService alertService; // Injected dependency
    private final IMessageService messageService; // Injected dependency

    /**
     * Constructs the ConfigEditorController with injected dependencies.
     *
     * @param alertService   The {@link IAlertService} instance used for displaying alerts
     *                       (e.g., validation errors, save errors). Must not be null.
     * @param messageService The {@link IMessageService} instance used for retrieving localized
     *                       or configured UI strings (e.g., button text, error messages). Must not be null.
     * @throws NullPointerException if alertService or messageService is null.
     */
    public ConfigEditorController(IAlertService alertService, IMessageService messageService) {
        this.alertService = Objects.requireNonNull(alertService, "AlertService cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");
    }

    /**
     * Initializes the controller after FXML loading and dependency injection.
     * This method is automatically called by the FXMLLoader. It sets up the
     * ViewModel, populates ComboBox choices, binds UI controls to ViewModel properties,
     * sets default values for a new configuration, configures the Spinners, and sets
     * localized text for UI elements using the injected {@link IMessageService}.
     */
    @FXML
    public void initialize() {
        viewModel = new ConfigurationViewModel();
        // Populate combo boxes
        mapVariant.getItems().addAll(MapVariant.values());
        movementVariant.getItems().addAll(MovementVariant.values());
        mutationVariant.getItems().addAll(MutationVariant.values());
        vegetationVariant.getItems().addAll(VegetationVariant.values());
        // Set defaults and bind
        setViewModelDefaults();
        bindControlsToViewModel();
        viewModel.loadInitialSpinnerValues();
        // Set UI Text using Message Service
        setUIText();
    }

    /** Sets text for labels (if needed) and buttons using the injected MessageService. */
    private void setUIText() {
        // Set text for elements whose text might need localization or configuration.
        // If labels in FXML have static text, this isn't needed for them.
        // Example (Requires labels to have fx:id):
        // configNameLabel.setText(messageService.getMessage("config.label.configName"));
        // mapSettingsLabel.setText(messageService.getMessage("config.label.mapSettings"));
        // ... set text for all other relevant labels ...

        // Set CheckBox and Button text
        saveStatistics.setText(messageService.getMessage("config.checkbox.saveStats"));
        cancelButton.setText(messageService.getMessage("config.button.cancel"));
        saveButton.setText(messageService.getMessage("config.button.save"));

        // Set window title (if needed, though usually set when stage is created)
        // Stage stage = (Stage) cancelButton.getScene().getWindow(); // Need node reference
        // if (stage != null) stage.setTitle(messageService.getMessage("config.editor.title.new")); // Or edit title
    }

    /**
     * Loads an existing configuration into the editor UI for modification.
     * It populates the internal {@link ConfigurationViewModel} with data from the provided
     * {@link SimulationProperties} object, which in turn updates the bound UI controls.
     * Also updates the window title to indicate editing.
     *
     * @param config The {@link SimulationProperties} object containing the configuration data to load. Must not be null.
     * @throws NullPointerException if config is null.
     */
    public void loadConfiguration(SimulationProperties config) {
        Objects.requireNonNull(config, "Cannot load null configuration");
        viewModel.loadFromSimulationProperties(config);

        // Update window title for editing
        // Need a reference to the stage, usually obtained after initialization
        // This logic might be better placed where the stage is created/shown.
        // If called after stage is shown:
        // Stage stage = (Stage) saveButton.getScene().getWindow();
        // if(stage != null) {
        //     stage.setTitle(messageService.getMessage("config.editor.title.edit"));
        // }
    }

    /**
     * Sets a callback function to be executed when a configuration is successfully saved
     * via the {@link #handleSave()} method. This allows the calling window (e.g., MainWindowController)
     * to react to the save event, for example, by refreshing its list of configurations.
     *
     * @param callback The {@link Runnable} task to execute after a successful save operation. Can be null if no callback is needed.
     */
    public void setOnConfigSaved(Runnable callback) {
        this.onConfigSaved = callback;
    }

    /**
     * Retrieves the name of the last configuration that was successfully saved by this editor instance
     * during its current lifecycle. This is useful for the calling window to know which configuration
     * was just created or edited.
     *
     * @return The name ({@link String}) of the last saved configuration, or {@code null} if no configuration has been saved yet in this instance.
     */
    public String getLastSavedConfigName() {
        return lastSavedConfigName;
    }

    /**
     * Handles the action triggered by clicking the 'Save' button.
     * It first validates the user inputs using {@link #validateInputs()}. If validation passes,
     * it creates a {@link SimulationProperties} object from the ViewModel, attempts to save it
     * using {@link ConfigManager}, stores the saved name, executes the {@code onConfigSaved} callback
     * if set, and finally closes the editor window. Errors during validation or saving are displayed
     * using the injected {@link IAlertService} with messages retrieved via {@link IMessageService}.
     */
    @FXML
    private void handleSave() {
        if (!validateInputs()) { // validateInputs now uses messageService for errors
            return;
        }
        try {
            SimulationProperties config = viewModel.createSimulationProperties();
            ConfigManager.saveConfig(config);
            this.lastSavedConfigName = config.getConfigName();
            if (onConfigSaved != null) {
                onConfigSaved.run();
            }
            closeWindow();
        } catch (IOException e) {
            showErrorUsingMessages("error.title", "error.save.config", e.getMessage());
            e.printStackTrace(); // Keep stack trace for detailed debugging
        } catch (NumberFormatException e) {
            showErrorUsingMessages("warning.title", "error.validation.numberFormat");
            e.printStackTrace();
        } catch (NullPointerException | IllegalArgumentException e) {
            // Use the exception message itself for validation errors from createSimulationProperties
            showError(messageService.getMessage("warning.title"), e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showErrorUsingMessages("error.title", "error.unexpected", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the action triggered by clicking the 'Cancel' button.
     * Closes the configuration editor window without attempting to save any changes.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // --- Private Helper Methods ---

    /** Sets default values in the ViewModel for a new configuration. */
    private void setViewModelDefaults() {
        // Implementation unchanged from previous step
        viewModel.configNameProperty().set("NewConfig");
        viewModel.mapVariantProperty().set(MapVariant.GLOBE);
        viewModel.movementVariantProperty().set(MovementVariant.PREDESTINED);
        viewModel.mutationVariantProperty().set(MutationVariant.RANDOM);
        viewModel.vegetationVariantProperty().set(VegetationVariant.FORESTED_EQUATOR);
        viewModel.widthProperty().set(50); viewModel.heightProperty().set(50);
        viewModel.animalCountProperty().set(20); viewModel.startEnergyProperty().set(100);
        viewModel.genotypeSizeProperty().set(10); viewModel.equatorHeightProperty().set(5);
        viewModel.plantCountProperty().set(50); viewModel.plantsPerDayProperty().set(5);
        viewModel.plantEnergyProperty().set(25); viewModel.energyNeededToReproduceProperty().set(50);
        viewModel.energyToPassToChildProperty().set(25); viewModel.energyCostToMoveProperty().set(1);
        viewModel.simulationStepProperty().set(100); viewModel.minimumNumberOfMutationsProperty().set(1);
        viewModel.maximumNumberOfMutationsProperty().set(3); viewModel.waterViolenceProperty().set(50);
        viewModel.saveStatisticsProperty().set(false);
    }

    /** Binds UI controls (TextField, ComboBoxes, Spinners) to ViewModel properties. */
    private void bindControlsToViewModel() {
        // Implementation unchanged from previous step
        configNameField.textProperty().bindBidirectional(viewModel.configNameProperty());
        mapVariant.valueProperty().bindBidirectional(viewModel.mapVariantProperty());
        movementVariant.valueProperty().bindBidirectional(viewModel.movementVariantProperty());
        mutationVariant.valueProperty().bindBidirectional(viewModel.mutationVariantProperty());
        vegetationVariant.valueProperty().bindBidirectional(viewModel.vegetationVariantProperty());
        saveStatistics.selectedProperty().bindBidirectional(viewModel.saveStatisticsProperty());

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
        setupSpinnerBinding(simulationStepSpinner, 10, 1000, viewModel.simulationStepProperty());
        setupSpinnerBinding(minimumNumberOfMutationsSpinner, 0, 100, viewModel.minimumNumberOfMutationsProperty());
        setupSpinnerBinding(maximumNumberOfMutationsSpinner, 0, 100, viewModel.maximumNumberOfMutationsProperty());
        setupSpinnerBinding(waterViolenceSpinner, 0, 100, viewModel.waterViolenceProperty());
    }

    /** Sets up bidirectional binding between an Integer Spinner and an IntegerProperty. */
    private void setupSpinnerBinding(Spinner<Integer> spinner, int min, int max, IntegerProperty property) {
        // Implementation unchanged from previous step
        int initialValue = Math.max(min, Math.min(max, property.get()));
        property.set(initialValue);
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue);
        spinner.setValueFactory(valueFactory);
        property.bind(valueFactory.valueProperty());
        viewModel.addValueFactory(property, valueFactory);
    }

    /**
     * Validates user inputs held in the ViewModel. Checks for empty name, missing variant selections,
     * and logical inconsistencies (e.g., min/max mutations). Displays localized error/warning messages
     * using {@link #showErrorUsingMessages(String, String, Object...)} if validation fails.
     *
     * @return {@code true} if all inputs are valid, {@code false} otherwise.
     */
    private boolean validateInputs() {
        // Use message keys for error messages
        if (viewModel.configNameProperty().get() == null || viewModel.configNameProperty().get().trim().isEmpty()) {
            showErrorUsingMessages("warning.title", "error.validation.nameEmpty");
            return false;
        }
        if (viewModel.mapVariantProperty().get() == null ||
                viewModel.movementVariantProperty().get() == null ||
                viewModel.mutationVariantProperty().get() == null ||
                viewModel.vegetationVariantProperty().get() == null) {
            showErrorUsingMessages("warning.title", "error.validation.variantMissing");
            return false;
        }
        if (viewModel.minimumNumberOfMutationsProperty().get() > viewModel.maximumNumberOfMutationsProperty().get()) {
            showErrorUsingMessages("warning.title", "error.validation.mutationOrder");
            return false;
        }
        if (viewModel.energyToPassToChildProperty().get() > viewModel.energyNeededToReproduceProperty().get()) {
            showErrorUsingMessages("warning.title", "error.validation.energyPass");
            return false;
        }
        if (viewModel.equatorHeightProperty().get() > viewModel.heightProperty().get()){
            showErrorUsingMessages("warning.title", "error.validation.equatorHeight");
            return false;
        }
        return true; // All checks passed
    }

    /**
     * Displays an error/warning dialog using the injected alert service and message service.
     * Determines the alert type based on whether the titleKey corresponds to a warning title.
     *
     * @param titleKey   The resource key for the dialog title.
     * @param contentKey The resource key for the dialog content message.
     * @param args       Optional arguments for formatting the content message.
     */
    private void showErrorUsingMessages(String titleKey, String contentKey, Object... args) {
        String title = messageService.getMessage(titleKey);
        String content = messageService.getFormattedMessage(contentKey, args);
        // Determine alert type based on title key (simple heuristic)
        IAlertService.AlertType type = titleKey.contains("warning")
                ? IAlertService.AlertType.WARNING
                : IAlertService.AlertType.ERROR;
        alertService.showAlert(type, title, content);
    }

    /**
     * Displays an error/warning dialog using the injected alert service with a literal content string.
     * Used when the error message comes directly from an exception or isn't suitable for resource bundle.
     *
     * @param title   The literal title for the dialog window.
     * @param content The literal content message.
     */
    private void showError(String title, String content) {
        // Reuse the logic from showErrorUsingMessages for determining AlertType based on title
        IAlertService.AlertType type = title.equalsIgnoreCase(messageService.getMessage("warning.title"))
                ? IAlertService.AlertType.WARNING
                : IAlertService.AlertType.ERROR;
        alertService.showAlert(type, title, content);
    }


    /** Closes the configuration editor window by obtaining the stage from a contained control. */
    private void closeWindow() {
        // Obtain the stage from any control within the scene
        Stage stage = (Stage) saveButton.getScene().getWindow(); // Use saveButton or any other @FXML control
        if (stage != null) {
            stage.close();
        } else {
            // This might happen if called before the stage is fully initialized or if the control reference is wrong
            System.err.println("Warning: Could not find stage to close ConfigEditor window.");
        }
    }
}