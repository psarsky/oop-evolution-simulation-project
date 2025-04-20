package proj.app.render;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import proj.app.GenotypeFormatter;
import proj.app.state.SimulationStateSnapshot;
import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.simulation.SimulationProperties;
import proj.util.Vector2d;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Renders the state of the simulation world onto a JavaFX {@link Canvas}.
 * It draws the grid, equator zone, water, plants, and animals based on data
 * provided in an immutable {@link SimulationStateSnapshot}. It also handles
 * highlighting for selected animals and animals with dominant genotypes.
 */
public class MapRenderer {
    private final Canvas canvas;
    private final SimulationProperties simProps;
    private double cellWidth;
    private double cellHeight;

    // Color definitions for rendering elements
    private static final Color GRID_COLOR = Color.GRAY;
    private static final double GRID_LINE_WIDTH = 0.5;
    private static final Color EQUATOR_FILL_COLOR = Color.rgb(144, 238, 144, 0.4); // LightGreen with transparency
    private static final Color WATER_FILL_COLOR = Color.rgb(173, 216, 230, 0.6); // LightBlue with transparency
    private static final Color PLANT_FILL_COLOR = Color.FORESTGREEN;
    private static final double PLANT_SIZE_RATIO = 0.7; // Relative size of plant within cell

    // Colors/Styles for highlighting
    private static final Color SELECTED_ANIMAL_BORDER_COLOR = Color.YELLOW;
    private static final double SELECTED_ANIMAL_BORDER_WIDTH = 3.0;
    private static final Color TOP_GENOTYPE_COLOR = Color.MAGENTA; // Color for the most dominant genotype
    private static final Color SECOND_GENOTYPE_COLOR = Color.BLACK;   // Color for the second most dominant
    private static final Color THIRD_GENOTYPE_COLOR = Color.BLUE;    // Color for the third most dominant
    private static final double GENOTYPE_BORDER_WIDTH = 2.0;

    /**
     * Constructs a {@code MapRenderer}.
     *
     * @param canvas   The {@link Canvas} on which the simulation will be rendered.
     * @param simProps The {@link SimulationProperties} containing map dimensions and other relevant info.
     */
    public MapRenderer(Canvas canvas, SimulationProperties simProps) {
        this.canvas = Objects.requireNonNull(canvas, "Canvas cannot be null");
        this.simProps = Objects.requireNonNull(simProps, "SimulationProperties cannot be null");
        updateCellDimensions(); // Calculate initial cell size
    }

    /**
     * Recalculates the width and height of each map cell based on the current canvas dimensions
     * and the map dimensions from {@link SimulationProperties}.
     * Should be called whenever the canvas size changes.
     */
    public void updateCellDimensions() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        // Prevent division by zero if map dimensions are invalid or canvas not yet sized
        if (canvasWidth > 0 && canvasHeight > 0 && simProps.getWidth() > 0 && simProps.getHeight() > 0) {
            cellWidth = canvasWidth / simProps.getWidth();
            cellHeight = canvasHeight / simProps.getHeight();
        } else {
            // Default or error state if dimensions are invalid
            cellWidth = 10; // Default small size
            cellHeight = 10;
            if (canvasWidth <= 0 || canvasHeight <= 0) System.err.println("MapRenderer: Canvas has zero dimensions.");
            if (simProps.getWidth() <= 0 || simProps.getHeight() <= 0) System.err.println("MapRenderer: SimulationProperties has zero map dimensions.");
        }
    }

    /**
     * Converts pixel coordinates from the canvas to logical map coordinates ({@link Vector2d}).
     *
     * @param canvasX The x-coordinate on the canvas.
     * @param canvasY The y-coordinate on the canvas.
     * @return The corresponding {@link Vector2d} map position. Returns (0,0) if cell dimensions are zero.
     */
    public Vector2d convertCanvasToMapPosition(double canvasX, double canvasY) {
        if (cellWidth <= 0 || cellHeight <= 0) {
            System.err.println("MapRenderer: Cannot convert canvas position, cell dimensions are zero.");
            return new Vector2d(0, 0); // Avoid division by zero
        }
        // Clamp coordinates to be within canvas bounds before division
        double clampedX = Math.max(0, Math.min(canvas.getWidth() - 1, canvasX));
        double clampedY = Math.max(0, Math.min(canvas.getHeight() - 1, canvasY));

        int mapX = (int) (clampedX / cellWidth);
        // Canvas Y=0 is top, Map Y=0 is often bottom. Adjust if necessary based on map model's coordinate system.
        // Assuming here Canvas Y maps directly to map Y for simplicity. If map Y increases upwards:
        // int mapY = simProps.getHeight() - 1 - (int) (clampedY / cellHeight);
        // If map Y increases downwards (like canvas):
        int mapY = (int) (clampedY / cellHeight);

        // Ensure map coordinates are within bounds
        mapX = Math.max(0, Math.min(simProps.getWidth() - 1, mapX));
        mapY = Math.max(0, Math.min(simProps.getHeight() - 1, mapY));

        return new Vector2d(mapX, mapY);
    }


    /**
     * Draws the entire simulation state onto the canvas using the provided snapshot data.
     * Clears the canvas and then draws the grid, equator, water, plants, and animals in order.
     * This method is designed to be called from the JavaFX Application Thread.
     *
     * @param stateSnapshot The immutable {@link SimulationStateSnapshot} containing the data to render. If null, the canvas is cleared.
     */
    public void drawSimulation(SimulationStateSnapshot stateSnapshot) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear previous frame

        if (stateSnapshot == null) {
            System.err.println("MapRenderer: Received null stateSnapshot, cannot draw.");
            // Optionally draw a placeholder or error message
            return;
        }

        // Draw elements layer by layer
        drawEquator(gc);      // Draw background first
        drawWater(gc, stateSnapshot.getWaterFields());
        drawGrid(gc);         // Grid on top of background/water
        drawPlants(gc, stateSnapshot.getPlants());
        drawAnimals(gc, stateSnapshot.getAnimals(), stateSnapshot.getSelectedAnimal(), stateSnapshot.getTopGenotypes());
    }

    // --- Private Drawing Helper Methods ---

    /** Draws the background grid lines on the canvas. */
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(GRID_LINE_WIDTH);
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        // Draw vertical lines
        for (int x = 0; x <= simProps.getWidth(); x++) {
            double lineX = x * cellWidth;
            gc.strokeLine(lineX, 0, lineX, canvasHeight);
        }
        // Draw horizontal lines
        for (int y = 0; y <= simProps.getHeight(); y++) {
            double lineY = y * cellHeight;
            gc.strokeLine(0, lineY, canvasWidth, lineY);
        }
    }

    /** Draws the highlighted equator region background. */
    private void drawEquator(GraphicsContext gc) {
        int equatorHeight = simProps.getEquatorHeight();
        if (equatorHeight <= 0) return; // No equator to draw

        int mapHeight = simProps.getHeight();
        // Calculate Y range for the equator, centered vertically
        int equatorStartY = Math.max(0, (mapHeight - equatorHeight) / 2);
        // Ensure endY doesn't exceed map height
        int equatorEndY = Math.min(mapHeight, equatorStartY + equatorHeight);

        gc.setFill(EQUATOR_FILL_COLOR);
        // Calculate pixel coordinates for the rectangle
        double rectX = 0;
        double rectY = equatorStartY * cellHeight;
        double rectWidth = simProps.getWidth() * cellWidth;
        double rectHeight = (equatorEndY - equatorStartY) * cellHeight;

        gc.fillRect(rectX, rectY, rectWidth, rectHeight);
    }

    /** Draws the water elements from the snapshot. */
    private void drawWater(GraphicsContext gc, Map<Vector2d, ?> waterFields) {
        // Check if waterFields exist (might be null for non-WaterWorld maps)
        if (waterFields != null && !waterFields.isEmpty()) {
            gc.setFill(WATER_FILL_COLOR);
            waterFields.forEach((pos, water) -> {
                // Draw a filled rectangle for each water cell
                drawCellRect(gc, pos.x(), pos.y());
            });
        }
    }

    /** Draws the plant elements from the snapshot. */
    private void drawPlants(GraphicsContext gc, Map<Vector2d, Plant> plants) {
        if (plants == null || plants.isEmpty()) return;

        gc.setFill(PLANT_FILL_COLOR);
        double plantSize = Math.min(cellWidth, cellHeight) * PLANT_SIZE_RATIO;
        double offset = (Math.min(cellWidth, cellHeight) - plantSize) / 2; // Center the oval

        plants.forEach((pos, plant) -> {
            gc.fillOval(
                    pos.x() * cellWidth + offset,
                    pos.y() * cellHeight + offset, // Adjust Y offset if map Y is inverted
                    plantSize,
                    plantSize
            );
        });
    }

    /** Draws the animal elements from the snapshot, including energy color, selection, and genotype highlights. */
    private void drawAnimals(GraphicsContext gc, Map<Vector2d, List<Animal>> animals,
                             Animal selectedAnimal, List<String> topGenotypes) {
        if (animals == null || animals.isEmpty()) return;

        long selectedAnimalId = (selectedAnimal != null) ? selectedAnimal.getId() : -1;

        animals.forEach((pos, animalList) -> {
            if (animalList != null && !animalList.isEmpty()) {
                // In case of multiple animals, draw the "strongest" or first one for simplicity
                // TODO: Consider how to represent multiple animals per cell if needed
                Animal animalToDraw = animalList.getFirst(); // Or find strongest based on some criteria

                // Draw base color based on energy
                gc.setFill(getAnimalEnergyColor(animalToDraw));
                drawCellRect(gc, pos.x(), pos.y());

                // Draw genotype border if applicable
                Color genotypeBorderColor = getGenotypeBorderColor(animalToDraw, topGenotypes);
                if (genotypeBorderColor != null) {
                    gc.setStroke(genotypeBorderColor);
                    gc.setLineWidth(GENOTYPE_BORDER_WIDTH);
                    // Stroke slightly inside the cell bounds
                    gc.strokeRect(
                            pos.x() * cellWidth + GENOTYPE_BORDER_WIDTH / 2,
                            pos.y() * cellHeight + GENOTYPE_BORDER_WIDTH / 2,
                            cellWidth - GENOTYPE_BORDER_WIDTH,
                            cellHeight - GENOTYPE_BORDER_WIDTH
                    );
                }

                // Draw selection highlight if this animal is selected
                if (animalToDraw.getId() == selectedAnimalId) {
                    gc.setStroke(SELECTED_ANIMAL_BORDER_COLOR);
                    gc.setLineWidth(SELECTED_ANIMAL_BORDER_WIDTH);
                    double offset = SELECTED_ANIMAL_BORDER_WIDTH / 2.0;
                    // Stroke slightly inside the cell bounds
                    gc.strokeRect(
                            pos.x() * cellWidth + offset,
                            pos.y() * cellHeight + offset,
                            cellWidth - SELECTED_ANIMAL_BORDER_WIDTH,
                            cellHeight - SELECTED_ANIMAL_BORDER_WIDTH
                    );
                }
            }
        });
    }

    /**
     * Determines the color for an animal based on its current energy level relative
     * to the initial starting energy. Ranges from red (low energy) to green (high energy).
     *
     * @param animal The {@link Animal} to get the color for.
     * @return The calculated {@link Color}.
     */
    private Color getAnimalEnergyColor(Animal animal) {
        // Ensure startEnergy is positive to avoid division by zero
        double maxEnergy = Math.max(1.0, (double) simProps.getStartEnergy()); // Use configured start energy as reference max
        // Consider using a higher reference if animals can exceed start energy significantly
        // maxEnergy = Math.max(maxEnergy, animal.getEnergy() * 1.2); // Example: Reference is slightly above current max

        // Calculate energy ratio (0.0 to 1.0, clamped)
        double energyRatio = Math.max(0.0, Math.min(1.0, animal.getEnergy() / maxEnergy));

        // Interpolate between red (low energy) and green (high energy)
        // Red = (1, 0, 0), Green = (0, 1, 0)
        int red = (int) (255 * (1 - energyRatio));
        int green = (int) (255 * energyRatio);
        int blue = 0; // No blue component

        return Color.rgb(red, green, blue);
    }

    /**
     * Determines the border color for highlighting animals with one of the top 3 genotypes.
     *
     * @param animal        The {@link Animal} to check.
     * @param topGenotypes  A {@link List} of the top 3 formatted genotype strings.
     * @return The corresponding highlight {@link Color}, or {@code null} if the animal's genotype is not in the top 3.
     */
    private Color getGenotypeBorderColor(Animal animal, List<String> topGenotypes) {
        if (topGenotypes == null || topGenotypes.isEmpty()) {
            return null;
        }

        String animalGenotypeStr = GenotypeFormatter.formatGenotype(animal.getGenes());

        if (animalGenotypeStr.equals(topGenotypes.get(0))) {
            return TOP_GENOTYPE_COLOR;
        } else if (topGenotypes.size() > 1 && animalGenotypeStr.equals(topGenotypes.get(1))) {
            return SECOND_GENOTYPE_COLOR;
        } else if (topGenotypes.size() > 2 && animalGenotypeStr.equals(topGenotypes.get(2))) {
            return THIRD_GENOTYPE_COLOR;
        }

        return null; // Not a top genotype
    }

    /**
     * Helper method to draw a filled rectangle covering a single map cell.
     *
     * @param gc The GraphicsContext to draw on.
     * @param x  The map x-coordinate of the cell.
     * @param y  The map y-coordinate of the cell.
     */
    private void drawCellRect(GraphicsContext gc, int x, int y) {
        // Add small inset to avoid overlapping grid lines if desired, or draw full cell
        // double inset = 0.5;
        // gc.fillRect(x * cellWidth + inset, y * cellHeight + inset, cellWidth - 2*inset, cellHeight - 2*inset);
        gc.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
    }
}