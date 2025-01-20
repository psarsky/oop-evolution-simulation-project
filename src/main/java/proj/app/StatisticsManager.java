// StatisticsManager.java
package proj.app;

import proj.model.elements.Animal;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatisticsManager {
    private final Simulation simulation;
    private final SimulationProperties simulationProperties;
    private final List<SimulationSnapshot> snapshots;

    public StatisticsManager(Simulation simulation, SimulationProperties simulationProperties) {
        this.simulation = simulation;
        this.simulationProperties = simulationProperties;
        this.snapshots = new ArrayList<>();
    }

    public void takeSnapshot() {
        SimulationSnapshot snapshot = new SimulationSnapshot(
                simulationProperties.getDaysElapsed(),
                simulation.getAnimals().size(),
                simulation.getMap().getPlants().size(),
                calculateAverageEnergy(),
                calculateAverageLifespan(),
                calculateAverageChildren()
        );
        snapshots.add(snapshot);
    }

    private double calculateAverageEnergy() {
        return simulation.getAnimals().stream()
                .mapToInt(Animal::getEnergy)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageLifespan() {
        List<Animal> deadAnimals = simulation.getDeadAnimals();
        if (deadAnimals.isEmpty()) return 0.0;

        return deadAnimals.stream()
                .mapToInt(animal -> animal.getDeathDate() - animal.getBirthDate())
                .average()
                .orElse(0.0);
    }

    private double calculateAverageChildren() {
        return simulation.getAnimals().stream()
                .mapToInt(Animal::getChildrenMade)
                .average()
                .orElse(0.0);
    }

    public void exportStatistics() {
        takeSnapshot(); // Take final snapshot

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "simulation_stats_" + timestamp + ".csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Day,Animals,Plants,AvgEnergy,AvgLifespan,AvgChildren");

            for (SimulationSnapshot snapshot : snapshots) {
                writer.println(String.format("%d,%d,%d,%.2f,%.2f,%.2f",
                        snapshot.day(),
                        snapshot.animalCount(),
                        snapshot.plantCount(),
                        snapshot.averageEnergy(),
                        snapshot.averageLifespan(),
                        snapshot.averageChildren()
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}