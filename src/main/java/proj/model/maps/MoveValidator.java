package proj.model.maps;

import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

public interface MoveValidator {
    PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction);
}
