package proj.app;

import proj.model.genotype.Mutation;
import proj.model.genotype.RandomMutation;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.Globe;
import proj.model.maps.WaterWorld;
import proj.model.movement.AbstractMovementVariant;
import proj.model.movement.OldAgeAintNoPicnic;
import proj.model.movement.PredestinedMovement;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.model.vegetation.ForestedEquator;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

/**
 * Factory class responsible for creating Simulation instances.
 * Encapsulates the complex creation logic.
 */
public class SimulationFactory {

    /**
     * Creates a new Simulation instance based on the provided properties.
     *
     * @param simulationProperties The properties to configure the simulation
     * @return A new Simulation instance
     */
    public static Simulation createSimulation(SimulationProperties simulationProperties) {
        AbstractVegetationVariant vegetation = createVegetationVariant(simulationProperties);
        Mutation mutation = createMutationVariant(simulationProperties);
        AbstractMovementVariant movement = createMovementVariant(simulationProperties);
        AbstractWorldMap map = createWorldMap(simulationProperties, vegetation, movement);

        return new Simulation(map, simulationProperties, mutation);
    }

    private static AbstractVegetationVariant createVegetationVariant(SimulationProperties props) {
        return switch(props.getVegetationVariant()) {
            case FORESTED_EQUATOR -> new ForestedEquator(
                    props.getEquatorHeight(),
                    props.getWidth(),
                    props.getHeight()
            );
        };
    }

    private static Mutation createMutationVariant(SimulationProperties props) {
        return switch(props.getMutationVariant()) {
            case RANDOM -> new RandomMutation();
        };
    }

    private static AbstractMovementVariant createMovementVariant(SimulationProperties props) {
        return switch(props.getMovementVariant()) {
            case PREDESTINED -> new PredestinedMovement();
            case OLD_AGE_AINT_NO_PICNIC -> new OldAgeAintNoPicnic();
        };
    }

    private static AbstractWorldMap createWorldMap(
            SimulationProperties props,
            AbstractVegetationVariant vegetation,
            AbstractMovementVariant movement) {

        return switch(props.getMapVariant()) {
            case GLOBE -> new Globe(props, vegetation, movement);
            case WATER_WORLD -> new WaterWorld(props, vegetation, movement);
        };
    }
}