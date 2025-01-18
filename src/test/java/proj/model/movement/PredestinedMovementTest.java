package proj.model.movement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import proj.model.elements.Animal;
import proj.model.genotype.Genotype;
import proj.model.genotype.MutationVariant;
import proj.model.genotype.RandomMutation;
import proj.model.maps.MapVariant;
import proj.model.maps.MoveValidator;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class PredestinedMovementTest {

    private PredestinedMovement predestinedMovement;
    private SimulationProperties simulationProperties;

    @BeforeEach
    void setUp() {
        this.predestinedMovement = new PredestinedMovement();
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
    }

    @Test
    void movementUpdatesAnimalPositionAndDirection() {
        int[] genes = {1, 2, 3, 4};
        Animal animal = new Animal(new Vector2d(5, 5), this.simulationProperties, new Genotype(this.simulationProperties, new RandomMutation()));
        animal.setGenes(genes);
        animal.setGeneIndex(-1);
        animal.setDir(MapDirection.NORTH);

        MoveValidator validator = new TestMoveValidator();

        this.predestinedMovement.move(animal, validator);

        assertEquals(0, animal.getGeneIndex());

        Vector2d expectedPosition = new Vector2d(6, 6);
        MapDirection expectedDirection = MapDirection.NORTHEAST;
        assertEquals(expectedPosition, animal.getPos());
        assertEquals(expectedDirection, animal.getDir());
    }

    @Test
    void movementModificationReturnsCorrectPositionDirection() {
        Vector2d oldPosition = new Vector2d(3, 3);
        Vector2d newPosition = new Vector2d(4, 4);
        MapDirection oldDirection = MapDirection.NORTH;
        MapDirection newDirection = MapDirection.NORTHEAST;

        PositionDirectionTuple result = this.predestinedMovement.movementModification(null, oldPosition, newPosition, oldDirection, newDirection);

        assertEquals(newPosition, result.position());
        assertEquals(newDirection, result.direction());
    }

    @Test
    void movementHandlesGeneWrapping() {
        int[] genes = {1, 2, 3, 4};
        Animal animal = new Animal(new Vector2d(2, 2), this.simulationProperties, new Genotype(this.simulationProperties, new RandomMutation()));
        animal.setGenes(genes);
        animal.setGeneIndex(3);
        animal.setDir(MapDirection.SOUTHWEST);

        MoveValidator validator = new TestMoveValidator();

        this.predestinedMovement.move(animal, validator);

        assertEquals(0, animal.getGeneIndex());

        Vector2d expectedPosition = new Vector2d(1, 2);
        MapDirection expectedDirection = MapDirection.WEST;
        assertEquals(expectedPosition, animal.getPos());
        assertEquals(expectedDirection, animal.getDir());
    }

    @Test
    void movementHandlesValidatorCorrection() {
        int[] genes = {1};
        Animal animal = new Animal(new Vector2d(1, 1), this.simulationProperties, new Genotype(this.simulationProperties, new RandomMutation()));
        animal.setGenes(genes);

        MoveValidator validator = (oldPos, newPos, direction) -> new PositionDirectionTuple(new Vector2d(0, 0), MapDirection.SOUTH);

        this.predestinedMovement.move(animal, validator);

        assertEquals(new Vector2d(0, 0), animal.getPos());
        assertEquals(MapDirection.SOUTH, animal.getDir());
    }
}

class TestMoveValidator implements MoveValidator {
    @Override
    public PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction) {
        return new PositionDirectionTuple(newPosition, direction);
    }
}