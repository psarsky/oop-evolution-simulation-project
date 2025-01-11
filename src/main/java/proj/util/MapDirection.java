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
        return switch(this) {
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