package proj.app;

import proj.model.elements.Animal;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.WaterWorld;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;
import proj.util.Vector2d;

import java.util.*;
import java.util.stream.Collectors;

public class StatisticsViewModel {
    private final Simulation simulation;
    private final SimulationProperties simProps;

    private List<String> topGenotypes = new ArrayList<>();
    private String genotypesText = "";
    private int emptyFieldsCount = 0;
    private double averageEnergy = 0.0;
    private double averageLifespan = 0.0;
    private double averageChildren = 0.0;

    public StatisticsViewModel(Simulation simulation, SimulationProperties simProps) {
        this.simulation = simulation;
        this.simProps = simProps;
    }

    public synchronized void updateStatistics() {
        List<Animal> currentAnimals;
        List<Animal> currentDeadAnimals;
        AbstractWorldMap currentMap;

        // Bezpieczne pobranie referencji do aktualnych danych
        synchronized (simulation) {
            currentAnimals = new ArrayList<>(simulation.getAnimals());
            currentDeadAnimals = new ArrayList<>(simulation.getDeadAnimals());
            currentMap = simulation.getMap();
        }

        // Aktualizacja pustych pól
        emptyFieldsCount = calculateEmptyFieldsCount(currentMap);

        // Aktualizacja statystyk genetycznych
        updateGenotypeStatistics(currentAnimals);

        // Aktualizacja statystyk zwierząt
        updateAnimalStatistics(currentAnimals, currentDeadAnimals);
    }

    private int calculateEmptyFieldsCount(AbstractWorldMap map) {
        int totalFields = simProps.getWidth() * simProps.getHeight();
        Set<Vector2d> occupiedPositions = new HashSet<>();

        synchronized (map) {
            // Dodaj pozycje zwierząt
            map.getAnimals().values().stream()
                    .filter(animals -> animals != null && !animals.isEmpty())
                    .forEach(animals -> occupiedPositions.add(animals.get(0).getPos()));

            // Dodaj pozycje roślin
            occupiedPositions.addAll(map.getPlants().keySet());

            // Dodaj pozycje wody dla WaterWorld
            if (map instanceof WaterWorld) {
                occupiedPositions.addAll(((WaterWorld) map).getWaterFields().keySet());
            }
        }

        return Math.max(0, totalFields - occupiedPositions.size());
    }

    private void updateGenotypeStatistics(List<Animal> animals) {
        if (animals.isEmpty()) {
            genotypesText = "No animals present";
            topGenotypes.clear();
            return;
        }

        Map<String, Long> genotypeCounts = animals.stream()
                .map(animal -> GenotypeFormatter.formatGenotype(animal.getGenes()))
                .collect(Collectors.groupingBy(
                        genotype -> genotype,
                        Collectors.counting()
                ));

        StringBuilder genotypeTextBuilder = new StringBuilder();
        genotypeTextBuilder.append("Top 3 genotypes (and their colors):\n\n");

        topGenotypes.clear();
        genotypeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    double percentage = (entry.getValue() * 100.0) / animals.size();
                    String genotypeStr = entry.getKey();
                    topGenotypes.add(genotypeStr);

                    String color = topGenotypes.indexOf(genotypeStr) == 0 ? "Magenta" :
                            topGenotypes.indexOf(genotypeStr) == 1 ? "Black" : "Blue";

                    genotypeTextBuilder.append(String.format("%s (%s)\n%d animals (%.1f%%)\n\n",
                            genotypeStr,
                            color,
                            entry.getValue(),
                            percentage));
                });

        genotypesText = genotypeTextBuilder.toString();
    }

    private void updateAnimalStatistics(List<Animal> animals, List<Animal> deadAnimals) {
        // Oblicz średnią energię
        averageEnergy = animals.isEmpty() ? 0.0 :
                animals.stream()
                        .mapToInt(Animal::getEnergy)
                        .average()
                        .orElse(0.0);

        // Oblicz średni czas życia martwych zwierząt
        averageLifespan = deadAnimals.isEmpty() ? 0.0 :
                deadAnimals.stream()
                        .filter(animal -> animal.getDeathDate() != -1)
                        .mapToInt(animal -> animal.getDeathDate() - animal.getBirthDate())
                        .average()
                        .orElse(0.0);

        // Oblicz średnią liczbę dzieci
        averageChildren = animals.isEmpty() ? 0.0 :
                animals.stream()
                        .mapToInt(Animal::getChildrenMade)
                        .average()
                        .orElse(0.0);
    }

    // Pozostałe metody gettera bez zmian
    public List<String> getTopGenotypes() {
        return topGenotypes;
    }

    public String getGenotypesText() {
        return genotypesText;
    }

    public int getEmptyFieldsCount() {
        return emptyFieldsCount;
    }

    public double getAverageEnergy() {
        return averageEnergy;
    }

    public double getAverageLifespan() {
        return averageLifespan;
    }

    public double getAverageChildren() {
        return averageChildren;
    }
}