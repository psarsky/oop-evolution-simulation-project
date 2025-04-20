package proj.app.controllers.handlers;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import proj.app.render.MapRenderer;
import proj.app.viewmodels.SelectedAnimalViewModel;
import proj.model.elements.Animal;
import proj.simulation.Simulation;
import proj.util.Vector2d;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Handles mouse click interactions on the simulation canvas.
 * It converts click coordinates to map positions, queries the simulation
 * for animals at that position, and updates the {@link SelectedAnimalViewModel}
 * with the details of the selected animal (typically the one with the highest energy).
 */
public class CanvasInteractionHandler {

    private final Canvas simulationCanvas;
    private final MapRenderer mapRenderer;
    private final Simulation simulation; // Provides access to the map and animals
    private final SelectedAnimalViewModel selectedAnimalViewModel; // ViewModel to update

    // Pre-defined event handler instance
    private final EventHandler<MouseEvent> mouseClickedHandler;

    /**
     * Constructs the handler, injecting required dependencies.
     *
     * @param simulationCanvas      The {@link Canvas} element to attach the click listener to (non-null).
     * @param mapRenderer           The {@link MapRenderer} used for coordinate conversion (non-null).
     * @param simulation            The {@link Simulation} instance providing access to the game state (non-null).
     * @param selectedAnimalViewModel The {@link SelectedAnimalViewModel} to update upon selection (non-null).
     * @throws NullPointerException if any dependency is null.
     */
    public CanvasInteractionHandler(Canvas simulationCanvas,
                                    MapRenderer mapRenderer,
                                    Simulation simulation,
                                    SelectedAnimalViewModel selectedAnimalViewModel) {

        this.simulationCanvas = Objects.requireNonNull(simulationCanvas, "simulationCanvas cannot be null");
        this.mapRenderer = Objects.requireNonNull(mapRenderer, "mapRenderer cannot be null");
        this.simulation = Objects.requireNonNull(simulation, "simulation cannot be null");
        this.selectedAnimalViewModel = Objects.requireNonNull(selectedAnimalViewModel, "selectedAnimalViewModel cannot be null");

        // Define the handler logic
        this.mouseClickedHandler = event -> {
            handleMouseClick(event);
        };
    }

    /**
     * Attaches the mouse click event handler to the simulation canvas.
     * Ensures that previous handlers are removed before adding the new one to prevent duplicates.
     */
    public void attachHandlers() {
        // Remove existing handler first to prevent duplicates if called multiple times
        simulationCanvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        simulationCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        System.out.println("CanvasInteractionHandler attached."); // For debugging
    }

    /**
     * Removes the mouse click event handler from the simulation canvas.
     */
    public void detachHandlers() {
        simulationCanvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        System.out.println("CanvasInteractionHandler detached."); // For debugging
    }

    /**
     * Processes a mouse click event on the canvas.
     * Converts coordinates, finds the animal, and updates the ViewModel.
     *
     * @param event The {@link MouseEvent} triggered by the click.
     */
    private void handleMouseClick(MouseEvent event) {
        // Ensure simulation map is available
        if (simulation.getMap() == null) {
            System.err.println("CanvasInteractionHandler: Cannot handle click, Simulation Map is not ready.");
            return;
        }

        try {
            // 1. Convert canvas coordinates (event.getX(), event.getY()) to map position
            Vector2d clickedMapPos = mapRenderer.convertCanvasToMapPosition(event.getX(), event.getY());
            System.out.println("Canvas clicked at: (" + event.getX() + ", " + event.getY() + ") -> Map Pos: " + clickedMapPos); // Debugging


            // 2. Find the "best" animal at the clicked position (e.g., highest energy)
            Animal clickedAnimal = findAnimalAt(clickedMapPos);
            if (clickedAnimal != null) {
                System.out.println("Animal found at " + clickedMapPos + ": ID=" + clickedAnimal.getId() + ", Energy=" + clickedAnimal.getEnergy());
            } else {
                System.out.println("No animal found at " + clickedMapPos);
            }

            // 3. Update the ViewModel. This will trigger UI updates via binding.
            //    If clickedAnimal is null, the ViewModel will be cleared.
            selectedAnimalViewModel.update(clickedAnimal);

            // 4. Visual highlighting is handled by the MapRenderer during its draw call.
            //    The renderer checks the selectedAnimal property in the SimulationStateSnapshot.

        } catch (Exception e) {
            // Catch potential errors during coordinate conversion or animal lookup
            System.err.println("CanvasInteractionHandler: Error processing mouse click event.");
            e.printStackTrace();
            selectedAnimalViewModel.clear(); // Clear selection on error
        }
    }


    /**
     * Finds the animal with the highest energy at a given map position.
     * Accesses the simulation's map state in a synchronized manner to ensure thread safety
     * when reading the list of animals at the position.
     *
     * @param position The map position ({@link Vector2d}) to query.
     * @return The highest-energy {@link Animal} at the position, or {@code null} if no animals are present or the map is unavailable.
     */
    private Animal findAnimalAt(Vector2d position) {
        if (simulation.getMap() == null) {
            return null; // Map not ready
        }

        Animal strongestAnimal = null;
        // Synchronize on the map object for thread-safe access to its internal animal map/list
        synchronized (simulation.getMap()) {
            List<Animal> animalsAtPos = simulation.getMap().getAnimals().get(position);

            // Check if the list exists and is not empty
            if (animalsAtPos != null && !animalsAtPos.isEmpty()) {
                // Use stream to find the animal with maximum energy.
                // If multiple animals have the same max energy, max() returns one of them.
                strongestAnimal = animalsAtPos.stream()
                        .max(Comparator.comparingInt(Animal::getEnergy))
                        .orElse(null); // Should not be null if list is not empty, but safety first
            }
        } // End synchronized block

        return strongestAnimal;
    }
}