package proj.app;

public record SimulationSnapshot(
        int day,
        int animalCount,
        int plantCount,
        double averageEnergy,
        double averageLifespan,
        double averageChildren
) {}