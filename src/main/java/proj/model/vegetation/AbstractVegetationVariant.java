package proj.model.vegetation;

import proj.util.Vector2d;

import java.util.Random;

/**
 * Abstract class representing a general vegetation variant.
 * Provides a structure for determining preferred plant positions
 * and includes a mechanism for validating plant placement with randomness.
 */
public abstract class AbstractVegetationVariant implements PlantPositionValidator {
    private final Random random = new Random(); // Random generator for introducing variability in plant placement
    /**
     * Combines the preferred position logic with a random factor to introduce variability.
     * A position is considered valid in the following cases:
     *  - If the position is preferred, it is valid with an 80% (4/5) probability.
     *  - If the position is not preferred, it can still be valid in 20% (1/5) cases,
     *  due to the comparison `false == false`.
     * This mechanism ensures some randomness in plant placement, even outside preferred areas.
     *
     * @param position      the position to validate
     * @return              true if the position is valid for planting, false otherwise
     */


    @Override
    public boolean validatePlantPosition(Vector2d position) {
        // The position is valid if it is preferred, with a 4/5 probability of success,
        // or if it is not preferred, with a 1/5 probability.
        return preferred(position) == (this.random.nextInt(5) != 4);
    }
    /**
     * Determines if a given position is preferred for vegetation growth.
     * Must be implemented by subclasses to define specific behavior for each variant.
     *
     * @param position      the position to check
     * @return              true if the position is preferred, false otherwise
     */
    abstract boolean preferred(Vector2d position);

    /**
     *
     * Returns the field type based on its position.
     *
     * @param position      the position of the field to check
     * @return              string containing field type description
     */
    abstract String getFieldType(Vector2d position);
}