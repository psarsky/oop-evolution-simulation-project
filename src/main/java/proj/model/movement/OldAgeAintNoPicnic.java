package proj.model.movement;

import proj.model.elements.Animal;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.Random;

public class OldAgeAintNoPicnic extends AbstractMovementVariant {
    private static final Random random = new Random();

    @Override
    public PositionDirectionTuple movementModification(Animal animal, Vector2d oldPosition, Vector2d newPosition, MapDirection oldDirection, MapDirection newDirection) {
        // The probability of skipping a move begins at 0% when the animal is 10 days old and gradually increases to 80% by the time it reaches 20 days old.
        return random.nextInt(100) > Math.min((animal.getAge() - 10) * 8, 80)
                ? new PositionDirectionTuple(newPosition, newDirection)
                : new PositionDirectionTuple(oldPosition, oldDirection);
    }

}
