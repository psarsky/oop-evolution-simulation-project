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
 * A factory class responsible for creating and configuring {@link Simulation} instances.
 * It encapsulates the logic for instantiating the correct map, vegetation, movement,
 * and mutation variants based on the provided {@link SimulationProperties}.
 */
public class SimulationFactory {

    /**
     * Creates a new {@link Simulation} instance based on the specified configuration properties.
     *
     * @param simulationProperties The {@link SimulationProperties} defining the parameters for the simulation.
     * @return A fully configured {@link Simulation} instance ready to be started.
     * @throws IllegalArgumentException if any configuration variant is unsupported.
     */
    public static Simulation createSimulation(SimulationProperties simulationProperties) {
        AbstractVegetationVariant vegetation = createVegetationVariant(simulationProperties);
        Mutation mutation = createMutationVariant(simulationProperties);
        AbstractMovementVariant movement = createMovementVariant(simulationProperties);
        AbstractWorldMap map = createWorldMap(simulationProperties, vegetation, movement);

        return new Simulation(map, simulationProperties, mutation);
    }

    /**
     * Creates the appropriate {@link AbstractVegetationVariant} based on the simulation properties.
     *
     * @param props The simulation properties.
     * @return The configured vegetation variant instance.
     */
    private static AbstractVegetationVariant createVegetationVariant(SimulationProperties props) {
        return switch (props.getVegetationVariant()) {
            case FORESTED_EQUATOR -> new ForestedEquator(
                    props.getEquatorHeight(),
                    props.getWidth(),
                    props.getHeight()
            );
            // Add cases for other VegetationVariants if they exist
            // default -> throw new IllegalArgumentException("Unsupported VegetationVariant: " + props.getVegetationVariant());
        };
    }

    /**
     * Creates the appropriate {@link Mutation} strategy based on the simulation properties.
     *
     * @param props The simulation properties.
     * @return The configured mutation strategy instance.
     */
    private static Mutation createMutationVariant(SimulationProperties props) {
        return switch (props.getMutationVariant()) {
            case RANDOM -> new RandomMutation();
            // Add cases for other MutationVariants if they exist
            // default -> throw new IllegalArgumentException("Unsupported MutationVariant: " + props.getMutationVariant());
        };
    }

    /**
     * Creates the appropriate {@link AbstractMovementVariant} based on the simulation properties.
     *
     * @param props The simulation properties.
     * @return The configured movement variant instance.
     */
    private static AbstractMovementVariant createMovementVariant(SimulationProperties props) {
        return switch (props.getMovementVariant()) {
            case PREDESTINED -> new PredestinedMovement();
            case OLD_AGE_AINT_NO_PICNIC -> new OldAgeAintNoPicnic();
            // Add cases for other MovementVariants if they exist
            // default -> throw new IllegalArgumentException("Unsupported MovementVariant: " + props.getMovementVariant());
        };
    }

    /**
     * Creates the appropriate {@link AbstractWorldMap} based on the simulation properties,
     * injecting the previously created vegetation and movement variants.
     *
     * @param props      The simulation properties.
     * @param vegetation The vegetation variant for the map.
     * @param movement   The movement variant for the map.
     * @return The configured world map instance.
     */
    private static AbstractWorldMap createWorldMap(
            SimulationProperties props,
            AbstractVegetationVariant vegetation,
            AbstractMovementVariant movement) {

        return switch (props.getMapVariant()) {
            case GLOBE -> new Globe(props, vegetation, movement);
            case WATER_WORLD -> new WaterWorld(props, vegetation, movement);
            // Add cases for other MapVariants if they exist
            // default -> throw new IllegalArgumentException("Unsupported MapVariant: " + props.getMapVariant());
        };
    }

    /**
     * Private constructor to prevent instantiation of this factory class.
     */
    private SimulationFactory() {
        // Factory class should not be instantiated.
    }
}