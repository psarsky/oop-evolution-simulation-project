package proj.model.elements;

import proj.util.Vector2d;
import java.util.Objects;

/**
 * Abstract base class for inanimate elements within the simulation world,
 * such as {@link Plant} or {@link Water}. Inanimate elements have a fixed position.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public abstract class AbstractInanimateElement implements WorldElement {
    private final Vector2d position; // The position of the element on the map

    /**
     * Constructs an {@code AbstractInanimateElement} at the specified position.
     *
     * @param position The position of the element on the map as a non-null {@link Vector2d} object.
     * @throws NullPointerException if position is null.
     */
    protected AbstractInanimateElement(Vector2d position) {
        this.position = Objects.requireNonNull(position, "Position cannot be null");
    }

    /**
     * {@inheritDoc}
     * Retrieves the fixed position of this inanimate element on the map.
     *
     * @return The position of the element as a {@link Vector2d} object.
     */
    @Override
    public Vector2d getPos() {
        return this.position;
    }

    // equals() and hashCode() based on position might be useful if storing these in Sets/Maps keyed by element
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractInanimateElement that = (AbstractInanimateElement) o;
        return position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }
}