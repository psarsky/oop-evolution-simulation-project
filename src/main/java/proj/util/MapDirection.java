package proj.util;

import java.util.Random;

/**
 * Represents the eight cardinal and intercardinal compass directions (North, Northeast, East, etc.).
 * Provides methods for rotating directions, finding opposites, converting to unit vectors for movement,
 * and selecting random directions. This enum is immutable.
 */
public enum MapDirection {
    NORTH,      // Index 0
    NORTHEAST,  // Index 1
    EAST,       // Index 2
    SOUTHEAST,  // Index 3
    SOUTH,      // Index 4
    SOUTHWEST,  // Index 5
    WEST,       // Index 6
    NORTHWEST;  // Index 7

    private static final MapDirection[] VALUES = values(); // Cache values() for performance
    private static final Random random = new Random();

    /**
     * Returns the direction that is 45 degrees clockwise from the current direction.
     * Wraps around from NORTHWEST to NORTH.
     *
     * @return The next {@link MapDirection} in clockwise order.
     */
    public MapDirection next() {
        int nextOrdinal = (this.ordinal() + 1) % VALUES.length;
        return VALUES[nextOrdinal];
    }

    /**
     * Returns the direction that is 45 degrees counter-clockwise from the current direction.
     * Wraps around from NORTH to NORTHWEST.
     *
     * @return The previous {@link MapDirection} in counter-clockwise order.
     */
    public MapDirection previous() {
        int prevOrdinal = (this.ordinal() - 1 + VALUES.length) % VALUES.length; // Use modulo for wrap-around
        return VALUES[prevOrdinal];
    }


    /**
     * Returns the direction directly opposite to the current direction (180 degrees rotation).
     * For example, NORTH.opposite() returns SOUTH.
     *
     * @return The opposite {@link MapDirection}.
     */
    public MapDirection opposite() {
        int oppositeOrdinal = (this.ordinal() + VALUES.length / 2) % VALUES.length; // Add 4 (half the directions)
        return VALUES[oppositeOrdinal];
        /* Alternative implementation using switch (less flexible if adding directions)
        return switch (this) {
            case NORTH      -> SOUTH;
            case NORTHEAST  -> SOUTHWEST;
            case EAST       -> WEST;
            case SOUTHEAST  -> NORTHWEST;
            case SOUTH      -> NORTH;
            case SOUTHWEST  -> NORTHEAST;
            case WEST       -> EAST;
            case NORTHWEST  -> SOUTHEAST;
        };
        */
    }


    /**
     * Converts this direction into a unit {@link Vector2d} representing the change
     * in (x, y) coordinates for one step in this direction.
     * Example: NORTH -> (0, 1), NORTHEAST -> (1, 1), EAST -> (1, 0).
     * Assumes Y increases upwards on the map model.
     *
     * @return A {@link Vector2d} representing the unit vector for this direction.
     */
    public Vector2d toUnitVector() {
        return switch (this) {
            // Assumes standard Cartesian coordinates where Y increases upwards
            case NORTH      -> new Vector2d( 0,  1);
            case NORTHEAST  -> new Vector2d( 1,  1);
            case EAST       -> new Vector2d( 1,  0);
            case SOUTHEAST  -> new Vector2d( 1, -1);
            case SOUTH      -> new Vector2d( 0, -1);
            case SOUTHWEST  -> new Vector2d(-1, -1);
            case WEST       -> new Vector2d(-1,  0);
            case NORTHWEST  -> new Vector2d(-1,  1);
        };
    }

    /**
     * Returns a randomly selected {@link MapDirection} from the eight possibilities
     * with uniform probability.
     *
     * @return A random {@link MapDirection}.
     */
    public static MapDirection getRandomDirection() {
        return VALUES[random.nextInt(VALUES.length)]; // Select random from cached array
    }

    /**
     * Rotates the current direction clockwise by the specified number of 45-degree steps.
     * A positive angle rotates clockwise, a negative angle rotates counter-clockwise.
     *
     * @param angle The number of 45-degree steps to rotate (can be positive or negative).
     * @return The new {@link MapDirection} after rotation.
     */
    public MapDirection rotate(int angle) {
        // Use floorMod to handle negative angles correctly for counter-clockwise rotation
        int newOrdinal = Math.floorMod(this.ordinal() + angle, VALUES.length);
        return VALUES[newOrdinal];
        /* Equivalent iterative approach:
        MapDirection result = this;
        if (angle > 0) {
            for (int i = 0; i < angle; i++) {
                result = result.next();
            }
        } else {
             for (int i = 0; i < -angle; i++) { // Iterate for counter-clockwise
                 result = result.previous();
             }
        }
        return result;
        */
    }

    /**
     * Provides a short string representation of the direction (e.g., "N", "NE", "E").
     *
     * @return The shorthand string representation of the direction.
     */
    @Override
    public String toString() {
        return switch (this) {
            case NORTH      -> "N";
            case NORTHEAST  -> "NE";
            case EAST       -> "E";
            case SOUTHEAST  -> "SE";
            case SOUTH      -> "S";
            case SOUTHWEST  -> "SW";
            case WEST       -> "W";
            case NORTHWEST  -> "NW";
        };
    }
}