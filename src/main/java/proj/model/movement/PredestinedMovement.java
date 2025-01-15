package proj.model.movement;

import proj.model.elements.Animal;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

/**
 * Default movement variant which represents a deterministic movement pattern,
 * where the path or behavior is predefined and does not change.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class PredestinedMovement extends AbstractMovementVariant {

    /**
     * Default variant - does not modify the move in any way.
     *
     * @param animal        The {@link Animal} to move on the map.
     * @param oldPosition   A {@link Vector2d} object representing the animal's initial map position.
     * @param newPosition   A {@link Vector2d} object representing the animal's potential new map position.
     * @param oldDirection  A {@link MapDirection} object representing the animal's initial direction.
     * @param newDirection  A {@link MapDirection} object representing the animal's potential new direction.
     * @return              A {@link PositionDirectionTuple} representing the animal's final position and direction.
     */
    @Override
    public PositionDirectionTuple movementModification(Animal animal, Vector2d oldPosition, Vector2d newPosition, MapDirection oldDirection, MapDirection newDirection) {
        return new PositionDirectionTuple(newPosition, newDirection);
    }
}
