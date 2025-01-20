package proj.app;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
//import proj.app.SimulationConfig;
import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.model.elements.Water;
import proj.model.genotype.Mutation;
import proj.model.genotype.RandomMutation;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.Globe;
import proj.model.maps.WaterWorld;
import proj.model.movement.AbstractMovementVariant;
import proj.model.movement.OldAgeAintNoPicnic;
import proj.model.movement.PredestinedMovement;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.model.vegetation.ForestedEquator;
import proj.presenter.ConsoleMapDisplay;
import proj.simulation.Simulation;
import proj.app.StatisticsManager;
import proj.simulation.SimulationProperties;
import proj.util.Vector2d;

public class SimulationWindowController {
    @FXML private Canvas simulationCanvas;
    @FXML private Button playPauseButton;
    @FXML private Slider speedSlider;
    @FXML private CheckBox showStatistics;
    @FXML private Label dayCount;
    @FXML private Label animalCount;
    @FXML private Label plantCount;
    @FXML private Label selectedAnimalEnergy;
    @FXML private Label selectedAnimalAge;
    @FXML private Label selectedAnimalChildren;
    @FXML private Label selectedAnimalPlantsEaten;

    private Simulation simulation;
    private SimulationProperties simProps;
    private StatisticsManager statisticsManager;
    private Animal selectedAnimal;
    private AnimationTimer animationTimer;
    private Thread simulationThread;
    private double cellWidth;
    private double cellHeight;

    public void initializeSimulation(SimulationProperties config) {
        System.out.println("Initializing simulation with config: " + config.getConfigName());
        simProps = config;
        simulation = getSimulation(simProps);
        statisticsManager = new StatisticsManager(simulation, simProps);

        System.out.println("Map dimensions: " + simProps.getWidth() + "x" + simProps.getHeight());

        setupCanvas();
        setupAnimationTimer();
        setupSimulationThread();
        setupCanvasInteraction();

        // Force initial draw
        updateCellDimensions();
        drawSimulation();

        simulationThread.start();
    }

    private static Simulation getSimulation(SimulationProperties simulationProperties) {
        AbstractVegetationVariant vegetation = switch(simulationProperties.getVegetationVariant()) {
            case FORESTED_EQUATOR -> new ForestedEquator(
                    simulationProperties.getEquatorHeight(),
                    simulationProperties.getWidth(),
                    simulationProperties.getHeight()
            );
        };
        Mutation mutation = switch(simulationProperties.getMutationVariant()) {
            case RANDOM -> new RandomMutation();
        };
        AbstractMovementVariant movement = switch(simulationProperties.getMovementVariant()) {
            case PREDESTINED -> new PredestinedMovement();
            case OLD_AGE_AINT_NO_PICNIC -> new OldAgeAintNoPicnic();
        };
        AbstractWorldMap map = switch(simulationProperties.getMapVariant()) {
            case GLOBE -> new Globe(simulationProperties, vegetation, movement);
            case WATER_WORLD -> new WaterWorld(simulationProperties, vegetation, movement);
        };
        ConsoleMapDisplay observer = new ConsoleMapDisplay();
        map.addObserver(observer);
        return new Simulation(map, simulationProperties, mutation);
    }

    private void setupCanvas() {
        // Set initial canvas size
        simulationCanvas.setWidth(800);
        simulationCanvas.setHeight(700);

        simulationCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                ScrollPane scrollPane = (ScrollPane) simulationCanvas.getParent().getParent();
                simulationCanvas.widthProperty().bind(scrollPane.widthProperty().multiply(0.8));
                simulationCanvas.heightProperty().bind(scrollPane.heightProperty().multiply(0.8));
            }
        });

        // Update dimensions when canvas size changes
        simulationCanvas.widthProperty().addListener((obs, oldVal, newVal) -> updateCellDimensions());
        simulationCanvas.heightProperty().addListener((obs, oldVal, newVal) -> updateCellDimensions());
    }


    private void updateCellDimensions() {
        if (simulation != null && simulation.getMap() != null) {
            double canvasWidth = simulationCanvas.getWidth();
            double canvasHeight = simulationCanvas.getHeight();

            System.out.println("Canvas dimensions: " + canvasWidth + "x" + canvasHeight);

            if (canvasWidth > 0 && canvasHeight > 0) {
                cellWidth = canvasWidth / simProps.getWidth();
                cellHeight = canvasHeight / simProps.getHeight();

                // Force redraw when dimensions are updated
                drawSimulation();
            }
        }
    }

    private void setupAnimationTimer() {
        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                long elapsedNanos = now - lastUpdate;
                if (elapsedNanos >= (1000000000 / (speedSlider.getValue() + 1))) {
                    System.out.println("Drawing frame...");
                    updateStatistics();
                    drawSimulation();
                    lastUpdate = now;
                }
            }
        };
        animationTimer.start();
    }

    private void setupSimulationThread() {
        simulationThread = new Thread(simulation);
        simulationThread.setDaemon(true);
    }

    private void setupCanvasInteraction() {
        simulationCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            double x = event.getX();
            double y = event.getY();

            // Convert canvas coordinates to map coordinates
            int mapX = (int) (x / cellWidth);
            int mapY = (int) (y / cellHeight);

            Vector2d clickedPos = new Vector2d(mapX, mapY);

            if (simulation.getMap().getAnimals().containsKey(clickedPos)) {
                selectedAnimal = simulation.getMap().getAnimals().get(clickedPos).get(0);
                updateSelectedAnimalStats();
                drawSimulation(); // Redraw to show selection
            } else {
                selectedAnimal = null;
                clearSelectedAnimalStats();
                drawSimulation(); // Redraw to clear selection
            }
        });
    }

    private void drawSimulation() {
        if (simulation == null || simulation.getMap() == null) return;

        GraphicsContext gc = simulationCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, simulationCanvas.getWidth(), simulationCanvas.getHeight());

        AbstractWorldMap map = simulation.getMap();

        // Draw grid
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);

        // Draw vertical grid lines
        for (int x = 0; x <= simProps.getWidth(); x++) {
            gc.strokeLine(x * cellWidth, 0, x * cellWidth, simulationCanvas.getHeight());
        }

        // Draw horizontal grid lines
        for (int y = 0; y <= simProps.getHeight(); y++) {
            gc.strokeLine(0, y * cellHeight, simulationCanvas.getWidth(), y * cellHeight);
        }

        // Draw water (if WaterWorld)
        if (map instanceof WaterWorld) {
            gc.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5)); // Semi-transparent water
            ((WaterWorld) map).getWaterFields().forEach((pos, water) -> {
                drawCell(gc, pos.x(), pos.y(), true);
            });
        }

        // Draw plants
        gc.setFill(Color.FORESTGREEN);
        map.getPlants().forEach((pos, plant) -> {
            double plantSize = Math.min(cellWidth, cellHeight) * 0.7;
            double offsetX = (cellWidth - plantSize) / 2;
            double offsetY = (cellHeight - plantSize) / 2;
            gc.fillOval(
                    pos.x() * cellWidth + offsetX,
                    pos.y() * cellHeight + offsetY,
                    plantSize,
                    plantSize
            );
        });

        // Draw animals
        map.getAnimals().forEach((pos, animals) -> {
            if (!animals.isEmpty()) {
                Animal animal = animals.get(0);
                // Calculate color based on energy
                double energyRatio = Math.min(1.0, animal.getEnergy() / (double)simProps.getStartEnergy());
                Color animalColor = Color.rgb(
                        (int)(255 * (1 - energyRatio)),  // More red when low energy
                        (int)(255 * energyRatio),        // More green when high energy
                        0
                );

                gc.setFill(animalColor);
                drawCell(gc, pos.x(), pos.y(), false);

                // Highlight selected animal
                if (animal == selectedAnimal) {
                    gc.setStroke(Color.YELLOW);
                    gc.setLineWidth(2);
                    gc.strokeRect(
                            pos.x() * cellWidth + 1,
                            pos.y() * cellHeight + 1,
                            cellWidth - 2,
                            cellHeight - 2
                    );
                }
            }
        });
    }

    private void drawCell(GraphicsContext gc, int x, int y, boolean fill) {
        if (fill) {
            gc.fillRect(
                    x * cellWidth,
                    y * cellHeight,
                    cellWidth,
                    cellHeight
            );
        } else {
            gc.fillRect(
                    x * cellWidth + 1,
                    y * cellHeight + 1,
                    cellWidth - 2,
                    cellHeight - 2
            );
        }
    }

    private void updateStatistics() {
        dayCount.setText(String.valueOf(simProps.getDaysElapsed()));
        animalCount.setText(String.valueOf(simulation.getAnimals().size()));
        plantCount.setText(String.valueOf(simulation.getMap().getPlants().size()));

        if (selectedAnimal != null && selectedAnimal.getEnergy() > 0) {
            updateSelectedAnimalStats();
        } else {
            selectedAnimal = null;
            clearSelectedAnimalStats();
        }
    }

    private void updateSelectedAnimalStats() {
        selectedAnimalEnergy.setText(String.valueOf(selectedAnimal.getEnergy()));
        selectedAnimalAge.setText(String.valueOf(selectedAnimal.getAge()));
        selectedAnimalChildren.setText(String.valueOf(selectedAnimal.getChildrenMade()));
        selectedAnimalPlantsEaten.setText(String.valueOf(selectedAnimal.getPlantsEaten()));
    }

    private void clearSelectedAnimalStats() {
        selectedAnimalEnergy.setText("-");
        selectedAnimalAge.setText("-");
        selectedAnimalChildren.setText("-");
        selectedAnimalPlantsEaten.setText("-");
    }

    @FXML
    private void handlePlayPause() {
        simulation.togglePause();
        playPauseButton.setText(simulation.isRunning() ? "Pause" : "Play");
    }

    @FXML
    private void handleExportStatistics() {
        statisticsManager.exportStatistics();
    }
}