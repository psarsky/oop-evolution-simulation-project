package proj.model.maps;

import proj.model.movement.AbstractMovementVariant;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.Vector2d;
// No other specific imports needed

/**
 * Represents a world map with a "globe" topology.
 * This map type allows entities to wrap around horizontally (moving off the left edge
 * reappears on the right, and vice-versa). Vertical movement is bounded by the top
 * and bottom edges, typically causing entities to "bounce" back or change direction
 * (as defined in {@link AbstractWorldMap#correctPosition(Vector2d, Vector2d, MapDirection)}).
 * Inherits most functionality from {@link AbstractWorldMap} and ensures the
 * initial free positions are calculated correctly after the base map is constructed.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class Globe extends AbstractWorldMap {

    /**
     * Constructs a {@code Globe} map instance. Calls the base constructor
     * and then explicitly initializes the free positions based on the initial (empty) state,
     * ensuring the list is correctly populated for this map type.
     *
     * @param simulationProperties Simulation parameters (dimensions, etc.). Must not be null.
     * @param vegetation           Vegetation spawning rules. Must not be null.
     * @param movement             Animal movement rules. Must not be null.
     */
    public Globe(SimulationProperties simulationProperties, AbstractVegetationVariant vegetation, AbstractMovementVariant movement) {
        super(simulationProperties, vegetation, movement); // Call base constructor first
        // Explicitly initialize free positions AFTER base constructor is done
        initializeFreePositions(); // Calculate initial free spots for this specific map type
    }

    // No methods need to be overridden for the basic Globe functionality defined
    // in AbstractWorldMap, as its default boundary handling already implements
    // the horizontal wrapping and vertical bouncing behavior.
}