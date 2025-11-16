package proj.model.movement;

import proj.model.elements.Animal;
import proj.model.maps.MoveValidator;

/**
 * Interface defining the contract for different animal movement strategies or variants.
 * Implementations encapsulate the logic for determining how an animal moves during a simulation step,
 * potentially considering factors like genotype, age, or random chance, before final validation by the map.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public interface Movement {

    /**
     * Calculates and applies the movement for a given animal for one simulation step,
     * according to the specific rules of this movement variant.
     * Implementations typically involve:
     * 1. Reading the animal's genotype/state.
     * 2. Calculating a potential move (new position/direction).
     * 3. Potentially modifying the move based on variant logic (e.g., randomness, age).
     * 4. Using the provided {@link MoveValidator} to ensure the final move respects map boundaries and obstacles.
     * 5. Updating the animal's state (position, direction, energy, age, active gene).
     *
     * @param animal    The non-null {@link Animal} instance to move.
     * @param validator A non-null {@link MoveValidator} (typically the map) used to correct the final position/direction.
     */
    void move(Animal animal, MoveValidator validator);
}