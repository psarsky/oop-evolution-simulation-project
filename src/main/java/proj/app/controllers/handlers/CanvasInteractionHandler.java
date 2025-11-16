// Plik: proj/app/controllers/handlers/CanvasInteractionHandler.java
package proj.app.controllers.handlers;

import javafx.application.Platform; // Needed for Platform.runLater
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import proj.app.render.MapRenderer;
import proj.app.services.IAlertService; // Import IAlertService
import proj.app.services.IMessageService; // Import IMessageService
import proj.app.viewmodels.SelectedAnimalViewModel;
import proj.model.elements.Animal;
import proj.simulation.Simulation;
import proj.util.Vector2d;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Handles mouse click interactions on the simulation canvas.
 * Converts coordinates, queries simulation state, updates SelectedAnimalViewModel,
 * and uses IAlertService for user feedback on errors.
 */
public class CanvasInteractionHandler {

    private final Canvas simulationCanvas;
    private final MapRenderer mapRenderer;
    private final Simulation simulation;
    private final SelectedAnimalViewModel selectedAnimalViewModel;
    private final IAlertService alertService; // Injected dependency
    private final IMessageService messageService; // Injected dependency

    private final EventHandler<MouseEvent> mouseClickedHandler;

    /**
     * Constructs the handler, injecting required dependencies.
     *
     * @param simulationCanvas        The {@link Canvas} element.
     * @param mapRenderer             The {@link MapRenderer} for coordinate conversion.
     * @param simulation              The {@link Simulation} instance.
     * @param selectedAnimalViewModel The {@link SelectedAnimalViewModel} to update.
     * @param alertService            The {@link IAlertService} for showing alerts.
     * @param messageService          The {@link IMessageService} for localized messages.
     * @throws NullPointerException if any dependency is null.
     */
    public CanvasInteractionHandler(Canvas simulationCanvas,
                                    MapRenderer mapRenderer,
                                    Simulation simulation,
                                    SelectedAnimalViewModel selectedAnimalViewModel,
                                    IAlertService alertService, // Added
                                    IMessageService messageService) { // Added

        this.simulationCanvas = Objects.requireNonNull(simulationCanvas, "simulationCanvas cannot be null");
        this.mapRenderer = Objects.requireNonNull(mapRenderer, "mapRenderer cannot be null");
        this.simulation = Objects.requireNonNull(simulation, "simulation cannot be null");
        this.selectedAnimalViewModel = Objects.requireNonNull(selectedAnimalViewModel, "selectedAnimalViewModel cannot be null");
        this.alertService = Objects.requireNonNull(alertService, "alertService cannot be null"); // Added check
        this.messageService = Objects.requireNonNull(messageService, "messageService cannot be null"); // Added check

        this.mouseClickedHandler = event -> handleMouseClick(event);
    }

    /** Attach handler to canvas. */
    public void attachHandlers() {
        simulationCanvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        simulationCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        System.out.println("CanvasInteractionHandler attached.");
    }

    /** Remove handler from canvas. */
    public void detachHandlers() {
        simulationCanvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
        System.out.println("CanvasInteractionHandler detached.");
    }

    /** Process mouse click event. */
    private void handleMouseClick(MouseEvent event) {
        if (simulation.getMap() == null) {
            // User clicked, but simulation isn't ready - show alert
            System.err.println("Internal Info: CanvasInteractionHandler: Cannot handle click, Simulation Map is not ready."); // Keep internal log
            showClickErrorAlert("error.canvas.click.mapNotReady.header", "error.canvas.click.mapNotReady.content");
            return;
        }

        try {
            Vector2d clickedMapPos = mapRenderer.convertCanvasToMapPosition(event.getX(), event.getY());
            // System.out.println("Canvas clicked at: (" + event.getX() + ", " + event.getY() + ") -> Map Pos: " + clickedMapPos); // Debugging

            Animal clickedAnimal = findAnimalAt(clickedMapPos);
            selectedAnimalViewModel.update(clickedAnimal); // Update ViewModel

        } catch (Exception e) {
            // Unexpected error during click processing - show alert
            System.err.println("CanvasInteractionHandler: Error processing mouse click event."); // Keep internal log
            e.printStackTrace(); // Keep stack trace for debugging
            selectedAnimalViewModel.clear(); // Clear selection on error
            showClickErrorAlert("error.canvas.click.unexpected.header", "error.canvas.click.unexpected.content", e.getMessage());
        }
    }

    /** Finds animal at position. */
    private Animal findAnimalAt(Vector2d position) {
        // ... (findAnimalAt implementation remains the same) ...
        if (simulation.getMap() == null) { return null; }
        Animal strongestAnimal = null;
        synchronized (simulation.getMap()) {
            List<Animal> animalsAtPos = simulation.getMap().getAnimals().get(position);
            if (animalsAtPos != null && !animalsAtPos.isEmpty()) {
                strongestAnimal = animalsAtPos.stream()
                        .max(Comparator.comparingInt(Animal::getEnergy))
                        .orElse(null);
            }
        }
        return strongestAnimal;
    }

    /** Helper method to show alerts, ensuring execution on FX thread. */
    private void showClickErrorAlert(String headerKey, String contentKey, Object... args) {
        // Assume this handler might be called from different contexts, ensure FX thread
        Platform.runLater(() -> {
            alertService.showAlert(
                    IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"), // Use standard error title
                    messageService.getMessage(headerKey), // Get localized header
                    messageService.getFormattedMessage(contentKey, args) // Get localized content
            );
        });
    }
}