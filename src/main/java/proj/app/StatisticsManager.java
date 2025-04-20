// ============================================================
// SOURCE FILE: proj/app/StatisticsManager.java
// ============================================================

package proj.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import proj.app.services.IFileSaveService;
import proj.model.elements.Animal;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.WaterWorld;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;
import proj.util.Vector2d;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the calculation, collection, caching, and saving of simulation statistics.
 * It creates snapshots ({@link SimulationStatisticsSnapshot}) containing key metrics like
 * population counts, energy averages, lifespans, and dominant genotypes by querying the
 * {@link Simulation} state. It supports automatic daily saving of statistics if configured,
 * and allows users to manually trigger saving a snapshot to a file via an {@link IFileSaveService}.
 * Synchronization is used for safe access and modification of shared state like cached genotypes.
 */
public class StatisticsManager {
    private final Simulation simulation; // Reference to the simulation for state access
    private final SimulationProperties simProps; // Reference to config for map size, etc.
    private final IFileSaveService fileSaveService; // Service for saving snapshots on demand
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // For JSON serialization

    // State fields
    private volatile boolean isCollectingData; // Controls automatic daily saving. Volatile for visibility.
    private File statisticsDirectory;          // Directory where daily stats are saved.
    // Cache for top genotypes to avoid recalculation on every state snapshot for rendering
    private List<String> currentTopGenotypes = Collections.emptyList(); // Guarded by synchronized access

    // Constants for formatting and directory/file naming
    private static final DateTimeFormatter DIR_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter SNAPSHOT_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String MAIN_STATS_DIR_NAME = "statistics";
    private static final int TOP_GENOTYPE_COUNT = 3; // How many top genotypes to track/display

    /**
     * Constructs a {@code StatisticsManager} for a given simulation.
     * Initializes the data collection state based on {@link SimulationProperties#getSaveStatisticsFlag()}
     * and attempts to set up the statistics directory if collection is enabled.
     *
     * @param simulation      The {@link Simulation} instance to gather data from. Must not be null.
     * @param simProps        The {@link SimulationProperties} containing configuration details (save flag, dimensions). Must not be null.
     * @param fileSaveService The {@link IFileSaveService} used for prompting the user when exporting snapshots manually. Must not be null.
     */
    public StatisticsManager(Simulation simulation, SimulationProperties simProps, IFileSaveService fileSaveService) {
        this.simulation = Objects.requireNonNull(simulation, "Simulation cannot be null");
        this.simProps = Objects.requireNonNull(simProps, "SimulationProperties cannot be null");
        this.fileSaveService = Objects.requireNonNull(fileSaveService, "FileSaveService cannot be null");
        this.isCollectingData = simProps.getSaveStatisticsFlag(); // Initialize based on config

        if (this.isCollectingData) {
            setupStatisticsDirectory(); // Attempt directory creation immediately if needed
        }
    }

    // --- Public API ---

    /**
     * Toggles the automatic daily statistics collection and saving on or off.
     * If enabling collection and the directory wasn't set up previously (or failed),
     * it attempts to create the necessary directory structure again.
     */
    public void toggleDataCollection() {
        isCollectingData = !isCollectingData;
        if (isCollectingData && statisticsDirectory == null) {
            setupStatisticsDirectory(); // Attempt directory creation if enabling
        }
        System.out.println("Statistics data collection toggled: " + (isCollectingData ? "On" : "Off"));
    }

    /**
     * Checks if automatic daily statistics collection and saving is currently enabled.
     *
     * @return {@code true} if collecting data, {@code false} otherwise.
     */
    public boolean isCollectingData() {
        return isCollectingData; // Read of volatile boolean
    }

    /**
     * Creates a snapshot of the current simulation statistics. It retrieves the current day,
     * animal lists (live and dead), and map state from the associated {@link Simulation} instance.
     * It then calculates various metrics (averages, counts) and identifies the current top genotypes,
     * caching the latter for efficient retrieval (e.g., by the state producer).
     * <p>
     * **Synchronization Note:** This method accesses potentially mutable state from the `Simulation`
     * (animal lists, map). It is crucial that this method is called from a context where concurrent
     * modification of the simulation state is prevented (e.g., called from within a synchronized block
     * that also covers simulation steps, or called by a listener executed sequentially at the end of
     * a simulation step like a DayEndListener). The method itself uses `synchronized` to protect
     * internal calculations and updates to `currentTopGenotypes`.
     * </p>
     *
     * @return A {@link SimulationStatisticsSnapshot} containing the calculated statistics for the current moment,
     *         or {@code null} if essential data (like the simulation map) is currently unavailable.
     */
    public synchronized SimulationStatisticsSnapshot createAndCacheStatisticsSnapshot() {
        // Get required state safely from Simulation
        AbstractWorldMap currentMap = simulation.getMap();
        int currentDay = simulation.getCurrentDay(); // Get current day from Simulation state
        if (currentMap == null) {
            System.err.println("StatisticsManager: Cannot create snapshot, map is null in Simulation object.");
            return null; // Cannot proceed without a map
        }

        // Get potentially large lists - Simulation getters should provide safe access
        // (e.g., synchronized lists or copies depending on implementation)
        List<Animal> currentAnimals = simulation.getAnimals();
        List<Animal> currentDeadAnimals = simulation.getDeadAnimals();

        // Perform calculations using helper methods. Access to list contents needs care.
        // Assuming the lists provided by Simulation are safe for iteration here (e.g., snapshots or synchronized views).
        double avgEnergy = calculateAverageEnergy(currentAnimals);
        double avgLifespan = calculateAverageLifespan(currentDeadAnimals);
        double avgChildren = calculateAverageChildren(currentAnimals);
        int emptyFieldsCount = calculateEmptyFieldsCount(currentMap); // Requires safe map access
        Map<String, Integer> genotypeCounts = calculateGenotypeCounts(currentAnimals);

        // Update the internal cache of top genotypes based on the latest counts
        this.currentTopGenotypes = calculateTopGenotypes(genotypeCounts);

        // Construct the immutable snapshot object
        return new SimulationStatisticsSnapshot(
                currentDay,             // Current day from Simulation state
                currentAnimals.size(),  // Safe to get size of synchronized list
                currentMap.getPlants().size(), // Access via map getter (assumed safe)
                avgEnergy,
                avgLifespan,
                avgChildren,
                emptyFieldsCount,
                genotypeCounts,         // Pass the calculated map
                simProps.getConfigName() // Configuration name is fixed
        );
    }

    /**
     * Returns a defensive copy of the currently cached list of the top N ({@value #TOP_GENOTYPE_COUNT})
     * most frequent genotype strings. This list is updated whenever {@link #createAndCacheStatisticsSnapshot()}
     * is successfully called. Access is synchronized.
     *
     * @return A new {@link List} containing the top genotype strings (formatted). Returns an empty list if none are cached.
     */
    public synchronized List<String> getCurrentTopGenotypes() {
        // Return a defensive copy to prevent external modification of the internal cache
        return new ArrayList<>(this.currentTopGenotypes);
    }

    /**
     * Saves the provided statistics snapshot to a JSON file within the designated statistics directory
     * for this simulation run. Files are named based on the simulation day (e.g., `day_00123.json`).
     * This method does nothing if automatic data collection (`isCollectingData`) is disabled,
     * if the statistics directory hasn't been successfully created, or if the provided snapshot is null.
     *
     * @param snapshot The {@link SimulationStatisticsSnapshot} to save.
     */
    public void saveDailyStatistics(SimulationStatisticsSnapshot snapshot) {
        // Pre-conditions check
        if (!isCollectingData || statisticsDirectory == null || snapshot == null) {
            return;
        }

        // Format filename for easy sorting (zero-padded day number)
        String filename = String.format("day_%05d.json", snapshot.day());
        File outputFile = new File(statisticsDirectory, filename);

        // Write snapshot to JSON file
        try (Writer writer = new FileWriter(outputFile)) {
            gson.toJson(snapshot, writer);
        } catch (IOException e) {
            System.err.println("Error saving daily statistics for day " + snapshot.day() + " to " + outputFile.getPath() + ": " + e.getMessage());
            // Consider more robust error handling/logging if needed
        }
    }

    /**
     * Creates a statistics snapshot of the current simulation state, prompts the user
     * via the {@link IFileSaveService} to choose a file location and name, and saves
     * the snapshot as a JSON file to the selected path.
     *
     * @throws IOException           If the snapshot generation fails or an error occurs during file writing.
     * @throws IllegalStateException If the {@link IFileSaveService} was not properly initialized during setup.
     */
    public void takeSnapshot() throws IOException, IllegalStateException {
        if (fileSaveService == null) {
            throw new IllegalStateException("File Save Service is not initialized.");
        }

        // Generate a fresh snapshot for this specific action
        SimulationStatisticsSnapshot currentSnapshot = createAndCacheStatisticsSnapshot();
        if (currentSnapshot == null) {
            // Handle error if snapshot couldn't be created
            System.err.println("Failed to take snapshot: Could not create current statistics snapshot.");
            throw new IOException("Could not generate statistics snapshot.");
        }

        // Create suggested filename including config name, day, and timestamp
        String timestamp = LocalDateTime.now().format(SNAPSHOT_TIMESTAMP_FORMATTER);
        String defaultFilename = String.format("snapshot_%s_day%d_%s.json",
                simProps.getConfigName(), currentSnapshot.day(), timestamp);

        // Use the injected service to prompt the user
        File file = fileSaveService.selectSaveFile(defaultFilename, "JSON files (*.json)", "*.json");

        if (file != null) { // User selected a file (did not cancel)
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(currentSnapshot, writer); // Save the generated snapshot
                System.out.println("Snapshot saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                // Handle file writing errors
                System.err.println("Error writing snapshot to file: " + file.getAbsolutePath());
                throw e; // Re-throw for the caller (e.g., UI controller) to handle
            }
        } else {
            // User cancelled the save dialog
            System.out.println("Snapshot save cancelled by user.");
        }
    }

    // --- Private Helper Methods ---

    /**
     * Sets up the directory structure for storing daily statistics files.
     * Creates a main `statistics` directory if needed, then a subdirectory named
     * `configName_timestamp` within it. If directory creation fails at any step,
     * disables automatic data collection.
     */
    private void setupStatisticsDirectory() {
        try {
            File mainStatsDir = new File(MAIN_STATS_DIR_NAME);
            // Create main statistics directory if it doesn't exist
            if (!mainStatsDir.exists() && !mainStatsDir.mkdirs()) {
                disableCollectionOnError("main stats dir"); return;
            }
            // Create timestamped subdirectory for this specific simulation run
            String timestamp = LocalDateTime.now().format(DIR_TIMESTAMP_FORMATTER);
            // Sanitize config name to be safe for directory names
            String safeConfigName = simProps.getConfigName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String dirName = safeConfigName + "_" + timestamp;
            statisticsDirectory = new File(mainStatsDir, dirName);
            // Create the run-specific directory
            if (!statisticsDirectory.exists() && !statisticsDirectory.mkdirs()) {
                disableCollectionOnError("simulation specific dir");
            } else {
                System.out.println("Statistics will be saved to: " + statisticsDirectory.getAbsolutePath());
            }
        } catch (Exception e) { // Catch potential SecurityExceptions or other unexpected issues
            disableCollectionOnError("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Disables data collection and logs an error message. Called when directory setup fails. */
    private void disableCollectionOnError(String context) {
        this.isCollectingData = false; this.statisticsDirectory = null;
        System.err.println("Failed to create statistics directory (" + context + "). Automatic daily saving disabled.");
    }

    /** Calculates average energy. Requires safe list access/iteration. */
    private double calculateAverageEnergy(List<Animal> animals) {
        synchronized (animals) {
            if (animals.isEmpty()) return 0.0;
            return animals.stream().mapToInt(Animal::getEnergy).average().orElse(0.0);
        }
    }

    /** Calculates average lifespan. Requires safe list access/iteration. */
    private double calculateAverageLifespan(List<Animal> deadAnimals) {
        synchronized (deadAnimals) {
            if (deadAnimals.isEmpty()) return 0.0;
            return deadAnimals.stream()
                    .mapToInt(animal -> animal.getDeathDate() - animal.getBirthDate())
                    .filter(lifespan -> lifespan >= 0) // Ignore potential inconsistencies
                    .average().orElse(0.0);
        }
    }

    /** Calculates average children count. Requires safe list access/iteration. */
    private double calculateAverageChildren(List<Animal> animals) {
        synchronized (animals) {
            if (animals.isEmpty()) return 0.0;
            return animals.stream().mapToInt(Animal::getChildrenMade).average().orElse(0.0);
        }
    }

    /** Calculates empty fields count. Requires safe map access. */
    private int calculateEmptyFieldsCount(AbstractWorldMap map) {
        int totalFields = simProps.getWidth() * simProps.getHeight();
        Set<Vector2d> occupiedPositions = new HashSet<>();
        synchronized (map) { // Ensure consistent view while getting keysets from map getters
            occupiedPositions.addAll(map.getAnimals().keySet()); // getAnimals() returns unmodifiable view
            occupiedPositions.addAll(map.getPlants().keySet());  // getPlants() returns unmodifiable view
            if (map instanceof WaterWorld waterMap) {
                occupiedPositions.addAll(waterMap.getWaterFields().keySet()); // getWaterFields() returns unmodifiable view
            }
        }
        return Math.max(0, totalFields - occupiedPositions.size());
    }

    /** Calculates genotype frequencies. Requires safe list access/iteration. */
    private Map<String, Integer> calculateGenotypeCounts(List<Animal> animals) {
        synchronized (animals) { // Synchronize iteration over the animal list
            if (animals.isEmpty()) return Collections.emptyMap();
            // Group animals by their formatted genotype string and count occurrences
            return animals.stream().collect(Collectors.groupingBy(
                    animal -> GenotypeFormatter.formatGenotype(animal.getGenes()), // Key is genotype string
                    Collectors.summingInt(animal -> 1) )); // Value is the count
        }
    }

    /** Determines the top N most frequent genotypes from the counts map. */
    private List<String> calculateTopGenotypes(Map<String, Integer> genotypeCounts) {
        if (genotypeCounts == null || genotypeCounts.isEmpty()) return Collections.emptyList();
        // Sort entries by value (count) in descending order, limit to top N, extract keys (genotype strings)
        return genotypeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // Sort descending by count
                .limit(TOP_GENOTYPE_COUNT) // Take only the top N
                .map(Map.Entry::getKey) // Extract the genotype string
                .collect(Collectors.toList()); // Collect into a list
    }
}