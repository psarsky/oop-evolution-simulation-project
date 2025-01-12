package proj.util;

import java.util.Random;

/**
 * Represents the eight cardinal and intercardinal directions (e.g., NORTH, EAST, etc.)
 * and provides utility methods for directional calculations, such as determining
 * the next direction, the opposite direction, and unit vectors for movement.
 */
public enum MapDirection {
    NORTH,
    NORTHEAST,
    EAST,
    SOUTHEAST,
    SOUTH,
    SOUTHWEST,
    WEST,
    NORTHWEST;

    private static final Random random = new Random();

    /**
     * Returns the next direction in a clockwise order.
     * For example, NORTH -> NORTHEAST, NORTHEAST -> EAST, etc.
     *
     * @return          The next MapDirection in clockwise order
     */
    public MapDirection next() {
        return getMapDirection(
                NORTHEAST,
                EAST,
                SOUTHEAST,
                SOUTH,
                SOUTHWEST,
                WEST,
                NORTHWEST,
                NORTH
        );
    }

    /**
     * Returns the opposite direction of the current direction.
     * For example, NORTH -> SOUTH, EAST -> WEST, etc.
     *
     * @return          The opposite MapDirection
     */
    public MapDirection opposite() {
        return getMapDirection(
                SOUTH,
                SOUTHWEST,
                WEST,
                NORTHWEST,
                NORTH,
                NORTHEAST,
                EAST,
                SOUTHEAST
        );
    }

    /**
     * Helper method for retrieving a direction based on the current direction.
     * Used internally by methods such as `next` and `opposite`.
     *
     * @param mapDirection      The direction for NORTH
     * @param mapDirection2     The direction for NORTHEAST
     * @param mapDirection3     The direction for EAST
     * @param mapDirection4     The direction for SOUTHEAST
     * @param mapDirection5     The direction for SOUTH
     * @param mapDirection6     The direction for SOUTHWEST
     * @param mapDirection7     The direction for WEST
     * @param mapDirection8     The direction for NORTHWEST
     * @return                  The corresponding MapDirection
     */
    private MapDirection getMapDirection(
            MapDirection mapDirection,
            MapDirection mapDirection2,
            MapDirection mapDirection3,
            MapDirection mapDirection4,
            MapDirection mapDirection5,
            MapDirection mapDirection6,
            MapDirection mapDirection7,
            MapDirection mapDirection8
    ) {
        return switch (this) {
            case NORTH      -> mapDirection;
            case NORTHEAST  -> mapDirection2;
            case EAST       -> mapDirection3;
            case SOUTHEAST  -> mapDirection4;
            case SOUTH      -> mapDirection5;
            case SOUTHWEST  -> mapDirection6;
            case WEST       -> mapDirection7;
            case NORTHWEST  -> mapDirection8;
        };
    }

    /**
     * Converts the current direction into a unit vector for movement.
     * For example, NORTH -> (0, 1), EAST -> (1, 0), etc.
     *
     * @return          A Vector2d representing the unit vector for the current direction
     */
    public Vector2d toUnitVector() {
        return switch (this) {
            case NORTH      -> new Vector2d(0, 1);
            case NORTHEAST  -> new Vector2d(1, 1);
            case EAST       -> new Vector2d(1, 0);
            case SOUTHEAST  -> new Vector2d(1, -1);
            case SOUTH      -> new Vector2d(0, -1);
            case SOUTHWEST  -> new Vector2d(-1, -1);
            case WEST       -> new Vector2d(-1, 0);
            case NORTHWEST  -> new Vector2d(-1, 1);
        };
    }

    /**
     * Returns a random direction from the eight possible directions.
     *
     * @return          A randomly selected MapDirection
     */
    public static MapDirection getRandomDirection() {
        return switch (random.nextInt(8)) {
            case 1  -> NORTHEAST;
            case 2  -> EAST;
            case 3  -> SOUTHEAST;
            case 4  -> SOUTH;
            case 5  -> SOUTHWEST;
            case 6  -> WEST;
            case 7  -> NORTHWEST;
            default -> NORTH;
        };
    }

    /**
     * Rotates the current direction by a specified number of 45-degree steps clockwise.
     * For example, rotating NORTH by 2 steps results in EAST.
     *
     * @param angle         The number of 45-degree steps to rotate
     * @return              The new MapDirection after rotation
     */
    public MapDirection rotate(int angle) {
        MapDirection result = this;
        for (int i = 0; i < angle; i++) {
            result = result.next();
        }
        return result;
    }

    /**
     * Provides a string representation of the direction in shorthand notation.
     * For example, NORTH -> "N", EAST -> "E", etc.
     *
     * @return          The shorthand string representation of the direction
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