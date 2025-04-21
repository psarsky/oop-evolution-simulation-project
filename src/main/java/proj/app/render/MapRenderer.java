package proj.app.render;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import proj.app.AppConstants; // Use constants
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
 * Uses constants defined in {@link AppConstants} for visual properties like
 * colors and line widths.
 */
public class MapRenderer {
    private final Canvas canvas;
    private final SimulationProperties simProps;
    private double cellWidth;
    private double cellHeight;

    // Visual properties are now referenced from AppConstants

    /**
     * Constructs a {@code MapRenderer}. Calculates initial cell dimensions based on
     * canvas size and simulation properties.
     *
     * @param canvas   The {@link Canvas} on which the simulation will be rendered. Must not be null.
     * @param simProps The {@link SimulationProperties} containing map dimensions and other relevant info
     *                 needed for rendering calculations. Must not be null.
     * @throws NullPointerException if canvas or simProps is null.
     */
    public MapRenderer(Canvas canvas, SimulationProperties simProps) {
        this.canvas = Objects.requireNonNull(canvas, "Canvas cannot be null");
        this.simProps = Objects.requireNonNull(simProps, "SimulationProperties cannot be null");
        updateCellDimensions(); // Calculate initial cell size
    }

    /**
     * Recalculates the width and height of each map cell based on the current canvas dimensions
     * and the map dimensions from {@link SimulationProperties}.
     * This method should be called whenever the canvas size changes to ensure the rendering scales correctly.
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
            // Log warnings for diagnostics
            if (canvasWidth <= 0 || canvasHeight <= 0) System.err.println("MapRenderer Warning: Canvas has zero or negative dimensions during cell calculation.");
            if (simProps.getWidth() <= 0 || simProps.getHeight() <= 0) System.err.println("MapRenderer Warning: SimulationProperties has zero or negative map dimensions during cell calculation.");
        }
    }

    /**
     * Converts pixel coordinates from the canvas (e.g., from a mouse click event)
     * to logical map coordinates represented by a {@link Vector2d}. Handles clamping
     * to ensure the resulting map coordinates are within the valid map bounds defined
     * by {@link SimulationProperties}.
     *
     * @param canvasX The x-coordinate (horizontal) on the canvas, typically obtained from mouse events.
     * @param canvasY The y-coordinate (vertical) on the canvas, typically obtained from mouse events.
     * @return The corresponding {@link Vector2d} map position (column x, row y). Returns {@code new Vector2d(0, 0)}
     *         if cell dimensions are currently zero or negative, logging an error.
     */
    public Vector2d convertCanvasToMapPosition(double canvasX, double canvasY) {
        if (cellWidth <= 0 || cellHeight <= 0) {
            System.err.println("MapRenderer Error: Cannot convert canvas position, cell dimensions are zero or negative.");
            return new Vector2d(0, 0); // Avoid division by zero and return a default valid position
        }
        // Clamp click coordinates to be within the visible canvas area before division
        double clampedX = Math.max(0, Math.min(canvas.getWidth() - 1, canvasX));
        double clampedY = Math.max(0, Math.min(canvas.getHeight() - 1, canvasY));

        // Calculate map indices
        int mapX = (int) (clampedX / cellWidth);
        // Canvas Y=0 is top, Map Y=0 is often bottom. Assuming Map Y increases downwards like Canvas Y here.
        int mapY = (int) (clampedY / cellHeight);

        // Ensure map coordinates are strictly within the defined map bounds [0, width-1] and [0, height-1]
        mapX = Math.max(0, Math.min(simProps.getWidth() - 1, mapX));
        mapY = Math.max(0, Math.min(simProps.getHeight() - 1, mapY));

        return new Vector2d(mapX, mapY);
    }


    /**
     * Draws the entire simulation state onto the canvas using the provided snapshot data.
     * It first clears the canvas of any previous drawings. Then, it draws the different layers
     * in a specific order: equator background, water tiles, the map grid, plants, and finally animals
     * (including selection and genotype highlights).
     * This method is designed to be called from the JavaFX Application Thread.
     *
     * @param stateSnapshot The immutable {@link SimulationStateSnapshot} containing the complete data
     *                      (animal positions, plant positions, water positions, selected animal, top genotypes)
     *                      to be rendered for the current frame. If this parameter is null, the canvas is
     *                      cleared, and an error message is logged, but no drawing occurs.
     */
    public void drawSimulation(SimulationStateSnapshot stateSnapshot) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear previous frame

        if (stateSnapshot == null) {
            System.err.println("MapRenderer Error: Received null stateSnapshot, cannot draw simulation state.");
            // Optionally draw a placeholder or error message on the canvas itself
            // gc.setFill(Color.RED); gc.fillText("Error: No simulation data", 10, 20);
            return;
        }

        // Draw elements layer by layer for correct visual overlap
        drawEquator(gc);      // Draw background region first
        drawWater(gc, stateSnapshot.getWaterFields()); // Draw water on top of equator
        drawGrid(gc);         // Draw grid lines over background/water
        drawPlants(gc, stateSnapshot.getPlants()); // Draw plants over grid
        // Draw animals last, including highlights for selection and genotype
        drawAnimals(gc, stateSnapshot.getAnimals(), stateSnapshot.getSelectedAnimal(), stateSnapshot.getTopGenotypes());
    }

    // --- Private Drawing Helper Methods ---

    /** Draws the background grid lines on the canvas using constants from AppConstants. */
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(AppConstants.COLOR_GRID);
        gc.setLineWidth(AppConstants.GRID_LINE_WIDTH);
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

    /** Draws the highlighted equator region background using constants from AppConstants. */
    private void drawEquator(GraphicsContext gc) {
        int equatorHeight = simProps.getEquatorHeight();
        if (equatorHeight <= 0) return; // No equator to draw if height is zero or negative

        int mapHeight = simProps.getHeight();
        // Calculate Y range for the equator, centered vertically
        int equatorStartY = Math.max(0, (mapHeight - equatorHeight) / 2);
        // Ensure endY doesn't exceed map height
        int equatorEndY = Math.min(mapHeight, equatorStartY + equatorHeight);

        gc.setFill(AppConstants.COLOR_EQUATOR_FILL); // Use constant color
        // Calculate pixel coordinates for the rectangle
        double rectX = 0;
        double rectY = equatorStartY * cellHeight;
        double rectWidth = simProps.getWidth() * cellWidth;
        double rectHeight = (equatorEndY - equatorStartY) * cellHeight;

        gc.fillRect(rectX, rectY, rectWidth, rectHeight);
    }

    /** Draws the water elements from the snapshot using constants from AppConstants. */
    private void drawWater(GraphicsContext gc, Map<Vector2d, ?> waterFields) {
        // Check if waterFields exist (might be null for non-WaterWorld maps in the snapshot)
        if (waterFields != null && !waterFields.isEmpty()) {
            gc.setFill(AppConstants.COLOR_WATER_FILL); // Use constant color
            // Iterate over water positions and draw a filled rectangle for each
            waterFields.forEach((pos, water) -> {
                drawCellRect(gc, pos.x(), pos.y());
            });
        }
    }

    /** Draws the plant elements from the snapshot using constants from AppConstants. */
    private void drawPlants(GraphicsContext gc, Map<Vector2d, Plant> plants) {
        if (plants == null || plants.isEmpty()) return; // Nothing to draw

        gc.setFill(AppConstants.COLOR_PLANT_FILL); // Use constant color
        // Calculate plant size based on cell dimensions and ratio constant
        double plantSize = Math.min(cellWidth, cellHeight) * AppConstants.PLANT_SIZE_RATIO;
        // Calculate offset to center the plant oval within the cell
        double offset = (Math.min(cellWidth, cellHeight) - plantSize) / 2;

        // Iterate over plant positions and draw an oval for each
        plants.forEach((pos, plant) -> {
            gc.fillOval(
                    pos.x() * cellWidth + offset,
                    pos.y() * cellHeight + offset, // Assuming map Y increases downwards
                    plantSize,
                    plantSize
            );
        });
    }

    /**
     * Draws the animal elements from the snapshot, including energy-based coloring,
     * selection highlighting, and dominant genotype highlighting.
     * Uses constants from AppConstants for colors and widths.
     * If multiple animals occupy the same cell, currently only the first one in the list is drawn.
     *
     * @param gc             The {@link GraphicsContext} to draw on.
     * @param animals        A map from position ({@link Vector2d}) to a list of {@link Animal}s at that position.
     * @param selectedAnimal The {@link Animal} currently selected in the UI (can be null), used for highlighting.
     * @param topGenotypes   A list of the top genotype strings (formatted), used for genotype highlighting.
     */
    private void drawAnimals(GraphicsContext gc, Map<Vector2d, List<Animal>> animals,
                             Animal selectedAnimal, List<String> topGenotypes) {
        if (animals == null || animals.isEmpty()) return; // Nothing to draw

        long selectedAnimalId = (selectedAnimal != null) ? selectedAnimal.getId() : -1; // Get ID or -1 if null

        // Iterate through each position containing animals
        animals.forEach((pos, animalList) -> {
            if (animalList != null && !animalList.isEmpty()) {
                // Simplification: Draw only the first animal if multiple exist at the same cell.
                // TODO: Consider alternative visualizations for multiple animals per cell if needed.
                Animal animalToDraw = animalList.getFirst();

                // Draw base color rectangle based on energy level
                gc.setFill(getAnimalEnergyColor(animalToDraw));
                drawCellRect(gc, pos.x(), pos.y());

                // --- Draw Genotype Border (if applicable) ---
                Color genotypeBorderColor = getGenotypeBorderColor(animalToDraw, topGenotypes);
                if (genotypeBorderColor != null) {
                    gc.setStroke(genotypeBorderColor); // Use determined color
                    gc.setLineWidth(AppConstants.GENOTYPE_BORDER_WIDTH); // Use constant width
                    // Calculate border position slightly inside the cell
                    double borderOffset = AppConstants.GENOTYPE_BORDER_WIDTH / 2.0;
                    gc.strokeRect(
                            pos.x() * cellWidth + borderOffset,
                            pos.y() * cellHeight + borderOffset,
                            cellWidth - AppConstants.GENOTYPE_BORDER_WIDTH, // Adjust size for border width
                            cellHeight - AppConstants.GENOTYPE_BORDER_WIDTH
                    );
                }

                // --- Draw Selection Highlight (if this animal is selected) ---
                if (animalToDraw.getId() == selectedAnimalId) {
                    gc.setStroke(AppConstants.COLOR_SELECTED_ANIMAL_BORDER); // Use constant color
                    gc.setLineWidth(AppConstants.SELECTED_ANIMAL_BORDER_WIDTH); // Use constant width
                    // Calculate border position slightly inside the cell
                    double selectOffset = AppConstants.SELECTED_ANIMAL_BORDER_WIDTH / 2.0;
                    gc.strokeRect(
                            pos.x() * cellWidth + selectOffset,
                            pos.y() * cellHeight + selectOffset,
                            cellWidth - AppConstants.SELECTED_ANIMAL_BORDER_WIDTH, // Adjust size for border width
                            cellHeight - AppConstants.SELECTED_ANIMAL_BORDER_WIDTH
                    );
                }
            }
        });
    }

    /**
     * Determines the fill color for an animal based on its current energy level relative
     * to the configured starting energy. The color interpolates from red (low energy)
     * towards green (high energy).
     *
     * @param animal The {@link Animal} whose energy level determines the color. Must not be null.
     * @return The calculated {@link Color} for the animal's cell representation.
     */
    private Color getAnimalEnergyColor(Animal animal) {
        // Use configured start energy as the reference maximum for color scaling.
        // Ensure maxEnergy is at least 1 to avoid division by zero.
        double maxEnergy = Math.max(1.0, (double) simProps.getStartEnergy());

        // Calculate energy ratio, clamped between 0.0 and 1.0.
        double energyRatio = Math.max(0.0, Math.min(1.0, animal.getEnergy() / maxEnergy));

        // Interpolate RGB values: More red component at low ratio, more green at high ratio.
        int red = (int) (255 * (1 - energyRatio));
        int green = (int) (255 * energyRatio);
        int blue = 0; // Keep blue component at 0 for red-green scale.

        return Color.rgb(red, green, blue);
    }

    /**
     * Determines the border color for highlighting animals possessing one of the top 3
     * dominant genotypes identified in the simulation state. Uses constants from AppConstants.
     *
     * @param animal        The {@link Animal} whose genotype is to be checked. Must not be null.
     * @param topGenotypes  A {@link List} of the top 3 formatted genotype strings (order matters: 0=1st, 1=2nd, 2=3rd).
     *                      Can be null or empty.
     * @return The corresponding highlight {@link Color} (Magenta, Black, or Blue based on rank)
     *         if the animal's genotype matches one in the top list. Returns {@code null} if the
     *         genotype is not among the top 3 or if the topGenotypes list is null/empty.
     */
    private Color getGenotypeBorderColor(Animal animal, List<String> topGenotypes) {
        if (topGenotypes == null || topGenotypes.isEmpty()) {
            return null; // No top genotypes defined or available
        }

        String animalGenotypeStr = GenotypeFormatter.formatGenotype(animal.getGenes());

        // Check against top genotypes in order and return corresponding constant color
        if (animalGenotypeStr.equals(topGenotypes.get(0))) {
            return AppConstants.COLOR_TOP_GENOTYPE_1_BORDER; // Most dominant
        } else if (topGenotypes.size() > 1 && animalGenotypeStr.equals(topGenotypes.get(1))) {
            return AppConstants.COLOR_TOP_GENOTYPE_2_BORDER; // Second most dominant
        } else if (topGenotypes.size() > 2 && animalGenotypeStr.equals(topGenotypes.get(2))) {
            return AppConstants.COLOR_TOP_GENOTYPE_3_BORDER; // Third most dominant
        }

        return null; // Animal's genotype is not one of the top tracked ones
    }

    /**
     * Helper method to draw a filled rectangle covering a single map cell
     * at the given map coordinates (x, y). Uses the currently set fill color
     * on the GraphicsContext.
     *
     * @param gc The {@link GraphicsContext} to draw on.
     * @param x  The map column index (x-coordinate) of the cell.
     * @param y  The map row index (y-coordinate) of the cell.
     */
    private void drawCellRect(GraphicsContext gc, int x, int y) {
        // Draws a rectangle occupying the full cell area defined by cellWidth/cellHeight
        gc.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
    }
}