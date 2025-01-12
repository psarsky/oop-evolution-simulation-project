package proj.util;

/**
 * Represents a tuple that combines a position and a direction.
 * Used to associate a 2D position with a specific direction.
 *
 * @param position          The position in 2D space (Vector2d)
 * @param direction         The direction associated with the position (MapDirection)
 */
public record PositionDirectionTuple(Vector2d position, MapDirection direction) {
}