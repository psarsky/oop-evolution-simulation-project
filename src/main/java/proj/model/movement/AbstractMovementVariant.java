package proj.model.movement;

import proj.model.elements.Animal;
import proj.model.maps.MoveValidator;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

public abstract class AbstractMovementVariant implements Movement {
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

    public abstract PositionDirectionTuple movementModification(Animal animal, Vector2d oldPosition, Vector2d newPosition, MapDirection oldDirection, MapDirection newDirection);
}
