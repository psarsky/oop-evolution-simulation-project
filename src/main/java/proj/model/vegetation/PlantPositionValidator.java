package proj.model.vegetation;

import proj.util.Vector2d;

/**
 * Interface defining a contract for validating whether a given map position
 * is suitable for new plant growth according to specific rules or strategies
 * (e.g., proximity to water, terrain type, existing vegetation density).
 * Implemented by vegetation variants like {@link AbstractVegetationVariant}.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public interface PlantPositionValidator {

    /**
     * Validates if a plant could potentially grow at the specified position based on
     * the implementing strategy's rules. This doesn't necessarily guarantee growth,
     * as other factors (like random chance or existing elements) might interfere.
     *
     * @param position A non-null {@link Vector2d} representing the map position to validate.
     * @return {@code true} if the position is considered suitable for planting according to the strategy, {@code false} otherwise.
     */
    boolean validatePlantPosition(Vector2d position);
}