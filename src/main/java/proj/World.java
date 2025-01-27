package proj;

import proj.model.genotype.Mutation;
import proj.model.genotype.MutationVariant;
import proj.model.genotype.RandomMutation;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.Globe;
import proj.model.maps.MapVariant;
import proj.model.maps.WaterWorld;
import proj.model.movement.AbstractMovementVariant;
import proj.model.movement.MovementVariant;
import proj.model.movement.OldAgeAintNoPicnic;
import proj.model.movement.PredestinedMovement;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.model.vegetation.ForestedEquator;
import proj.model.vegetation.VegetationVariant;
//import proj.presenter.ConsoleMapDisplay;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

public class World {
    public static void main(String[] args) {
        System.out.println("START");
        SimulationProperties simulationProperties = new SimulationProperties(
                "console_config",
                100,    // gene count
                MovementVariant.PREDESTINED,
                MutationVariant.RANDOM,
                MapVariant.WATER_WORLD,
                VegetationVariant.FORESTED_EQUATOR,
                50,     // width
                30,     // height
                4,      // equator height
                20,     // animal count
                100,    // plant count
                10,     // plants per day
                10,     // start energy
                5,      // plant energy
                10,     // energy to reproduce
                5,      // energy passed to child
                1,      // energy consumed to move
                100,    // simulation step
                0,      // min mutation
                0,      // max mutation
                50,      // water violence
                false
        );
        Simulation simulation = getSimulation(simulationProperties);
        simulation.run();
        System.out.println("STOP");
    }

    private static Simulation getSimulation(SimulationProperties simulationProperties) {
        AbstractVegetationVariant vegetation = switch(simulationProperties.getVegetationVariant()) {
            case FORESTED_EQUATOR -> new ForestedEquator(
                    simulationProperties.getEquatorHeight(),
                    simulationProperties.getWidth(),
                    simulationProperties.getHeight()
            );
        };
        Mutation mutation = switch(simulationProperties.getMutationVariant()) {
            case RANDOM -> new RandomMutation();
        };
        AbstractMovementVariant movement = switch(simulationProperties.getMovementVariant()) {
            case PREDESTINED -> new PredestinedMovement();
            case OLD_AGE_AINT_NO_PICNIC -> new OldAgeAintNoPicnic();
        };
        AbstractWorldMap map = switch(simulationProperties.getMapVariant()) {
            case GLOBE -> new Globe(simulationProperties, vegetation, movement);
            case WATER_WORLD -> new WaterWorld(simulationProperties, vegetation, movement);
        };
//        ConsoleMapDisplay observer = new ConsoleMapDisplay();
//        map.addObserver(observer);
        return new Simulation(map, simulationProperties, mutation);
    }
}
