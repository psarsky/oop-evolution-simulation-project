package proj.model.elements;

import proj.util.Vector2d;

/**
 * Represents an abstract inanimate object in the simulation (water, plant etc.).
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public abstract class AbstractInanimateElement implements WorldElement {
    private final Vector2d position; // The position of the element on the map

    /**
     * Constructs an {@code AbstractInanimateElement} at the specified position.
     *
     * @param position The position of the element on the map as a {@link Vector2d} object.
     */
    public AbstractInanimateElement(Vector2d position) {this.position = position;}

    /**
     * Retrieves the position of the element on the map.
     *
     * @return The position of the element as a {@link Vector2d} object.
     */
    @Override
    public Vector2d getPos() {return this.position;}
}
