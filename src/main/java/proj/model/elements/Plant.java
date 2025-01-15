/*
todo:
add JavaFX display properties
*/

package proj.model.elements;

import proj.util.Vector2d;

/**
 * Represents a plant element in the simulation.
 * Plants serve as a food source for animals and occupy a specific position on the map.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class Plant extends AbstractInanimateElement {

    /**
     * Constructs a {@code Plant} at the specified position.
     *
     * @param position The position of the plant on the map as a {@link Vector2d} object.
     */
    public Plant(Vector2d position) {super(position);}

    /**
     * Provides a string representation of the plant for console display purposes.
     * The default symbol is '*', but this can be customized during testing
     * to reduce visual clutter.
     *
     * @return The {@link String} representation of the plant.
     */
    @Override
    public String toString() {
        return "*";
        // return " ";
    }

    @Override
    public ElementType getElementType() {return ElementType.PLANT;}
}
