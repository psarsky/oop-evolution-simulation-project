package proj.model.elements;

import proj.util.Vector2d;

/**
 * Interface representing any element that can exist at a position
 * within the simulation world map (e.g., {@link Animal}, {@link Plant}, {@link Water}).
 * Requires implementing elements to provide their position and type.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public interface WorldElement {

    /**
     * Returns the current position of the world element on the map.
     *
     * @return The position as a non-null {@link Vector2d} object.
     */
    Vector2d getPos();

    /**
     * Returns the specific type of this world element.
     *
     * @return The element's type as an {@link ElementType} enum value.
     */
    ElementType getElementType();
}