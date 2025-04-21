package proj.app.render;

import javafx.animation.AnimationTimer;
import proj.app.AppConstants; // Use constants
import proj.app.state.SimulationStateQueue;
import proj.app.state.SimulationStateSnapshot;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Manages the rendering loop using a JavaFX {@link AnimationTimer} for the simulation window.
 * It efficiently dequeues simulation state snapshots from a {@link SimulationStateQueue},
 * passes them to a processor callback (typically in the controller), and then delegates
 * the actual drawing task to the injected {@link MapRenderer}. This decouples rendering
 * updates from the simulation logic thread. Uses timing constants from {@link AppConstants}.
 */
public class SimulationRenderer {

    private final SimulationStateQueue stateQueue;
    private final MapRenderer mapRenderer;
    private final Consumer<SimulationStateSnapshot> snapshotProcessor; // Callback for the controller
    private AnimationTimer animationTimer;
    private volatile boolean isRunning = false; // Controls the AnimationTimer running state

    /**
     * Constructs the SimulationRenderer.
     *
     * @param stateQueue        The thread-safe queue providing {@link SimulationStateSnapshot} objects
     *                          produced by the simulation state producer. Must not be null.
     * @param mapRenderer       The {@link MapRenderer} instance responsible for drawing the simulation
     *                          state onto the designated canvas. Must not be null.
     * @param snapshotProcessor A {@link Consumer} functional interface (e.g., a method reference or lambda)
     *                          that will be invoked on the JavaFX Application Thread just before a snapshot
     *                          is rendered. This allows the caller (e.g., {@link proj.app.controllers.SimulationWindowController})
     *                          to perform actions based on the snapshot being rendered, such as updating internal state
     *                          or checking selected animal liveness. Must not be null.
     * @throws NullPointerException if any parameter is null.
     */
    public SimulationRenderer(SimulationStateQueue stateQueue, MapRenderer mapRenderer, Consumer<SimulationStateSnapshot> snapshotProcessor) {
        this.stateQueue = Objects.requireNonNull(stateQueue, "StateQueue cannot be null");
        this.mapRenderer = Objects.requireNonNull(mapRenderer, "MapRenderer cannot be null");
        this.snapshotProcessor = Objects.requireNonNull(snapshotProcessor, "SnapshotProcessor callback cannot be null");
        createAnimationTimer();
    }

    /** Creates the internal AnimationTimer instance that drives the rendering loop. */
    private void createAnimationTimer() {
        animationTimer = new AnimationTimer() {
            private long lastUiUpdateTimestamp = 0; // Tracks the timestamp of the last rendered frame

            /**
             * This method is invoked by the JavaFX framework on the JavaFX Application Thread
             * for each frame pulse (typically aiming for ~60 FPS). It throttles rendering updates
             * based on {@link AppConstants#UI_RENDER_INTERVAL_NANOS}, dequeues the latest available
             * {@link SimulationStateSnapshot} from the queue, invokes the {@code snapshotProcessor} callback,
             * and then instructs the {@link MapRenderer} to draw the snapshot.
             *
             * @param now The timestamp of the current frame in nanoseconds, provided by the JavaFX runtime.
             */
            @Override
            public void handle(long now) {
                // Throttle rendering updates to approximately match the target frame rate.
                // Skip processing this frame if the timer is stopped or if not enough time has passed since the last update.
                if (!isRunning || (now - lastUiUpdateTimestamp < AppConstants.UI_RENDER_INTERVAL_NANOS)) {
                    return;
                }
                lastUiUpdateTimestamp = now; // Record the timestamp of this update

                // Attempt to dequeue the next state snapshot from the shared queue.
                SimulationStateSnapshot snapshotToRender = stateQueue.dequeue();

                // Only proceed if a snapshot was successfully dequeued (queue was not empty).
                if (snapshotToRender != null) {
                    // Invoke the callback provided during construction, passing the snapshot.
                    // This allows the controller or other components to react to the snapshot being rendered.
                    snapshotProcessor.accept(snapshotToRender);

                    // Delegate the actual drawing of the snapshot to the MapRenderer.
                    mapRenderer.drawSimulation(snapshotToRender);
                }
                // If snapshotToRender is null, the queue was empty, so we draw nothing new in this frame.
            }
        };
    }

    /**
     * Starts the rendering loop by starting the internal {@link AnimationTimer}.
     * If the renderer is already running, this method has no effect.
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            animationTimer.start();
            System.out.println("SimulationRenderer started.");
        }
    }

    /**
     * Stops the rendering loop by stopping the internal {@link AnimationTimer}.
     * If the renderer is already stopped, this method has no effect.
     */
    public void stop() {
        if (isRunning) {
            isRunning = false;
            animationTimer.stop();
            System.out.println("SimulationRenderer stopped.");
        }
    }

    /**
     * Checks if the rendering loop (AnimationTimer) is currently active.
     *
     * @return {@code true} if the renderer's AnimationTimer is running, {@code false} otherwise.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Forces an immediate redraw of the provided simulation state snapshot using the MapRenderer.
     * This bypasses the normal AnimationTimer throttling and queue mechanism. Useful for instantly
     * updating the view after events like resizing or loading initial state. This method should
     * typically be called on the JavaFX Application Thread.
     *
     * @param snapshotToDraw The {@link SimulationStateSnapshot} to draw immediately. If null, the
     *                       behavior depends on the {@link MapRenderer#drawSimulation(SimulationStateSnapshot)}
     *                       implementation (it might clear the canvas or do nothing).
     */
    public void redrawFrame(SimulationStateSnapshot snapshotToDraw) {
        // Delegate directly to mapRenderer; assumes mapRenderer handles null correctly
        if (mapRenderer != null) {
            mapRenderer.drawSimulation(snapshotToDraw);
        }
    }
}