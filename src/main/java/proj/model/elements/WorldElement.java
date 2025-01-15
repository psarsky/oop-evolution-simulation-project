package proj.model.elements;

import proj.util.Vector2d;

/**
 * Interface representing a general element in the simulation world.
 * All elements in the simulation, such as animals, plants, and water,
 * should implement this interface to provide their position.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public interface WorldElement {

    /**
     * Returns the position of the world element on the map.
     *
     * @return The position of the element as a {@link Vector2d} object.
     */
    Vector2d getPos();

    /**
     * Gets the type of the world element.
     *
     * @return The element's type as an {@link ElementType} value.
     */
    ElementType getElementType();
}
