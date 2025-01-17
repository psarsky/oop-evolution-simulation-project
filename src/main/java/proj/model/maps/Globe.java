package proj.model.maps;

import proj.model.movement.AbstractMovementVariant;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.simulation.SimulationProperties;

/**
 * Represents a map with a globe-like topology.
 * The globe allows horizontal wrapping of positions, meaning an animal that moves off one horizontal edge
 * reappears on the opposite edge. Vertical movement is limited by the map's top and bottom boundaries.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class Globe extends AbstractWorldMap {

    /**
     * Constructor for the {@code Globe} map.
     * Initializes the map with the specified simulation properties, such as width and height.
     *
     * @param simulationProperties  A {@link SimulationProperties} object defining the simulation parameters.
     * @param vegetation            An {@link AbstractVegetationVariant} object defining vegetation rules.
     * @param movement              An {@link AbstractMovementVariant} object defining movement rules.
     */
    public Globe(SimulationProperties simulationProperties, AbstractVegetationVariant vegetation, AbstractMovementVariant movement) {
        super(simulationProperties, vegetation, movement);
    }
}
