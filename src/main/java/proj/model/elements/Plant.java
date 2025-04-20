package proj.model.elements;

import proj.util.Vector2d;

/**
 * Represents a Plant element in the simulation.
 * Plants serve as a static food source for {@link Animal}s and occupy a specific
 * position on the map until consumed or removed.
 * Inherits position handling from {@link AbstractInanimateElement}.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class Plant extends AbstractInanimateElement {

    /**
     * Constructs a {@code Plant} at the specified position.
     *
     * @param position The non-null {@link Vector2d} position of the plant on the map.
     */
    public Plant(Vector2d position) {
        super(position); // Calls constructor of AbstractInanimateElement
    }

    /**
     * {@inheritDoc}
     * Returns the element type, which is always {@link ElementType#PLANT}.
     * @return {@link ElementType#PLANT}.
     */
    @Override
    public ElementType getElementType() {
        return ElementType.PLANT;
    }

    /**
     * Provides a string representation of the plant for console display purposes.
     * The default symbol is '*'.
     *
     * @return The {@link String} representation ("*").
     */
    @Override
    public String toString() {
        return "*";
        // return "P"; // Alternative representation
    }
}