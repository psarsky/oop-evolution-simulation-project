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
import proj.util.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class OldAgeAintNoPicnicTest {

    private OldAgeAintNoPicnic oldAgeAintNoPicnic;
    private SimulationProperties simulationProperties;

    @BeforeEach
    void setUp() {
        this.oldAgeAintNoPicnic = new OldAgeAintNoPicnic();
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
    void youngAnimalMovement() {
        int[] genes = {2, 4, 6};
        Animal animal = new Animal(new Vector2d(0, 0), this.simulationProperties, new Genotype(this.simulationProperties, new RandomMutation()));
        animal.setGenes(genes);
        animal.setGeneIndex(-1);
        animal.setDir(MapDirection.NORTH);
        animal.setAge(5);

        MoveValidator validator = new TestMoveValidator();

        this.oldAgeAintNoPicnic.move(animal, validator);

        Vector2d expectedPosition = new Vector2d(1, 0);
        MapDirection expectedDirection = MapDirection.EAST;
        assertEquals(expectedPosition, animal.getPos());
        assertEquals(expectedDirection, animal.getDirection());
    }
}
