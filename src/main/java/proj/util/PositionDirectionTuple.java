package proj.util;

import java.util.Objects;

/**
 * An immutable record representing a paired position ({@link Vector2d}) and direction ({@link MapDirection}).
 * Commonly used to return the result of a movement calculation or position correction.
 *
 * @param position  The non-null {@link Vector2d} representing the position component.
 * @param direction The non-null {@link MapDirection} representing the direction component.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public record PositionDirectionTuple(Vector2d position, MapDirection direction) {
    /**
     * Canonical constructor for the PositionDirectionTuple record.
     * Ensures that both the position and direction components are non-null.
     *
     * @param position The position vector.
     * @param direction The map direction.
     * @throws NullPointerException if position or direction is null.
     */
    public PositionDirectionTuple {
        Objects.requireNonNull(position, "Position vector cannot be null");
        Objects.requireNonNull(direction, "MapDirection cannot be null");
    }

    // Records automatically provide immutable fields, getters, equals(), hashCode(), and toString().
    // No need for explicit implementations unless custom behavior is required.

    /* Example of default toString() output:
     * PositionDirectionTuple[position=Vector2d[x=5, y=10], direction=NORTH]
     */
}