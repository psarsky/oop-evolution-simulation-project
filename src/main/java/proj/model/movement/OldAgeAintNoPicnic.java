package proj.model.movement;

import proj.model.elements.Animal;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.Random;

/**
 * This movement variant introduces age-dependent movement behavior.
 * As the entity ages, the probability of skipping a movement on a given day increases,
 * reaching a maximum skip chance of 80%.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class OldAgeAintNoPicnic extends AbstractMovementVariant {
    private static final Random random = new Random();

    /**
     * Modifies the animal's movement basing its age.
     * The probability of skipping a move begins at 0% when the animal is between 0 and 10 days old
     * and gradually increases to 80% by the time it reaches 20 days old.
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
        return random.nextInt(100) > Math.min((animal.getAge() - 10) * 8, 80)
                ? new PositionDirectionTuple(newPosition, newDirection)
                : new PositionDirectionTuple(oldPosition, oldDirection);
    }
}
