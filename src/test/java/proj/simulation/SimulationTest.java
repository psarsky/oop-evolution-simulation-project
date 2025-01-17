package proj.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.model.genotype.Genotype;
import proj.model.genotype.Mutation;
import proj.model.genotype.MutationVariant;
import proj.model.genotype.RandomMutation;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.Globe;
import proj.model.maps.MapVariant;
import proj.model.movement.AbstractMovementVariant;
import proj.model.movement.MovementVariant;
import proj.model.movement.PredestinedMovement;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.model.vegetation.ForestedEquator;
import proj.model.vegetation.VegetationVariant;
import proj.util.Vector2d;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationTest {
    private AbstractWorldMap map;
    private SimulationProperties simulationProperties;
    private Mutation mutation;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        this.simulationProperties = new SimulationProperties(
                6,
                MovementVariant.PREDESTINED,
                MutationVariant.RANDOM,
                MapVariant.GLOBE,
                VegetationVariant.FORESTED_EQUATOR,
                10,
                10,
                1,
                10,
                10,
                2,
                10,
                5,
                40,
                5,
                1,
                10,
                0,
                0,
                50
        );
        AbstractMovementVariant movement = new PredestinedMovement();
        AbstractVegetationVariant vegetation = new ForestedEquator(simulationProperties.getEquatorHeight(), simulationProperties.getWidth(), simulationProperties.getHeight());
        map = new Globe(simulationProperties, vegetation, movement);
        mutation = new RandomMutation();
        simulation = new Simulation(map, simulationProperties, mutation);
    }

    @Test
    void testSimulationInitialization() {
        List<Animal> animals = simulation.getAnimals();
        assertEquals(simulationProperties.getAnimalCount(), animals.size());

        for (Animal animal : animals) {
            assertNotNull(animal.getPos());
            assertNotNull(map.objectAt(animal.getPos()));
        }

        assertEquals(simulationProperties.getPlantCount(), map.getPlants().size());
    }

    @Test
    void testTogglePause() {
        boolean initialRunningState = simulation.isRunning();
        simulation.togglePause();
        assertNotEquals(initialRunningState, simulation.isRunning());
    }

    @Test
    void testSimulationStopWhenNoAnimals() {
        simulation.getAnimals().clear();
        simulation.run();

        assertTrue(simulation.getAnimals().isEmpty());
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