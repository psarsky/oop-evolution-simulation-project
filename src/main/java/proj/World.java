package proj;

import proj.model.genotype.MutationVariant;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.Globe;
import proj.model.maps.MapVariant;
import proj.model.maps.WaterWorld;
import proj.model.movement.MovementVariant;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.model.vegetation.ForestedEquator;
import proj.model.vegetation.VegetationVariant;
import proj.presenter.ConsoleMapDisplay;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

public class World {
    public static void main(String[] args) {
        System.out.println("START");
        SimulationProperties simulationProperties = new SimulationProperties(
                100,    // gene count
                MovementVariant.PREDESTINED,
                MutationVariant.RANDOM,
                MapVariant.WATER_WORLD,
                VegetationVariant.FORESTED_EQUATOR,
                50,     // width
                30,     // height
                4,      // equator height
                20,     // animal count
                300,    // plant count
                20,     // plants per day
                10,     // start energy
                5,      // plant energy
                10,     // energy to reproduce
                5,      // energy passed to child
                1,      // energy consumed to move
                100,    // simulation step
                0,      // min mutation
                0       // max mutation
        );
        Simulation simulation = getSimulation(simulationProperties);
        simulation.run();
        System.out.println("STOP");
    }

    private static Simulation getSimulation(SimulationProperties simulationProperties) {
        AbstractWorldMap map = simulationProperties.getMapVariant() == MapVariant.WATER_WORLD ? new WaterWorld(simulationProperties) : new Globe(simulationProperties);
        AbstractVegetationVariant vegetationVariant = new ForestedEquator(simulationProperties.getEquatorHeight(), simulationProperties.getWidth(), simulationProperties.getHeight());
        ConsoleMapDisplay observer = new ConsoleMapDisplay();
        map.addObserver(observer);
        return new Simulation(map, vegetationVariant, simulationProperties, );
    }

    /*
    static SimulationEngine getSimulationEngine(List<Vector2d> positions, List<MoveDirection> directions) {
        List<Simulation> simulations = new ArrayList<>();
        ConsoleMapDisplay observer = new ConsoleMapDisplay();
        for (int i = 0; i < 1000; i++) {
            AbstractWorldMap map = i % 2 == 0 ? new GrassField(10) : new RectangularMap(8, 8);
            map.addObserver(observer);
            Simulation simulation = new Simulation(positions, directions, map);
            simulations.add(simulation);
        }
        return new SimulationEngine(simulations);
    }
    */
}
