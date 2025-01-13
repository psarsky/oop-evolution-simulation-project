package proj.model.elements;

import proj.util.Vector2d;

/**
 * Represents an inanimate object in the simulation (water, plant etc.).
 *
 */
public abstract class InanimateElement implements WorldElement {
    private final Vector2d position; // The position of the water element on the map

    /**
     * Constructs an inanimate element at the specified position.
     *
     * @param position          The position of the element on the map
     */
    public InanimateElement(Vector2d position) {this.position = position;}

    /**
     * Retrieves the position of the element on the map.
     *
     * @return          The position of the element as a {@link Vector2d} object
     */
    @Override
    public Vector2d getPos() {return this.position;}
}
