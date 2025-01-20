package proj.app;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
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
//import proj.presenter.ConsoleMapDisplay;
import proj.simulation.Simulation;
import proj.app.StatisticsManager;
import proj.simulation.SimulationProperties;
import proj.util.Vector2d;

import java.util.*;
import java.util.stream.Collectors;

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
    @FXML private Label emptyFieldsCount;
    @FXML private TextArea popularGenotypes;
    @FXML private Label averageEnergy;
    @FXML private Label averageLifespan;
    @FXML private Label averageChildren;
    @FXML private Label selectedAnimalGenotype;
    @FXML private Label selectedAnimalActiveGene;
    @FXML private Label selectedAnimalDescendants;
    @FXML private Label selectedAnimalDeathDate;


    private Simulation simulation;
    private SimulationProperties simProps;
    private StatisticsManager statisticsManager;
    private Animal selectedAnimal;
    private AnimationTimer animationTimer;
    private Thread simulationThread;
    private double cellWidth;
    private double cellHeight;

    // Colors for top genotypes
    private static final Color TOP_GENOTYPE_COLOR = Color.MAGENTA;
    private static final Color SECOND_GENOTYPE_COLOR = Color.BLACK;
    private static final Color THIRD_GENOTYPE_COLOR = Color.BLUE;
    private static final double GENOTYPE_BORDER_WIDTH = 2.0;

    // Store current top genotypes for coloring
    private List<String> topGenotypeStrings = new ArrayList<>();

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
//        ConsoleMapDisplay observer = new ConsoleMapDisplay();
//        map.addObserver(observer);
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

        // Highlight preferred fields (equator) in gray
        int equatorHeight = simProps.getEquatorHeight(); // Pobierz wysokość równika z konfiguracji
        int mapHeight = simProps.getHeight();

        int equatorStartY = (mapHeight - equatorHeight) / 2;
        int equatorEndY = equatorStartY + equatorHeight;

        gc.setFill(Color.LIGHTGRAY);
        for (int y = equatorStartY; y < equatorEndY; y++) {
            for (int x = 0; x < simProps.getWidth(); x++) {
                gc.fillRect(
                        x * cellWidth,
                        y * cellHeight,
                        cellWidth,
                        cellHeight
                );
            }
        }

        // Draw water (if WaterWorld)
        if (map instanceof WaterWorld) {
            gc.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
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

                // Draw animal with energy-based color
                gc.setFill(getAnimalEnergyColor(animal));
                drawCell(gc, pos.x(), pos.y(), false);

                // Draw genotype border if it's one of the top genotypes
                Color borderColor = getGenotypeBorderColor(animal);
                if (borderColor != null) {
                    gc.setStroke(borderColor);
                    gc.setLineWidth(GENOTYPE_BORDER_WIDTH);
                    gc.strokeRect(
                            pos.x() * cellWidth + GENOTYPE_BORDER_WIDTH / 2,
                            pos.y() * cellHeight + GENOTYPE_BORDER_WIDTH / 2,
                            cellWidth - GENOTYPE_BORDER_WIDTH,
                            cellHeight - GENOTYPE_BORDER_WIDTH
                    );
                }

                // Highlight selected animal
                if (animal == selectedAnimal) {
                    gc.setStroke(Color.YELLOW);
                    gc.setLineWidth(3);
                    gc.strokeRect(
                            pos.x() * cellWidth + 3 / 2,
                            pos.y() * cellHeight + 3 / 2,
                            cellWidth - 3,
                            cellHeight - 3
                    );
                }
            }
        });
    }


    private Color getAnimalEnergyColor(Animal animal) {
        double energyRatio = Math.min(1.0, animal.getEnergy() / (double)simProps.getStartEnergy());
        return Color.rgb(
                (int)(255 * (1 - energyRatio)),  // More red when low energy
                (int)(255 * energyRatio),        // More green when high energy
                0
        );
    }

    private Color getGenotypeBorderColor(Animal animal) {
        String animalGenotype = geneSequenceToString(animal.getGenes());

        if (!topGenotypeStrings.isEmpty() && animalGenotype.equals(topGenotypeStrings.get(0))) {
            return TOP_GENOTYPE_COLOR;
        } else if (topGenotypeStrings.size() > 1 && animalGenotype.equals(topGenotypeStrings.get(1))) {
            return SECOND_GENOTYPE_COLOR;
        } else if (topGenotypeStrings.size() > 2 && animalGenotype.equals(topGenotypeStrings.get(2))) {
            return THIRD_GENOTYPE_COLOR;
        }
        return null;
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

    // Helper method to convert gene sequence to readable direction names
    private String geneSequenceToString(int[] genes) {
        StringBuilder sb = new StringBuilder();
        for (int gene : genes) {
            switch (gene) {
                case 0 -> sb.append("N");
                case 1 -> sb.append("NE");
                case 2 -> sb.append("E");
                case 3 -> sb.append("SE");
                case 4 -> sb.append("S");
                case 5 -> sb.append("SW");
                case 6 -> sb.append("W");
                case 7 -> sb.append("NW");
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    private void updateStatistics() {
        AbstractWorldMap map = simulation.getMap();
        List<Animal> animals = simulation.getAnimals();

        // Update existing statistics
        dayCount.setText(String.valueOf(simProps.getDaysElapsed()));
        animalCount.setText(String.valueOf(animals.size()));
        plantCount.setText(String.valueOf(map.getPlants().size()));

        // Calculate and update empty fields
        int totalFields = simProps.getWidth() * simProps.getHeight();
        Set<Vector2d> occupiedPositions = new HashSet<>();

// Dodaj pozycje zwierząt
        map.getAnimals().forEach((position, animal) -> {
            if (animal != null && !animal.isEmpty()) {
                occupiedPositions.add(position);
            }
        });

// Dodaj pozycje roślin
        map.getPlants().forEach((position, plant) -> {
            if (plant != null) {
                occupiedPositions.add(position);
            }
        });

// Dodaj pozycje pól wody (jeśli mapa to WaterWorld)
        if (map instanceof WaterWorld) {
            ((WaterWorld) map).getWaterFields().forEach((position, water) -> {
                occupiedPositions.add(position);
            });
        }

// Oblicz zajęte i wolne pola
        int occupiedFields = occupiedPositions.size();
        int emptyFields = Math.max(0, totalFields - occupiedFields); // Zabezpieczenie przed ujemnymi wartościami
        emptyFieldsCount.setText(String.valueOf(emptyFields));




        // Update genotype statistics and store top genotypes for coloring
        Map<String, Long> genotypeCounts = animals.stream()
                .map(animal -> geneSequenceToString(animal.getGenes()))
                .collect(Collectors.groupingBy(
                        genotype -> genotype,
                        Collectors.counting()
                ));

        // Clear previous top genotypes
        topGenotypeStrings.clear();

        StringBuilder genotypeText = new StringBuilder();
        genotypeText.append("Top 3 genotypes (and their colors):\n\n");

        genotypeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    double percentage = (entry.getValue() * 100.0) / animals.size();
                    String genotypeStr = entry.getKey();
                    topGenotypeStrings.add(genotypeStr);

                    String color = topGenotypeStrings.indexOf(genotypeStr) == 0 ? "Magenta" :
                            topGenotypeStrings.indexOf(genotypeStr) == 1 ? "Black" : "Blue";

                    genotypeText.append(String.format("%s (%s)\n%d animals (%.1f%%)\n\n",
                            genotypeStr,
                            color,
                            entry.getValue(),
                            percentage));
                });

        popularGenotypes.setText(animals.isEmpty() ? "No animals present" : genotypeText.toString());

        // Calculate average energy for living animals
        double avgEnergy = animals.stream()
                .mapToInt(Animal::getEnergy)
                .average()
                .orElse(0.0);
        averageEnergy.setText(String.format("%.2f", avgEnergy));

        // Calculate average lifespan for dead animals
        List<Animal> deadAnimals = simulation.getDeadAnimals();
        double avgLifespan = deadAnimals.isEmpty() ? 0.0 :
                deadAnimals.stream()
                        .filter(animal -> animal.getDeathDate() != -1)
                        .mapToInt(animal -> animal.getDeathDate() - animal.getBirthDate())
                        .average()
                        .orElse(0.0);
        averageLifespan.setText(String.format("%.2f days", avgLifespan));

        // Calculate average number of children for living animals
        double avgChildren = animals.stream()
                .mapToInt(Animal::getChildrenMade)
                .average()
                .orElse(0.0);
        averageChildren.setText(String.format("%.2f", avgChildren));

        // Update selected animal statistics if present
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

        // Ustawienie genotypu
        selectedAnimalGenotype.setText(geneSequenceToString(selectedAnimal.getGenes()));

        // Aktywna część genomu
        selectedAnimalActiveGene.setText(String.valueOf(selectedAnimal.getActiveGeneIndex()));

        // Liczba potomków
        selectedAnimalDescendants.setText(String.valueOf(selectedAnimal.getDescendantsCount()));

        // Dzień śmierci (jeśli zwierzę nie żyje)
        if (selectedAnimal.isAlive()) {
            selectedAnimalDeathDate.setText("-");
        } else {
            selectedAnimalDeathDate.setText(String.valueOf(selectedAnimal.getDeathDate()));
        }
    }


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