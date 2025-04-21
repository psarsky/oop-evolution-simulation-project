package proj.app.state;

import proj.app.AppConstants; // Use constants
import proj.app.StatisticsManager;
import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.WaterWorld;
import proj.simulation.Simulation;
import proj.app.viewmodels.SelectedAnimalViewModel;
import proj.util.Vector2d;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A background task (implementing {@link Runnable}) responsible for periodically creating
 * immutable snapshots ({@link SimulationStateSnapshot}) of the current simulation state.
 * It queries the live {@link Simulation} object, gathers necessary data (animal positions,
 * plants, water, selected animal, top genotypes from {@link StatisticsManager}), creates
 * defensive copies where necessary, and enqueues the resulting snapshot into a shared
 * {@link SimulationStateQueue} for consumption by the UI thread (via {@link proj.app.render.SimulationRenderer}).
 * This decouples the potentially slower simulation logic thread from the UI rendering thread,
 * improving responsiveness. Uses timing constants from {@link AppConstants}.
 */
public class SimulationStateProducer implements Runnable {

    private final Simulation simulation;
    private final SimulationStateQueue stateQueue;
    private final StatisticsManager statisticsManager; // Source for top genotypes cache
    private final SelectedAnimalViewModel selectedAnimalViewModel; // Source for selected animal info
    private volatile boolean running = true; // Flag to control the producer loop execution

    // Lock is currently not used, relying on Simulation/Map synchronization.
    // private final ReentrantLock simulationLock = new ReentrantLock();

    /**
     * Constructs a {@code SimulationStateProducer}.
     *
     * @param simulation            The {@link Simulation} instance providing the live simulation state. Must not be null.
     * @param stateQueue            The thread-safe {@link SimulationStateQueue} where created snapshots will be enqueued. Must not be null.
     * @param statisticsManager     The {@link StatisticsManager} used to retrieve the cached list of current top genotypes. Must not be null.
     * @param selectedAnimalViewModel The {@link SelectedAnimalViewModel} used to retrieve a reference to the currently selected animal
     *                              for inclusion in the snapshot. Must not be null.
     * @throws NullPointerException if any parameter is null.
     */
    public SimulationStateProducer(Simulation simulation,
                                   SimulationStateQueue stateQueue,
                                   StatisticsManager statisticsManager,
                                   SelectedAnimalViewModel selectedAnimalViewModel) {
        this.simulation = Objects.requireNonNull(simulation, "Simulation cannot be null");
        this.stateQueue = Objects.requireNonNull(stateQueue, "SimulationStateQueue cannot be null");
        this.statisticsManager = Objects.requireNonNull(statisticsManager, "StatisticsManager cannot be null");
        this.selectedAnimalViewModel = Objects.requireNonNull(selectedAnimalViewModel, "SelectedAnimalViewModel cannot be null");
    }

    /**
     * Signals the producer thread to stop its execution loop gracefully.
     * The thread will finish its current iteration (if any) and then terminate.
     */
    public void stop() {
        this.running = false;
        System.out.println("Simulation State Producer stop requested.");
        // Consider interrupting the thread if it might be sleeping for a long time
        // Thread.currentThread().interrupt(); // If needed, handle InterruptedException in run()
    }

    /**
     * The main execution loop of the producer thread.
     * While the {@code running} flag is true, it repeatedly:
     * 1. Calls {@link #createCurrentSnapshotData()} to generate a snapshot of the simulation state.
     * 2. Enqueues the snapshot into the {@link SimulationStateQueue}.
     * 3. Sleeps for the interval specified by {@link AppConstants#SIMULATION_STATE_PRODUCER_INTERVAL_MS}.
     * Handles {@link InterruptedException} to allow graceful termination and catches other
     * potential exceptions during snapshot creation or queueing.
     */
    @Override
    public void run() {
        System.out.println("Simulation State Producer started.");
        while (running) {
            try {
                // Create a snapshot of the current simulation state
                SimulationStateSnapshot snapshot = createCurrentSnapshotData();

                if (snapshot != null) {
                    stateQueue.enqueue(snapshot); // Add the snapshot to the queue
                } else {
                    // Map might not be ready yet, or another issue occurred.
                    System.out.println("State Producer: Snapshot was null, retrying after delay.");
                    // Wait a bit longer before retrying if snapshot creation failed
                    Thread.sleep(AppConstants.SIMULATION_STATE_PRODUCER_INTERVAL_MS * 2);
                    continue; // Skip the rest of this iteration's sleep
                }

                // Wait for the specified interval before creating the next snapshot
                Thread.sleep(AppConstants.SIMULATION_STATE_PRODUCER_INTERVAL_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Re-assert interrupt status
                running = false; // Ensure loop terminates on interrupt
                System.out.println("Simulation State Producer interrupted during sleep or operation.");
            } catch (Exception e) {
                // Catch unexpected errors during snapshot creation or queueing
                if (running) { // Avoid logging errors if stop() was called concurrently
                    System.err.println("Error in Simulation State Producer loop: " + e.getMessage());
                    e.printStackTrace();
                    // Avoid busy-waiting in case of persistent errors by sleeping longer
                    try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); running = false; }
                }
            }
        }
        System.out.println("Simulation State Producer finished.");
    }

    /**
     * Creates an immutable snapshot of the current simulation state by querying
     * the {@link Simulation}, {@link StatisticsManager}, and {@link SelectedAnimalViewModel}.
     * It performs defensive copies of mutable data structures (like animal/plant maps using
     * immutable collections or deep copies where necessary) to ensure the snapshot's
     * integrity and thread safety when passed to the UI thread.
     * <p>
     * **Synchronization Note:** This method relies on the internal synchronization mechanisms
     * of the {@link Simulation} and {@link AbstractWorldMap} classes (and potentially
     * {@link StatisticsManager} and {@link SelectedAnimalViewModel}) to provide consistent
     * views of the data being snapshotted. It uses a synchronized block on the map object
     * specifically when copying map-related collections.
     *
     * @return A {@link SimulationStateSnapshot} representing the current state, or {@code null}
     *         if the simulation map is not yet available or another critical error occurs during data retrieval.
     */
    private SimulationStateSnapshot createCurrentSnapshotData() {
        AbstractWorldMap map = simulation.getMap();
        if (map == null) {
            System.err.println("State Producer Warning: Map is not available yet for snapshot creation.");
            return null; // Map not initialized or ready
        }

        Map<Vector2d, ?> waterFieldsCopy = null;
        Map<Vector2d, List<Animal>> animalsCopy;
        Map<Vector2d, Plant> plantsCopy;
        List<String> topGenotypes; // Assumes returned list is safe or a copy
        Animal currentSelectedAnimal; // Just the reference

        // Synchronize on the map object to ensure atomic read of related collections
        synchronized (map) {
            // Copy water fields if applicable (map type check)
            if (map instanceof WaterWorld waterMap) {
                // Create a new HashMap copy from the potentially mutable internal map
                waterFieldsCopy = new HashMap<>(waterMap.getWaterFields());
            }

            // Create a deep copy of the animal map: Outer map is new HashMap, inner lists are immutable copies.
            animalsCopy = new HashMap<>();
            map.getAnimals().forEach((key, valueList) -> { // getAnimals() returns unmodifiable view
                if (valueList != null) {
                    // Create an immutable copy of the list for the snapshot using List.copyOf()
                    animalsCopy.put(key, List.copyOf(valueList));
                } else {
                    // Ensure null lists are represented as empty immutable lists
                    animalsCopy.put(key, Collections.emptyList());
                }
            });

            // Create a copy of the plant map. Plant objects themselves are assumed immutable enough.
            plantsCopy = new HashMap<>(map.getPlants()); // getPlants() returns unmodifiable view
        } // End synchronized block on map

        // Get top genotypes (StatisticsManager getter provides a safe copy)
        topGenotypes = statisticsManager.getCurrentTopGenotypes();

        // Get currently selected animal reference (ViewModel provides safe access)
        currentSelectedAnimal = selectedAnimalViewModel.getCurrentAnimalReference();

        // Create the immutable snapshot object using the copied/safe data
        return new SimulationStateSnapshot(
                animalsCopy,
                plantsCopy,
                waterFieldsCopy, // This might be null if not a WaterWorld
                topGenotypes,
                currentSelectedAnimal // Pass the reference (Animal objects assumed handled)
        );
    }

    /**
     * Creates an initial snapshot of the simulation state. This is typically called
     * once when the simulation window is first displayed, before the regular
     * production loop starts, to provide an immediate visual representation.
     * It uses the same underlying logic as the periodic snapshot creation.
     *
     * @return The initial {@link SimulationStateSnapshot} representing the state at the moment
     *         it's called, or {@code null} if the snapshot creation fails (e.g., map not ready).
     */
    public SimulationStateSnapshot createInitialSnapshot() {
        System.out.println("Creating initial simulation state snapshot...");
        SimulationStateSnapshot snapshot = createCurrentSnapshotData(); // Reuse the safe creation logic
        if (snapshot == null) {
            System.err.println("Failed to create initial snapshot.");
        }
        return snapshot;
    }
}