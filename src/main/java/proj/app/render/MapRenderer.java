package proj.app.render;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import proj.app.AppConstants;
import proj.app.GenotypeFormatter;
import proj.app.state.SimulationRenderSnapshot; // Używa SimulationRenderSnapshot
import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.simulation.SimulationProperties;
import proj.util.Vector2d;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Renderuje stan świata symulacji na płótnie JavaFX {@link Canvas}.
 * Rysuje siatkę, strefę równika, wodę, rośliny i zwierzęta na podstawie danych
 * dostarczonych w niezmiennej migawce {@link SimulationRenderSnapshot}. Obsługuje również
 * podświetlanie wybranego zwierzęcia i zwierząt z dominującymi genotypami.
 * Używa stałych zdefiniowanych w {@link AppConstants} dla właściwości wizualnych.
 */
public class MapRenderer {
    private final Canvas canvas;
    private final SimulationProperties simProps;
    private double cellWidth;
    private double cellHeight;

    /**
     * Konstruuje {@code MapRenderer}. Oblicza początkowe wymiary komórek.
     *
     * @param canvas   Płótno {@link Canvas}. Nie może być null.
     * @param simProps {@link SimulationProperties}. Nie może być null.
     * @throws NullPointerException jeśli canvas lub simProps jest null.
     */
    public MapRenderer(Canvas canvas, SimulationProperties simProps) {
        this.canvas = Objects.requireNonNull(canvas, "Canvas cannot be null");
        this.simProps = Objects.requireNonNull(simProps, "SimulationProperties cannot be null");
        updateCellDimensions();
    }

    /** Ponownie oblicza wymiary komórki na podstawie rozmiaru płótna. */
    public void updateCellDimensions() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        if (canvasWidth > 0 && canvasHeight > 0 && simProps.getWidth() > 0 && simProps.getHeight() > 0) {
            cellWidth = canvasWidth / simProps.getWidth();
            cellHeight = canvasHeight / simProps.getHeight();
        } else {
            cellWidth = 10; // Domyślny rozmiar
            cellHeight = 10;
            // Logowanie ostrzeżeń
            if (canvasWidth <= 0 || canvasHeight <= 0) System.err.println("MapRenderer Warning: Canvas dimensions are zero or negative.");
            if (simProps.getWidth() <= 0 || simProps.getHeight() <= 0) System.err.println("MapRenderer Warning: Simulation map dimensions are zero or negative.");
        }
    }

    /** Konwertuje współrzędne płótna na pozycję mapy. */
    public Vector2d convertCanvasToMapPosition(double canvasX, double canvasY) {
        if (cellWidth <= 0 || cellHeight <= 0) {
            System.err.println("MapRenderer Error: Cannot convert canvas position, cell dimensions are invalid.");
            return new Vector2d(0, 0);
        }
        double clampedX = Math.max(0, Math.min(canvas.getWidth() - 1, canvasX));
        double clampedY = Math.max(0, Math.min(canvas.getHeight() - 1, canvasY));

        int mapX = (int) (clampedX / cellWidth);
        int mapY = (int) (clampedY / cellHeight);

        mapX = Math.max(0, Math.min(simProps.getWidth() - 1, mapX));
        mapY = Math.max(0, Math.min(simProps.getHeight() - 1, mapY));

        return new Vector2d(mapX, mapY);
    }

    /**
     * Rysuje cały stan symulacji na płótnie, używając danych z {@link SimulationRenderSnapshot}.
     *
     * @param stateSnapshot Migawka {@link SimulationRenderSnapshot} do renderowania. Jeśli null, płótno jest czyszczone.
     */
    public void drawSimulation(SimulationRenderSnapshot stateSnapshot) { // Akceptuje SimulationRenderSnapshot
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (stateSnapshot == null) {
            System.err.println("MapRenderer Error: Received null SimulationRenderSnapshot, cannot draw.");
            return;
        }

        // Rysuj warstwy używając getterów rekordu
        drawEquator(gc);
        drawWater(gc, stateSnapshot.waterFields());
        drawGrid(gc);
        drawPlants(gc, stateSnapshot.plants());
        drawAnimals(gc, stateSnapshot.animals(), stateSnapshot.selectedAnimal(), stateSnapshot.topGenotypes());
    }

    /** Rysuje linie siatki tła. */
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(AppConstants.COLOR_GRID);
        gc.setLineWidth(AppConstants.GRID_LINE_WIDTH);
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        for (int x = 0; x <= simProps.getWidth(); x++) {
            double lineX = x * cellWidth;
            gc.strokeLine(lineX, 0, lineX, canvasHeight);
        }
        for (int y = 0; y <= simProps.getHeight(); y++) {
            double lineY = y * cellHeight;
            gc.strokeLine(0, lineY, canvasWidth, lineY);
        }
    }

    /** Rysuje podświetlone tło regionu równika. */
    private void drawEquator(GraphicsContext gc) {
        int equatorHeight = simProps.getEquatorHeight();
        if (equatorHeight <= 0) return;

        int mapHeight = simProps.getHeight();
        int equatorStartY = Math.max(0, (mapHeight - equatorHeight) / 2);
        int equatorEndY = Math.min(mapHeight, equatorStartY + equatorHeight);

        gc.setFill(AppConstants.COLOR_EQUATOR_FILL);
        double rectX = 0;
        double rectY = equatorStartY * cellHeight;
        double rectWidth = simProps.getWidth() * cellWidth;
        double rectHeight = (equatorEndY - equatorStartY) * cellHeight;

        gc.fillRect(rectX, rectY, rectWidth, rectHeight);
    }

    /** Rysuje elementy wody. */
    private void drawWater(GraphicsContext gc, Map<Vector2d, ?> waterFields) {
        if (waterFields != null && !waterFields.isEmpty()) {
            gc.setFill(AppConstants.COLOR_WATER_FILL);
            waterFields.forEach((pos, water) -> drawCellRect(gc, pos.x(), pos.y()));
        }
    }

    /** Rysuje elementy roślin. */
    private void drawPlants(GraphicsContext gc, Map<Vector2d, Plant> plants) {
        if (plants == null || plants.isEmpty()) return;

        gc.setFill(AppConstants.COLOR_PLANT_FILL);
        double plantSize = Math.min(cellWidth, cellHeight) * AppConstants.PLANT_SIZE_RATIO;
        double offset = (Math.min(cellWidth, cellHeight) - plantSize) / 2;

        plants.forEach((pos, plant) -> {
            gc.fillOval(pos.x() * cellWidth + offset, pos.y() * cellHeight + offset, plantSize, plantSize);
        });
    }

    /** Rysuje elementy zwierząt wraz z podświetleniami. */
    private void drawAnimals(GraphicsContext gc, Map<Vector2d, List<Animal>> animals,
                             Animal selectedAnimal, List<String> topGenotypes) {
        if (animals == null || animals.isEmpty()) return;

        long selectedAnimalId = (selectedAnimal != null) ? selectedAnimal.getId() : -1;

        animals.forEach((pos, animalList) -> {
            if (animalList != null && !animalList.isEmpty()) {
                Animal animalToDraw = animalList.getFirst(); // Rysuj tylko pierwsze

                gc.setFill(getAnimalEnergyColor(animalToDraw));
                drawCellRect(gc, pos.x(), pos.y());

                // Obramowanie genotypu
                Color genotypeBorderColor = getGenotypeBorderColor(animalToDraw, topGenotypes);
                if (genotypeBorderColor != null) {
                    gc.setStroke(genotypeBorderColor);
                    gc.setLineWidth(AppConstants.GENOTYPE_BORDER_WIDTH);
                    double borderOffset = AppConstants.GENOTYPE_BORDER_WIDTH / 2.0;
                    gc.strokeRect(
                            pos.x() * cellWidth + borderOffset, pos.y() * cellHeight + borderOffset,
                            cellWidth - AppConstants.GENOTYPE_BORDER_WIDTH,
                            cellHeight - AppConstants.GENOTYPE_BORDER_WIDTH
                    );
                }

                // Podświetlenie wyboru
                if (animalToDraw.getId() == selectedAnimalId) {
                    gc.setStroke(AppConstants.COLOR_SELECTED_ANIMAL_BORDER);
                    gc.setLineWidth(AppConstants.SELECTED_ANIMAL_BORDER_WIDTH);
                    double selectOffset = AppConstants.SELECTED_ANIMAL_BORDER_WIDTH / 2.0;
                    gc.strokeRect(
                            pos.x() * cellWidth + selectOffset, pos.y() * cellHeight + selectOffset,
                            cellWidth - AppConstants.SELECTED_ANIMAL_BORDER_WIDTH,
                            cellHeight - AppConstants.SELECTED_ANIMAL_BORDER_WIDTH
                    );
                }
            }
        });
    }

    /** Określa kolor zwierzęcia na podstawie energii. */
    private Color getAnimalEnergyColor(Animal animal) {
        double maxEnergy = Math.max(1.0, (double) simProps.getStartEnergy());
        double energyRatio = Math.max(0.0, Math.min(1.0, animal.getEnergy() / maxEnergy));
        int red = (int) (255 * (1 - energyRatio));
        int green = (int) (255 * energyRatio);
        return Color.rgb(red, green, 0);
    }

    /** Określa kolor obramowania dla dominujących genotypów. */
    private Color getGenotypeBorderColor(Animal animal, List<String> topGenotypes) {
        if (topGenotypes == null || topGenotypes.isEmpty()) return null;
        String animalGenotypeStr = GenotypeFormatter.formatGenotype(animal.getGenes());

        if (animalGenotypeStr.equals(topGenotypes.get(0))) {
            return AppConstants.COLOR_TOP_GENOTYPE_1_BORDER;
        } else if (topGenotypes.size() > 1 && animalGenotypeStr.equals(topGenotypes.get(1))) {
            return AppConstants.COLOR_TOP_GENOTYPE_2_BORDER;
        } else if (topGenotypes.size() > 2 && animalGenotypeStr.equals(topGenotypes.get(2))) {
            return AppConstants.COLOR_TOP_GENOTYPE_3_BORDER;
        }
        return null;
    }

    /** Pomocnicza metoda rysująca wypełniony prostokąt komórki. */
    private void drawCellRect(GraphicsContext gc, int x, int y) {
        gc.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
    }
}