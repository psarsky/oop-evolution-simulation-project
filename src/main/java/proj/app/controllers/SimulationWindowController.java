package proj.app.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import proj.app.*; // Includes AppConstants
import proj.app.controllers.handlers.CanvasInteractionHandler;
import proj.app.render.MapRenderer;
import proj.app.render.SimulationRenderer;
import proj.app.services.IAlertService;
import proj.app.services.IFileSaveService;
import proj.app.services.IMessageService; // Import message service
import proj.app.state.SimulationStateProducer;
import proj.app.state.SimulationStateSnapshot;
import proj.app.viewmodels.SelectedAnimalViewModel;
import proj.app.viewmodels.StatisticsViewModel;
import proj.model.elements.Animal;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Controller for the Simulation Window (SimulationWindow.fxml).
 * Coordinates the user interface, delegates simulation initialization, lifecycle management,
 * and rendering to dedicated classes. Manages user interactions (like canvas clicks, button presses)
 * and updates associated ViewModels (StatisticsViewModel, SelectedAnimalViewModel).
 * It registers itself with the {@link ActiveSimulationRegistry} upon start and unregisters upon closing.
 * Required dependencies (services, initializer, registry, message service) are injected via the constructor.
 * Uses messages from {@link IMessageService} and constants from {@link AppConstants}.
 */
public class SimulationWindowController {

    //<editor-fold desc="FXML Fields">
    // Define FXML variables matching fx:id attributes in SimulationWindow.fxml
    @FXML private Canvas simulationCanvas;
    @FXML private ScrollPane canvasScrollPane; // Used for resizing canvas
    @FXML private Button playPauseButton;
    @FXML private Slider speedSlider;
    @FXML private Button collectingDataButton;
    @FXML private Button takeSnapshotButton;
    @FXML private Label dayLabel; // Label preceding day count
    @FXML private Label dayCount;
    @FXML private Label animalLabel; // Label preceding animal count
    @FXML private Label animalCount;
    @FXML private Label plantLabel; // Label preceding plant count
    @FXML private Label plantCount;
    @FXML private Label emptyFieldsLabel; // Label preceding empty fields count
    @FXML private Label emptyFieldsCount;
    @FXML private Label avgEnergyLabel; // Label preceding average energy
    @FXML private Label averageEnergy;
    @FXML private Label avgLifespanLabel; // Label preceding average lifespan
    @FXML private Label averageLifespan;
    @FXML private Label avgChildrenLabel; // Label preceding average children
    @FXML private Label averageChildren;
    @FXML private Label popularGenotypesLabel; // Label preceding popular genotypes
    @FXML private TextArea popularGenotypes;
    @FXML private Label selectedAnimalEnergyLabel; // Label preceding selected animal energy
    @FXML private Label selectedAnimalEnergy;
    @FXML private Label selectedAnimalAgeLabel; // Label preceding selected animal age
    @FXML private Label selectedAnimalAge;
    @FXML private Label selectedAnimalChildrenLabel; // Label preceding selected animal children
    @FXML private Label selectedAnimalChildren;
    @FXML private Label selectedAnimalPlantsEatenLabel; // Label preceding selected animal plants eaten
    @FXML private Label selectedAnimalPlantsEaten;
    @FXML private Label selectedAnimalGenotypeLabel; // Label preceding selected animal genotype
    @FXML private Label selectedAnimalGenotype;
    @FXML private Label selectedAnimalActiveGeneLabel; // Label preceding selected animal active gene
    @FXML private Label selectedAnimalActiveGene;
    @FXML private Label selectedAnimalDescendantsLabel; // Label preceding selected animal descendants
    @FXML private Label selectedAnimalDescendants;
    @FXML private Label selectedAnimalDeathDateLabel; // Label preceding selected animal death date
    @FXML private Label selectedAnimalDeathDate;
    @FXML private CheckBox showStatisticsCheckbox; // Checkbox to toggle statistics panel visibility
    @FXML private TitledPane currentStatsPane; // TitledPane for current stats
    @FXML private TitledPane selectedAnimalPane; // TitledPane for selected animal details
    //</editor-fold>

    //<editor-fold desc="Dependencies (Injected)">
    private final IAlertService alertService;
    private final SimulationInitializer simulationInitializer;
    private final ActiveSimulationRegistry activeSimulationRegistry;
    private final IMessageService messageService; // Injected message service
    //</editor-fold>

    //<editor-fold desc="Internal Components & State">
    private final StatisticsViewModel statisticsViewModel = new StatisticsViewModel();
    private final SelectedAnimalViewModel selectedAnimalViewModel = new SelectedAnimalViewModel();
    private SimulationLifecycleManager lifecycleManager;
    private SimulationRenderer simulationRenderer;
    private CanvasInteractionHandler canvasInteractionHandler;
    private SimulationController simulationController;
    private StatisticsManager statisticsManager;
    private SimulationStateProducer stateProducer;
    private MapRenderer mapRenderer;
    private Simulation simulation;
    private SimulationStateSnapshot lastProcessedSnapshot = null;
    private SimulationProperties simProps;
    private String configName = "UNKNOWN";
    private Stage ownerStage;
    //</editor-fold>

    /**
     * Constructs the SimulationWindowController with injected dependencies.
     * These dependencies are used throughout the controller's lifecycle for tasks like
     * displaying alerts, initializing simulation components, managing the window's
     * registration as an active simulation, and retrieving localized UI text.
     *
     * @param alertService            The {@link IAlertService} instance used for displaying alerts and error messages to the user. Must not be null.
     * @param simulationInitializer   The {@link SimulationInitializer} instance responsible for creating and setting up
     *                                the core simulation components (Simulation, Controller, StatisticsManager, etc.). Must not be null.
     * @param activeSimulationRegistry The singleton {@link ActiveSimulationRegistry} instance used to register this simulation window
     *                                upon start and unregister it upon close, allowing management from the main application window. Must not be null.
     * @param messageService          The {@link IMessageService} instance used for retrieving localized or configured UI strings
     *                                (e.g., button text, labels, titles, error messages). Must not be null.
     * @throws NullPointerException if any injected dependency is null.
     */
    public SimulationWindowController(IAlertService alertService,
                                      SimulationInitializer simulationInitializer,
                                      ActiveSimulationRegistry activeSimulationRegistry,
                                      IMessageService messageService) { // Added messageService
        this.alertService = Objects.requireNonNull(alertService, "AlertService cannot be null");
        this.simulationInitializer = Objects.requireNonNull(simulationInitializer, "SimulationInitializer cannot be null");
        this.activeSimulationRegistry = Objects.requireNonNull(activeSimulationRegistry, "ActiveSimulationRegistry cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null"); // Store message service
    }

    /**
     * Initializes the controller after FXML loading and dependency injection.
     * This method is automatically called by the FXMLLoader. It performs initial UI setup,
     * such as binding ViewModels to UI elements, setting up action handlers for buttons,
     * setting initial UI control states, and setting localized text for static UI elements
     * using the injected {@link IMessageService}. The main simulation setup and start
     * occurs later in {@link #setupAndRunSimulation(SimulationProperties, Stage)}.
     */
    @FXML
    public void initialize() {
        bindViewModelUIElements(); // Bind dynamic data labels
        // Setup button actions
        playPauseButton.setOnAction(event -> handlePlayPause());
        collectingDataButton.setOnAction(event -> handleToggleDataCollection());
        takeSnapshotButton.setOnAction(event -> handleTakeSnapshot());
        // Set initial state for controls (mostly disabled until simulation runs)
        setInitialUIState();
        // Set localized text for static elements
        setUIText();
    }

    /** Sets the initial disabled/text state for UI controls before the simulation starts. */
    private void setInitialUIState() {
        // Buttons/Slider enabled state is handled by binding to lifecycleManager later
        playPauseButton.setDisable(true);
        speedSlider.setDisable(true);
        // Set initial text using message service for consistency, even if disabled
        collectingDataButton.setText(messageService.getMessage("sim.button.logDataError")); // Indicates state unknown
        collectingDataButton.setDisable(true);
        takeSnapshotButton.setDisable(true);
    }

    /** Sets static text elements (labels, button text) using the injected MessageService. */
    private void setUIText() {
        // Set text for labels using keys from messages.properties
        dayLabel.setText(messageService.getMessage("sim.label.day"));
        animalLabel.setText(messageService.getMessage("sim.label.animals"));
        plantLabel.setText(messageService.getMessage("sim.label.plants"));
        emptyFieldsLabel.setText(messageService.getMessage("sim.label.emptyFields"));
        avgEnergyLabel.setText(messageService.getMessage("sim.label.avgEnergy"));
        avgLifespanLabel.setText(messageService.getMessage("sim.label.avgLifespan"));
        avgChildrenLabel.setText(messageService.getMessage("sim.label.avgChildren"));
        popularGenotypesLabel.setText(messageService.getMessage("sim.label.popularGenotypes"));

        selectedAnimalEnergyLabel.setText(messageService.getMessage("sim.label.selected.energy"));
        selectedAnimalAgeLabel.setText(messageService.getMessage("sim.label.selected.age"));
        selectedAnimalChildrenLabel.setText(messageService.getMessage("sim.label.selected.children"));
        selectedAnimalPlantsEatenLabel.setText(messageService.getMessage("sim.label.selected.plantsEaten"));
        selectedAnimalGenotypeLabel.setText(messageService.getMessage("sim.label.selected.genotype"));
        selectedAnimalActiveGeneLabel.setText(messageService.getMessage("sim.label.selected.activeGene"));
        selectedAnimalDescendantsLabel.setText(messageService.getMessage("sim.label.selected.descendants"));
        selectedAnimalDeathDateLabel.setText(messageService.getMessage("sim.label.selected.deathDate"));

        // Set text for controls where it's static or initialized here
        showStatisticsCheckbox.setText(messageService.getMessage("sim.checkbox.showStats"));
        takeSnapshotButton.setText(messageService.getMessage("sim.button.takeSnapshot"));
        // Note: playPauseButton and collectingDataButton text is dynamic, set via bindings/updates

        // Set TitledPane text
        currentStatsPane.setText(messageService.getMessage("sim.titledPane.currentStats"));
        selectedAnimalPane.setText(messageService.getMessage("sim.titledPane.selectedAnimal"));
    }

    /**
     * Configures and starts the simulation display and logic. This method should be called
     * externally (e.g., by {@link MainWindowController}) after the controller is instantiated,
     * the FXML is loaded, and the {@link Stage} is available.
     * It uses the injected {@link SimulationInitializer} to create core components, sets up
     * UI managers (like {@link SimulationLifecycleManager}, {@link SimulationRenderer},
     * {@link CanvasInteractionHandler}), configures listeners for simulation events and window closing,
     * registers this instance with the injected {@link ActiveSimulationRegistry}, updates the window title
     * using the injected {@link IMessageService}, and starts the necessary background threads and the rendering loop.
     *
     * @param config      The {@link SimulationProperties} object defining the parameters for this specific simulation run. Must not be null.
     * @param ownerStage  The {@link Stage} hosting this controller's scene. Used for dialog ownership, context, registration, and title setting. Must not be null.
     * @throws NullPointerException if config or ownerStage is null.
     * @throws RuntimeException if the initialization process fails at any step (e.g., component creation, thread start),
     *                          encapsulating the original exception cause. Errors are also shown via the injected alert service.
     */
    public void setupAndRunSimulation(SimulationProperties config, Stage ownerStage) {
        this.simProps = Objects.requireNonNull(config, "SimulationProperties cannot be null");
        this.ownerStage = Objects.requireNonNull(ownerStage, "Owner Stage cannot be null");
        this.configName = config.getConfigName();

        // Update window title using format string from message service
        // Assumes the title initially set by MainWindowController has the run name first.
        String currentTitle = ownerStage.getTitle();
        String runName = currentTitle != null ? currentTitle.split(" - ")[0] : "Simulation"; // Extract run name or use default
        ownerStage.setTitle(messageService.getFormattedMessage("simulation.window.title.format", runName, this.configName));

        try {
            // 1. Initialize components using injected initializer
            SimulationComponents components = simulationInitializer.initializeSimulationComponents(
                    config, simulationCanvas, ownerStage, selectedAnimalViewModel
            );
            this.simulation = components.simulation();
            this.simulationController = components.simulationController();
            this.statisticsManager = components.statisticsManager();
            this.stateProducer = components.stateProducer();
            this.mapRenderer = components.mapRenderer();

            // 2. Create UI managing components
            this.lifecycleManager = new SimulationLifecycleManager(simulationController, config);
            this.simulationRenderer = new SimulationRenderer(
                    components.stateQueue(), this.mapRenderer, this::processRenderedSnapshot
            );

            // 3. Configure canvas interaction handler
            this.canvasInteractionHandler = new CanvasInteractionHandler(
                    simulationCanvas, this.mapRenderer, this.simulation, this.selectedAnimalViewModel
            );
            this.canvasInteractionHandler.attachHandlers();

            // 4. Setup UI bindings & responsiveness
            bindLifecycleControls(); // Includes dynamic button text
            setupCanvasResponsiveness();

            // 5. Setup listeners (simulation day end, window close)
            setupDayEndListener();
            setupWindowCloseHandler(ownerStage);

            // 6. Register with injected registry
            this.activeSimulationRegistry.register(this.configName, this, this.ownerStage);

            // 7. Start background threads and rendering
            startBackgroundThreads();
            simulationRenderer.start();
            simulationController.start(); // Start the main simulation logic thread

            // 8. Final UI setup (enable buttons, set initial text for dynamic buttons)
            updateCollectingDataButtonText(); // Set initial text based on state
            collectingDataButton.setDisable(false); // Enable now that manager exists
            takeSnapshotButton.setDisable(false); // Enable snapshot button

            // 9. Draw the initial state of the map
            Platform.runLater(this::drawInitialMapState);

        } catch (Exception e) {
            // Use message service for error formatting
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.sim.init", e.getMessage()));
            e.printStackTrace();
            shutdownUI(); // Attempt to disable UI elements
            // Attempt to unregister if registration occurred before the error
            this.activeSimulationRegistry.unregister(this.configName, this);
            throw new RuntimeException("Simulation initialization failed", e); // Re-throw for calling code
        }
    }

    // --- Setup Helper Methods ---

    /** Binds dynamic UI labels to properties in the ViewModels. */
    private void bindViewModelUIElements() {
        // Bind statistic value labels
        dayCount.textProperty().bind(statisticsViewModel.dayCountProperty().asString("%d"));
        animalCount.textProperty().bind(statisticsViewModel.animalCountProperty().asString("%d"));
        plantCount.textProperty().bind(statisticsViewModel.plantCountProperty().asString("%d"));
        emptyFieldsCount.textProperty().bind(statisticsViewModel.emptyFieldsCountProperty().asString("%d"));
        averageEnergy.textProperty().bind(statisticsViewModel.averageEnergyProperty().asString("%.1f"));
        averageLifespan.textProperty().bind(statisticsViewModel.averageLifespanProperty().asString("%.1f " + messageService.getMessage("unit.days"))); // Add localized unit
        averageChildren.textProperty().bind(statisticsViewModel.averageChildrenProperty().asString("%.2f"));
        popularGenotypes.textProperty().bind(statisticsViewModel.popularGenotypesTextProperty());
        Tooltip genotypesTooltip = new Tooltip(); // Tooltip for genotypes text area
        genotypesTooltip.textProperty().bind(statisticsViewModel.popularGenotypesTextProperty());
        Tooltip.install(popularGenotypes, genotypesTooltip);
        popularGenotypes.setEditable(false);
        popularGenotypes.setWrapText(true);

        // Bind selected animal detail labels using helper method for formatting
        selectedAnimalEnergy.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.energyProperty()));
        selectedAnimalAge.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.ageProperty(), "unit.days")); // Use key for suffix
        selectedAnimalChildren.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.childrenMadeProperty()));
        selectedAnimalPlantsEaten.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.plantsEatenProperty()));
        selectedAnimalGenotype.textProperty().bind(selectedAnimalViewModel.genotypeProperty()); // String property
        selectedAnimalActiveGene.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.activeGeneIndexProperty()));
        selectedAnimalDescendants.textProperty().bind(createSelectedAnimalBinding(selectedAnimalViewModel.descendantsCountProperty()));
        selectedAnimalDeathDate.textProperty().bind(selectedAnimalViewModel.deathDateProperty()); // String property handles "-"
    }

    /** Binds lifecycle UI controls (buttons, slider) to the LifecycleManager, using MessageService for text. */
    private void bindLifecycleControls() {
        if (lifecycleManager == null) return;
        // Bind disable properties
        playPauseButton.disableProperty().bind(lifecycleManager.canControlProperty().not());
        speedSlider.disableProperty().bind(lifecycleManager.canControlProperty().not());

        // Bind play/pause button text using message service keys
        playPauseButton.textProperty().bind(Bindings.createStringBinding(() -> {
            if (!lifecycleManager.canControlProperty().get()) { // If simulation ended
                return messageService.getMessage("sim.button.ended");
            } else { // Simulation controllable
                return lifecycleManager.pausedProperty().get()
                        ? messageService.getMessage("sim.button.play") // Show "Play" if paused
                        : messageService.getMessage("sim.button.pause"); // Show "Pause" if running
            }
        }, lifecycleManager.pausedProperty(), lifecycleManager.canControlProperty())); // Depends on both properties

        // Bind slider value bidirectionally
        speedSlider.valueProperty().bindBidirectional(lifecycleManager.speedSliderValueProperty());
    }

    /** Configures the simulation canvas to resize dynamically within its ScrollPane container. */
    private void setupCanvasResponsiveness() {
        if (canvasScrollPane != null && simulationCanvas != null && mapRenderer != null) {
            // Use constant for padding
            final double padding = AppConstants.CANVAS_PADDING;
            // Bind canvas size to scroll pane viewport size minus padding
            simulationCanvas.widthProperty().bind(canvasScrollPane.widthProperty().subtract(padding));
            simulationCanvas.heightProperty().bind(canvasScrollPane.heightProperty().subtract(padding));
            // Add listeners to redraw the map when canvas dimensions change
            simulationCanvas.widthProperty().addListener((obs, oldVal, newVal) -> updateCanvasAndRedraw());
            simulationCanvas.heightProperty().addListener((obs, oldVal, newVal) -> updateCanvasAndRedraw());
            // Ensure initial redraw after layout pass
            Platform.runLater(this::updateCanvasAndRedraw);
        } else {
            System.err.println("Warning: Canvas ScrollPane, Canvas, or MapRenderer is null. Canvas will not resize dynamically.");
            // Fallback size if canvas exists but container/renderer might be missing
            if (simulationCanvas != null && mapRenderer != null) {
                simulationCanvas.setWidth(600); simulationCanvas.setHeight(500);
                Platform.runLater(this::updateCanvasAndRedraw);
            }
        }
    }

    /**
     * Sets up the listener that reacts to the end of each simulation day.
     * This listener runs in the simulation thread. It triggers statistics calculation/saving
     * and schedules UI updates (statistics display, selected animal check, lifecycle control state)
     * on the JavaFX Application Thread using {@code Platform.runLater}.
     */
    private void setupDayEndListener() {
        // Ensure required components are initialized before adding the listener
        if (simulation == null || statisticsManager == null || selectedAnimalViewModel == null || lifecycleManager == null || simulationController == null) {
            System.err.println("Cannot setup day end listener - required components missing.");
            return;
        }
        // Add a lambda to be executed by the Simulation thread at the end of each day
        simulation.addDayEndListener(() -> { // This lambda runs in the SimulationController's thread
            // 1. Create and potentially save statistics snapshot
            SimulationStatisticsSnapshot statsSnapshot = statisticsManager.createAndCacheStatisticsSnapshot();
            if (statisticsManager.isCollectingData() && statsSnapshot != null) {
                statisticsManager.saveDailyStatistics(statsSnapshot); // Save if enabled
            }

            // 2. Schedule UI updates on the JavaFX Application Thread
            Platform.runLater(() -> {
                // Update statistics display via ViewModel
                if (statsSnapshot != null) {
                    statisticsViewModel.updateStatistics(statsSnapshot);
                }
                // Check liveness of the selected animal based on the last rendered state
                if (lastProcessedSnapshot != null) {
                    checkSelectedAnimalLiveness(lastProcessedSnapshot);
                }
                // Check if the simulation controller has stopped (e.g., no animals left)
                if (simulationController.isStopped()) {
                    lifecycleManager.simulationEnded(); // Notify manager to update UI state (buttons/slider)
                }
            });
        });
    }

    /**
     * Attaches an event handler to the window's close request (clicking the 'X' button).
     * This handler ensures that the simulation instance is unregistered from the
     * {@link ActiveSimulationRegistry} *before* signaling the simulation threads to stop.
     *
     * @param ownerWindow The {@link Window} (Stage) hosting this controller. Must not be null.
     */
    private void setupWindowCloseHandler(Window ownerWindow) {
        Objects.requireNonNull(ownerWindow, "OwnerWindow cannot be null for close handler setup");
        // Use injected activeSimulationRegistry
        ownerWindow.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            String windowTitle = (ownerStage != null) ? ownerStage.getTitle() : "Unknown Window";
            System.out.println("Simulation window closing request received for window: " + windowTitle);
            // --- Important: Unregister BEFORE stopping threads ---
            // This prevents potential issues if stopping takes time or another action tries to find this window.
            this.activeSimulationRegistry.unregister(this.configName, this);
            // --- Now signal threads to stop ---
            stopSimulationThreads();
            // Do not consume the event, allow the window to close naturally.
        });
    }

    // --- Starting Background Processes ---

    /** Starts background threads, currently just the SimulationStateProducer. */
    private void startBackgroundThreads() {
        if (stateProducer != null) {
            Thread producerThread = new Thread(stateProducer, "SimStateProducer-" + configName);
            producerThread.setDaemon(true); // Allow JVM exit even if this thread runs
            producerThread.start();
        } else {
            System.err.println("Error: Cannot start state producer thread, stateProducer is null.");
        }
    }

    // --- UI Action Handlers ---

    /** Handles the Play/Pause button click by delegating to the SimulationLifecycleManager. */
    @FXML private void handlePlayPause() {
        if (lifecycleManager != null) {
            lifecycleManager.togglePause();
        }
    }

    /** Handles the 'Log Data' button click by toggling data collection in StatisticsManager and updating button text. */
    @FXML private void handleToggleDataCollection() {
        if (statisticsManager != null) {
            statisticsManager.toggleDataCollection();
            updateCollectingDataButtonText(); // Update button text reflects new state
        }
    }

    /**
     * Handles the 'Take Snapshot' button click.
     * Temporarily pauses the simulation (if running), triggers the {@link StatisticsManager}
     * to generate and save a snapshot (prompting the user via {@link IFileSaveService}),
     * and then resumes the simulation if it was paused. Uses the injected {@link IAlertService}
     * and {@link IMessageService} to display any errors during the process.
     */
    @FXML private void handleTakeSnapshot() {
        // Check required components (alertService and messageService are guaranteed by constructor)
        if (statisticsManager == null || lifecycleManager == null) {
            System.err.println("Cannot take snapshot: Required components (StatisticsManager, LifecycleManager) are missing.");
            // Optionally show an alert here
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    "Snapshot functionality is unavailable due to missing components.");
            return;
        }

        boolean wasPausedForSnapshot = lifecycleManager.pauseForAction(); // Pause simulation if running
        try {
            // Delegate snapshot taking and saving (including file chooser)
            statisticsManager.takeSnapshot();
        } catch (IOException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.snapshot.save", e.getMessage()));
            e.printStackTrace();
        } catch (IllegalStateException e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.snapshot.init", e.getMessage()));
            e.printStackTrace();
        } catch (Exception e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.snapshot.unexpected", e.getMessage()));
            e.printStackTrace();
        } finally {
            // Ensure simulation resumes ONLY if it was specifically paused by this action
            if (wasPausedForSnapshot) {
                // Schedule the resume call on the JavaFX thread to ensure it runs after any potential dialogs close
                Platform.runLater(lifecycleManager::resumeAfterAction);
            }
        }
    }

    // --- Rendering and State Processing ---

    /**
     * Callback method invoked by the {@link SimulationRenderer} when a new snapshot has been dequeued
     * and is about to be rendered. This method runs on the JavaFX Application Thread.
     * It updates the controller's reference to the latest processed snapshot and checks the liveness
     * of the animal currently selected in the UI.
     *
     * @param snapshot The {@link SimulationStateSnapshot} that was just dequeued and processed by the renderer. Must not be null.
     */
    private void processRenderedSnapshot(SimulationStateSnapshot snapshot) {
        lastProcessedSnapshot = snapshot; // Update the reference
        checkSelectedAnimalLiveness(snapshot); // Check selected animal status
    }

    /** Draws the initial state of the simulation map, typically on window load. */
    private void drawInitialMapState() {
        if (stateProducer != null && simulationRenderer != null) {
            System.out.println("Drawing initial simulation map state...");
            SimulationStateSnapshot initialSnapshot = stateProducer.createInitialSnapshot();
            if (initialSnapshot != null) {
                lastProcessedSnapshot = initialSnapshot; // Store as the first processed state
                simulationRenderer.redrawFrame(initialSnapshot); // Ask renderer to draw it
            } else {
                System.err.println("Could not draw initial map state - snapshot creation failed.");
                clearCanvas(); // Clear canvas as fallback
            }
        } else {
            System.err.println("Cannot draw initial state: StateProducer or SimulationRenderer is null.");
            clearCanvas(); // Clear canvas
        }
    }

    /** Clears the simulation canvas by filling it with a default background color or transparency. */
    private void clearCanvas() {
        if (simulationCanvas != null) {
            GraphicsContext gc = simulationCanvas.getGraphicsContext2D();
            // Clear the rectangle covering the entire canvas
            gc.clearRect(0, 0, simulationCanvas.getWidth(), simulationCanvas.getHeight());
            // Optional: Fill with a background color if desired when empty
            // gc.setFill(Color.LIGHTGRAY);
            // gc.fillRect(0, 0, simulationCanvas.getWidth(), simulationCanvas.getHeight());
        }
    }

    /** Called when the canvas size changes. Updates the renderer's cell dimensions and triggers a redraw of the last known state. */
    private void updateCanvasAndRedraw() {
        if (mapRenderer != null) {
            mapRenderer.updateCellDimensions(); // Recalculate cell sizes
        }
        // Trigger a redraw using the SimulationRenderer's redrawFrame method
        if (simulationRenderer != null) {
            redrawLastState(); // Use helper method to redraw last snapshot
        }
    }

    /** Schedules a redraw of the last processed simulation state snapshot on the JavaFX Application Thread. */
    private void redrawLastState() {
        Platform.runLater(() -> {
            if (simulationRenderer != null) {
                // Tell the renderer to draw the last known snapshot
                simulationRenderer.redrawFrame(lastProcessedSnapshot);
            } else if (lastProcessedSnapshot == null){
                // Fallback: If no snapshot processed yet, try drawing initial state again
                // This might happen if resize occurs before first snapshot is ready
                drawInitialMapState();
            }
        });
    }

    /**
     * Checks if the animal currently selected in the UI (via {@link SelectedAnimalViewModel})
     * still exists and is alive in the provided {@link SimulationStateSnapshot}. Updates the ViewModel
     * accordingly. If the animal is found, its details are refreshed. If not found (presumed dead or removed),
     * the ViewModel is updated based on the last known state of the animal reference.
     *
     * @param snapshot The {@link SimulationStateSnapshot} representing the current state to check against. Must not be null.
     */
    private void checkSelectedAnimalLiveness(SimulationStateSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "Snapshot cannot be null for checking liveness");
        // Check if an animal is actually selected in the ViewModel
        if (!selectedAnimalViewModel.isSelectedProperty().get()) {
            return; // Nothing selected, nothing to check
        }
        Animal currentlySelected = selectedAnimalViewModel.getCurrentAnimalReference();
        // If ViewModel indicates selection but has no reference, clear it for consistency
        if (currentlySelected == null) {
            selectedAnimalViewModel.clear();
            return;
        }
        long selectedId = currentlySelected.getId();
        boolean foundAliveInSnapshot = false;

        // Search for the animal ID within the snapshot's animal map (which is immutable)
        if (snapshot.getAnimals() != null) {
            searchLoop: // Label allows breaking out of nested loops once found
            for (List<Animal> listAtPos : snapshot.getAnimals().values()) { // Iterate through lists at each position
                // No need to check list for null, snapshot guarantees immutable non-null lists (could be empty)
                for (Animal animalInSnapshot : listAtPos) { // Iterate animals at this position
                    if (animalInSnapshot.getId() == selectedId) {
                        foundAliveInSnapshot = true;
                        // Update the ViewModel with the most recent data from the snapshot's animal instance
                        selectedAnimalViewModel.update(animalInSnapshot);
                        break searchLoop; // Exit both loops once found
                    }
                }
            }
        }

        // Handle the case where the selected animal was NOT found in the current snapshot
        if (!foundAliveInSnapshot) {
            // The animal is gone from the live map. Refresh the ViewModel using the
            // last known reference. If the animal died between snapshots, its reference
            // might now reflect isAlive=false and have a deathDate.
            selectedAnimalViewModel.update(currentlySelected);

            // Log a warning if the internal reference still indicates 'alive' but it wasn't found.
            // This could indicate a brief inconsistency or a bug.
            if(currentlySelected.isAlive()){
                System.out.println("Warning: Selected animal ID " + selectedId + " not found in current snapshot, but internal reference indicates it should be alive.");
            }
        }
        // If foundAliveInSnapshot was true, the ViewModel was already updated inside the loop.
    }

    // --- Shutdown Logic ---

    /**
     * Stops all simulation-related background threads and timers associated with this window instance.
     * This method is typically called when the window is closing or if the simulation needs to be
     * forcefully stopped (e.g., when its configuration is deleted). It signals the
     * {@link SimulationRenderer}, {@link SimulationController}, and {@link SimulationStateProducer} to stop
     * and detaches canvas event handlers.
     * Note: This method does *not* handle unregistering from the {@link ActiveSimulationRegistry};
     * that is typically handled by the caller (e.g., the window close handler or the registry itself).
     */
    public void stopSimulationThreads() {
        String windowTitle = (ownerStage != null) ? ownerStage.getTitle() : "Unknown Window";
        System.out.println("Stopping simulation components for window: " + windowTitle);
        // Stop components gracefully, renderer first is often good practice
        if (simulationRenderer != null) simulationRenderer.stop();
        if (simulationController != null) simulationController.stop(); // Signals simulation logic thread
        if (stateProducer != null) stateProducer.stop(); // Signals state producer thread
        if (canvasInteractionHandler != null) canvasInteractionHandler.detachHandlers(); // Remove mouse listener
        System.out.println("Simulation components stop signals sent for window: " + windowTitle);
    }

    /** Disables UI controls and clears canvas, typically called on initialization failure to prevent interaction. */
    private void shutdownUI() {
        setInitialUIState(); // Reset controls to initial disabled/default state
        // Ensure event handlers are removed if they were attached before failure
        if (canvasInteractionHandler != null) canvasInteractionHandler.detachHandlers();
        clearCanvas(); // Clear any partially rendered state
    }

    // --- Private UI Helper Methods ---

    /** Updates the text on the 'Log Data' button based on the StatisticsManager's state, using MessageService. */
    private void updateCollectingDataButtonText() {
        if (statisticsManager != null) {
            // Choose the correct message key based on the data collection state
            String messageKey = statisticsManager.isCollectingData()
                    ? "sim.button.logDataOn"
                    : "sim.button.logDataOff";
            collectingDataButton.setText(messageService.getMessage(messageKey));
        } else {
            // Fallback if manager isn't ready (e.g., during initialization)
            collectingDataButton.setText(messageService.getMessage("sim.button.logDataError"));
            collectingDataButton.setDisable(true); // Keep disabled if manager unavailable
        }
    }

    /**
     * Creates a {@link StringBinding} for a UI label displaying an integer property from the
     * {@link SelectedAnimalViewModel}. The binding shows the integer value followed by a
     * localized suffix (if provided) when an animal is selected, otherwise it shows a
     * localized placeholder ("-").
     *
     * @param property  The {@link ReadOnlyIntegerProperty} from the ViewModel to display.
     * @param suffixKey The resource key (in messages.properties) for the optional suffix string (e.g., "unit.days"). Can be null.
     * @return A {@link StringBinding} ready to be bound to a Label's textProperty.
     */
    private StringBinding createSelectedAnimalBinding(ReadOnlyIntegerProperty property, String suffixKey) {
        // Get suffix and placeholder from message service
        String suffix = (suffixKey != null && !suffixKey.isEmpty()) ? messageService.getMessage(suffixKey) : "";
        String placeholder = messageService.getMessage("placeholder.selected.none"); // Use placeholder key

        // Create binding: updates when selection state or property value changes
        return Bindings.createStringBinding(
                () -> selectedAnimalViewModel.isSelectedProperty().get()
                        ? String.valueOf(property.get()) + suffix // Value + suffix if selected
                        : placeholder, // Placeholder if not selected
                selectedAnimalViewModel.isSelectedProperty(), property // Dependencies
        );
    }

    /**
     * Overload for {@link #createSelectedAnimalBinding(ReadOnlyIntegerProperty, String)} that takes no suffix key.
     * Creates a binding that shows only the integer value or the placeholder.
     *
     * @param property The {@link ReadOnlyIntegerProperty} from the ViewModel to display.
     * @return A {@link StringBinding} showing the integer value or a placeholder "-".
     */
    private StringBinding createSelectedAnimalBinding(ReadOnlyIntegerProperty property) {
        return createSelectedAnimalBinding(property, null); // Call main method with null suffix key
    }
}