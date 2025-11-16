package proj.model.elements;

import proj.util.Vector2d;

/**
 * Represents a Water element in the simulation.
 * Water typically occupies a fixed position (though it might spread/recede in specific map types like {@link proj.model.maps.WaterWorld})
 * and acts as an environmental feature or potential obstacle/hazard.
 * Inherits position handling from {@link AbstractInanimateElement}.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class Water extends AbstractInanimateElement {

    /**
     * Constructs a {@code Water} element at the specified position.
     *
     * @param position The non-null {@link Vector2d} position of the water element on the map.
     */
    public Water(Vector2d position) {
        super(position); // Calls constructor of AbstractInanimateElement
    }

    /**
     * {@inheritDoc}
     * Returns the element type, which is always {@link ElementType#WATER}.
     * @return {@link ElementType#WATER}.
     */
    @Override
    public ElementType getElementType() {
        return ElementType.WATER;
    }


    /**
     * Provides a string representation of the water element for console display purposes.
     * The default symbol is '~'.
     *
     * @return The {@link String} representation ("~").
     */
    @Override
    public String toString() {
        return "~";
        // return "W"; // Alternative representation
    }
}