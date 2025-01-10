package proj.util;

import java.util.Random;

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

    public MapDirection next() {
        return switch(this) {
            case NORTH      -> NORTHEAST;
            case NORTHEAST  -> EAST;
            case EAST       -> SOUTHEAST;
            case SOUTHEAST  -> SOUTH;
            case SOUTH      -> SOUTHWEST;
            case SOUTHWEST  -> WEST;
            case WEST       -> NORTHWEST;
            case NORTHWEST  -> NORTH;
        };
    }

    public Vector2d toUnitVector() {
        return switch (this) {
            case NORTH      -> new Vector2d (0, 1);
            case NORTHEAST  -> new Vector2d (1, 1);
            case EAST       -> new Vector2d (1, 0);
            case SOUTHEAST  -> new Vector2d (1, -1);
            case SOUTH      -> new Vector2d (0, -1);
            case SOUTHWEST  -> new Vector2d (-1, -1);
            case WEST       -> new Vector2d (-1, 0);
            case NORTHWEST  -> new Vector2d (-1, 1);
        };
    }

    public static MapDirection getRandomDirection() {
        return switch(random.nextInt(8)) {
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

    public MapDirection rotate(int angle) {
        MapDirection result = this;
        for (int i = 0; i < angle; i++) {
            result = result.next();
        }
        return result;
    }

    @Override
    public String toString() {
        return switch(this) {
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