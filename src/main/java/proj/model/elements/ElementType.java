package proj.model.elements;

/**
 * Enum representing different types of elements that can exist in the simulation.
 * These elements are used to classify objects on the map.
 */
public enum ElementType {
    /**
     * ANIMAL:
     *
     * Represents an animal element. Animals are dynamic entities in the simulation
     * that can move, reproduce, and interact with other elements.
     */
    ANIMAL,

    /**
     * PLANT:
     *
     * Represents a plant element. Plants are static entities that can be consumed
     * by animals to restore energy.
     */
    PLANT,

    /**
     * WATER:
     *
     * Represents a water element. Water serves as an environmental feature
     * or potential obstacle in the simulation.
     */
    WATER
}