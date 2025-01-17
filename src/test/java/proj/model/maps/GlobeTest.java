package proj.model.maps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import proj.model.elements.Animal;
import proj.model.genotype.Genotype;
import proj.model.genotype.RandomMutation;
import proj.model.movement.AbstractMovementVariant;
import proj.model.movement.MovementVariant;
import proj.model.genotype.MutationVariant;
import proj.model.elements.Plant;
import proj.model.movement.PredestinedMovement;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.model.vegetation.ForestedEquator;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobeTest {

    private Globe globe;
    private SimulationProperties simulationProperties;

    @BeforeEach
    void setUp() {
        this.simulationProperties = new SimulationProperties(
                6,
                MovementVariant.PREDESTINED,
                MutationVariant.RANDOM,
                MapVariant.WATER_WORLD,
                VegetationVariant.FORESTED_EQUATOR,
                10,
                10,
                1,
                0,
                0,
                3,
                10,
                5,
                40,
                20,
                0,
                1,
                0,
                0,
                50
        );

        AbstractVegetationVariant vegetationVariant = new ForestedEquator(this.simulationProperties.getEquatorHeight(), this.simulationProperties.getWidth(), this.simulationProperties.getHeight());
        AbstractMovementVariant movementVariant = new PredestinedMovement();

        this.globe = new Globe(this.simulationProperties, vegetationVariant, movementVariant);
    }

    @Test
    void constructorInitializesFreePlantPositions() {
        List<Vector2d> freePlantPositions = this.globe.getFreePlantPositions();
        assertEquals(this.simulationProperties.getWidth() * this.simulationProperties.getHeight(), freePlantPositions.size());
    }

    @Test
    void placeAnimalAddsAnimalToCorrectPosition() {
        Vector2d position = new Vector2d(5, 5);
        Animal animal = new Animal(position, this.simulationProperties, new Genotype(this.simulationProperties, new RandomMutation()));

        this.globe.placeAnimal(position, animal);

        HashMap<Vector2d, List<Animal>> animals = this.globe.getAnimals();
        assertTrue(animals.containsKey(position));
        assertTrue(animals.get(position).contains(animal));
    }

    @Test
    void removeAnimalRemovesAnimalFromMap() {
        Vector2d position = new Vector2d(5, 5);
        Animal animal = new Animal(position, this.simulationProperties, new Genotype(this.simulationProperties, new RandomMutation()));

        this.globe.placeAnimal(position, animal);
        this.globe.removeAnimal(animal);

        HashMap<Vector2d, List<Animal>> animals = this.globe.getAnimals();
        assertFalse(animals.get(position).contains(animal));
    }

    @Test
    void moveUpdatesAnimalPosition() {
        Vector2d initialPosition = new Vector2d(2, 2);
        Animal animal = new Animal(initialPosition, this.simulationProperties, new Genotype(this.simulationProperties, new RandomMutation()));

        this.globe.placeAnimal(initialPosition, animal);
        this.globe.move(animal);

        assertFalse(this.globe.getAnimals().get(initialPosition).contains(animal));
        assertTrue(this.globe.getAnimals().get(animal.getPos()).contains(animal));
    }

    @Test
    void placePlantRemovesPositionFromFreePlantPositions() {
        Vector2d position = new Vector2d(4, 4);
        Plant plant = new Plant(position);

        this.globe.placePlant(position, plant);

        assertFalse(this.globe.getFreePlantPositions().contains(position));
        assertEquals(plant, this.globe.getPlants().get(position));
    }

    @Test
    void spawnPlantAddsPlantToFreePosition() {
        this.globe.spawnPlant();

        assertEquals(this.simulationProperties.getWidth() * this.simulationProperties.getHeight() - 1, this.globe.getFreePlantPositions().size());
    }

    @Test
    void updateWorldElementsSpawnsPlants() {
        int initialFreePositions = this.globe.getFreePlantPositions().size();

        this.globe.updateWorldElements();

        assertEquals(initialFreePositions - this.simulationProperties.getPlantsPerDay(), this.globe.getFreePlantPositions().size());
    }

    @Test
    void testObjectAtReturnsCorrectElement() {
        Vector2d animalPosition = new Vector2d(3, 3);
        Animal animal = new Animal(animalPosition, this.simulationProperties, new Genotype(this.simulationProperties, new RandomMutation()));
        this.globe.placeAnimal(animalPosition, animal);

        Vector2d plantPosition = new Vector2d(4, 4);
        Plant plant = new Plant(plantPosition);
        this.globe.placePlant(plantPosition, plant);

        assertEquals(animal, this.globe.objectAt(animalPosition));
        assertEquals(plant, this.globe.objectAt(plantPosition));
        assertNull(this.globe.objectAt(new Vector2d(0, 0)));
    }

    @Test
    void testCorrectPositionAdjustsOutOfBoundsMovement() {
        Vector2d oldPosition = new Vector2d(5, 5);
        Vector2d newPosition = new Vector2d(-1, -1);
        MapDirection direction = MapDirection.SOUTH;

        PositionDirectionTuple corrected = this.globe.correctPosition(oldPosition, newPosition, direction);

        assertEquals(new Vector2d(9, 1), corrected.position());
        assertEquals(MapDirection.NORTH, corrected.direction());
    }


    @Test
    public void correctPosition() {
        this.simulationProperties = new SimulationProperties(6, MovementVariant.PREDESTINED, MutationVariant.RANDOM, MapVariant.GLOBE, VegetationVariant.FORESTED_EQUATOR, 5, 5, 2, 0, 0, 0, 100, 1, 40, 20, 0, 1, 0, 0, 0);
        Globe map = new Globe(this.simulationProperties, new ForestedEquator(this.simulationProperties.getEquatorHeight(), this.simulationProperties.getWidth(), this.simulationProperties.getHeight()), new PredestinedMovement());
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
}