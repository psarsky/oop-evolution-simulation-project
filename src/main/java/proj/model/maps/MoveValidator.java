package proj.model.maps;

import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

/**
 * Interface defining a contract for validating and correcting movement on a map.
 * Implementations of this interface define how positions and directions are adjusted
 * when entities attempt to move outside the map boundaries or encounter specific terrain features.
 */
public interface MoveValidator {

    /**
     * Corrects the position and direction of an entity based on the map's rules.
     * This method is typically used when an entity moves outside the map boundaries
     * or when movement needs to be adjusted due to special map constraints (e.g., water or wrapping edges).
     *
     * @param oldPosition       The entity's previous position before the move
     * @param newPosition       The intended new position after the move
     * @param direction         The intended direction of movement
     * @return                  A {@link PositionDirectionTuple} containing the corrected position and direction
     */
    PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction);
}
