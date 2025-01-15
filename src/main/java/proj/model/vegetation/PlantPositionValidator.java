package proj.model.vegetation;

import proj.util.Vector2d;

/**
 * Interface representing a vegetation variant that is utilized while generating new plants that grow on the map.
 * Implementations of this interface define specific ways to manage plant growth.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public interface PlantPositionValidator {
    /**
     * Validates whether a given position is suitable for a new plant to grow on.
     *
     * @param position  A {@link Vector2d} object representing the position to validate.
     * @return          {@code true} if the position is valid for planting, {@code false} otherwise.
     */
    boolean validatePlantPosition(Vector2d position);
}
