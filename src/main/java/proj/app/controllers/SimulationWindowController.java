// ============================================================
// SOURCE FILE: proj/app/controllers/SimulationWindowController.java
// ============================================================

package proj.app.controllers;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import proj.app.controllers.handlers.CanvasInteractionHandler;
import proj.app.state.SimulationStateProducer;
import proj.app.state.SimulationStateQueue;
import proj.app.state.SimulationStateSnapshot;
import proj.app.SimulationStatisticsSnapshot;
import proj.app.render.MapRenderer;
import proj.app.services.IAlertService;
import proj.app.services.IFileSaveService;
import proj.app.services.JavaFXAlertService;
import proj.app.services.JavaFXFileSaveService;
import proj.app.viewmodels.SelectedAnimalViewModel;
import proj.app.viewmodels.StatisticsViewModel;
import proj.model.elements.Animal;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;
import proj.app.SimulationController;
import proj.app.SimulationFactory;
import proj.app.StatisticsManager;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Controller for the Simulation Window (SimulationWindow.fxml).
 * Manages the visualization and interaction with a running simulation instance.
 * Coordinates the simulation controller thread, state production, rendering, UI updates,
 * statistics display, and user actions like pausing/resuming, saving snapshots,
 * selecting animals, and dynamically adjusting simulation speed via a slider.
 */
public class SimulationWindowController {

    //<editor-fold desc="FXML Fields">
    @FXML private Canvas simulationCanvas;
    @FXML private ScrollPane canvasScrollPane;
    @FXML private Button playPauseButton;
    @FXML private Slider speedSlider; // Slider to control simulation speed
    @FXML private Button collectingDataButton;
    @FXML private Button takeSnapshotButton;
    // Statistics Panel
    @FXML private Label dayCount;
    @FXML private Label animalCount;
    @FXML private Label plantCount;
    @FXML private Label emptyFieldsCount;
    @FXML private Label averageEnergy;
    @FXML private Label averageLifespan;
    @FXML private Label averageChildren;
    @FXML private TextArea popularGenotypes;
    // Selected Animal Panel
    @FXML private Label selectedAnimalEnergy;
    @FXML private Label selectedAnimalAge;
    @FXML private Label selectedAnimalChildren;
    @FXML private Label selectedAnimalPlantsEaten;
    @FXML private Label selectedAnimalGenotype;
    @FXML private Label selectedAnimalActiveGene;
    @FXML private Label selectedAnimalDescendants;
    @FXML private Label selectedAnimalDeathDate;
    //</editor-fold>

    //<editor-fold desc="Simulation Core Components">
    private Simulation simulation;
    private SimulationController simulationController;
    private SimulationProperties simProps;
    private SimulationStateQueue stateQueue;
    private SimulationStateProducer stateProducer;
    private StatisticsManager statisticsManager;
    private MapRenderer mapRenderer;
    //</editor-fold>

    //<editor-fold desc="UI & Interaction Components">
    private StatisticsViewModel statisticsViewModel;
    private SelectedAnimalViewModel selectedAnimalViewModel;
    private IFileSaveService fileSaveService;
    private IAlertService alertService;
    private CanvasInteractionHandler canvasInteractionHandler;
    private AnimationTimer animationTimer;
    private SimulationStateSnapshot lastProcessedSnapshot = null;
    //</editor-fold>

    //<editor-fold desc="State & Constants">
    private static final int MAX_STATE_QUEUE_SIZE = 5;
    private static final long UI_UPDATE_INTERVAL_NANOS = 16_666_666L; // ~60 FPS

    // Speed Slider Constants
    private static final double SLIDER_MIN_VAL = 0.0;
    private static final double SLIDER_MAX_VAL = 100.0;
    private static final double SLIDER_MID_VAL = 50.0; // Midpoint for nominal speed
    // Define delay bounds (should match SimulationController)
    private static final long MIN_DELAY_MS = 10;   // Fastest speed delay (at slider max)
    private static final long MAX_DELAY_MS = 1000; // Slowest speed delay (at slider min)
    private long nominalDelayMs = 100; // Default nominal delay, will be updated from simProps
    //</editor-fold>

    /**
     * Initializes the controller after FXML loading.
     * Sets up ViewModels, binds UI elements, configures the speed slider range and its listener
     * for dynamic speed adjustments, and sets initial button states (disabled).
     */
    @FXML
    public void initialize() {
        this.statisticsViewModel = new StatisticsViewModel();
        this.selectedAnimalViewModel = new SelectedAnimalViewModel();

        bindUIElements();

        // Configure speed slider range and increments
        speedSlider.setMin(SLIDER_MIN_VAL);
        speedSlider.setMax(SLIDER_MAX_VAL);
        speedSlider.setBlockIncrement(10); // Step size for arrow keys

        // Add listener to update simulation speed when slider value changes
        speedSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (simulationController != null) { // Ensure controller is initialized
                    // Map the new slider value to a simulation delay using the piecewise mapping
                    long calculatedDelay = mapSliderValueToDelay(newValue.doubleValue());
                    simulationController.setStepDelay(calculatedDelay); // Update the controller's delay
                }
            }
        });

        // Setup button actions
        playPauseButton.setOnAction(event -> handlePlayPause());
        collectingDataButton.setOnAction(event -> handleToggleDataCollection());
        takeSnapshotButton.setOnAction(event -> handleTakeSnapshot());

        // Initial UI state (controls disabled until simulation is fully set up)
        playPauseButton.setText("Play");
        playPauseButton.setDisable(true);
        collectingDataButton.setDisable(true);
        takeSnapshotButton.setDisable(true);
        speedSlider.setDisable(true);
    }

    /**
     * Sets up the core simulation components, starts the simulation controller thread and related threads,
     * and prepares the UI for interaction. Called externally after FXML loading.
     * Sets the initial speed slider position based on the actual starting delay.
     *
     * @param config      The {@link SimulationProperties} for this simulation instance.
     * @param ownerWindow The {@link Window} (Stage) hosting this controller's scene.
     */
    public void setupAndRunSimulation(SimulationProperties config, Window ownerWindow) {
        this.simProps = Objects.requireNonNull(config, "SimulationProperties cannot be null");
        Objects.requireNonNull(ownerWindow, "Owner Window cannot be null");

        // Store the nominal delay from properties (used for slider mapping)
        // Clamp nominal delay within MIN/MAX bounds as well for consistent mapping
        this.nominalDelayMs = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, simProps.getSimulationStep()));

        // Initialize services
        this.fileSaveService = new JavaFXFileSaveService(ownerWindow);
        this.alertService = new JavaFXAlertService();

        // Create core components
        this.simulation = SimulationFactory.createSimulation(simProps);
        // Controller uses the actual clamped initial delay
        this.simulationController = new SimulationController(simulation, simProps);

        // Initialize dependent components
        this.stateQueue = new SimulationStateQueue(MAX_STATE_QUEUE_SIZE);
        this.statisticsManager = new StatisticsManager(simulation, simProps, fileSaveService);
        this.mapRenderer = new MapRenderer(simulationCanvas, simProps);

        this.stateProducer = new SimulationStateProducer(simulation, stateQueue, statisticsManager, selectedAnimalViewModel);
        this.canvasInteractionHandler = new CanvasInteractionHandler(simulationCanvas, mapRenderer, simulation, selectedAnimalViewModel);

        // Setup UI listeners and responsiveness
        setupCanvasResponsiveness();
        this.canvasInteractionHandler.attachHandlers();
        setupDayEndListener();
        setupWindowCloseHandler(ownerWindow);

        // Start background threads and UI timer
        startStateProducerThread();
        startAnimationTimer();
        simulationController.start(); // Start the controller thread

        // --- Final UI Setup ---
        playPauseButton.setDisable(false);
        playPauseButton.setText("Pause"); // Controller starts in 'running' state
        updateCollectingDataButtonText();
        collectingDataButton.setDisable(false);
        takeSnapshotButton.setDisable(false);
        speedSlider.setDisable(false); // Enable speed slider

        // Set initial slider value based on the ACTUAL starting delay used by the controller
        // (which might be clamped differently from the raw simProps value if it was outside MIN/MAX_DELAY_MS)
        long actualStartDelay = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, simProps.getSimulationStep()));
        speedSlider.setValue(mapDelayToSliderValue(actualStartDelay));


        Platform.runLater(this::drawInitialMapState);
    }

    // --- Event Handlers (FXML Button Actions) ---

    /**
     * Handles the Play/Pause button action. Toggles the simulation's running state
     * via the {@link SimulationController} and updates the button text accordingly.
     */
    @FXML
    private void handlePlayPause() {
        if (simulationController == null) return;
        simulationController.togglePause(); // Delegate control
        playPauseButton.setText(simulationController.isRunning() ? "Pause" : "Play");
    }

    /**
     * Handles the 'Log Data' button action. Toggles automatic daily statistics saving
     * via the {@link StatisticsManager} and updates the button text.
     */
    @FXML
    private void handleToggleDataCollection() {
        if (statisticsManager == null) return;
        statisticsManager.toggleDataCollection();
        updateCollectingDataButtonText();
    }

    /**
     * Handles the 'Take Snapshot' button action. Temporarily pauses the simulation (if running),
     * triggers the {@link StatisticsManager} to save a statistics snapshot, and then resumes
     * the simulation if it was previously running. Displays alerts for errors.
     */
    @FXML
    private void handleTakeSnapshot() {
        if (statisticsManager == null || alertService == null || simulationController == null) {
            System.err.println("Cannot take snapshot: Core components missing.");
            return;
        }

        boolean wasRunning = simulationController.isRunning();
        if (wasRunning) {
            simulationController.togglePause(); // Pause
            playPauseButton.setText("Play");
            // Brief delay allows the controller thread to likely finish current step and pause
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }

        try {
            // This generates a fresh snapshot and prompts user to save via file service
            statisticsManager.takeSnapshot();
        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Snapshot Error", "Failed to save snapshot file: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalStateException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Snapshot Error", "Could not initiate snapshot saving: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            alertService.showAlert(IAlertService.AlertType.ERROR, "Snapshot Error", "An unexpected error occurred while taking snapshot: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure simulation resumes if it was paused for the snapshot
            if (wasRunning) {
                Platform.runLater(() -> { // Schedule resume after any dialogs close
                    if (simulationController != null && !simulationController.isStopped()){
                        simulationController.togglePause(); // Resume
                        playPauseButton.setText("Pause");
                    }
                });
            }
        }
    }


    // --- Initialization & Setup Helpers ---

    /**
     * Binds JavaFX UI controls (Labels, TextAreas) to properties in the
     * {@link StatisticsViewModel} and {@link SelectedAnimalViewModel}.
     */
    private void bindUIElements() {
        // Statistics Panel Bindings
        dayCount.textProperty().bind(statisticsViewModel.dayCountProperty().asString("%d"));
        animalCount.textProperty().bind(statisticsViewModel.animalCountProperty().asString("%d"));
        plantCount.textProperty().bind(statisticsViewModel.plantCountProperty().asString("%d"));
        emptyFieldsCount.textProperty().bind(statisticsViewModel.emptyFieldsCountProperty().asString("%d"));
        averageEnergy.textProperty().bind(statisticsViewModel.averageEnergyProperty().asString("%.1f"));
        averageLifespan.textProperty().bind(statisticsViewModel.averageLifespanProperty().asString("%.1f days"));
        averageChildren.textProperty().bind(statisticsViewModel.averageChildrenProperty().asString("%.2f"));
        popularGenotypes.textProperty().bind(statisticsViewModel.popularGenotypesTextProperty());

        // Selected Animal Panel Bindings
        selectedAnimalEnergy.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.energyProperty()));
        selectedAnimalAge.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.ageProperty(), " days"));
        selectedAnimalChildren.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.childrenMadeProperty()));
        selectedAnimalPlantsEaten.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.plantsEatenProperty()));
        selectedAnimalGenotype.textProperty().bind(selectedAnimalViewModel.genotypeProperty());
        selectedAnimalActiveGene.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.activeGeneIndexProperty()));
        selectedAnimalDescendants.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.descendantsCountProperty()));
        selectedAnimalDeathDate.textProperty().bind(selectedAnimalViewModel.deathDateProperty());

        Tooltip genotypesTooltip = new Tooltip();
        genotypesTooltip.textProperty().bind(statisticsViewModel.popularGenotypesTextProperty());
        Tooltip.install(popularGenotypes, genotypesTooltip);
        popularGenotypes.setEditable(false);
        popularGenotypes.setWrapText(true);
    }

    /** Helper to create bindings for selected animal labels, showing "-" when no animal is selected. */
    private StringBinding createSelectedAnimalBinding(ReadOnlyIntegerProperty property, String suffix) {
        return Bindings.createStringBinding(
                () -> selectedAnimalViewModel.isSelectedProperty().get()
                        ? String.valueOf(property.get()) + suffix
                        : "-",
                selectedAnimalViewModel.isSelectedProperty(), property);
    }
    /** Overload for no suffix. */
    private StringBinding createSelectedAnimalBinding(ReadOnlyIntegerProperty property) {
        return createSelectedAnimalBinding(property, "");
    }

    /** Configures canvas resizing behavior. */
    private void setupCanvasResponsiveness() {
        if (canvasScrollPane != null && simulationCanvas != null) {
            final double padding = 2.0;
            simulationCanvas.widthProperty().bind(canvasScrollPane.widthProperty().subtract(padding));
            simulationCanvas.heightProperty().bind(canvasScrollPane.heightProperty().subtract(padding));
            simulationCanvas.widthProperty().addListener((obs, oldVal, newVal) -> updateCanvasAndRedraw());
            simulationCanvas.heightProperty().addListener((obs, oldVal, newVal) -> updateCanvasAndRedraw());
            Platform.runLater(this::updateCanvasAndRedraw);
        } else {
            System.err.println("Warning: Canvas ScrollPane or Canvas is null. Canvas will not resize.");
            if (simulationCanvas != null) {
                simulationCanvas.setWidth(600); simulationCanvas.setHeight(500);
                Platform.runLater(this::updateCanvasAndRedraw);
            }
        }
    }

    /** Sets up the listener for simulation day end events. */
    private void setupDayEndListener() {
        simulation.addDayEndListener(() -> { // Runs in SimulationController thread
            SimulationStatisticsSnapshot statsSnapshot = statisticsManager.createAndCacheStatisticsSnapshot();
            if (statisticsManager.isCollectingData() && statsSnapshot != null) {
                statisticsManager.saveDailyStatistics(statsSnapshot);
            }
            Platform.runLater(() -> { // Update UI on FX thread
                if (statsSnapshot != null) statisticsViewModel.updateStatistics(statsSnapshot);
                if (lastProcessedSnapshot != null) checkSelectedAnimalLiveness(lastProcessedSnapshot);
                if (simulationController != null && simulationController.isStopped() && simulation.getAnimals().isEmpty()){
                    playPauseButton.setText("Ended");
                    playPauseButton.setDisable(true);
                    speedSlider.setDisable(true);
                }
            });
        });
    }

    /** Sets up the handler for window close requests. */
    private void setupWindowCloseHandler(Window ownerWindow) {
        ownerWindow.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            System.out.println("Simulation window closing request received.");
            stopSimulationThreads(); // Trigger the shutdown sequence
        });
    }

    // --- Simulation State & Rendering ---

    /** Starts the background thread producing simulation state snapshots. */
    private void startStateProducerThread() {
        if (stateProducer == null) {
            System.err.println("Error: Cannot start state producer thread, stateProducer is null.");
            return;
        }
        Thread producerThread = new Thread(stateProducer, "SimStateProducer-" + simProps.getConfigName());
        producerThread.setDaemon(true);
        producerThread.start();
    }

    /** Starts the JavaFX AnimationTimer for rendering snapshots. */
    private void startAnimationTimer() {
        if (animationTimer != null) animationTimer.stop();
        animationTimer = new AnimationTimer() {
            private long lastUiUpdateTimestamp = 0;
            @Override
            public void handle(long now) {
                if (now - lastUiUpdateTimestamp < UI_UPDATE_INTERVAL_NANOS) return; // Throttle
                lastUiUpdateTimestamp = now;
                SimulationStateSnapshot snapshotToRender = stateQueue.dequeue();
                if (snapshotToRender != null) {
                    lastProcessedSnapshot = snapshotToRender;
                    checkSelectedAnimalLiveness(snapshotToRender);
                    if (mapRenderer != null) mapRenderer.drawSimulation(snapshotToRender);
                }
            }
        };
        animationTimer.start();
    }

    /** Draws the initial simulation state once at the beginning. */
    private void drawInitialMapState() {
        if (simulation != null && mapRenderer != null && stateProducer != null) {
            System.out.println("Drawing initial map state...");
            SimulationStateSnapshot initialSnapshot = stateProducer.createInitialSnapshot();
            if (initialSnapshot != null) {
                lastProcessedSnapshot = initialSnapshot;
                mapRenderer.drawSimulation(initialSnapshot);
            } else {
                System.err.println("Could not draw initial map state - snapshot creation failed.");
                if (mapRenderer != null && simulationCanvas != null) { // Check canvas too
                    GraphicsContext gc = simulationCanvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, simulationCanvas.getWidth(), simulationCanvas.getHeight());
                }
            }
        } else {
            System.err.println("Cannot draw initial state: Simulation, renderer, or producer is null.");
        }
    }

    /** Updates canvas dimensions and redraws the last known state. */
    private void updateCanvasAndRedraw() {
        if (mapRenderer != null) {
            mapRenderer.updateCellDimensions();
            redrawLastState();
        }
    }

    /** Redraws the last processed snapshot, ensuring execution on FX thread. */
    private void redrawLastState() {
        Platform.runLater(() -> {
            if (lastProcessedSnapshot != null && mapRenderer != null) {
                mapRenderer.drawSimulation(lastProcessedSnapshot);
            } else {
                // Fallback if no snapshot available yet
                drawInitialMapState();
            }
        });
    }

    /** Checks if the selected animal still exists in the current snapshot. */
    private void checkSelectedAnimalLiveness(SimulationStateSnapshot snapshot) {
        if (!selectedAnimalViewModel.isSelectedProperty().get()) return;
        Animal currentlySelected = selectedAnimalViewModel.getCurrentAnimalReference();
        if (currentlySelected == null || snapshot == null || snapshot.getAnimals() == null) {
            selectedAnimalViewModel.clear(); return;
        }
        long selectedId = currentlySelected.getId();
        boolean foundAliveInSnapshot = false;
        for (List<Animal> listAtPos : snapshot.getAnimals().values()) {
            if(listAtPos == null) continue;
            for (Animal animalInSnapshot : listAtPos) {
                if (animalInSnapshot.getId() == selectedId) {
                    foundAliveInSnapshot = true;
                    selectedAnimalViewModel.update(animalInSnapshot); break; // Update with latest data
                }
            }
            if (foundAliveInSnapshot) break;
        }
        if (!foundAliveInSnapshot) {
            // Update with last known state (will show death date if applicable)
            selectedAnimalViewModel.update(currentlySelected);
        }
    }

    // --- Shutdown ---
    /** Stops all simulation-related threads and timers. */
    public void stopSimulationThreads() {
        String windowTitle = "Unknown Window";
        try { windowTitle = ((Stage)simulationCanvas.getScene().getWindow()).getTitle(); } catch (Exception e) { /* ignore */ }
        System.out.println("Stopping simulation components for window: " + windowTitle);
        if (simulationController != null) simulationController.stop();
        if (stateProducer != null) stateProducer.stop();
        if (animationTimer != null) animationTimer.stop();
        if (canvasInteractionHandler != null) canvasInteractionHandler.detachHandlers();
        System.out.println("Simulation components stop signals sent for window: " + windowTitle);
    }

    // --- UI Update Helpers ---
    /** Updates the text on the data collection toggle button. */
    private void updateCollectingDataButtonText() {
        if (statisticsManager == null) return;
        collectingDataButton.setText("Log Data: " + (statisticsManager.isCollectingData() ? "ON" : "OFF"));
    }

    // --- Speed Slider Mapping ---

    /**
     * Maps the slider value (e.g., 0-100) to a simulation step delay in milliseconds.
     * Uses a piecewise linear mapping:
     * - Slider [0, 50] maps linearly to Delay [MAX_DELAY_MS, nominalDelayMs].
     * - Slider [50, 100] maps linearly to Delay [nominalDelayMs, MIN_DELAY_MS].
     * This makes the slider midpoint correspond to the nominal simulation speed.
     *
     * @param sliderValue The current value from the slider (assumed to be between SLIDER_MIN_VAL and SLIDER_MAX_VAL).
     * @return The calculated delay in milliseconds, clamped between MIN_DELAY_MS and MAX_DELAY_MS.
     */
    private long mapSliderValueToDelay(double sliderValue) {
        double delay;

        // Ensure nominal delay is within global min/max for calculations
        long currentNominal = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, this.nominalDelayMs));

        if (sliderValue <= SLIDER_MID_VAL) {
            // Map slider 0-50 to Delay MAX -> nominal
            double range = MAX_DELAY_MS - currentNominal;
            double proportion = sliderValue / SLIDER_MID_VAL; // 0.0 to 1.0
            delay = MAX_DELAY_MS - proportion * range;
        } else {
            // Map slider 50-100 to Delay nominal -> MIN
            double range = currentNominal - MIN_DELAY_MS;
            double proportion = (sliderValue - SLIDER_MID_VAL) / (SLIDER_MAX_VAL - SLIDER_MID_VAL); // 0.0 to 1.0
            delay = currentNominal - proportion * range;
        }

        // Clamp final result just in case of floating point inaccuracies or edge cases
        return (long) Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, Math.round(delay)));
    }

    /**
     * Maps a simulation step delay (ms) back to the corresponding slider value (0-100).
     * This is the inverse of {@link #mapSliderValueToDelay(double)}, using the same
     * piecewise linear mapping centered around the nominal delay.
     * Used to set the initial slider position based on the actual starting delay.
     *
     * @param delayMs The delay in milliseconds (will be clamped between MIN_DELAY_MS and MAX_DELAY_MS).
     * @return The corresponding slider value (between SLIDER_MIN_VAL and SLIDER_MAX_VAL).
     */
    private double mapDelayToSliderValue(long delayMs) {
        // Clamp the input delay to the valid range
        delayMs = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, delayMs));
        // Ensure nominal delay is also clamped for consistent comparison
        long currentNominal = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, this.nominalDelayMs));
        double sliderValue;

        if (delayMs >= currentNominal) {
            // Delay is between nominal and MAX (maps to slider 0-50)
            double range = MAX_DELAY_MS - currentNominal;
            if (range <= 0) { // Avoid division by zero if MAX == nominal
                sliderValue = SLIDER_MID_VAL; // Or 0 if MAX==nominal==delay? Let's use midpoint.
            } else {
                double proportion = (double)(MAX_DELAY_MS - delayMs) / range; // 0.0 (at MAX) to 1.0 (at nominal)
                sliderValue = proportion * SLIDER_MID_VAL; // Map proportion to 0-50
            }
        } else {
            // Delay is between MIN and nominal (maps to slider 50-100)
            double range = currentNominal - MIN_DELAY_MS;
            if (range <= 0) { // Avoid division by zero if nominal == MIN
                sliderValue = SLIDER_MID_VAL; // Or 100 if nominal==MIN==delay? Use midpoint.
            } else {
                double proportion = (double)(currentNominal - delayMs) / range; // 0.0 (at nominal) to 1.0 (at MIN)
                sliderValue = SLIDER_MID_VAL + proportion * (SLIDER_MAX_VAL - SLIDER_MID_VAL); // Map proportion to 50-100
            }
        }
        // Clamp final slider value to handle potential floating point issues
        return Math.max(SLIDER_MIN_VAL, Math.min(SLIDER_MAX_VAL, sliderValue));
    }
}