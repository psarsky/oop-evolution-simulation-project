/*
todo:
add JavaFX display properties
*/

package proj.model.elements;

import proj.util.Vector2d;

/**
 * Represents a water element in the simulation.
 * Water may serve as an environmental feature or obstacle in the simulation,
 * occupying a specific position on the map.
 */
public class Water implements WorldElement {
    private final Vector2d position; // The position of the water element on the map

    /**
     * Constructs a water element at the specified position.
     *
     * @param position          The position of the water element on the map
     */
    public Water(Vector2d position) {
        this.position = position;
    }

    /**
     * Provides a string representation of the water element for display purposes.
     * The default symbol is '~', but this can be customized during testing
     * to reduce visual clutter.
     *
     * @return          The string representation of the water element
     */
    @Override
    public String toString() {
        return "~";
        // return " "; // for testing purposes - less visual clutter
    }

    /**
     * Retrieves the position of the water element on the map.
     *
     * @return          The position of the water element as a {@link Vector2d} object
     */
    @Override
    public Vector2d getPos() {
        return this.position;
    }
}