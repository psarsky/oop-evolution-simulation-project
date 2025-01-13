/*
todo:
add JavaFX display properties
*/

package proj.model.elements;

import proj.util.Vector2d;

/**
 * Represents a plant element in the simulation.
 * Plants serve as a food source for animals and occupy a specific position on the map.
 */
public class Plant extends InanimateElement {
    /**
     * Constructs a plant at the specified position.
     *
     * @param position          The position of the plant on the map
     */
    public Plant(Vector2d position) {super(position);}

    /**
     * Provides a string representation of the plant for display purposes.
     * The default symbol is '*', but this can be customized during testing
     * to reduce visual clutter.
     *
     * @return          The string representation of the plant
     */
    @Override
    public String toString() {
        return "*";
        // return " "; // for testing purposes - less visual clutter
    }

    @Override
    public ElementType getElementType() {return ElementType.PLANT;}
}
