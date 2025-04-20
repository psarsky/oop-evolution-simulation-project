package proj.app;

import java.util.Map;

/**
 * An immutable record holding a snapshot of key simulation statistics at a specific point in time (day).
 * Used for displaying statistics and potentially saving daily summaries.
 *
 * @param day              The simulation day number when the snapshot was taken.
 * @param animalCount      The total number of living animals.
 * @param plantCount       The total number of plants on the map.
 * @param averageEnergy    The average energy level of all living animals.
 * @param averageLifespan  The average lifespan (in days) of animals that have died so far.
 * @param averageChildren  The average number of children produced by living animals.
 * @param emptyFieldsCount The number of map cells not occupied by animals, plants, or water.
 * @param genotypeCounts   A map where keys are formatted genotype strings and values are the counts of animals with that genotype.
 * @param configName       The name of the simulation configuration this snapshot belongs to.
 */
public record SimulationStatisticsSnapshot(
        int day,
        int animalCount,
        int plantCount,
        double averageEnergy,
        double averageLifespan,
        double averageChildren,
        int emptyFieldsCount,
        Map<String, Integer> genotypeCounts,
        String configName
) {
}