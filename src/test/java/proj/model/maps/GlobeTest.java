package proj.model.maps;

import org.junit.jupiter.api.Test;
import proj.model.elements.Animal;
import proj.model.genotype.Genotype;
import proj.model.genotype.RandomMutation;
import proj.model.movement.MovementVariant;
import proj.model.genotype.MutationVariant;
import proj.model.elements.Plant;
import proj.model.movement.PredestinedMovement;
import proj.model.vegetation.ForestedEquator;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class GlobeTest {

    @Test
    public void correctPosition() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.GLOBE, VegetationVariant.FORESTED_EQUATOR, 5, 5, 2, 0, 0, 0, 100, 1, 40, 20, 0, 1, 0, 0, 0);
        Globe map = new Globe(simulationProperties, new ForestedEquator(simulationProperties.getEquatorHeight(), simulationProperties.getWidth(), simulationProperties.getHeight()), new PredestinedMovement());
        assertEquals(new PositionDirectionTuple(new Vector2d(0, 1), MapDirection.NORTH), map.correctPosition(new Vector2d(0, 0), new Vector2d(0, -1), MapDirection.SOUTH));
        assertEquals(new PositionDirectionTuple(new Vector2d(0, 3), MapDirection.SOUTH), map.correctPosition(new Vector2d(0, 4), new Vector2d(0, 5), MapDirection.NORTH));
        assertEquals(new PositionDirectionTuple(new Vector2d(1, 1), MapDirection.NORTHWEST), map.correctPosition(new Vector2d(0, 0), new Vector2d(1, -1), MapDirection.SOUTHWEST));
        assertEquals(new PositionDirectionTuple(new Vector2d(1, 3), MapDirection.SOUTHWEST), map.correctPosition(new Vector2d(0, 4), new Vector2d(1, 5), MapDirection.NORTHWEST));
        assertEquals(new PositionDirectionTuple(new Vector2d(4, 1), MapDirection.NORTHEAST), map.correctPosition(new Vector2d(0, 0), new Vector2d(4, -1), MapDirection.SOUTHEAST));
        assertEquals(new PositionDirectionTuple(new Vector2d(4, 3), MapDirection.SOUTHEAST), map.correctPosition(new Vector2d(0, 4), new Vector2d(4, 5), MapDirection.NORTHEAST));
        assertEquals(new PositionDirectionTuple(new Vector2d(0, 1), MapDirection.NORTH), map.correctPosition(new Vector2d(0, 0), new Vector2d(5, -1), MapDirection.SOUTH));
        assertEquals(new PositionDirectionTuple(new Vector2d(0, 3), MapDirection.SOUTH), map.correctPosition(new Vector2d(0, 4), new Vector2d(5, 5), MapDirection.NORTH));
        assertEquals(new PositionDirectionTuple(new Vector2d(1, 1), MapDirection.NORTHWEST), map.correctPosition(new Vector2d(0, 0), new Vector2d(6, -1), MapDirection.SOUTHWEST));
        assertEquals(new PositionDirectionTuple(new Vector2d(1, 3), MapDirection.SOUTHWEST), map.correctPosition(new Vector2d(0, 4), new Vector2d(6, 5), MapDirection.NORTHWEST));
        assertEquals(new PositionDirectionTuple(new Vector2d(4, 1), MapDirection.NORTHEAST), map.correctPosition(new Vector2d(0, 0), new Vector2d(9, -1), MapDirection.SOUTHEAST));
        assertEquals(new PositionDirectionTuple(new Vector2d(4, 3), MapDirection.SOUTHEAST), map.correctPosition(new Vector2d(0, 4), new Vector2d(9, 5), MapDirection.NORTHEAST));
        assertEquals(new PositionDirectionTuple(new Vector2d(4, 1), MapDirection.NORTH), map.correctPosition(new Vector2d(0, 0), new Vector2d(-1, -1), MapDirection.SOUTH));
        assertEquals(new PositionDirectionTuple(new Vector2d(4, 3), MapDirection.SOUTH), map.correctPosition(new Vector2d(0, 4), new Vector2d(-1, 5), MapDirection.NORTH));
        assertEquals(new PositionDirectionTuple(new Vector2d(4, 1), MapDirection.NORTHWEST), map.correctPosition(new Vector2d(0, 0), new Vector2d(-1, -1), MapDirection.SOUTHWEST));
    }

    @Test
    public void eatPlants() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.GLOBE, VegetationVariant.FORESTED_EQUATOR, 5, 5, 1, 0, 0, 0, 10, 1, 40, 20, 0, 1, 0, 0, 0);
        Globe map = new Globe(simulationProperties, new ForestedEquator(simulationProperties.getEquatorHeight(), simulationProperties.getWidth(), simulationProperties.getHeight()), new PredestinedMovement());
        Simulation simulation = new Simulation(map, simulationProperties, new RandomMutation());
        map.placeAnimal(new Vector2d(0, 0), new Animal(new Vector2d(0, 0), simulationProperties, new Genotype(simulationProperties, new RandomMutation())));
        map.placePlant(new Vector2d(0, 0), new Plant(new Vector2d(0, 0)));
        simulation.eat();
        assertEquals(11, map.getAnimals().get(new Vector2d(0, 0)).getFirst().getEnergy());
    }

    @Test
    public void twoAnimalsEatPlants() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.GLOBE, VegetationVariant.FORESTED_EQUATOR, 5, 5, 1, 0, 0, 0, 10, 1, 40, 20, 0, 1, 0, 0, 0);
        Globe map = new Globe(simulationProperties, new ForestedEquator(simulationProperties.getEquatorHeight(), simulationProperties.getWidth(), simulationProperties.getHeight()), new PredestinedMovement());
        Simulation simulation = new Simulation(map, simulationProperties, new RandomMutation());
        map.placeAnimal(new Vector2d(0, 0), new Animal(new Vector2d(0, 0), simulationProperties, new Genotype(simulationProperties, new RandomMutation())));
        map.placeAnimal(new Vector2d(0, 0), new Animal(new Vector2d(0, 0), simulationProperties, new Genotype(simulationProperties, new RandomMutation())));
        map.placePlant(new Vector2d(0, 0), new Plant(new Vector2d(0, 0)));
        simulation.eat();
        assertEquals(11, map.getAnimals().get(new Vector2d(0, 0)).get(0).getEnergy());
        assertEquals(10, map.getAnimals().get(new Vector2d(0, 0)).get(1).getEnergy());
    }

    @Test
    public void reproduce() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.GLOBE, VegetationVariant.FORESTED_EQUATOR, 5, 5, 1, 0, 0, 0, 100, 1, 40, 20, 0, 1, 0, 0, 0);
        Globe map = new Globe(simulationProperties, new ForestedEquator(simulationProperties.getEquatorHeight(), simulationProperties.getWidth(), simulationProperties.getHeight()), new PredestinedMovement());
        Simulation simulation = new Simulation(map, simulationProperties, new RandomMutation());
        map.placeAnimal(new Vector2d(0, 0), new Animal(new Vector2d(0, 0), simulationProperties, new Genotype(simulationProperties, new RandomMutation())));
        map.placeAnimal(new Vector2d(0, 0), new Animal(new Vector2d(0, 0), simulationProperties, new Genotype(simulationProperties, new RandomMutation())));
        map.placePlant(new Vector2d(0, 0), new Plant(new Vector2d(0, 0)));
        simulation.reproduce();
        assertEquals(80, map.getAnimals().get(new Vector2d(0, 0)).get(0).getEnergy());
        assertEquals(80, map.getAnimals().get(new Vector2d(0, 0)).get(1).getEnergy());
        assertEquals(3, map.getAnimals().get(new Vector2d(0, 0)).size());
        assertEquals(40, map.getAnimals().get(new Vector2d(0, 0)).get(2).getEnergy());
    }
}