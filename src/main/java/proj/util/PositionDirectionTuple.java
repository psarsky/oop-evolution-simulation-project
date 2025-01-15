package proj.util;

/**
 * Represents a tuple that combines a position and a direction.
 * Used to associate a 2D position with a specific direction.
 *
 * @param position  A {@link Vector2d} object representing the position in 2D space.
 * @param direction A {@link MapDirection} object representing the direction.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public record PositionDirectionTuple(Vector2d position, MapDirection direction) {
}