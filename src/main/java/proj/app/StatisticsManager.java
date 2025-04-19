package proj.app;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import proj.model.elements.Animal;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.WaterWorld;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import proj.util.Vector2d;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manager for simulation statistics.
 * Handles exporting statistics to files and generating reports.
 */
public class StatisticsManager {
    private final Simulation simulation;
    private final SimulationProperties simProps;
    private boolean isCollectingData = true;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File statisticsDirectory;


    /**
     * Creates a new StatisticsManager.
     *
     * @param simulation The simulation to gather statistics from
     * @param simProps The simulation properties
     */
    public StatisticsManager(Simulation simulation, SimulationProperties simProps) {
        this.simulation = simulation;
        this.simProps = simProps;

        if (simProps.getSaveStatisticsFlag()) {
            setupStatisticsDirectory();
        }
    }

    private void setupStatisticsDirectory() {
        // Ensure the main statistics directory exists
        File mainStatisticsDir = new File("statistics");
        if (!mainStatisticsDir.exists()) {
            mainStatisticsDir.mkdirs();
        }

        // Create a directory for this specific simulation run
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String dirName = "statistics_" + simProps.getConfigName() + "_" + timestamp;

        statisticsDirectory = new File(mainStatisticsDir, dirName);
        if (!statisticsDirectory.exists()) {
            statisticsDirectory.mkdirs();
        }
    }


    public void toggleDataCollection() {
        isCollectingData = !isCollectingData;
        if (isCollectingData && statisticsDirectory == null) {
            setupStatisticsDirectory();
        }
//        return isCollectingData;
    }

    public boolean isCollectingData() {
        return isCollectingData;
    }

    public void saveCurrentDayStatistics() {
        if (!isCollectingData || statisticsDirectory == null) {
            return;
        }

        try {
            // Create snapshot of current simulation statistics
            SimulationStatisticsSnapshot snapshot = createStatisticsSnapshot();

            // Save to a JSON file in the statistics directory
            String filename = String.format("day_%d.json", simProps.getDaysElapsed());
            File outputFile = new File(statisticsDirectory, filename);

            try (FileWriter writer = new FileWriter(outputFile)) {
                gson.toJson(snapshot, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving daily statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void takeSnapshot() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Simulation Snapshot");

        // Set default filename with timestamp and day
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String defaultFilename = "simulation_snapshot_day" + simProps.getDaysElapsed() + "_" + timestamp + ".json";
        fileChooser.setInitialFileName(defaultFilename);

        // Set file extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save dialog
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                // Create snapshot
                SimulationStatisticsSnapshot snapshot = createStatisticsSnapshot();

                // Save to JSON
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(snapshot, writer);
                }

                // Show success message
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Snapshot Saved");
                alert.setHeaderText(null);
                alert.setContentText("Simulation snapshot saved successfully to:\n" + file.getAbsolutePath());
                alert.showAndWait();

            } catch (IOException e) {
                // Error message
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Snapshot Failed");
                alert.setHeaderText(null);
                alert.setContentText("Failed to save snapshot: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private SimulationStatisticsSnapshot createStatisticsSnapshot() {
        // Get animals and calculate basic statistics
        List<Animal> animals = simulation.getAnimals();
        List<Animal> deadAnimals = simulation.getDeadAnimals();

        // Calculate averages
        double avgEnergy = animals.stream()
                .mapToInt(Animal::getEnergy)
                .average()
                .orElse(0.0);

        double avgLifespan = deadAnimals.isEmpty() ? 0.0 :
                deadAnimals.stream()
                        .filter(animal -> animal.getDeathDate() != -1)
                        .mapToInt(animal -> animal.getDeathDate() - animal.getBirthDate())
                        .average()
                        .orElse(0.0);

        double avgChildren = animals.stream()
                .mapToInt(Animal::getChildrenMade)
                .average()
                .orElse(0.0);

        // Calculate empty fields
        int emptyFieldsCount = calculateEmptyFieldsCount();

        // Calculate genotype distribution
        Map<String, Integer> genotypeCounts = animals.stream()
                .map(animal -> GenotypeFormatter.formatGenotype(animal.getGenes()))
                .collect(Collectors.groupingBy(
                        genotype -> genotype,
                        Collectors.summingInt(genotype -> 1)
                ));

        // Create and return the snapshot
        return new SimulationStatisticsSnapshot(
                simProps.getDaysElapsed(),
                animals.size(),
                simulation.getMap().getPlants().size(),
                avgEnergy,
                avgLifespan,
                avgChildren,
                emptyFieldsCount,
                genotypeCounts,
                simProps.getConfigName()
        );
    }

    private int calculateEmptyFieldsCount() {
        int totalFields = simProps.getWidth() * simProps.getHeight();
        Set<Vector2d> occupiedPositions = new HashSet<>();

        AbstractWorldMap map = simulation.getMap();
        synchronized (map) {
            // Add animal positions
            map.getAnimals().values().stream()
                    .filter(animals -> animals != null && !animals.isEmpty())
                    .forEach(animals -> occupiedPositions.add(animals.getFirst().getPos()));

            // Add plant positions
            occupiedPositions.addAll(map.getPlants().keySet());

            // Add water positions for WaterWorld
            if (map instanceof WaterWorld) {
                occupiedPositions.addAll(((WaterWorld) map).getWaterFields().keySet());
            }
        }

        return Math.max(0, totalFields - occupiedPositions.size());
    }

}