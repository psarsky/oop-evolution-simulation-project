package proj.app;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.simulation.SimulationProperties;
import proj.util.Vector2d;

import java.util.*;

/**
 * Responsible for rendering the simulation map to a canvas.
 * Modified to use the producer-consumer pattern with simulation state snapshots.
 */
public class MapRenderer {
    private final Canvas canvas;
    private final SimulationProperties simProps;
    private double cellWidth;
    private double cellHeight;

    // Colors for top genotypes
    private static final Color TOP_GENOTYPE_COLOR = Color.MAGENTA;
    private static final Color SECOND_GENOTYPE_COLOR = Color.BLACK;
    private static final Color THIRD_GENOTYPE_COLOR = Color.BLUE;
    private static final double GENOTYPE_BORDER_WIDTH = 2.0;

    /**
     * Creates a new MapRenderer.
     *
     * @param canvas The canvas to render on
     * @param simProps The simulation properties
     */
    public MapRenderer(Canvas canvas, SimulationProperties simProps) {
        this.canvas = canvas;
        this.simProps = simProps;
        updateCellDimensions();
    }

    /**
     * Updates the cell dimensions based on canvas size.
     */
    public void updateCellDimensions() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        if (canvasWidth > 0 && canvasHeight > 0) {
            cellWidth = canvasWidth / simProps.getWidth();
            cellHeight = canvasHeight / simProps.getHeight();
        }
    }

    /**
     * Converts canvas coordinates to map position.
     *
     * @param canvasX X coordinate on the canvas
     * @param canvasY Y coordinate on the canvas
     * @return The corresponding map position
     */
    public Vector2d convertCanvasToMapPosition(double canvasX, double canvasY) {
        int mapX = (int) (canvasX / cellWidth);
        int mapY = (int) (canvasY / cellHeight);
        return new Vector2d(mapX, mapY);
    }

    /**
     * Draws the simulation state on the canvas using the provided state snapshot.
     * This method is now thread-safe as it works on an immutable snapshot.
     *
     * @param stateSnapshot The snapshot of simulation state to render
     */
    public void drawSimulation(SimulationStateSnapshot stateSnapshot) {
        if (stateSnapshot == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawGrid(gc);
        drawEquator(gc);
        drawWater(gc, stateSnapshot.getWaterFields());
        drawPlants(gc, stateSnapshot.getPlants());
        drawAnimals(gc, stateSnapshot.getAnimals(), stateSnapshot.getSelectedAnimal(), stateSnapshot.getTopGenotypes());
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);

        // Draw vertical grid lines
        for (int x = 0; x <= simProps.getWidth(); x++) {
            gc.strokeLine(x * cellWidth, 0, x * cellWidth, canvas.getHeight());
        }

        // Draw horizontal grid lines
        for (int y = 0; y <= simProps.getHeight(); y++) {
            gc.strokeLine(0, y * cellHeight, simProps.getWidth() * cellWidth, y * cellHeight);
        }
    }

    private void drawEquator(GraphicsContext gc) {
        int equatorHeight = simProps.getEquatorHeight();
        int mapHeight = simProps.getHeight();
        int equatorStartY = (mapHeight - equatorHeight) / 2;
        int equatorEndY = equatorStartY + equatorHeight;

        gc.setFill(Color.LIGHTGREEN);
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
    }

    private void drawWater(GraphicsContext gc, Map<Vector2d, ?> waterFields) {
        if (waterFields != null) {
            gc.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
            waterFields.forEach((pos, water) -> {
                drawCell(gc, pos.x(), pos.y(), true);
            });
        }
    }

    private void drawPlants(GraphicsContext gc, Map<Vector2d, Plant> plants) {
        // No need for synchronization - we're working on an immutable snapshot
        gc.setFill(Color.FORESTGREEN);
        plants.forEach((pos, plant) -> {
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
    }

    private void drawAnimals(GraphicsContext gc, Map<Vector2d, List<Animal>> animals,
                             Animal selectedAnimal, List<String> topGenotypes) {
        // No need for synchronization - we're working on an immutable snapshot
        animals.forEach((pos, animalList) -> {
            if (!animalList.isEmpty()) {
                Animal animal = animalList.getFirst();

                gc.setFill(getAnimalEnergyColor(animal));
                drawCell(gc, pos.x(), pos.y(), false);

                // Draw genotype border if it's one of the top genotypes
                Color borderColor = getGenotypeBorderColor(animal, topGenotypes);
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
                            pos.x() * cellWidth + (double) 3 / 2,
                            pos.y() * cellHeight + (double) 3 / 2,
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

    private Color getGenotypeBorderColor(Animal animal, List<String> topGenotypes) {
        String animalGenotype = GenotypeFormatter.formatGenotype(animal.getGenes());

        if (!topGenotypes.isEmpty() && animalGenotype.equals(topGenotypes.get(0))) {
            return TOP_GENOTYPE_COLOR;
        } else if (topGenotypes.size() > 1 && animalGenotype.equals(topGenotypes.get(1))) {
            return SECOND_GENOTYPE_COLOR;
        } else if (topGenotypes.size() > 2 && animalGenotype.equals(topGenotypes.get(2))) {
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
}