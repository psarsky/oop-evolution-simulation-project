package proj.model.elements;

/**
 * Enumeration defining the distinct types of elements that can exist
 * within the simulation world map. Used for identification and classification.
 */
public enum ElementType {
    /**
     * Represents an Animal element. Animals are dynamic entities capable of
     * movement, interaction, reproduction, etc.
     */
    ANIMAL,

    /**
     * Represents a Plant element. Plants are typically static food sources
     * consumed by animals.
     */
    PLANT,

    /**
     * Represents a Water element. Water acts as an environmental feature,
     * potentially an obstacle or hazard depending on the map type (e.g., {@link proj.model.maps.WaterWorld}).
     */
    WATER

    // Add other types as needed (e.g., OBSTACLE, TERRAIN_FEATURE)
}