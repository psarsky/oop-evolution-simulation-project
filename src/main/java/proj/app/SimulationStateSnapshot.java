package proj.app;

import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.util.Vector2d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of simulation state for UI rendering.
 * This class holds a copy of the simulation state at a specific moment.
 */
public class SimulationStateSnapshot {
    private final Map<Vector2d, List<Animal>> animals;
    private final Map<Vector2d, Plant> plants;
    private final Map<Vector2d, ?> waterFields;
    private final List<String> topGenotypes;
    private final Animal selectedAnimal;

    public SimulationStateSnapshot(Map<Vector2d, List<Animal>> animals,
                                   Map<Vector2d, Plant> plants,
                                   Map<Vector2d, ?> waterFields,
                                   List<String> topGenotypes,
                                   Animal selectedAnimal) {
        // Deep copy all collections to ensure thread safety
        this.animals = new HashMap<>();
        animals.forEach((pos, animalList) ->
                this.animals.put(pos, List.copyOf(animalList))
        );

        this.plants = new HashMap<>(plants);
        this.waterFields = waterFields != null ? new HashMap<>(waterFields) : null;
        this.topGenotypes = List.copyOf(topGenotypes);
        this.selectedAnimal = selectedAnimal;
    }

    public Map<Vector2d, List<Animal>> getAnimals() {
        return animals;
    }

    public Map<Vector2d, Plant> getPlants() {
        return plants;
    }

    public Map<Vector2d, ?> getWaterFields() {
        return waterFields;
    }

    public List<String> getTopGenotypes() {
        return topGenotypes;
    }

    public Animal getSelectedAnimal() {
        return selectedAnimal;
    }
}