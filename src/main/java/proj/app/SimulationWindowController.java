package proj.app;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import proj.model.elements.Animal;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.WaterWorld;
import proj.simulation.*;
import proj.util.Vector2d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class for the simulation window.
 * Modified to use producer-consumer pattern for thread-safe rendering.
 */
public class SimulationWindowController {
    @FXML private Canvas simulationCanvas;
    @FXML private Button playPauseButton;
    @FXML private Slider speedSlider;
    @FXML private Label dayCount;
    @FXML private Label animalCount;
    @FXML private Label plantCount;
    @FXML private Label selectedAnimalEnergy;
    @FXML private Label selectedAnimalAge;
    @FXML private Label selectedAnimalChildren;
    @FXML private Label selectedAnimalPlantsEaten;
    @FXML private Label emptyFieldsCount;
    @FXML private TextArea popularGenotypes;
    @FXML private Label averageEnergy;
    @FXML private Label averageLifespan;
    @FXML private Label averageChildren;
    @FXML private Label selectedAnimalGenotype;
    @FXML private Label selectedAnimalActiveGene;
    @FXML private Label selectedAnimalDescendants;
    @FXML private Label selectedAnimalDeathDate;
    @FXML private Button collectingDataButton;
    @FXML private Button takeSnapshotButton;

    private Simulation simulation;
    private SimulationProperties simProps;
    private StatisticsManager statisticsManager;
    private MapRenderer mapRenderer;
    private StatisticsViewModel statisticsViewModel;
    private SimulationController simulationController;

    // New fields for producer-consumer pattern
    private SimulationStateQueue stateQueue;
    private SimulationStateProducer stateProducer;
    private Animal selectedAnimal;
    private static final int MAX_QUEUE_SIZE = 5;  // Limit queue size to prevent memory issues

    /**
     * Initializes the simulation with the given configuration.
     * Sets up the components needed for the simulation.
     *
     * @param config The simulation configuration
     */
    public void initializeSimulation(SimulationProperties config) {
        this.simProps = config;
        this.simulation = SimulationFactory.createSimulation(simProps);
        this.statisticsManager = new StatisticsManager(simulation, simProps);

        // Initialize the specialized components
        this.mapRenderer = new MapRenderer(simulationCanvas, simProps);
        this.statisticsViewModel = new StatisticsViewModel(simulation, simProps);
        this.simulationController = new SimulationController(simulation, statisticsManager);

        // Initialize producer-consumer components
        this.stateQueue = new SimulationStateQueue(MAX_QUEUE_SIZE);
        this.stateProducer = new SimulationStateProducer(simulation, statisticsViewModel, stateQueue);

        updateCollectingDataButton();

        setupCanvas();
        setupAnimationTimer();
        setupSimulationThread();
        setupCanvasInteraction();
        setupStateProducerThread();

        // Force initial draw
        mapRenderer.updateCellDimensions();
        updateMapFromSimulation();

        simulationController.startSimulation();
    }

    private void updateCollectingDataButton() {
        boolean collecting = statisticsManager.isCollectingData();
        collectingDataButton.setText("Collecting data: " + (collecting ? "On" : "Off"));
    }

    /**
     * Sets up the state producer thread.
     */
    private void setupStateProducerThread() {
        Thread producerThread = new Thread(stateProducer);
        producerThread.setDaemon(true);
        producerThread.start();
    }

    /**
     * Sets up the canvas properties and listeners.
     */
    private void setupCanvas() {
        // Set initial canvas size
        simulationCanvas.setWidth(800);
        simulationCanvas.setHeight(700);

        // Bezpieczne wiązanie rozmiaru z wykorzystaniem sceny
        simulationCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Znajdź ScrollPane w hierarchii widoku
                ScrollPane scrollPane = findScrollPane(simulationCanvas);

                if (scrollPane != null) {
                    simulationCanvas.widthProperty().bind(scrollPane.widthProperty().multiply(0.8));
                    simulationCanvas.heightProperty().bind(scrollPane.heightProperty().multiply(0.8));
                }
            }
        });

        // Update cell dimensions when canvas size changes
        simulationCanvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (mapRenderer != null) {
                mapRenderer.updateCellDimensions();
            }
        });

        simulationCanvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (mapRenderer != null) {
                mapRenderer.updateCellDimensions();
            }
        });
    }

    // Pomocnicza metoda do znajdowania ScrollPane w hierarchii widoku
    private ScrollPane findScrollPane(javafx.scene.Node node) {
        if (node == null) return null;

        // Sprawdź aktualny węzeł
        if (node instanceof ScrollPane) {
            return (ScrollPane) node;
        }

        // Sprawdź rodzica
        if (node.getParent() instanceof ScrollPane) {
            return (ScrollPane) node.getParent();
        }

        // Rekurencyjnie sprawdź rodziców
        Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane) {
                return (ScrollPane) parent;
            }
            parent = parent.getParent();
        }

        return null;
    }

    /**
     * Sets up the animation timer for updating the UI.
     */
    private void setupAnimationTimer() {
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                long elapsedNanos = now - lastUpdate;
                if (elapsedNanos >= (1000000000 / (speedSlider.getValue() + 1))) {
                    // Check if there's a new state in the queue
                    if (stateQueue.hasSnapshots()) {
                        // Consume the state and update UI
                        SimulationStateSnapshot snapshot = stateQueue.dequeue();
                        if (snapshot != null) {
                            updateUI(snapshot);
                            // Draw the map using the snapshot
                            mapRenderer.drawSimulation(snapshot);
                        }
                    }
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    /**
     * Updates UI elements with data from the simulation state snapshot.
     */
    private void updateUI(SimulationStateSnapshot snapshot) {
        try {
            Platform.runLater(() -> {
                try {
                    dayCount.setText(String.valueOf(simProps.getDaysElapsed()));
                    animalCount.setText(String.valueOf(countAnimals(snapshot)));
                    plantCount.setText(String.valueOf(snapshot.getPlants().size()));
                    emptyFieldsCount.setText(String.valueOf(statisticsViewModel.getEmptyFieldsCount()));
                    popularGenotypes.setText(statisticsViewModel.getGenotypesText());
                    averageEnergy.setText(String.format("%.2f", statisticsViewModel.getAverageEnergy()));
                    averageLifespan.setText(String.format("%.2f days", statisticsViewModel.getAverageLifespan()));
                    averageChildren.setText(String.format("%.2f", statisticsViewModel.getAverageChildren()));

                    // Check if selected animal is still valid
                    if (selectedAnimal != null) {
                        if (selectedAnimal.getEnergy() > 0) {
                            updateSelectedAnimalStats();
                        } else {
                            selectedAnimal = null;
                            clearSelectedAnimalStats();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Błąd podczas aktualizacji interfejsu: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Błąd podczas aktualizacji statystyk: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Count total animals in the snapshot.
     */
    private int countAnimals(SimulationStateSnapshot snapshot) {
        int count = 0;
        for (List<Animal> animals : snapshot.getAnimals().values()) {
            count += animals.size();
        }
        return count;
    }

    /**
     * Sets up the simulation thread.
     */
    private void setupSimulationThread() {
        Thread simulationThread = new Thread(simulation);
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    /**
     * Updates the map directly from simulation (used for initial rendering).
     */
    private void updateMapFromSimulation() {
        // Create a snapshot directly for initial rendering
        AbstractWorldMap map = simulation.getMap();
        if (map != null) {
            Map<Vector2d, ?> waterFields = null;
            if (map instanceof WaterWorld) {
                synchronized (map) {
                    waterFields = new HashMap<>(((WaterWorld) map).getWaterFields());
                }
            }

            SimulationStateSnapshot initialSnapshot;
            synchronized (map) {
                initialSnapshot = new SimulationStateSnapshot(
                        map.getAnimals(),
                        map.getPlants(),
                        waterFields,
                        statisticsViewModel.getTopGenotypes(),
                        selectedAnimal
                );
            }

            mapRenderer.drawSimulation(initialSnapshot);
        }
    }

    /**
     * Sets up the canvas interaction for animal selection.
     */
    private void setupCanvasInteraction() {
        simulationCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Vector2d clickedPos = mapRenderer.convertCanvasToMapPosition(event.getX(), event.getY());

            AbstractWorldMap map = simulation.getMap();
            synchronized (map) {
                if (map.getAnimals().containsKey(clickedPos) && !map.getAnimals().get(clickedPos).isEmpty()) {
                    selectedAnimal = map.getAnimals().get(clickedPos).getFirst();
                    updateSelectedAnimalStats();
                } else {
                    selectedAnimal = null;
                    clearSelectedAnimalStats();
                }
            }

            // The selected animal will be included in the next state snapshot
            stateProducer.setSelectedAnimal(selectedAnimal);
        });
    }

    /**
     * Updates the UI with the selected animal's statistics.
     */
    private void updateSelectedAnimalStats() {
        selectedAnimalEnergy.setText(String.valueOf(selectedAnimal.getEnergy()));
        selectedAnimalAge.setText(String.valueOf(selectedAnimal.getAge()));
        selectedAnimalChildren.setText(String.valueOf(selectedAnimal.getChildrenMade()));
        selectedAnimalPlantsEaten.setText(String.valueOf(selectedAnimal.getPlantsEaten()));
        selectedAnimalGenotype.setText(GenotypeFormatter.formatGenotype(selectedAnimal.getGenes()));
        selectedAnimalActiveGene.setText(String.valueOf(selectedAnimal.getActiveGeneIndex()));
        selectedAnimalDescendants.setText(String.valueOf(selectedAnimal.getDescendantsCount()));
        selectedAnimalDeathDate.setText(selectedAnimal.isAlive() ? "-" : String.valueOf(selectedAnimal.getDeathDate()));
    }

    /**
     * Clears the selected animal's statistics from the UI.
     */
    private void clearSelectedAnimalStats() {
        selectedAnimalEnergy.setText("-");
        selectedAnimalAge.setText("-");
        selectedAnimalChildren.setText("-");
        selectedAnimalGenotype.setText("-");
        selectedAnimalPlantsEaten.setText("-");
        selectedAnimalActiveGene.setText("-");
        selectedAnimalDescendants.setText("-");
        selectedAnimalDeathDate.setText("-");
    }

    /**
     * Handles the play/pause button action.
     */
    @FXML
    private void handlePlayPause() {
        simulationController.togglePause();
        playPauseButton.setText(simulation.isRunning() ? "Pause" : "Play");
    }

    @FXML
    private void handleToggleDataCollection() {
        statisticsManager.toggleDataCollection();
        updateCollectingDataButton();
    }

    @FXML
    private void handleTakeSnapshot() {
        statisticsManager.takeSnapshot();
    }
}