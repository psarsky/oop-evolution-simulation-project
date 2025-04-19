// In SimulationStatisticsSnapshot.java
package proj.app;

import java.util.Map;

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
) {}