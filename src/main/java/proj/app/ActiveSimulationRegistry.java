package proj.app; // Or a more suitable package like proj.app.registry

import javafx.application.Platform;
import javafx.stage.Stage;
import proj.app.controllers.SimulationWindowController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList; // Thread-safe list for iteration

/**
 * A thread-safe Singleton registry to track active simulation windows.
 * Allows registering, unregistering, and stopping simulations based on their
 * configuration name. This decouples MainWindowController from the need
 * to parse window titles or use Stage UserData for stopping simulations.
 */
public final class ActiveSimulationRegistry {

    /**
     * Internal record to hold information about a running simulation instance.
     *
     * @param controller The SimulationWindowController managing the simulation logic and UI.
     * @param stage      The Stage (window) hosting the simulation UI.
     */
    private record SimulationInfo(SimulationWindowController controller, Stage stage) {}

    // The single instance of the registry (Singleton pattern)
    private static final ActiveSimulationRegistry INSTANCE = new ActiveSimulationRegistry();

    /**
     * The main data structure storing simulation information.
     * Key: Configuration Name (String)
     * Value: A thread-safe list of SimulationInfo records for simulations using that config.
     * Using ConcurrentHashMap for thread-safe map operations.
     * Using CopyOnWriteArrayList for thread-safe iteration and modification of the list values.
     */
    private final Map<String, List<SimulationInfo>> activeSimulations = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent external instantiation (Singleton pattern).
     */
    private ActiveSimulationRegistry() {}

    /**
     * Gets the single instance of the ActiveSimulationRegistry.
     *
     * @return The singleton ActiveSimulationRegistry instance.
     */
    public static ActiveSimulationRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers an active simulation instance.
     * Associates the controller and its stage with the given configuration name.
     * This method is thread-safe.
     *
     * @param configName The name of the configuration used by the simulation (non-null).
     * @param controller The SimulationWindowController instance to register (non-null).
     * @param stage      The Stage hosting the simulation window (non-null).
     */
    public void register(String configName, SimulationWindowController controller, Stage stage) {
        if (configName == null || controller == null || stage == null) {
            System.err.println("Error registering simulation: Null parameter provided.");
            return;
        }
        SimulationInfo info = new SimulationInfo(controller, stage);
        // Use computeIfAbsent for atomic creation of the list if the key doesn't exist
        activeSimulations.computeIfAbsent(configName, k -> new CopyOnWriteArrayList<>()).add(info);
        System.out.println("Registered simulation for config '" + configName + "' (Controller: " + controller.hashCode() + ")");
    }

    /**
     * Unregisters a specific simulation instance, typically when its window is closing.
     * Removes the association of the controller and stage from the given configuration name.
     * This method is thread-safe.
     *
     * @param configName The name of the configuration used by the simulation (non-null).
     * @param controller The SimulationWindowController instance to unregister (non-null).
     */
    public void unregister(String configName, SimulationWindowController controller) {
        if (configName == null || controller == null) {
            System.err.println("Error unregistering simulation: Null parameter provided.");
            return;
        }
        // Use computeIfPresent to safely modify the list if the key exists
        activeSimulations.computeIfPresent(configName, (key, list) -> {
            // Remove the specific SimulationInfo based on the controller instance
            list.removeIf(info -> info.controller() == controller);
            // If the list becomes empty after removal, remove the configName key from the map
            return list.isEmpty() ? null : list;
        });
        System.out.println("Unregistered simulation for config '" + configName + "' (Controller: " + controller.hashCode() + ")");
    }

    /**
     * Finds all active simulation instances associated with the given configuration name,
     * signals their controllers to stop simulation threads, and closes their stages.
     * Finally, removes them from the registry. This method is thread-safe.
     *
     * @param configName The name of the configuration whose simulations should be stopped.
     * @return The number of simulation instances that were stopped and closed.
     */
    public int stopSimulations(String configName) {
        if (configName == null) {
            return 0;
        }

        // Atomically get and remove the list for the config name.
        // This prevents new registrations/unregister actions on this list while we process it.
        List<SimulationInfo> simulationsToStop = activeSimulations.remove(configName);

        int stoppedCount = 0;
        if (simulationsToStop != null && !simulationsToStop.isEmpty()) {
            System.out.println("Stopping " + simulationsToStop.size() + " simulation(s) for config '" + configName + "'...");
            for (SimulationInfo info : simulationsToStop) {
                try {
                    // 1. Signal the controller to stop its background threads
                    info.controller().stopSimulationThreads();

                    // 2. Close the window (Stage) on the JavaFX Application Thread
                    // Ensure Stage is not null before attempting closure
                    if (info.stage() != null) {
                        // Use runLater for safety, although immediate close might also work
                        Platform.runLater(() -> {
                            try {
                                if (info.stage().isShowing()) { // Check if showing before closing
                                    info.stage().close();
                                }
                            } catch (Exception e) {
                                System.err.println("Error closing stage for stopped simulation (Config: " + configName + "): " + e.getMessage());
                            }
                        });
                    } else {
                        System.err.println("Warning: Stage was null for controller during stopSimulations (Config: " + configName + ")");
                    }
                    stoppedCount++;
                } catch (Exception e) {
                    // Log error but continue trying to stop others
                    System.err.println("Error stopping simulation controller or closing stage for config '" + configName + "': " + e.getMessage());
                    e.printStackTrace();
                    // Attempt to close the stage anyway if controller stop failed
                    if (info.stage() != null) {
                        Platform.runLater(() -> {
                            try { if (info.stage().isShowing()) info.stage().close(); } catch (Exception ce) {/* ignore */}
                        });
                    }
                }
                // No need to explicitly unregister here, as the entire list was removed from the map.
            }
            System.out.println("Finished stopping simulations for config '" + configName + "'. Stopped count: " + stoppedCount);
        } else {
            System.out.println("No active simulations found to stop for config '" + configName + "'.");
        }
        return stoppedCount;
    }
}