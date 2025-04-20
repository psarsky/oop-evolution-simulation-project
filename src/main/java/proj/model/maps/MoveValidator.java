package proj.model.maps;

import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

/**
 * Interface defining a contract for validating and correcting entity movement
 * based on the map's boundaries and rules. Implementations (typically within
 * {@link AbstractWorldMap} subclasses) ensure entities stay within valid map areas
 * or handle boundary interactions like wrapping or bouncing.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public interface MoveValidator {

    /**
     * Calculates the corrected final position and potentially adjusted direction
     * for an entity attempting to move to a target position. This method enforces
     * map boundaries and handles specific boundary interactions (wrapping, bouncing, etc.).
     *
     * @param oldPosition   The entity's valid position *before* the attempted move.
     * @param newPosition   The calculated position *after* applying movement vector, which might be outside map bounds or in invalid terrain.
     * @param direction     The intended direction of movement leading to `newPosition`.
     * @return A non-null {@link PositionDirectionTuple} containing the final, valid position
     *         on the map and the potentially adjusted {@link MapDirection} after boundary interaction.
     */
    PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction);
}