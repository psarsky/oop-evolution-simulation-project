package proj.model.movement;

import proj.model.elements.Animal;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

public class PredestinedMovement extends AbstractMovementVariant {
    @Override
    public PositionDirectionTuple movementModification(Animal animal, Vector2d oldPosition, Vector2d newPosition, MapDirection oldDirection, MapDirection newDirection) {
        return new PositionDirectionTuple(newPosition, newDirection);
    }
}
