package proj.model.movement;

import proj.model.elements.Animal;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.Random;

/**
 * Implements the "Old Age Ain't No Picnic" movement variant.
 * In this variant, an animal's probability of *not* moving (staying in its current position
 * and keeping its current direction for the step) increases with age. The chance to skip
 * starts potentially after a certain age and increases up to a maximum percentage.
 * Extends {@link AbstractMovementVariant}.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class OldAgeAintNoPicnic extends AbstractMovementVariant {
    private static final Random random = new Random();
    private static final int AGE_THRESHOLD_START = 10; // Age (days) when skip chance starts increasing
    private static final int AGE_THRESHOLD_MAX_CHANCE = 30; // Age (days) when max skip chance is reached
    private static final double MAX_SKIP_CHANCE_PERCENT = 80.0; // Maximum probability (0-100) of skipping move

    /**
     * Modifies the potential movement based on the animal's age.
     * Calculates a probability of skipping the move (staying put) which increases linearly
     * from 0% at age {@code AGE_THRESHOLD_START} up to {@code MAX_SKIP_CHANCE_PERCENT}%
     * at age {@code AGE_THRESHOLD_MAX_CHANCE} and beyond. If a random check determines the
     * animal should skip, the original position and direction are returned; otherwise, the
     * calculated potential new position and direction are returned.
     *
     * @param animal                The {@link Animal} attempting to move.
     * @param oldPosition           The animal's position *before* this movement step.
     * @param potentialNewPosition  The calculated position *after* applying genotype rotation.
     * @param oldDirection          The animal's direction *before* applying genotype rotation (used if skipping move).
     * @param potentialNewDirection The calculated direction *after* applying genotype rotation.
     * @return A {@link PositionDirectionTuple} representing either the `potentialNewPosition/Direction` or the `oldPosition/Direction` if the move is skipped.
     */
    @Override
    public PositionDirectionTuple movementModification(Animal animal, Vector2d oldPosition, Vector2d potentialNewPosition, MapDirection oldDirection, MapDirection potentialNewDirection) {
        int age = animal.getAge();
        double skipChance = 0.0;

        if (age > AGE_THRESHOLD_START) {
            // Calculate progression factor (0.0 to 1.0) between start and max age thresholds
            double ageProgress = (double) Math.max(0, age - AGE_THRESHOLD_START) /
                    Math.max(1, AGE_THRESHOLD_MAX_CHANCE - AGE_THRESHOLD_START); // Avoid div by zero
            // Clamp progress to 1.0 if age exceeds max threshold
            ageProgress = Math.min(1.0, ageProgress);
            // Linearly interpolate skip chance
            skipChance = ageProgress * MAX_SKIP_CHANCE_PERCENT;
        }

        // Perform random check against skip chance
        boolean skipMove = random.nextDouble() * 100.0 < skipChance; // Check if random double (0-100) is less than chance

        if (skipMove) {
            // Skip the move: return current position and the direction *before* rotation might be more intuitive?
            // Or keep the potential new direction but stay at old position? Let's return old pos/dir.
            return new PositionDirectionTuple(oldPosition, oldDirection);
        } else {
            // Don't skip: return the calculated potential new position and direction
            return new PositionDirectionTuple(potentialNewPosition, potentialNewDirection);
        }
    }
}