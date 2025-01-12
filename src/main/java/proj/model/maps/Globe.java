package proj.model.maps;

import proj.simulation.SimulationProperties;

/**
 * Represents a map with a globe-like topology.
 * The globe allows horizontal wrapping of positions, meaning an animal that moves off one horizontal edge
 * reappears on the opposite edge. Vertical movement is limited by the map's top and bottom boundaries.
 */
public class Globe extends AbstractWorldMap {

    /**
     * Constructor for the Globe map.
     * Initializes the map with the specified simulation properties, such as width and height.
     *
     * @param simulationProperties          The properties defining the map's dimensions and settings
     */
    public Globe(SimulationProperties simulationProperties) {
        super(simulationProperties);
    }
}
