// ============================================================
// SOURCE FILE: proj/app/viewmodels/StatisticsViewModel.java
// ============================================================

package proj.app.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import proj.app.GenotypeFormatter;
import proj.app.SimulationStatisticsSnapshot; // Ensure correct import

// Imports for SimulationProperties, Animal, Vector2d removed - no longer needed here

import java.util.*;
import java.util.stream.Collectors;

/**
 * ViewModel responsible for aggregating and exposing overall simulation statistics
 * in a format suitable for binding to JavaFX UI controls (e.g., Labels, TextArea).
 * It holds JavaFX properties for key metrics like population counts, averages (energy, lifespan, children),
 * and dominant genotypes. Its state is updated exclusively based on data received via
 * {@link SimulationStatisticsSnapshot} objects, typically provided by a {@link proj.app.StatisticsManager}
 * or similar component.
 */
public class StatisticsViewModel {

    //<editor-fold desc="Properties - Backing fields for UI binding">
    // Use ReadOnly*Wrapper for internal modification, expose ReadOnly*Property for binding.
    private final ReadOnlyIntegerWrapper dayCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper animalCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper plantCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper emptyFieldsCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyDoubleWrapper averageEnergy = new ReadOnlyDoubleWrapper(0.0);
    private final ReadOnlyDoubleWrapper averageLifespan = new ReadOnlyDoubleWrapper(0.0);
    private final ReadOnlyDoubleWrapper averageChildren = new ReadOnlyDoubleWrapper(0.0);
    // Formatted text for the TextArea displaying top genotypes and their stats
    private final ReadOnlyStringWrapper popularGenotypesText = new ReadOnlyStringWrapper("N/A");
    // Raw list property holding the strings of the top genotypes (might be used for coloring/logic)
    private final ReadOnlyListWrapper<String> topGenotypes = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
    //</editor-fold>

    /**
     * Updates the ViewModel's statistics properties based on the data contained within the provided snapshot.
     * This method ensures that any updates to the underlying JavaFX properties happen safely on the
     * JavaFX Application Thread, making it safe to call from background threads (like simulation listeners).
     * If the provided snapshot is null, the statistics will be cleared or reset to default values.
     *
     * @param statsSnapshot A {@link SimulationStatisticsSnapshot} containing the latest calculated statistics.
     *                      If {@code null}, statistics will be cleared.
     */
    public void updateStatistics(SimulationStatisticsSnapshot statsSnapshot) {
        // Ensure UI updates occur on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateStatisticsInternal(statsSnapshot));
        } else {
            updateStatisticsInternal(statsSnapshot);
        }
    }

    // setSimulationProperties REMOVED - Configuration properties are not needed here anymore.

    // --- Internal Update Logic ---

    /**
     * Internal method containing the core logic for updating the ViewModel's JavaFX properties
     * based on the received snapshot data. This method *must* be executed on the JavaFX Application Thread.
     *
     * @param statsSnapshot The statistics snapshot. If null, calls {@link #clearStatistics()}.
     */
    private void updateStatisticsInternal(SimulationStatisticsSnapshot statsSnapshot) {
        if (statsSnapshot != null) {
            // Update all ViewModel properties directly from the snapshot data
            dayCount.set(statsSnapshot.day());
            animalCount.set(statsSnapshot.animalCount());
            plantCount.set(statsSnapshot.plantCount());
            emptyFieldsCount.set(statsSnapshot.emptyFieldsCount());
            averageEnergy.set(statsSnapshot.averageEnergy());
            averageLifespan.set(statsSnapshot.averageLifespan());
            averageChildren.set(statsSnapshot.averageChildren());

            // Update the genotype display text and list based on counts from the snapshot
            updateGenotypeStatistics(statsSnapshot.genotypeCounts(), statsSnapshot.animalCount());

        } else {
            // If snapshot is null (e.g., simulation starting or error), clear the stats display
            System.err.println("Warning: StatisticsViewModel received null statsSnapshot. Clearing display.");
            clearStatistics();
        }
    }

    /**
     * Updates the properties related to genotype display (`popularGenotypesText` and `topGenotypes`)
     * based on the provided genotype counts and the total number of animals.
     * It sorts the genotypes by frequency, takes the top few, formats them into a user-friendly string,
     * and updates the corresponding ViewModel properties.
     *
     * @param genotypeCounts A map where keys are formatted genotype strings (e.g., "N E S W...") and values are their counts.
     * @param totalAnimals   The total number of living animals, used for calculating percentages.
     */
    private void updateGenotypeStatistics(Map<String, ? extends Number> genotypeCounts, int totalAnimals) {
        // Handle edge cases: no data or no animals
        if (genotypeCounts == null || genotypeCounts.isEmpty() || totalAnimals <= 0) {
            popularGenotypesText.set("No genotype data available."); // More descriptive message
            topGenotypes.set(FXCollections.observableArrayList()); // Ensure list property is empty observable list
            return;
        }

        StringBuilder genotypeTextBuilder = new StringBuilder("Top Genotypes (Count, %):\n");
        List<String> currentTopGenotypesList = new ArrayList<>(); // Temp list for raw top genotypes

        // Sort genotypes by count (descending) and limit to the top 3
        List<Map.Entry<String, ? extends Number>> sortedGenotypes = genotypeCounts.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().doubleValue(), e1.getValue().doubleValue())) // Sort descending by count
                .limit(3) // Consider making TOP_GENOTYPE_COUNT a constant
                .collect(Collectors.toList());

        // Format the text for display in the TextArea
        for (int i = 0; i < sortedGenotypes.size(); i++) {
            Map.Entry<String, ? extends Number> entry = sortedGenotypes.get(i);
            long count = entry.getValue().longValue();
            double percentage = (count * 100.0) / totalAnimals; // Calculate percentage
            String genotypeStr = entry.getKey();
            currentTopGenotypesList.add(genotypeStr); // Add to the raw list

            // Add color indicator based on rank (matching renderer colors)
            String colorIndicator = switch (i) {
                case 0 -> "(Magenta)";
                case 1 -> "(Black)";
                case 2 -> "(Blue)";
                default -> "";
            };

            // Append formatted string for this genotype
            genotypeTextBuilder.append(String.format("\n%d. %s %s\n   (%d, %.1f%%)",
                    i + 1,            // Rank (1, 2, 3)
                    genotypeStr,      // The genotype sequence string
                    colorIndicator,   // Color hint
                    count,            // Number of animals with this genotype
                    percentage));     // Percentage of total animals
        }

        // Update the ViewModel properties
        popularGenotypesText.set(genotypeTextBuilder.toString().trim()); // Set formatted text
        // Update the observable list property safely
        topGenotypes.set(FXCollections.observableArrayList(currentTopGenotypesList));
    }


    /**
     * Resets all statistics properties in the ViewModel to their default initial values
     * (typically 0 for counts/averages and "N/A" or empty for text/lists).
     */
    private void clearStatistics() {
        dayCount.set(0);
        animalCount.set(0);
        plantCount.set(0);
        emptyFieldsCount.set(0);
        averageEnergy.set(0.0);
        averageLifespan.set(0.0);
        averageChildren.set(0.0);
        popularGenotypesText.set("N/A");
        topGenotypes.set(FXCollections.observableArrayList()); // Reset to empty observable list
    }

    //<editor-fold desc="ReadOnly Property Getters - For UI Binding">
    // These methods provide read-only access to the properties for binding in FXML or code.
    public ReadOnlyIntegerProperty dayCountProperty() { return dayCount.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty animalCountProperty() { return animalCount.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty plantCountProperty() { return plantCount.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty emptyFieldsCountProperty() { return emptyFieldsCount.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty averageEnergyProperty() { return averageEnergy.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty averageLifespanProperty() { return averageLifespan.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty averageChildrenProperty() { return averageChildren.getReadOnlyProperty(); }
    public ReadOnlyStringProperty popularGenotypesTextProperty() { return popularGenotypesText.getReadOnlyProperty(); }
    public ReadOnlyListProperty<String> topGenotypesProperty() { return topGenotypes.getReadOnlyProperty(); }
    //</editor-fold>

    /**
     * Gets the current raw list of top genotype strings held by the ViewModel.
     * Primarily intended for internal use or components that need the raw list data (e.g., renderer).
     *
     * @return A potentially empty, immutable {@link List} copy of the top genotype strings.
     */
    public List<String> getTopGenotypes() {
        // Return immutable copy or empty list
        return topGenotypes.get() == null ? List.of() : List.copyOf(topGenotypes.get());
    }
}