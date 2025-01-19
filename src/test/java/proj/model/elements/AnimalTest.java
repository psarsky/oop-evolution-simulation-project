package proj.model.elements;

import org.junit.jupiter.api.Test;
import proj.model.genotype.Genotype;
import proj.model.genotype.MutationVariant;
import proj.model.genotype.RandomMutation;
import proj.model.maps.MapVariant;
import proj.model.movement.MovementVariant;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class AnimalTest {

    @Test
    public void animalConstructor() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.WATER_WORLD, VegetationVariant.FORESTED_EQUATOR, 5, 5, 1, 0, 0, 0, 10, 5, 40, 20, 0, 1, 0, 0, 0);
        Animal animal = new Animal(new Vector2d(1, 1),  simulationProperties, new Genotype(simulationProperties, new RandomMutation()));
        assertEquals(animal.getAge(), 0);
        assertEquals(animal.getEnergy(), 10);
        assertEquals(animal.getBirthDate(), 0);
        assertEquals(animal.getDeathDate(), -1);
        assertEquals(animal.getChildrenMade(), 0);
        assertEquals(animal.getPlantsEaten(), 0);
        assertEquals(animal.getPos(), new Vector2d(1, 1));
    }

    @Test
    public void animalMovement() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.WATER_WORLD, VegetationVariant.FORESTED_EQUATOR, 5, 5, 1, 0, 0, 0, 10, 5, 40, 20, 0, 1, 0, 0, 0);
        Animal animal = new Animal(new Vector2d(1, 1),  simulationProperties, new Genotype(simulationProperties, new RandomMutation()));
        PositionDirectionTuple newPosDir = new PositionDirectionTuple(new Vector2d(2, 1), MapDirection.EAST);
        animal.move(newPosDir);
        assertEquals(animal.getPos(), new Vector2d(2, 1));
        assertEquals(animal.getDirection(), MapDirection.EAST);
    }

    @Test
    public void eatingPlants() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.WATER_WORLD, VegetationVariant.FORESTED_EQUATOR, 5, 5, 1, 0, 0, 0, 10, 5, 40, 20, 0, 1, 0, 0, 0);
        Animal animal = new Animal(new Vector2d(1, 1),  simulationProperties, new Genotype(simulationProperties, new RandomMutation()));
        System.out.println(animal.getEnergy());
        animal.eatPlant(5);
        assertEquals(animal.getEnergy(), 15);
    }

    @Test
    public void animalReproductionValid() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.WATER_WORLD, VegetationVariant.FORESTED_EQUATOR, 5, 5, 1, 0, 0, 0, 40, 5, 40, 5, 0, 1, 0, 0, 0);
        Animal animal1 = new Animal(new Vector2d(1, 1),  simulationProperties, new Genotype(simulationProperties, new RandomMutation()));
        Animal animal2 = new Animal(new Vector2d(1, 1),  simulationProperties, new Genotype(simulationProperties, new RandomMutation()));
        Animal child = animal1.reproduce(animal2, simulationProperties);
        assertNotEquals(child, null);
        assertEquals(child.getEnergy(), 10);
        assertEquals(animal1.getChildrenMade(), 1);
        assertEquals(animal2.getChildrenMade(), 1);
    }

    @Test
    public void animalReproductionInvalid() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.WATER_WORLD, VegetationVariant.FORESTED_EQUATOR, 5, 5, 1, 0, 0, 0, 10, 5, 40, 20, 0, 1, 0, 0, 0);
        Animal animal1 = new Animal(new Vector2d(1, 1),  simulationProperties, new Genotype(simulationProperties, new RandomMutation()));
        Animal animal2 = new Animal(new Vector2d(1, 1),  simulationProperties, new Genotype(simulationProperties, new RandomMutation()));
        Animal child = animal1.reproduce(animal2, simulationProperties);
        assertNull(child);
        assertEquals(animal1.getChildrenMade(), 0);
        assertEquals(animal2.getChildrenMade(), 0);
    }
}