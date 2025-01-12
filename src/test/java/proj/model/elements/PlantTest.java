package proj.model.elements;

import org.junit.jupiter.api.Test;
import proj.util.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class PlantTest {

    @Test
    public void getPos() {
        Vector2d position = new Vector2d(1, 2);
        Plant plant = new Plant(position);
        assertEquals(position, plant.getPos());
    }

    @Test
    public void toStringTest() {
        Vector2d position = new Vector2d(1, 2);
        Plant plant = new Plant(position);
        assertEquals("*", plant.toString());
    }

    @Test
    public void getElementType() {
        Vector2d position = new Vector2d(1, 2);
        Plant plant = new Plant(position);
        assertEquals(ElementType.PLANT, plant.getElementType());
    }
}