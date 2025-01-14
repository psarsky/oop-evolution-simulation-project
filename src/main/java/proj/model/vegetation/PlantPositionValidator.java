package proj.model.vegetation;

import proj.util.Vector2d;

public interface PlantPositionValidator {
    /**
     * Validates whether a given position is suitable for planting vegetation.
     *
     * @param position      the position to validate
     * @return              true if the position is valid for planting, false otherwise
     */
    boolean validatePlantPosition(Vector2d position);
}
