package proj.model.elements;

import org.junit.jupiter.api.Test;
import proj.model.genotype.Genotype;
import proj.model.genotype.MutationVariant;
import proj.model.genotype.RandomMutation;
import proj.model.maps.MapVariant;
import proj.model.movement.MovementVariant;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.SimulationProperties;
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
    public void eatingPlants() {
        SimulationProperties simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.WATER_WORLD, VegetationVariant.FORESTED_EQUATOR, 5, 5, 1, 0, 0, 0, 10, 5, 40, 20, 0, 1, 0, 0, 0);
        Animal animal = new Animal(new Vector2d(1, 1),  simulationProperties, new Genotype(simulationProperties, new RandomMutation()));
        System.out.println(animal.getEnergy());
        animal.eatPlant(5);
        assertEquals(animal.getEnergy(), 15);
    }
}