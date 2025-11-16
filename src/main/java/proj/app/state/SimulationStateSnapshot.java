package proj.app.state;

import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.util.Vector2d;

import java.util.*;

/**
 * Represents an immutable snapshot of the simulation's world state at a specific moment.
 * It contains copies or immutable views of the simulation data (animals, plants, water, etc.),
 * making it safe to pass between threads (e.g., from the simulation thread to the UI thread).
 */
public class SimulationStateSnapshot {

    private final Map<Vector2d, List<Animal>> animals; // Map position -> immutable list of animals
    private final Map<Vector2d, Plant> plants;         // Map position -> plant
    private final Map<Vector2d, ?> waterFields;      // Map position -> water element (can be null if not WaterWorld)
    private final List<String> topGenotypes;           // Immutable list of top genotype strings
    private final Animal selectedAnimal;             // Reference to the selected animal *at the time of snapshot creation*

    /**
     * Constructs an immutable {@code SimulationStateSnapshot}.
     * Defensive copies (using immutable collections like {@code Map.copyOf}, {@code List.copyOf})
     * are made of the provided mutable collections to ensure the snapshot's integrity.
     *
     * @param animals        A map where keys are positions ({@link Vector2d}) and values are lists of {@link Animal}s at that position. Assumed lists within the map might be mutable.
     * @param plants         A map where keys are positions ({@link Vector2d}) and values are {@link Plant} objects.
     * @param waterFields    A map representing water positions (keys are {@link Vector2d}). Can be {@code null} if the map type doesn't include water.
     * @param topGenotypes   A list of strings representing the most frequent genotypes.
     * @param selectedAnimal A reference to the {@link Animal} that was selected in the UI when the snapshot was created. Can be {@code null}.
     */
    public SimulationStateSnapshot(Map<Vector2d, List<Animal>> animals,
                                   Map<Vector2d, Plant> plants,
                                   Map<Vector2d, ?> waterFields, // Can be null
                                   List<String> topGenotypes,
                                   Animal selectedAnimal) { // Can be null

        // Create immutable copies of the maps and lists
        // Map.copyOf creates an immutable map. For the animals map, we also need to ensure the inner lists are immutable.
        Map<Vector2d, List<Animal>> immutableAnimals = new HashMap<>();
        if (animals != null) {
            animals.forEach((pos, list) -> {
                if (list != null) {
                    immutableAnimals.put(pos, List.copyOf(list)); // List.copyOf creates an immutable list
                } else {
                    immutableAnimals.put(pos, Collections.emptyList());
                }
            });
        }
        this.animals = Map.copyOf(immutableAnimals); // Make the outer map immutable

        this.plants = (plants != null) ? Map.copyOf(plants) : Map.of(); // Map.of() for empty immutable map

        // Handle potentially null waterFields map
        this.waterFields = (waterFields != null) ? Map.copyOf(waterFields) : null;

        this.topGenotypes = (topGenotypes != null) ? List.copyOf(topGenotypes) : List.of(); // List.of() for empty immutable list

        // Store the reference to the selected animal. Animal object itself isn't copied.
        this.selectedAnimal = selectedAnimal;
    }

    /**
     * Gets an immutable map of animal positions to lists of animals.
     * The inner lists are also immutable.
     *
     * @return An immutable {@link Map} of {@link Vector2d} to immutable {@link List} of {@link Animal}.
     */
    public Map<Vector2d, List<Animal>> getAnimals() {
        return animals;
    }

    /**
     * Gets an immutable map of plant positions to plants.
     *
     * @return An immutable {@link Map} of {@link Vector2d} to {@link Plant}.
     */
    public Map<Vector2d, Plant> getPlants() {
        return plants;
    }

    /**
     * Gets an immutable map of water positions.
     * May return {@code null} if the simulation map type does not include water.
     * The type of the value is intentionally generic ({@code ?}) as only the keys (positions) are typically needed.
     *
     * @return An immutable {@link Map} of {@link Vector2d} to water elements, or {@code null}.
     */
    public Map<Vector2d, ?> getWaterFields() {
        return waterFields;
    }

    /**
     * Gets an immutable list of the top genotype strings determined at the time of the snapshot.
     *
     * @return An immutable {@link List} of {@link String}.
     */
    public List<String> getTopGenotypes() {
        return topGenotypes;
    }

    /**
     * Gets the reference to the {@link Animal} that was selected in the UI when this snapshot was created.
     * Returns {@code null} if no animal was selected at that time.
     *
     * @return The selected {@link Animal} reference, or {@code null}.
     */
    public Animal getSelectedAnimal() {
        return selectedAnimal;
    }
}