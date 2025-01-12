package proj.model.vegetation;

import proj.util.Vector2d;

import java.util.Random;

/**
 * Abstract class representing a general vegetation variant.
 * Provides a structure for determining preferred plant positions
 * and includes a mechanism for validating plant placement with randomness.
 */
public abstract class AbstractVegetationVariant implements Vegetation {
    private final Random random = new Random(); // Random generator for introducing variability in plant placement

    public boolean validatePlantPosition(Vector2d position) {
        // The position is valid if it is preferred, with a 4/5 probability of success,
        // or if it is not preferred, with a 1/5 probability.
        return preferred(position) == (this.random.nextInt(5) != 4);
    }
}