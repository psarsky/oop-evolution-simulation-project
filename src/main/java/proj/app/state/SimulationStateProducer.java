package proj.app.state;

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
import java.util.concurrent.locks.ReentrantLock;

/**
 * A background task (Runnable) responsible for periodically creating immutable snapshots
 * ({@link SimulationStateSnapshot}) of the current simulation state. These snapshots are
 * then enqueued into a {@link SimulationStateQueue} for consumption by the UI thread (renderer).
 * This decouples the simulation logic thread from the UI rendering thread.
 */
public class SimulationStateProducer implements Runnable {

    private final Simulation simulation;
    private final SimulationStateQueue stateQueue;
    private final StatisticsManager statisticsManager; // Source for top genotypes
    private final SelectedAnimalViewModel selectedAnimalViewModel; // Source for selected animal info
    private volatile boolean running = true; // Flag to control the producer loop
    private static final long UPDATE_INTERVAL_MS = 50; // ~20 snapshots per second

    // Optional lock if finer-grained control over simulation access is needed
    // private final ReentrantLock simulationLock = new ReentrantLock();

    /**
     * Constructs a {@code SimulationStateProducer}.
     *
     * @param simulation            The {@link Simulation} instance to query for state.
     * @param stateQueue            The {@link SimulationStateQueue} to enqueue snapshots into.
     * @param statisticsManager     The {@link StatisticsManager} to retrieve cached top genotypes.
     * @param selectedAnimalViewModel The {@link SelectedAnimalViewModel} to retrieve the currently selected animal reference.
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
     * Signals the producer thread to stop its execution loop.
     */
    public void stop() {
        this.running = false;
        System.out.println("Simulation State Producer stop requested.");
    }

    /**
     * The main execution loop of the producer thread.
     * Periodically calls {@link #createCurrentSnapshotData()} to generate a snapshot
     * and enqueues it into the {@link SimulationStateQueue}. Handles interruptions
     * and potential errors during snapshot creation.
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
                    // Logged within createCurrentSnapshotData. Wait before retrying.
                    System.out.println("State Producer: Snapshot was null, retrying after delay.");
                    Thread.sleep(UPDATE_INTERVAL_MS * 2); // Wait a bit longer if snapshot failed
                    continue; // Skip the rest of this iteration
                }

                // Wait for the specified interval before creating the next snapshot
                Thread.sleep(UPDATE_INTERVAL_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Re-interrupt the thread
                running = false; // Ensure loop terminates
                System.out.println("Simulation State Producer interrupted.");
            } catch (Exception e) {
                // Catch unexpected errors during snapshot creation or queueing
                if (running) { // Avoid logging errors if we're already stopping
                    System.err.println("Error in Simulation State Producer loop: " + e.getMessage());
                    e.printStackTrace();
                    // Avoid busy-waiting in case of persistent errors
                    try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); running = false; }
                }
            }
        }
        System.out.println("Simulation State Producer finished.");
    }

    /**
     * Creates an immutable snapshot of the current simulation state by querying
     * the {@link Simulation}, {@link StatisticsManager}, and {@link SelectedAnimalViewModel}.
     * Performs defensive copies of mutable data structures (like animal/plant maps)
     * to ensure the snapshot's immutability and thread safety.
     * Requires careful synchronization if the underlying simulation state can be modified concurrently.
     *
     * @return A {@link SimulationStateSnapshot} representing the current state, or {@code null} if the map is not yet available.
     */
    private SimulationStateSnapshot createCurrentSnapshotData() {
        AbstractWorldMap map = simulation.getMap();
        if (map == null) {
            System.err.println("State Producer Warning: Map is not available yet.");
            return null; // Map not initialized or ready
        }

        Map<Vector2d, ?> waterFieldsCopy = null;
        Map<Vector2d, List<Animal>> animalsCopy;
        Map<Vector2d, Plant> plantsCopy;
        List<String> topGenotypes; // Assumes returned list is safe or a copy
        Animal currentSelectedAnimal; // Just the reference

        // --- Synchronization Point ---
        // Access to the map's internal state needs to be thread-safe.
        // Assuming the map itself handles internal synchronization or this method
        // is called from a context where concurrent modification is prevented.
        // If not, synchronize explicitly: synchronized (map) { ... }
        synchronized (map) {
            // Copy water fields if applicable
            if (map instanceof WaterWorld waterMap) {
                // Defensive copy: Create a new HashMap from the existing one
                waterFieldsCopy = new HashMap<>(waterMap.getWaterFields());
            }

            // Defensive copy of animal map: Deep copy if lists are mutable, shallow if immutable
            animalsCopy = new HashMap<>();
            map.getAnimals().forEach((key, valueList) -> {
                if (valueList != null) {
                    // Create an immutable copy of the list for the snapshot
                    animalsCopy.put(key, List.copyOf(valueList));
                } else {
                    animalsCopy.put(key, Collections.emptyList());
                }
            });

            // Defensive copy of plant map
            plantsCopy = new HashMap<>(map.getPlants());
        } // --- End Synchronization Point ---


        // Get top genotypes (assumes StatisticsManager provides a thread-safe way, e.g., cached copy)
        topGenotypes = statisticsManager.getCurrentTopGenotypes();

        // Get currently selected animal reference (ViewModel access is generally thread-safe for reading properties)
        currentSelectedAnimal = selectedAnimalViewModel.getCurrentAnimalReference();

        // Create the immutable snapshot with the copied data
        return new SimulationStateSnapshot(
                animalsCopy,
                plantsCopy,
                waterFieldsCopy, // Can be null
                topGenotypes,
                currentSelectedAnimal // Pass the reference (Animal objects assumed immutable or handled carefully)
        );
    }

    /**
     * Creates an initial snapshot, typically used for the very first rendering
     * before the main producer loop starts generating regular snapshots.
     *
     * @return The initial {@link SimulationStateSnapshot}, or {@code null} if creation fails (e.g., map not ready).
     */
    public SimulationStateSnapshot createInitialSnapshot() {
        System.out.println("Creating initial simulation state snapshot...");
        SimulationStateSnapshot snapshot = createCurrentSnapshotData(); // Use the same safe creation logic
        if (snapshot == null) {
            System.err.println("Failed to create initial snapshot.");
        }
        return snapshot;
    }
}