package proj.model.movement;

import proj.model.elements.Animal;
import proj.model.maps.MoveValidator;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

/**
 * Represents an abstrat movement variation applied to animal movement.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public abstract class AbstractMovementVariant implements Movement {

    /**
     * Defines the default movement logic - animals cycle throught their genes,
     * activating the next gene in the array every day.
     *
     * @param animal    The {@link Animal} to move on the map.
     * @param validator A {@link MoveValidator} object that verifies the validity of the potential new position.
     */
    @Override
    public void move(Animal animal, MoveValidator validator) {
        int[] genes = animal.getGenes();
        int newGeneIndex = (animal.getGeneIndex() + 1) % genes.length;
        int rotationAngle = genes[newGeneIndex];

        MapDirection newDirection = animal.getDir().rotate(rotationAngle);
        Vector2d newPosition = animal.getPos().add(newDirection.toUnitVector());

        PositionDirectionTuple newPositionDirection = movementModification(animal, animal.getPos(), newPosition, animal.getDir(), newDirection);

        animal.setGeneIndex(newGeneIndex);
        newPositionDirection = validator.correctPosition(animal.getPos(), newPositionDirection.position(), newPositionDirection.direction());
        animal.move(newPositionDirection);
    }

    /**
     * Modifies the animal's movement basing on the selected movement variant.
     *
     * @param animal        The {@link Animal} to move on the map.
     * @param oldPosition   A {@link Vector2d} object representing the animal's initial map position.
     * @param newPosition   A {@link Vector2d} object representing the animal's potential new map position.
     * @param oldDirection  A {@link MapDirection} object representing the animal's initial direction.
     * @param newDirection  A {@link MapDirection} object representing the animal's potential new direction.
     * @return              A {@link PositionDirectionTuple} representing the animal's final position and direction.
     */
    public abstract PositionDirectionTuple movementModification(Animal animal, Vector2d oldPosition, Vector2d newPosition, MapDirection oldDirection, MapDirection newDirection);
}
