package proj.model.vegetation;

import proj.util.Vector2d;
import java.util.Random;

/**
 * Abstract base class for defining vegetation spawning variants or strategies.
 * It implements the {@link PlantPositionValidator} interface by combining a
 * preference logic (defined by subclasses in {@link #isPreferred}) with a degree of randomness.
 * This allows for areas with higher or lower likelihood of plant growth.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public abstract class AbstractVegetationVariant implements PlantPositionValidator {

    private static final Random random = new Random();
    // Constants for validation probability (80% chance if preferred, 20% chance if not preferred)
    private static final int PREFERRED_ROLL_TARGET = 4; // Needs a roll of 0, 1, 2, 3 (4 out of 5)
    private static final int NON_PREFERRED_ROLL_TARGET = 4; // Needs a roll of 4 (1 out of 5)
    private static final int RANDOM_BOUND = 5;

    /**
     * Validates if a plant can potentially grow at the given position based on the variant's rules.
     * It checks if the position is considered "preferred" by the specific variant implementation
     * (using {@link #isPreferred}) and then applies a probabilistic check:
     * - Preferred positions have an 80% chance of being valid.
     * - Non-preferred positions have a 20% chance of being valid.
     * This allows plants to occasionally grow outside preferred zones and fail to grow inside them.
     *
     * @param position A non-null {@link Vector2d} representing the position to validate.
     * @return {@code true} if the position is considered valid for planting based on preference and random chance, {@code false} otherwise.
     */
    @Override
    public boolean validatePlantPosition(Vector2d position) {
        boolean preferred = isPreferred(position);
        int roll = random.nextInt(RANDOM_BOUND); // Generates a number from 0 to 4

        if (preferred) {
            // Valid if roll is 0, 1, 2, or 3 (4/5 = 80%)
            return roll < PREFERRED_ROLL_TARGET;
        } else {
            // Valid if roll is exactly 4 (1/5 = 20%)
            return roll == NON_PREFERRED_ROLL_TARGET;
        }
        // Equivalent logic: return preferred == (roll != 4); // Valid if preferred AND roll isn't 4, OR if not preferred AND roll IS 4.
    }

    /**
     * Abstract method to be implemented by concrete vegetation variants.
     * Determines if a given map position is considered a "preferred" location for
     * vegetation growth according to the rules of the specific variant (e.g., inside an equator zone).
     *
     * @param position A non-null {@link Vector2d} representing the position to check.
     * @return {@code true} if the position is preferred for growth, {@code false} otherwise.
     */
    abstract boolean isPreferred(Vector2d position);

    /**
     * Abstract method potentially used to classify a map field based on the vegetation variant's logic
     * (e.g., "Equator", "Non-Equator", "Fertile", "Barren").
     * This might be useful for visualization or other game logic.
     *
     * @param position A non-null {@link Vector2d} representing the position of the field to classify.
     * @return A {@link String} describing the field type according to this vegetation variant.
     */
    abstract String getFieldType(Vector2d position);
}