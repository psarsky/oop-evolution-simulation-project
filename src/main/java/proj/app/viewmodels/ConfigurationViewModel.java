package proj.app.viewmodels;

import javafx.beans.property.*;
import javafx.scene.control.SpinnerValueFactory;
import proj.model.genotype.MutationVariant;
import proj.model.maps.MapVariant;
import proj.model.movement.MovementVariant;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.SimulationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ViewModel for the Configuration Editor window (ConfigEditor.fxml).
 * It holds the state of the simulation configuration being edited, using JavaFX Properties
 * for easy two-way binding with UI controls (TextFields, ComboBoxes, Spinners, CheckBoxes).
 * It also provides methods to load from and save to {@link SimulationProperties} objects.
 */
public class ConfigurationViewModel {

    // --- Properties for Configuration Settings ---
    // Grouped logically for clarity
    // Basic Info
    private final StringProperty configName = new SimpleStringProperty();
    // Variants
    private final ObjectProperty<MapVariant> mapVariant = new SimpleObjectProperty<>();
    private final ObjectProperty<MovementVariant> movementVariant = new SimpleObjectProperty<>();
    private final ObjectProperty<MutationVariant> mutationVariant = new SimpleObjectProperty<>();
    private final ObjectProperty<VegetationVariant> vegetationVariant = new SimpleObjectProperty<>();
    // Map Dimensions & Features
    private final IntegerProperty width = new SimpleIntegerProperty();
    private final IntegerProperty height = new SimpleIntegerProperty();
    private final IntegerProperty equatorHeight = new SimpleIntegerProperty(); // Relevant for ForestedEquator
    private final IntegerProperty waterViolence = new SimpleIntegerProperty(); // Relevant for WaterWorld
    // Initial Population
    private final IntegerProperty animalCount = new SimpleIntegerProperty();
    private final IntegerProperty plantCount = new SimpleIntegerProperty();
    // Energy Settings
    private final IntegerProperty startEnergy = new SimpleIntegerProperty();
    private final IntegerProperty plantEnergy = new SimpleIntegerProperty();
    private final IntegerProperty energyNeededToReproduce = new SimpleIntegerProperty();
    private final IntegerProperty energyToPassToChild = new SimpleIntegerProperty();
    private final IntegerProperty energyCostToMove = new SimpleIntegerProperty();
    // Genotype & Mutation
    private final IntegerProperty genotypeSize = new SimpleIntegerProperty();
    private final IntegerProperty minimumNumberOfMutations = new SimpleIntegerProperty();
    private final IntegerProperty maximumNumberOfMutations = new SimpleIntegerProperty();
    // Simulation Control & Saving
    private final IntegerProperty plantsPerDay = new SimpleIntegerProperty();
    private final IntegerProperty simulationStep = new SimpleIntegerProperty(); // Delay in ms
    private final BooleanProperty saveStatistics = new SimpleBooleanProperty();

    // --- Internal State for Spinner Management ---
    // Stores the value factories linked to properties to manage spinner updates, especially when loading data.
    private final Map<Property<?>, SpinnerValueFactory<?>> valueFactories = new HashMap<>();

    // --- Property Getters (for binding in Controller) ---
    // These methods expose the JavaFX properties for binding with UI controls.
    public StringProperty configNameProperty() { return configName; }
    public ObjectProperty<MapVariant> mapVariantProperty() { return mapVariant; }
    public ObjectProperty<MovementVariant> movementVariantProperty() { return movementVariant; }
    public ObjectProperty<MutationVariant> mutationVariantProperty() { return mutationVariant; }
    public ObjectProperty<VegetationVariant> vegetationVariantProperty() { return vegetationVariant; }
    public IntegerProperty widthProperty() { return width; }
    public IntegerProperty heightProperty() { return height; }
    public IntegerProperty equatorHeightProperty() { return equatorHeight; }
    public IntegerProperty waterViolenceProperty() { return waterViolence; }
    public IntegerProperty animalCountProperty() { return animalCount; }
    public IntegerProperty plantCountProperty() { return plantCount; }
    public IntegerProperty startEnergyProperty() { return startEnergy; }
    public IntegerProperty plantEnergyProperty() { return plantEnergy; }
    public IntegerProperty energyNeededToReproduceProperty() { return energyNeededToReproduce; }
    public IntegerProperty energyToPassToChildProperty() { return energyToPassToChild; }
    public IntegerProperty energyCostToMoveProperty() { return energyCostToMove; }
    public IntegerProperty genotypeSizeProperty() { return genotypeSize; }
    public IntegerProperty minimumNumberOfMutationsProperty() { return minimumNumberOfMutations; }
    public IntegerProperty maximumNumberOfMutationsProperty() { return maximumNumberOfMutations; }
    public IntegerProperty plantsPerDayProperty() { return plantsPerDay; }
    public IntegerProperty simulationStepProperty() { return simulationStep; }
    public BooleanProperty saveStatisticsProperty() { return saveStatistics; }

    // --- Methods for Managing Spinner Values ---

    /**
     * Associates a JavaFX Property with its corresponding SpinnerValueFactory.
     * This allows the ViewModel to later set the Spinner's displayed value directly
     * via its factory, which is necessary when loading data from a SimulationProperties object.
     *
     * @param property The JavaFX Property (e.g., IntegerProperty).
     * @param factory  The SpinnerValueFactory controlling the Spinner bound to the property.
     */
    public void addValueFactory(Property<?> property, SpinnerValueFactory<?> factory) {
        valueFactories.put(property, factory);
    }

    /**
     * Updates the value of each registered SpinnerValueFactory based on the current
     * value held in its associated ViewModel Property. This is crucial for ensuring
     * that Spinners visually reflect the data loaded into the ViewModel, for example,
     * when editing an existing configuration.
     */
    @SuppressWarnings("unchecked") // Suppress warning for casting SpinnerValueFactory
    public void loadInitialSpinnerValues() {
        valueFactories.forEach((property, factory) -> {
            try {
                if (property instanceof IntegerProperty intProp && factory instanceof SpinnerValueFactory.IntegerSpinnerValueFactory intFactory) {
                    intFactory.setValue(intProp.get());
                } else if (property instanceof DoubleProperty doubleProp && factory instanceof SpinnerValueFactory.DoubleSpinnerValueFactory doubleFactory) {
                    doubleFactory.setValue(doubleProp.get());
                }
                // Add handling for other property/factory types if needed
            } catch (Exception e) {
                // Catch potential errors during value setting (e.g., if property/factory types mismatch unexpectedly)
                System.err.println("Error setting spinner value for property: " + property.getName());
                e.printStackTrace();
            }
        });
    }

    // --- Data Conversion Methods ---

    /**
     * Creates a {@link SimulationProperties} object from the current state of the ViewModel properties.
     * Performs basic validation to ensure required fields are set.
     *
     * @return A {@link SimulationProperties} instance reflecting the current ViewModel state.
     * @throws NullPointerException     if required fields (name, variants) are null.
     * @throws IllegalArgumentException if validation fails (e.g., name is empty).
     */
    public SimulationProperties createSimulationProperties() {
        // --- Input Validation ---
        String name = configName.get();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration name cannot be empty.");
        }
        Objects.requireNonNull(mapVariant.get(), "Map Variant must be selected.");
        Objects.requireNonNull(movementVariant.get(), "Movement Variant must be selected.");
        Objects.requireNonNull(mutationVariant.get(), "Mutation Variant must be selected.");
        Objects.requireNonNull(vegetationVariant.get(), "Vegetation Variant must be selected.");
        // Add other logical validations if needed (e.g., min/max values already handled by spinners, but cross-field checks might be needed)
        if (minimumNumberOfMutations.get() > maximumNumberOfMutations.get()){
            throw new IllegalArgumentException("Minimum mutations cannot exceed maximum mutations.");
        }
        if (energyToPassToChild.get() > energyNeededToReproduce.get()){
            throw new IllegalArgumentException("Energy passed to child cannot exceed energy needed for reproduction.");
        }
        if (equatorHeight.get() > height.get()){
            throw new IllegalArgumentException("Equator height cannot exceed map height.");
        }

        // --- Create Properties Object ---
        return new SimulationProperties(
                name.trim(), // Use trimmed name
                genotypeSize.get(),
                movementVariant.get(),
                mutationVariant.get(),
                mapVariant.get(),
                vegetationVariant.get(),
                width.get(),
                height.get(),
                equatorHeight.get(),
                animalCount.get(),
                plantCount.get(),
                plantsPerDay.get(),
                startEnergy.get(),
                plantEnergy.get(),
                energyNeededToReproduce.get(),
                energyToPassToChild.get(),
                energyCostToMove.get(),
                simulationStep.get(),
                minimumNumberOfMutations.get(),
                maximumNumberOfMutations.get(),
                waterViolence.get(),
                saveStatistics.get()
        );
    }

    /**
     * Populates the ViewModel properties with data from an existing {@link SimulationProperties} object.
     * This is typically used when loading a configuration for editing. After setting the properties,
     * it calls {@link #loadInitialSpinnerValues()} to update the visual state of bound Spinners.
     *
     * @param props The {@link SimulationProperties} object to load data from. Must not be null.
     */
    public void loadFromSimulationProperties(SimulationProperties props) {
        Objects.requireNonNull(props, "Cannot load from null SimulationProperties");

        // --- Set ViewModel Properties ---
        configName.set(props.getConfigName());
        mapVariant.set(props.getMapVariant());
        movementVariant.set(props.getMovementVariant());
        mutationVariant.set(props.getMutationVariant());
        vegetationVariant.set(props.getVegetationVariant());
        width.set(props.getWidth());
        height.set(props.getHeight());
        animalCount.set(props.getAnimalCount());
        startEnergy.set(props.getStartEnergy());
        genotypeSize.set(props.getGenotypeSize());
        equatorHeight.set(props.getEquatorHeight());
        plantCount.set(props.getPlantCount());
        plantsPerDay.set(props.getPlantsPerDay());
        plantEnergy.set(props.getPlantEnergy());
        energyNeededToReproduce.set(props.getEnergyNeededToReproduce());
        energyToPassToChild.set(props.getEnergyToPassToChild());
        energyCostToMove.set(props.getEnergyCostToMove());
        simulationStep.set(props.getSimulationStep());
        minimumNumberOfMutations.set(props.getMinimumNumberOfMutations());
        maximumNumberOfMutations.set(props.getMaximumNumberOfMutations());
        waterViolence.set(props.getWaterViolence());
        saveStatistics.set(props.getSaveStatisticsFlag());

        // --- Update Spinners ---
        // Crucially, update the spinners' displayed values via their factories *after* setting the properties.
        loadInitialSpinnerValues();
    }
}