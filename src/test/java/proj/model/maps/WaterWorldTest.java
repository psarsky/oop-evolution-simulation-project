package proj.model.maps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import proj.model.elements.Water;
import proj.model.genotype.MutationVariant;
import proj.model.movement.AbstractMovementVariant;
import proj.model.movement.MovementVariant;
import proj.model.movement.PredestinedMovement;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.model.vegetation.ForestedEquator;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class WaterWorldTest {

    private WaterWorld waterWorld;
    private SimulationProperties simulationProperties;

    @BeforeEach
    public void setUp() {
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
                0,
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

        this.waterWorld = new WaterWorld(this.simulationProperties, vegetationVariant, movementVariant);
    }

    @Test
    public void constructorInitializesWaterFields() {
        assertFalse(this.waterWorld.getWaterFields().isEmpty());
        assertTrue(this.waterWorld.getWaterFields().size() <= this.simulationProperties.getWidth() * this.simulationProperties.getHeight() / 10);
    }

    @Test
    public void waterFlowLowTide() {
        int initialWaterFieldCount = this.waterWorld.getWaterFields().size();

        this.waterWorld.waterFlow(false, this.simulationProperties.getWaterViolence());

        assertTrue(this.waterWorld.getWaterFields().size() < initialWaterFieldCount);
    }

    @Test
    public void generateFreePlantPositions() {
        this.waterWorld.generateFreePlantPositions();

        for (Vector2d position : this.waterWorld.getFreePlantPositions()) {
            assertFalse(this.waterWorld.getWaterFields().containsKey(position));
        }
    }

    @Test
    public void correctPositionRedirectsOnWater() {
        Vector2d oldPosition = new Vector2d(3, 3);
        Vector2d newPosition = new Vector2d(4, 4);
        MapDirection direction = MapDirection.NORTH;

        this.waterWorld.getWaterFields().put(newPosition, new Water(newPosition));

        PositionDirectionTuple corrected = this.waterWorld.correctPosition(oldPosition, newPosition, direction);

        assertEquals(oldPosition, corrected.position());
        assertEquals(direction.opposite(), corrected.direction());
    }

    @Test
    public void updateWorldElementsTriggersWaterFlow() {
        int initialWaterFieldCount = this.waterWorld.getWaterFields().size();

        this.waterWorld.updateWorldElements();

        assertNotEquals(initialWaterFieldCount, this.waterWorld.getWaterFields().size());
        assertNotNull(this.waterWorld.getFreePlantPositions());
    }
}
