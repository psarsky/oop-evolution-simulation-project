package proj.model.elements;

import org.junit.jupiter.api.Test;
import proj.util.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class WaterTest {

    @Test
    public void getPos() {
        Vector2d position = new Vector2d(1, 2);
        Water water = new Water(position);
        assertEquals(position, water.getPos());
    }

    @Test
    public void toStringTest() {
        Water water = new Water(new Vector2d(1, 2));
        assertEquals("~", water.toString());
    }

    @Test
    public void getElementType() {
        Water water = new Water(new Vector2d(1, 2));
        assertEquals(ElementType.WATER, water.getElementType());
    }
}