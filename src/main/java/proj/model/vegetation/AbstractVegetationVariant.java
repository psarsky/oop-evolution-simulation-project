package proj.model.vegetation;

import proj.util.Vector2d;

import java.util.Random;

/**
 * Abstract class representing a general vegetation variant.
 * Provides a structure for determining preferred plant positions
 * and includes a mechanism for validating plant placement with randomness.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public abstract class AbstractVegetationVariant implements PlantPositionValidator {
    private final Random random = new Random();

    /**
     * Combines the preferred position logic with a random factor to introduce variability.
     * A position is considered valid in the following cases: </br>
     *  - If the position is preferred, it is valid with an 80% (4/5) probability. </br>
     *  - If the position is not preferred, it can still be valid in 20% (1/5) cases,
     *  due to the comparison `false == false`. </br>
     * This mechanism ensures some randomness in plant placement, even outside preferred areas.
     *
     * @param position  A {@link Vector2d} object representing the position to validate.
     * @return          {@code true} if the position is valid for planting, {@code false} otherwise.
     */
    @Override
    public boolean validatePlantPosition(Vector2d position) {
        return preferred(position) == (this.random.nextInt(5) != 4);
    }

    /**
     * Determines if a given position is preferred for vegetation growth.
     * Must be implemented by subclasses to define specific behavior for each variant.
     *
     * @param position  A {@link Vector2d} object representing the position to validate.
     * @return          {@code true} if the position is preferred, {@code false} otherwise.
     */
    abstract boolean preferred(Vector2d position);

    /**
     * Returns the field type based on its position.
     * Must be implemented by subclasses to define specific field types for each variant.
     *
     * @param position  A {@link Vector2d} object representing the position of the field to check.
     * @return          A {@link String} containing field type description.
     */
    abstract String getFieldType(Vector2d position);
}