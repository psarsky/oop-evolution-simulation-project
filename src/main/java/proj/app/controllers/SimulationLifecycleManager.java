package proj.app.controllers;

// Imports needed for ReadOnly*Wrapper and ReadOnly*Property
import javafx.beans.property.BooleanProperty; // Keep for return type clarity in Javadoc? Or change to ReadOnlyBooleanProperty
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty; // Import ReadOnly version
import javafx.beans.property.ReadOnlyBooleanWrapper; // Import Wrapper
import javafx.beans.property.SimpleBooleanProperty; // Keep if used elsewhere, otherwise remove
import javafx.beans.property.SimpleDoubleProperty;
import proj.app.AppConstants; // Use constants
import proj.app.SimulationController;
import proj.simulation.SimulationProperties;

import java.util.Objects;

/**
 * Manages UI interactions related to the simulation's lifecycle control (pause/resume, speed).
 * Uses JavaFX properties for UI binding and communicates speed changes to the {@link SimulationController}.
 * Uses constants from {@link AppConstants} for slider and delay bounds.
 * Exposes paused and canControl states as ReadOnly properties externally.
 */
public class SimulationLifecycleManager {

    // Use constants for slider/delay mapping sourced from AppConstants
    private static final double SLIDER_MIN_VAL = AppConstants.SPEED_SLIDER_MIN;
    private static final double SLIDER_MAX_VAL = AppConstants.SPEED_SLIDER_MAX;
    private static final double SLIDER_DEFAULT_VAL = AppConstants.SPEED_SLIDER_DEFAULT;
    // Delay bounds used for clamping mapping results (must match SimulationController)
    private static final long MIN_DELAY_MS = AppConstants.MIN_STEP_DELAY_MS;
    private static final long MAX_DELAY_MS = AppConstants.MAX_STEP_DELAY_MS;

    private final long nominalDelayMs; // Nominal delay from SimulationProperties, clamped
    private final SimulationController simulationController;

    // --- JavaFX Properties for Binding ---
    // Use ReadOnly*Wrapper for internal control, expose ReadOnly*Property externally
    /** Property indicating if the simulation is currently paused (true) or running (false). Internally writable via wrapper. */
    private final ReadOnlyBooleanWrapper pausedProperty = new ReadOnlyBooleanWrapper(false); // Initially running
    /** Property indicating if the simulation controls should be enabled (true if not stopped). Internally writable via wrapper. */
    private final ReadOnlyBooleanWrapper canControlProperty = new ReadOnlyBooleanWrapper(true); // Initially controllable
    /** Property bound bidirectionally to the speed slider's value in the UI. Remains mutable. */
    private final SimpleDoubleProperty speedSliderValueProperty = new SimpleDoubleProperty(SLIDER_DEFAULT_VAL); // Default slider value

    /**
     * Constructs the SimulationLifecycleManager.
     * Initializes slider value based on properties and sets up listener for slider changes.
     *
     * @param simulationController The {@link SimulationController} instance to manage. Must not be null.
     * @param properties           The {@link SimulationProperties} used to determine nominal speed. Must not be null.
     * @throws NullPointerException if simulationController or properties is null.
     */
    public SimulationLifecycleManager(SimulationController simulationController, SimulationProperties properties) {
        this.simulationController = Objects.requireNonNull(simulationController, "SimulationController cannot be null");
        Objects.requireNonNull(properties, "SimulationProperties cannot be null");

        // Set nominal delay, clamped by global bounds
        this.nominalDelayMs = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, properties.getSimulationStep()));

        // Set initial slider value based on ACTUAL clamped starting delay
        long actualStartDelay = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, properties.getSimulationStep()));
        this.speedSliderValueProperty.set(mapDelayToSliderValue(actualStartDelay));

        // Listener updates SimulationController delay when slider changes
        this.speedSliderValueProperty.addListener((obs, oldVal, newVal) -> {
            if (this.simulationController != null && newVal != null) {
                long calculatedDelay = mapSliderValueToDelay(newVal.doubleValue());
                this.simulationController.setStepDelay(calculatedDelay);
            }
        });
        updateProperties(); // Set initial property states
    }

    /** Toggles the simulation between paused and running states, if controllable. */
    public void togglePause() {
        if (simulationController.isStopped()) return;
        simulationController.togglePause();
        updateProperties();
    }

    /**
     * Temporarily pauses the simulation if running, for actions like snapshots.
     *
     * @return {@code true} if the simulation was running and is now paused by this call, {@code false} otherwise.
     */
    public boolean pauseForAction() {
        if (simulationController.isRunning()) {
            simulationController.togglePause();
            updateProperties();
            try { Thread.sleep(50); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            return true;
        }
        return false;
    }

    /** Resumes the simulation if it was paused by {@link #pauseForAction()} and hasn't stopped. */
    public void resumeAfterAction() {
        if (!simulationController.isStopped() && !simulationController.isRunning()) {
            simulationController.togglePause();
            updateProperties();
        }
    }

    /** Updates UI control states when the simulation ends naturally. */
    public void simulationEnded() {
        updateProperties();
    }

    /** Updates internal JavaFX properties based on the SimulationController's state. */
    private void updateProperties() {
        boolean isStopped = simulationController.isStopped();
        boolean isRunning = simulationController.isRunning();
        // Use the wrapper's set() method for internal updates
        pausedProperty.set(!isRunning && !isStopped);
        canControlProperty.set(!isStopped);
    }

    // --- Property Getters (for UI binding) ---

    /**
     * Returns the read-only JavaFX property representing the paused state of the simulation.
     * UI elements (like the Play/Pause button text) can bind to this property.
     *
     * @return The {@link ReadOnlyBooleanProperty} indicating the paused state (true if paused, false if running or stopped).
     */
    public ReadOnlyBooleanProperty pausedProperty() {
        // Return the read-only view from the wrapper
        return pausedProperty.getReadOnlyProperty();
    }

    /**
     * Returns the read-only JavaFX property indicating whether the simulation lifecycle controls
     * (Play/Pause button, Speed slider) should be enabled. Controls are typically disabled
     * once the simulation has permanently stopped.
     *
     * @return The {@link ReadOnlyBooleanProperty} indicating controllability (true if not stopped, false if stopped).
     */
    public ReadOnlyBooleanProperty canControlProperty() {
        // Return the read-only view from the wrapper
        return canControlProperty.getReadOnlyProperty();
    }

    /**
     * Returns the JavaFX property representing the current value of the speed control slider.
     * This property should be bound bidirectionally to the slider in the UI. Changes to this
     * property (from the slider) trigger updates to the simulation delay via an internal listener.
     *
     * @return The {@link DoubleProperty} holding the slider's current value (typically 0-100).
     */
    public DoubleProperty speedSliderValueProperty() {
        // This one remains mutable for bidirectional binding
        return speedSliderValueProperty;
    }

    // --- Private Speed Mapping Methods (using constants from AppConstants) ---

    /**
     * Maps the UI slider value to a simulation step delay in milliseconds.
     * Uses constants from {@link AppConstants} for ranges.
     *
     * @param sliderValue The current value from the speed slider UI element.
     * @return The calculated simulation step delay in milliseconds, clamped within defined bounds.
     */
    private long mapSliderValueToDelay(double sliderValue) {
        // ... (Implementation remains the same) ...
        double delay;
        long currentNominal = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, this.nominalDelayMs));
        if (sliderValue <= SLIDER_DEFAULT_VAL) {
            double range = MAX_DELAY_MS - currentNominal;
            double proportion = (SLIDER_DEFAULT_VAL == SLIDER_MIN_VAL) ? 0.0 : sliderValue / SLIDER_DEFAULT_VAL;
            proportion = Math.max(0.0, Math.min(1.0, proportion));
            delay = MAX_DELAY_MS - proportion * range;
        } else {
            double range = currentNominal - MIN_DELAY_MS;
            double proportion = (SLIDER_DEFAULT_VAL == SLIDER_MAX_VAL) ? 1.0 :
                    (sliderValue - SLIDER_DEFAULT_VAL) / (SLIDER_MAX_VAL - SLIDER_DEFAULT_VAL);
            proportion = Math.max(0.0, Math.min(1.0, proportion));
            delay = currentNominal - proportion * range;
        }
        return (long) Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, Math.round(delay)));
    }

    /**
     * Maps a simulation step delay (ms) back to the corresponding UI slider value.
     * Uses constants from {@link AppConstants}.
     *
     * @param delayMs The delay in milliseconds (will be clamped).
     * @return The corresponding slider value, clamped within the slider's defined min/max range.
     */
    private double mapDelayToSliderValue(long delayMs) {
        // ... (Implementation remains the same) ...
        delayMs = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, delayMs));
        long currentNominal = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, this.nominalDelayMs));
        double sliderValue;
        if (delayMs >= currentNominal) {
            double range = MAX_DELAY_MS - currentNominal;
            if (range <= 0) { sliderValue = SLIDER_DEFAULT_VAL; }
            else {
                double proportion = (double)(MAX_DELAY_MS - delayMs) / range;
                sliderValue = SLIDER_MIN_VAL + proportion * (SLIDER_DEFAULT_VAL - SLIDER_MIN_VAL);
            }
        } else {
            double range = currentNominal - MIN_DELAY_MS;
            if (range <= 0) { sliderValue = SLIDER_DEFAULT_VAL; }
            else {
                double proportion = (double)(currentNominal - delayMs) / range;
                sliderValue = SLIDER_DEFAULT_VAL + proportion * (SLIDER_MAX_VAL - SLIDER_DEFAULT_VAL);
            }
        }
        return Math.max(SLIDER_MIN_VAL, Math.min(SLIDER_MAX_VAL, sliderValue));
    }
}