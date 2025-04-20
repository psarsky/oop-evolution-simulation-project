package proj.model.movement;

import proj.model.elements.Animal;
import proj.model.maps.MoveValidator;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;
import java.util.Objects;

/**
 * Abstract base class implementing the {@link Movement} interface.
 * Provides the core logic for calculating an animal's next potential move based on its genotype
 * and current direction. Subclasses must implement {@link #movementModification} to apply
 * specific variant behaviors (e.g., age-based effects, randomness) before final position
 * correction by the {@link MoveValidator}.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public abstract class AbstractMovementVariant implements Movement {

    /**
     * Calculates and applies an animal's movement for one simulation step.
     * 1. Determines the rotation based on the next gene in the animal's sequence.
     * 2. Calculates the potential new direction and position.
     * 3. Calls {@link #movementModification} to allow subclasses to alter the move (e.g., skip turn).
     * 4. Uses the {@link MoveValidator} (map) to correct the final position and direction based on boundaries and obstacles.
     * 5. Updates the animal's internal state (position, direction, gene index, energy, age) via `animal.move()`.
     *
     * @param animal    The non-null {@link Animal} to move.
     * @param validator A non-null {@link MoveValidator} (typically the map instance) used to correct the final position.
     */
    @Override
    public void move(Animal animal, MoveValidator validator) {
        Objects.requireNonNull(animal, "Animal cannot be null");
        Objects.requireNonNull(validator, "MoveValidator cannot be null");

        int[] genes = animal.getGenes(); // Get genes (might be a copy)
        if (genes.length == 0) {
            System.err.println("Warning: Animal " + animal.getId() + " has zero genes. Cannot determine move.");
            // Animal stays in place, potentially still ages/loses energy?
            // Update age/energy without changing position/direction.
            animal.move(new PositionDirectionTuple(animal.getPos(), animal.getDirection()));
            return;
        }

        // 1. Determine rotation from next gene
        // Use floorMod for correct wrapping with potentially negative indices if logic changes
        int nextGeneIndex = Math.floorMod(animal.getActiveGeneIndex() + 1, genes.length);
        int rotationAngle = genes[nextGeneIndex]; // 0-7 represents rotation steps

        // 2. Calculate potential new direction and position
        MapDirection currentDirection = animal.getDirection();
        MapDirection rotatedDirection = currentDirection.rotate(rotationAngle);
        Vector2d currentPosition = animal.getPos();
        Vector2d potentialNewPosition = currentPosition.add(rotatedDirection.toUnitVector());

        // 3. Apply variant-specific modification (e.g., OldAgeAintNoPicnic might return old position/direction)
        PositionDirectionTuple modifiedMove = movementModification(
                animal,
                currentPosition,
                potentialNewPosition,
                currentDirection, // Pass original direction before rotation
                rotatedDirection  // Pass direction after rotation
        );

        // 4. Validate and correct the modified move using the map rules
        PositionDirectionTuple finalMove = validator.correctPosition(
                currentPosition,           // Pass the position *before* any potential move
                modifiedMove.position(),   // The position resulting from the modification
                modifiedMove.direction()   // The direction resulting from the modification
        );

        // 5. Update animal state: Set new gene index and apply final move results
        animal.setGeneIndex(nextGeneIndex); // Update active gene index
        animal.move(finalMove);             // Updates position, direction, age, energy
    }

    /**
     * Abstract method to be implemented by concrete movement variants.
     * Allows modification of the calculated move (potential new position and direction)
     * based on the specific variant's rules (e.g., randomness, age effects) before
     * boundary validation occurs.
     *
     * @param animal            The {@link Animal} attempting to move.
     * @param oldPosition       The animal's position *before* this movement step.
     * @param potentialNewPosition The calculated position *after* applying genotype rotation, before modification and validation.
     * @param oldDirection      The animal's direction *before* applying genotype rotation.
     * @param potentialNewDirection The calculated direction *after* applying genotype rotation, before modification.
     * @return A {@link PositionDirectionTuple} representing the potentially modified position and direction.
     *         This might be the same as `potentialNewPosition/Direction`, or it could be `oldPosition/Direction`
     *         if the variant decides the animal should not move this turn.
     */
    public abstract PositionDirectionTuple movementModification(Animal animal, Vector2d oldPosition, Vector2d potentialNewPosition, MapDirection oldDirection, MapDirection potentialNewDirection);
}