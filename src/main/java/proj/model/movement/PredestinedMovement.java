package proj.model.movement;

import proj.model.elements.Animal;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

/**
 * Implements the default "Predestined" movement variant.
 * This variant represents deterministic movement where the animal's next potential position
 * and direction are solely determined by its current state and the next gene in its sequence,
 * without any random modifications or external factors (other than map boundaries handled later).
 * Extends {@link AbstractMovementVariant}.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class PredestinedMovement extends AbstractMovementVariant {

    /**
     * Implements the movement modification step for the Predestined variant.
     * This variant does not introduce any modifications; it simply passes through the
     * calculated potential new position and direction. The final position will still be
     * subject to boundary validation by the map.
     *
     * @param animal                The {@link Animal} attempting to move.
     * @param oldPosition           The animal's position *before* this movement step.
     * @param potentialNewPosition  The calculated position *after* applying genotype rotation.
     * @param oldDirection          The animal's direction *before* applying genotype rotation.
     * @param potentialNewDirection The calculated direction *after* applying genotype rotation.
     * @return A {@link PositionDirectionTuple} containing the unmodified `potentialNewPosition` and `potentialNewDirection`.
     */
    @Override
    public PositionDirectionTuple movementModification(Animal animal, Vector2d oldPosition, Vector2d potentialNewPosition, MapDirection oldDirection, MapDirection potentialNewDirection) {
        // No modification applied in this variant
        return new PositionDirectionTuple(potentialNewPosition, potentialNewDirection);
    }
}