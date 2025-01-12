package proj.model.maps;

/**
 * Enum representing different types of map variants in the simulation.
 * Each variant defines a unique way in which the world map behaves and interacts with entities.
 */
public enum MapVariant {
    /**
     * GLOBE:
     *
     * Represents a map with a globe-like topology where horizontal wrapping occurs.
     * Animals that move beyond the left or right edge reappear on the opposite side,
     * while vertical movement is constrained by the top and bottom boundaries.
     */
    GLOBE,

    /**
     * WATER_WORLD:
     *
     * Represents a map with significant water coverage.
     * Water regions impose specific movement and interaction constraints,
     * such as restricting land-based entities
     * or draining energy from the animals that get covered by water during the water tide.
     */
    WATER_WORLD
}
