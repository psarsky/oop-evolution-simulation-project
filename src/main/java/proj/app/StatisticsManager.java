package proj.app;

import com.google.gson.Gson;
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
 * It creates snapshots ({@link SimulationStatisticsSnapshot}) containing key metrics by querying the
 * {@link Simulation} state. It supports automatic daily saving and manual snapshot saving via an {@link IFileSaveService}.
 * Dependencies (like Gson and IFileSaveService) are injected.
 */
public class StatisticsManager {
    private final Simulation simulation; // Reference to the simulation for state access
    private final SimulationProperties simProps; // Reference to config for map size, etc.
    private final IFileSaveService fileSaveService; // Service for saving snapshots on demand
    private final Gson gson; // Gson instance for JSON serialization (injected)

    // State fields
    private volatile boolean isCollectingData; // Controls automatic daily saving. Volatile for visibility.
    private File statisticsDirectory;          // Directory where daily stats are saved.
    private List<String> currentTopGenotypes = Collections.emptyList(); // Guarded by synchronized access

    // Constants
    private static final DateTimeFormatter DIR_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter SNAPSHOT_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String MAIN_STATS_DIR_NAME = "statistics";
    private static final int TOP_GENOTYPE_COUNT = 3;

    /**
     * Constructs a {@code StatisticsManager} with injected dependencies.
     * Initializes the data collection state based on simulation properties.
     *
     * @param simulation      The {@link Simulation} instance to gather data from. Must not be null.
     * @param simProps        The {@link SimulationProperties} containing configuration details (like save flag, dimensions). Must not be null.
     * @param fileSaveService The {@link IFileSaveService} used for prompting the user during manual snapshot export. Must not be null.
     * @param gson            The {@link Gson} instance used for JSON serialization/deserialization. Must not be null.
     * @throws NullPointerException if any parameter is null.
     */
    public StatisticsManager(Simulation simulation, SimulationProperties simProps, IFileSaveService fileSaveService, Gson gson) {
        this.simulation = Objects.requireNonNull(simulation, "Simulation cannot be null");
        this.simProps = Objects.requireNonNull(simProps, "SimulationProperties cannot be null");
        this.fileSaveService = Objects.requireNonNull(fileSaveService, "FileSaveService cannot be null");
        this.gson = Objects.requireNonNull(gson, "Gson instance cannot be null");
        this.isCollectingData = simProps.getSaveStatisticsFlag();
        if (this.isCollectingData) {
            setupStatisticsDirectory();
        }
    }

    /**
     * Toggles the automatic daily statistics collection and saving feature on or off.
     * If enabling collection, it ensures the statistics directory is created if it doesn't exist.
     */
    public void toggleDataCollection() {
        isCollectingData = !isCollectingData;
        if (isCollectingData && statisticsDirectory == null) {
            setupStatisticsDirectory();
        }
        System.out.println("Statistics data collection toggled: " + (isCollectingData ? "On" : "Off"));
    }

    /**
     * Checks if automatic daily statistics collection and saving is currently enabled.
     * This method is thread-safe for reading the volatile flag.
     *
     * @return {@code true} if data collection and saving is enabled, {@code false} otherwise.
     */
    public boolean isCollectingData() {
        return isCollectingData;
    }

    /**
     * Creates a snapshot of the current simulation statistics at the current simulation day.
     * It queries the {@link Simulation} state for animal lists, map state, and current day.
     * Calculates various metrics (averages, counts) and identifies the current top genotypes,
     * caching the latter for efficient retrieval.
     * <p>
     * **Synchronization Note:** This method internally synchronizes access to its cached top genotypes list.
     * However, it relies on the caller to ensure that access to the underlying {@link Simulation} state
     * (e.g., animal lists, map data) is performed in a thread-safe manner, typically by calling this method
     * from within a synchronized block or a sequentially executed listener (like a DayEndListener).
     * </p>
     *
     * @return A {@link SimulationStatisticsSnapshot} containing the calculated statistics for the current moment.
     *         Returns {@code null} if essential data (like the simulation map) is currently unavailable.
     */
    public synchronized SimulationStatisticsSnapshot createAndCacheStatisticsSnapshot() {
        AbstractWorldMap currentMap = simulation.getMap();
        int currentDay = simulation.getCurrentDay();
        if (currentMap == null) {
            System.err.println("StatisticsManager: Cannot create snapshot, map is null.");
            return null;
        }

        List<Animal> currentAnimals = simulation.getAnimals(); // Simulation returns synchronized list/copy
        List<Animal> currentDeadAnimals = simulation.getDeadAnimals(); // Simulation returns synchronized list/copy

        double avgEnergy = calculateAverageEnergy(currentAnimals);
        double avgLifespan = calculateAverageLifespan(currentDeadAnimals);
        double avgChildren = calculateAverageChildren(currentAnimals);
        int emptyFieldsCount = calculateEmptyFieldsCount(currentMap);
        Map<String, Integer> genotypeCounts = calculateGenotypeCounts(currentAnimals);

        this.currentTopGenotypes = calculateTopGenotypes(genotypeCounts);

        return new SimulationStatisticsSnapshot(
                currentDay,
                currentAnimals.size(),
                currentMap.getPlants().size(), // Assumes map.getPlants() is safe or returns copy/unmodifiable
                avgEnergy,
                avgLifespan,
                avgChildren,
                emptyFieldsCount,
                genotypeCounts,
                simProps.getConfigName()
        );
    }

    /**
     * Returns a defensive copy of the currently cached list of the top N ({@value #TOP_GENOTYPE_COUNT})
     * most frequent genotype strings, sorted by frequency in descending order.
     * This list is updated whenever {@link #createAndCacheStatisticsSnapshot()} is successfully called.
     * Access to the internal cache is synchronized.
     *
     * @return A new {@link List} containing the top genotype strings (formatted according to {@link GenotypeFormatter}).
     *         Returns an empty list if no genotypes are cached or available.
     */
    public synchronized List<String> getCurrentTopGenotypes() {
        return new ArrayList<>(this.currentTopGenotypes);
    }

    /**
     * Saves the provided statistics snapshot to a JSON file within the designated statistics directory
     * for this simulation run. Files are named based on the simulation day (e.g., `day_00123.json`).
     * This method performs no action if automatic data collection (`isCollectingData`) is disabled,
     * if the statistics directory hasn't been successfully created, or if the provided snapshot is null.
     * Uses the injected {@link Gson} instance for serialization.
     *
     * @param snapshot The {@link SimulationStatisticsSnapshot} to save. If null, the method does nothing.
     */
    public void saveDailyStatistics(SimulationStatisticsSnapshot snapshot) {
        if (!isCollectingData || statisticsDirectory == null || snapshot == null) {
            return;
        }
        String filename = String.format("day_%05d.json", snapshot.day());
        File outputFile = new File(statisticsDirectory, filename);

        try (Writer writer = new FileWriter(outputFile)) {
            gson.toJson(snapshot, writer);
        } catch (IOException e) {
            System.err.println("Error saving daily statistics for day " + snapshot.day() + " to " + outputFile.getPath() + ": " + e.getMessage());
        }
    }

    /**
     * Creates a statistics snapshot of the current simulation state, prompts the user
     * via the injected {@link IFileSaveService} to choose a file location and name, and saves
     * the snapshot as a JSON file to the selected path using the injected {@link Gson} instance.
     *
     * @throws IOException           If the snapshot generation fails (returns null) or if an error occurs during file writing.
     * @throws IllegalStateException If the {@link IFileSaveService} was not properly initialized or encounters an issue (though unlikely with DI).
     */
    public void takeSnapshot() throws IOException, IllegalStateException {
        SimulationStatisticsSnapshot currentSnapshot = createAndCacheStatisticsSnapshot();
        if (currentSnapshot == null) {
            System.err.println("Failed to take snapshot: Could not create current statistics snapshot.");
            throw new IOException("Could not generate statistics snapshot.");
        }

        String timestamp = LocalDateTime.now().format(SNAPSHOT_TIMESTAMP_FORMATTER);
        String defaultFilename = String.format("snapshot_%s_day%d_%s.json",
                simProps.getConfigName(), currentSnapshot.day(), timestamp);

        File file = fileSaveService.selectSaveFile(defaultFilename, "JSON files (*.json)", "*.json");

        if (file != null) {
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(currentSnapshot, writer);
                System.out.println("Snapshot saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error writing snapshot to file: " + file.getAbsolutePath());
                throw e;
            }
        } else {
            System.out.println("Snapshot save cancelled by user.");
        }
    }

    // --- Private Helper Methods ---

    /** Sets up the directory structure for storing daily statistics files. */
    private void setupStatisticsDirectory() {
        try {
            File mainStatsDir = new File(MAIN_STATS_DIR_NAME);
            if (!mainStatsDir.exists() && !mainStatsDir.mkdirs()) { disableCollectionOnError("main stats dir"); return; }
            String timestamp = LocalDateTime.now().format(DIR_TIMESTAMP_FORMATTER);
            String safeConfigName = simProps.getConfigName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String dirName = safeConfigName + "_" + timestamp;
            statisticsDirectory = new File(mainStatsDir, dirName);
            if (!statisticsDirectory.exists() && !statisticsDirectory.mkdirs()) { disableCollectionOnError("simulation specific dir"); }
            else { System.out.println("Statistics will be saved to: " + statisticsDirectory.getAbsolutePath()); }
        } catch (Exception e) { disableCollectionOnError("Exception: " + e.getMessage()); e.printStackTrace(); }
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
                    .filter(lifespan -> lifespan >= 0)
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
        synchronized (map) { // Synchronize access to map's internal state
            occupiedPositions.addAll(map.getAnimals().keySet());
            occupiedPositions.addAll(map.getPlants().keySet());
            if (map instanceof WaterWorld waterMap) { occupiedPositions.addAll(waterMap.getWaterFields().keySet()); }
        }
        return Math.max(0, totalFields - occupiedPositions.size());
    }

    /** Calculates genotype frequencies. Requires safe list access/iteration. */
    private Map<String, Integer> calculateGenotypeCounts(List<Animal> animals) {
        synchronized (animals) { // Synchronize iteration over the animal list
            if (animals.isEmpty()) return Collections.emptyMap();
            return animals.stream().collect(Collectors.groupingBy(
                    animal -> GenotypeFormatter.formatGenotype(animal.getGenes()), Collectors.summingInt(animal -> 1) ));
        }
    }

    /** Determines the top N most frequent genotypes from the counts map. */
    private List<String> calculateTopGenotypes(Map<String, Integer> genotypeCounts) {
        if (genotypeCounts == null || genotypeCounts.isEmpty()) return Collections.emptyList();
        return genotypeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(TOP_GENOTYPE_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}