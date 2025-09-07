// Plik: proj/app/controllers/SimulationWindowController.java
package proj.app.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Parent; // Import Parent
import javafx.scene.Scene; // Import Scene
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane; // **** Import GridPane ****
import javafx.scene.layout.StackPane; // Import StackPane
// import javafx.scene.layout.VBox; // Import usunięty (niepotrzebny)
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import proj.app.*; // Zawiera AppConstants
import proj.app.controllers.handlers.CanvasInteractionHandler;
import proj.app.render.MapRenderer;
import proj.app.render.SimulationRenderer;
import proj.app.services.IAlertService;
import proj.app.services.IMessageService;
import proj.app.state.SimulationRenderSnapshot;
import proj.app.state.SimulationStateProducer;
import proj.app.state.SimulationStateQueue;
import proj.app.SimulationStatisticsSnapshot;
import proj.app.viewmodels.ChartDataModel; // Import modelu danych wykresu
import proj.app.viewmodels.SelectedAnimalViewModel;
import proj.app.viewmodels.StatisticsViewModel;
import proj.model.elements.Animal;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Główny kontroler dla okna symulacji (SimulationWindow.fxml).
 * Koordynuje główny layout, cykl życia symulacji, renderowanie mapy
 * i interakcje. Deleguje wyświetlanie szczegółowych paneli (statystyki,
 * wybrane zwierzę, genotypy, kontrolki wykresu, wykres) do zagnieżdżonych
 * kontrolerów, przekazując im odpowiednie modele danych i zależności.
 * Używa GridPane do równego rozłożenia paneli statystyk.
 */
public class SimulationWindowController {

    //<editor-fold desc="FXML Fields - Główne komponenty okna">
    @FXML private Canvas simulationCanvas;
    @FXML private ScrollPane canvasScrollPane;
    @FXML private Button playPauseButton;
    @FXML private Slider speedSlider;
    @FXML private Button collectingDataButton;
    @FXML private Button takeSnapshotButton;
    @FXML private CheckBox showStatisticsCheckbox;
    @FXML private Label speedLabel;
    // @FXML private VBox statsVBox; // Zmieniono na GridPane
    @FXML private GridPane statsGridPane; // **** Nowe pole dla GridPane ****
    @FXML private SplitPane mainSplitPane;
    @FXML private StackPane chartPanelContainer;
    //</editor-fold>

    //<editor-fold desc="FXML Fields - Wstrzyknięte kontrolery paneli">
    @FXML private StatisticsPanelController statisticsPanelController;
    @FXML private SelectedAnimalPanelController selectedAnimalPanelController;
    @FXML private GenotypesPanelController genotypesPanelController;
    @FXML private ChartControlsPanelController chartControlsPanelController;
    @FXML private StatisticsChartPanelController chartPanelController;
    //</editor-fold>

    //<editor-fold desc="Dependencies (Injected)">
    private final IAlertService alertService;
    private final SimulationInitializer simulationInitializer;
    private final ActiveSimulationRegistry activeSimulationRegistry;
    private final IMessageService messageService;
    //</editor-fold>

    //<editor-fold desc="Internal Components & State">
    private StatisticsViewModel statisticsViewModel;
    private SelectedAnimalViewModel selectedAnimalViewModel;
    private ChartDataModel chartDataModel;

    private SimulationLifecycleManager lifecycleManager;
    private SimulationRenderer simulationRenderer;
    private CanvasInteractionHandler canvasInteractionHandler;
    private SimulationEngine simulationEngine;
    private StatisticsManager statisticsManager;
    private SimulationStateProducer stateProducer;
    private MapRenderer mapRenderer;
    private Simulation simulation;

    private SimulationRenderSnapshot lastProcessedSnapshot = null;
    private SimulationProperties simProps;
    private String configName = "UNKNOWN";
    private Stage ownerStage;
    //</editor-fold>

    /**
     * Konstruuje SimulationWindowController z wstrzykniętymi zależnościami.
     */
    public SimulationWindowController(IAlertService alertService,
                                      SimulationInitializer simulationInitializer,
                                      ActiveSimulationRegistry activeSimulationRegistry,
                                      IMessageService messageService) {
        this.alertService = Objects.requireNonNull(alertService, "AlertService cannot be null");
        this.simulationInitializer = Objects.requireNonNull(simulationInitializer, "SimulationInitializer cannot be null");
        this.activeSimulationRegistry = Objects.requireNonNull(activeSimulationRegistry, "ActiveSimulationRegistry cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");
    }

    /**
     * Inicjalizuje kontroler po załadowaniu FXML.
     * Odpowiada za wstępną konfigurację UI: ustawienie statycznych tekstów,
     * powiązanie widoczności paneli bocznych i wykresu, dodanie handlerów do przycisków.
     */
    @FXML
    public void initialize() {
        setInitialUIState();
        setUIText();

        // Powiąż widoczność GridPane (zamiast VBox) z checkboxem
        if (statsGridPane != null && showStatisticsCheckbox != null) {
            statsGridPane.visibleProperty().bind(showStatisticsCheckbox.selectedProperty());
            statsGridPane.managedProperty().bind(showStatisticsCheckbox.selectedProperty());

            // Nasłuchuj zmian w widoczności GridPane, aby dostosować divider SplitPane
            statsGridPane.visibleProperty().addListener((obs, oldVal, newVal) -> {
                if (mainSplitPane != null) {
                    Platform.runLater(() -> mainSplitPane.setDividerPositions(newVal ? 0.70 : 1.0)); // Dostosuj proporcję 0.70 wg potrzeb
                }
            });
        } else {
            System.err.println("Warning: Could not bind visibility for stats GridPane or mainSplitPane is null.");
        }

        // Powiąż widoczność kontenera wykresu (dolny panel) z tym samym checkboxem (bez zmian)
        if (chartPanelContainer != null && showStatisticsCheckbox != null) {
            chartPanelContainer.visibleProperty().bind(showStatisticsCheckbox.selectedProperty());
            chartPanelContainer.managedProperty().bind(showStatisticsCheckbox.selectedProperty());
            chartPanelContainer.minHeightProperty().bind(
                    Bindings.when(showStatisticsCheckbox.selectedProperty())
                            .then(150).otherwise(0)
            );
            chartPanelContainer.prefHeightProperty().bind(
                    Bindings.when(showStatisticsCheckbox.selectedProperty())
                            .then(250).otherwise(0)
            );
        } else {
            System.err.println("Warning: Could not bind visibility/height for chartPanelContainer.");
        }

        // Dodaj obsługę zdarzeń do głównych przycisków (bez zmian)
        playPauseButton.setOnAction(event -> handlePlayPause());
        collectingDataButton.setOnAction(event -> handleToggleDataCollection());
        takeSnapshotButton.setOnAction(event -> handleTakeSnapshot());
    }

    /** Ustawia początkowy stan kontrolek UI (np. wyłączenie przycisków). */
    private void setInitialUIState() {
        playPauseButton.setDisable(true);
        speedSlider.setDisable(true);
        collectingDataButton.setText(messageService.getMessage("sim.button.logDataError"));
        collectingDataButton.setDisable(true);
        takeSnapshotButton.setDisable(true);
    }

    /** Ustawia statyczne teksty elementów UI pozostających w tym kontrolerze. */
    private void setUIText() {
        speedLabel.setText(messageService.getMessage("sim.label.speed"));
        showStatisticsCheckbox.setText(messageService.getMessage("sim.checkbox.showStats"));
        takeSnapshotButton.setText(messageService.getMessage("sim.button.takeSnapshot"));
    }

    /**
     * Konfiguruje i uruchamia wyświetlanie oraz logikę symulacji.
     * Tworzy ViewModels i model danych wykresu, przekazuje je do zagnieżdżonych kontrolerów,
     * inicjalizuje komponenty symulacji i stosuje arkusz stylów CSS.
     *
     * @param config            Właściwości konfiguracyjne symulacji.
     * @param ownerStage        Główne okno (Stage) tej instancji symulacji.
     * @param simulationRunName Nazwa nadana temu konkretnemu uruchomieniu symulacji.
     */
    public void setupAndRunSimulation(SimulationProperties config, Stage ownerStage, String simulationRunName) { // <-- Dodano trzeci parametr
        this.simProps = Objects.requireNonNull(config, "SimulationProperties cannot be null");
        this.ownerStage = Objects.requireNonNull(ownerStage, "Owner Stage cannot be null");
        this.configName = config.getConfigName();

        // Aktualizacja tytułu okna - użyj przekazanej nazwy uruchomienia
        ownerStage.setTitle(messageService.getFormattedMessage(
                "simulation.window.title.format",
                Objects.requireNonNullElse(simulationRunName, "Simulation"), // Użyj przekazanej nazwy, fallback "Simulation"
                this.configName
        ));


        try {
            // --- Tworzenie ViewModeli i Modeli ---
            statisticsViewModel = new StatisticsViewModel(messageService);
            selectedAnimalViewModel = new SelectedAnimalViewModel(messageService);
            chartDataModel = new ChartDataModel();

            // --- Inicjalizacja komponentów symulacji ---
            SimulationComponents components = simulationInitializer.initializeSimulationComponents(
                    config, simulationCanvas, ownerStage, selectedAnimalViewModel
            );
            this.simulation = components.simulation();
            this.simulationEngine = components.simulationEngine();
            this.statisticsManager = components.statisticsManager();
            this.stateProducer = components.stateProducer();
            this.mapRenderer = components.mapRenderer();
            SimulationStateQueue<SimulationRenderSnapshot> stateQueue = components.stateQueue();

            // --- Tworzenie menedżerów UI ---
            this.lifecycleManager = new SimulationLifecycleManager(simulationEngine, config);
            this.simulationRenderer = new SimulationRenderer(
                    stateQueue, this.mapRenderer, this::processRenderedSnapshot
            );

            // --- Konfiguracja handlera interakcji z płótnem ---
            this.canvasInteractionHandler = new CanvasInteractionHandler(
                    simulationCanvas, this.mapRenderer, this.simulation,
                    this.selectedAnimalViewModel, this.alertService, this.messageService
            );
            this.canvasInteractionHandler.attachHandlers();

            // --- Inicjalizacja zagnieżdżonych kontrolerów ---
            if (statisticsPanelController == null) throw new IllegalStateException("StatisticsPanelController not injected.");
            statisticsPanelController.initializeController(this.statisticsViewModel, this.messageService);

            if (selectedAnimalPanelController == null) throw new IllegalStateException("SelectedAnimalPanelController not injected.");
            selectedAnimalPanelController.initializeController(this.selectedAnimalViewModel, this.messageService);

            if (genotypesPanelController == null) throw new IllegalStateException("GenotypesPanelController not injected.");
            genotypesPanelController.initializeController(this.statisticsViewModel, this.messageService);

            if (chartPanelController == null) throw new IllegalStateException("StatisticsChartPanelController not injected.");
            chartPanelController.initializeController(this.chartDataModel, this.messageService);

            if (chartControlsPanelController == null) throw new IllegalStateException("ChartControlsPanelController not injected.");
            chartControlsPanelController.initializeController(this.chartPanelController, this.messageService);

            // --- Konfiguracja Sceny i CSS ---
            Scene scene = ownerStage.getScene();
            if (scene == null) { throw new IllegalStateException("Scene not found on owner stage. Cannot apply CSS."); }
            String cssPath = "/css/simulation-styles.css";
            try {
                String cssUrl = getClass().getResource(cssPath).toExternalForm();
                if (!scene.getStylesheets().contains(cssUrl)) {
                    scene.getStylesheets().add(cssUrl);
                    System.out.println("Applied CSS: " + cssPath);
                }
            } catch (NullPointerException e) {
                System.err.println("Warning: Could not load CSS file: " + cssPath + ". Styles may be missing.");
            } catch (Exception e) {
                System.err.println("Error applying CSS file " + cssPath + ": " + e.getMessage());
                e.printStackTrace();
            }

            // --- Ustawienie powiązań UI i responsywności ---
            bindLifecycleControls();
            setupCanvasResponsiveness(); // Setup canvas responsiveness AFTER mapRenderer is initialized

            // --- Ustawienie nasłuchiwaczy ---
            setupDayEndListener();
            setupWindowCloseHandler(ownerStage);

            // --- Rejestracja w ActiveSimulationRegistry ---
            this.activeSimulationRegistry.register(this.configName, this, this.ownerStage);

            // --- Uruchomienie wątków tła i renderowania ---
            startBackgroundThreads();
            simulationRenderer.start();
            simulationEngine.start();

            // --- Końcowa konfiguracja UI ---
            updateCollectingDataButtonText();
            collectingDataButton.setDisable(false);
            takeSnapshotButton.setDisable(false);

            // --- Narysuj początkowy stan mapy ---
            Platform.runLater(this::drawInitialMapState);

        } catch (Exception e) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.sim.init", e.getMessage()));
            e.printStackTrace();
            shutdownUI();
            // W przypadku błędu inicjalizacji, wyrejestruj się i zamknij okno
            this.activeSimulationRegistry.unregister(this.configName, this); // Wyrejestruj się
            if (ownerStage != null && ownerStage.isShowing()) {
                ownerStage.close();
            }
        }
    }

    /** Wiąże kontrolki cyklu życia (przyciski, suwak) z SimulationLifecycleManager. */
    private void bindLifecycleControls() {
        if (lifecycleManager == null) return;
        playPauseButton.disableProperty().bind(lifecycleManager.canControlProperty().not());
        speedSlider.disableProperty().bind(lifecycleManager.canControlProperty().not());
        playPauseButton.textProperty().bind(Bindings.createStringBinding(() -> {
            if (!lifecycleManager.canControlProperty().get()) {
                return messageService.getMessage("sim.button.ended");
            } else {
                return lifecycleManager.pausedProperty().get()
                        ? messageService.getMessage("sim.button.play")
                        : messageService.getMessage("sim.button.pause");
            }
        }, lifecycleManager.pausedProperty(), lifecycleManager.canControlProperty()));
        speedSlider.valueProperty().bindBidirectional(lifecycleManager.speedSliderValueProperty());
    }

    /** Konfiguruje płótno symulacji do dynamicznego zmieniania rozmiaru. */
    private void setupCanvasResponsiveness() {
        if (canvasScrollPane != null && simulationCanvas != null && mapRenderer != null) {
            final double padding = AppConstants.CANVAS_PADDING;

            // Bind canvas size to scroll pane viewport size minus padding
            simulationCanvas.widthProperty().bind(canvasScrollPane.widthProperty().subtract(padding));
            simulationCanvas.heightProperty().bind(canvasScrollPane.heightProperty().subtract(padding));

            // Add listeners to redraw when the bound size actually changes
            simulationCanvas.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() > 0) { // Only redraw if valid size
                    updateCanvasAndRedraw();
                }
            });
            simulationCanvas.heightProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() > 0) { // Only redraw if valid size
                    updateCanvasAndRedraw();
                }
            });
            // Initial attempt to update/redraw (might still be zero size here)
            // Platform.runLater(this::updateCanvasAndRedraw); // Moved initial draw later
        } else {
            System.err.println("Warning: Canvas ScrollPane, Canvas, or MapRenderer is null. Canvas will not resize.");
        }
    }


    /**
     * Ustawia nasłuchiwacz reagujący na koniec dnia symulacji.
     * Aktualizuje ViewModel statystyk oraz model danych wykresu.
     */
    private void setupDayEndListener() {
        if (simulation == null || statisticsManager == null || selectedAnimalViewModel == null || chartDataModel == null || lifecycleManager == null || simulationEngine == null) {
            System.err.println("Cannot setup day end listener - required components missing.");
            return;
        }
        simulation.addDayEndListener(() -> {
            statisticsManager.generateAndSaveDailyStatisticsIfNeeded();
            Optional<SimulationStatisticsSnapshot> statsSnapshotOpt = statisticsManager.generateCurrentSnapshot();

            Platform.runLater(() -> {
                statsSnapshotOpt.ifPresent(snapshot -> {
                    statisticsViewModel.updateStatistics(snapshot);
                    if (this.chartDataModel != null) {
                        this.chartDataModel.addDailyData(snapshot);
                    }
                });
                if (statsSnapshotOpt.isEmpty()) {
                    statisticsViewModel.clearStatistics();
                }

                if (lastProcessedSnapshot != null) {
                    checkSelectedAnimalLiveness(lastProcessedSnapshot);
                }
                if (simulationEngine.isStopped()) {
                    lifecycleManager.simulationEnded();
                }
            });
        });
    }

    /** Dodaje handler zdarzenia zamknięcia okna do zatrzymania wątków. */
    private void setupWindowCloseHandler(Window ownerWindow) {
        Objects.requireNonNull(ownerWindow, "OwnerWindow cannot be null for close handler setup");
        ownerWindow.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            String windowTitle = (ownerStage != null) ? ownerStage.getTitle() : "Unknown Window";
            System.out.println("Simulation window closing request received: " + windowTitle);
            this.activeSimulationRegistry.unregister(this.configName, this);
            System.out.println("Unregistered simulation from ActiveSimulationRegistry: " + windowTitle);
            stopSimulationThreads();
        });
    }

    /** Uruchamia wątki tła (np. SimulationStateProducer). */
    private void startBackgroundThreads() {
        if (stateProducer != null) {
            Thread producerThread = new Thread(stateProducer, "SimStateProducer-" + configName);
            producerThread.setDaemon(true);
            producerThread.start();
        } else {
            System.err.println("Error: Cannot start state producer thread, stateProducer is null.");
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getMessage("error.thread.start.fail"));
        }
    }

    /** Obsługuje kliknięcie przycisku Play/Pause. */
    @FXML private void handlePlayPause() {
        if (lifecycleManager != null) {
            lifecycleManager.togglePause();
        }
    }

    /** Obsługuje kliknięcie przycisku 'Log Data'. */
    @FXML private void handleToggleDataCollection() {
        if (statisticsManager != null) {
            statisticsManager.toggleDataCollection();
            updateCollectingDataButtonText();
        }
    }

    /** Obsługuje kliknięcie przycisku 'Take Snapshot'. */
    @FXML private void handleTakeSnapshot() {
        if (statisticsManager == null || lifecycleManager == null) {
            System.err.println("Cannot take snapshot: Required components missing.");
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getMessage("error.snapshot.unavailable"));
            return;
        }
        boolean wasPausedForSnapshot = lifecycleManager.pauseForAction();
        try {
            statisticsManager.generateAndSaveSnapshotManually();
        } catch (IllegalStateException e) {
            System.err.println("State exception during manual snapshot save: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during manual snapshot: " + e.getMessage());
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.snapshot.unexpected", e.getMessage()));
            e.printStackTrace();
        } finally {
            if (wasPausedForSnapshot) {
                Platform.runLater(lifecycleManager::resumeAfterAction);
            }
        }
    }

    /** Callback wywoływany przez renderer po przetworzeniu migawki renderowania. */
    private void processRenderedSnapshot(SimulationRenderSnapshot snapshot) {
        lastProcessedSnapshot = snapshot;
        checkSelectedAnimalLiveness(snapshot);
    }

    /** Rysuje początkowy stan mapy, używając IAlertService do informowania o błędach. */
    private void drawInitialMapState() {
        if (stateProducer != null && simulationRenderer != null) {
            // --- Check canvas size before initial draw ---
            if (simulationCanvas == null || simulationCanvas.getWidth() <= 0 || simulationCanvas.getHeight() <= 0) {
                System.err.println("drawInitialMapState: Canvas size invalid, delaying initial draw.");
                // Reschedule if size is still invalid
                Platform.runLater(() -> {
                    if (simulationCanvas == null || simulationCanvas.getWidth() <= 0 || simulationCanvas.getHeight() <= 0){
                        System.err.println("drawInitialMapState (delayed): Canvas size still invalid. Initial state might not render.");
                        alertService.showAlert(IAlertService.AlertType.WARNING,
                                messageService.getMessage("warning.render.initial.header"),
                                messageService.getMessage("warning.render.initial.content")
                        );
                    } else {
                        drawInitialMapState(); // Try drawing again now that size might be valid
                    }
                });
                return; // Stop this attempt
            }
            // --- End canvas size check ---

            System.out.println("Drawing initial simulation map state...");
            SimulationRenderSnapshot initialSnapshot = stateProducer.createInitialSnapshot();
            if (initialSnapshot != null) {
                lastProcessedSnapshot = initialSnapshot;
                simulationRenderer.redrawFrame(initialSnapshot); // Draw the frame
            } else {
                System.err.println("Internal Error: Could not draw initial map state - initial snapshot creation failed.");
                clearCanvas();
                alertService.showAlert(IAlertService.AlertType.WARNING,
                        messageService.getMessage("warning.title"),
                        messageService.getMessage("warning.render.initial.header"),
                        messageService.getMessage("warning.render.initial.content")
                );
            }
        } else {
            System.err.println("Internal Error: Cannot draw initial state: StateProducer or SimulationRenderer is null.");
            clearCanvas();
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getMessage("error.render.initial.componentNull.header"),
                    messageService.getMessage("error.render.initial.componentNull.content")
            );
        }
    }

    /** Czyści płótno. */
    private void clearCanvas() {
        if (simulationCanvas != null) {
            GraphicsContext gc = simulationCanvas.getGraphicsContext2D();
            if (gc != null) { // Add null check for GraphicsContext
                gc.clearRect(0, 0, simulationCanvas.getWidth(), simulationCanvas.getHeight());
            }
        }
    }

    /** Aktualizuje renderer mapy i przerysowuje ją po zmianie rozmiaru płótna. */
    private void updateCanvasAndRedraw() {
        // ---- ADD SIZE CHECK ----
        if (simulationCanvas == null || simulationCanvas.getWidth() <= 0 || simulationCanvas.getHeight() <= 0) {
            //System.out.println("updateCanvasAndRedraw skipped: Canvas size invalid."); // Optional log
            return; // Don't redraw if canvas size is not yet determined or invalid
        }
        // ---- END SIZE CHECK ----

        if (mapRenderer != null) {
            mapRenderer.updateCellDimensions(); // Calculate new dimensions
        }
        // No need to check simulationRenderer == null here, redrawLastState does it
        redrawLastState(); // Redraw using the last known snapshot
    }

    /** Planuje przerysowanie ostatniego znanego stanu w wątku UI. */
    private void redrawLastState() {
        Platform.runLater(() -> {
            // --- ADD SIZE CHECK (again, important before drawing) ---
            if (simulationCanvas == null || simulationCanvas.getWidth() <= 0 || simulationCanvas.getHeight() <= 0) {
                // System.out.println("redrawLastState skipped: Canvas size invalid."); // Optional log
                return;
            }
            // --- END SIZE CHECK ---

            if (simulationRenderer != null) {
                // Draw the last known snapshot, which might be null initially
                simulationRenderer.redrawFrame(lastProcessedSnapshot);
            }
            // Removed redundant drawInitialMapState call
        });
    }

    /** Sprawdza, czy aktualnie wybrane zwierzę nadal istnieje w ostatniej migawce renderowania. */
    private void checkSelectedAnimalLiveness(SimulationRenderSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "Snapshot cannot be null for checking liveness");
        if (selectedAnimalViewModel == null || !selectedAnimalViewModel.isSelectedProperty().get()) return;

        Animal currentlySelected = selectedAnimalViewModel.getCurrentAnimalReference();
        if (currentlySelected == null) {
            selectedAnimalViewModel.clear();
            return;
        }

        long selectedId = currentlySelected.getId();
        boolean foundAliveInSnapshot = false;

        if (snapshot.animals() != null) {
            searchLoop:
            for (List<Animal> listAtPos : snapshot.animals().values()) {
                for (Animal animalInSnapshot : listAtPos) {
                    if (animalInSnapshot.getId() == selectedId) {
                        foundAliveInSnapshot = true;
                        selectedAnimalViewModel.update(animalInSnapshot);
                        break searchLoop;
                    }
                }
            }
        }

        if (!foundAliveInSnapshot) {
            // If not found in snapshot, update ViewModel with the last known reference
            // This will show its final state (potentially dead)
            selectedAnimalViewModel.update(currentlySelected);
            if (currentlySelected.isAlive()) {
                // Log if the animal reference claims it's alive but wasn't in the snapshot's live list
                System.err.println("Warning: Selected animal ID " + selectedId + " not found in render snapshot's animal map, but reference indicates alive.");
            }
        }
    }

    /** Zatrzymuje wszystkie wątki związane z symulacją. Wywoływana przy zamykaniu okna. */
    public void stopSimulationThreads() {
        String windowTitle = (ownerStage != null) ? ownerStage.getTitle() : "Unknown Window";
        System.out.println("Stopping simulation components for window: " + windowTitle);
        if (simulationRenderer != null) simulationRenderer.stop();
        if (simulationEngine != null) simulationEngine.stop();
        if (stateProducer != null) stateProducer.stop();
        if (canvasInteractionHandler != null) canvasInteractionHandler.detachHandlers();
        System.out.println("Simulation components stop signals sent for window: " + windowTitle);
    }

    /** Wyłącza UI w razie krytycznego błędu inicjalizacji. */
    private void shutdownUI() {
        setInitialUIState();
        if (canvasInteractionHandler != null) canvasInteractionHandler.detachHandlers();
        clearCanvas();
    }

    /** Aktualizuje tekst przycisku 'Log Data' na podstawie stanu StatisticsManager. */
    private void updateCollectingDataButtonText() {
        if (statisticsManager != null) {
            boolean isCollecting = statisticsManager.isCollectingData();
            String messageKey = isCollecting
                    ? "sim.button.logDataOn"
                    : "sim.button.logDataOff";
            collectingDataButton.setText(messageService.getMessage(messageKey));
        } else {
            collectingDataButton.setText(messageService.getMessage("sim.button.logDataError"));
            collectingDataButton.setDisable(true);
        }
    }

    // Gettery dla ViewModeli i Modelu Danych Wykresu
    public StatisticsViewModel getStatisticsViewModel() { return statisticsViewModel; }
    public SelectedAnimalViewModel getSelectedAnimalViewModel() { return selectedAnimalViewModel; }
    public ChartDataModel getChartDataModel() { return chartDataModel; }
}