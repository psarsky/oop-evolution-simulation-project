package proj.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MapDirectionTest {
    @Test
    public void testToString() {
        assertEquals("N", MapDirection.NORTH.toString());
        assertEquals("NE", MapDirection.NORTHEAST.toString());
        assertEquals("E", MapDirection.EAST.toString());
        assertEquals("SE", MapDirection.SOUTHEAST.toString());
        assertEquals("S", MapDirection.SOUTH.toString());
        assertEquals("SW", MapDirection.SOUTHWEST.toString());
        assertEquals("W", MapDirection.WEST.toString());
        assertEquals("NW", MapDirection.NORTHWEST.toString());
    }

    @Test
    public void testNext() {
        assertEquals(MapDirection.NORTHEAST, MapDirection.NORTH.next());
        assertEquals(MapDirection.EAST, MapDirection.NORTHEAST.next());
        assertEquals(MapDirection.SOUTHEAST, MapDirection.EAST.next());
        assertEquals(MapDirection.SOUTH, MapDirection.SOUTHEAST.next());
        assertEquals(MapDirection.SOUTHWEST, MapDirection.SOUTH.next());
        assertEquals(MapDirection.WEST, MapDirection.SOUTHWEST.next());
        assertEquals(MapDirection.NORTHWEST, MapDirection.WEST.next());
        assertEquals(MapDirection.NORTH, MapDirection.NORTHWEST.next());
    }

    @Test
    public void testRotate() {
        assertEquals(MapDirection.NORTH, MapDirection.NORTH.rotate(0));
        assertEquals(MapDirection.NORTHEAST, MapDirection.NORTH.rotate(1));
        assertEquals(MapDirection.EAST, MapDirection.NORTH.rotate(2));
        assertEquals(MapDirection.SOUTHEAST, MapDirection.NORTH.rotate(3));
        assertEquals(MapDirection.SOUTH, MapDirection.NORTH.rotate(4));
        assertEquals(MapDirection.SOUTHWEST, MapDirection.NORTH.rotate(5));
        assertEquals(MapDirection.WEST, MapDirection.NORTH.rotate(6));
        assertEquals(MapDirection.NORTHWEST, MapDirection.NORTH.rotate(7));
    }

    @Test
    public void testGetRandomDirection() {
        for (int i = 0; i < 100; i++) {
            MapDirection direction = MapDirection.getRandomDirection();
            assertTrue(direction == MapDirection.NORTH ||
                    direction == MapDirection.NORTHEAST ||
                    direction == MapDirection.EAST ||
                    direction == MapDirection.SOUTHEAST ||
                    direction == MapDirection.SOUTH ||
                    direction == MapDirection.SOUTHWEST ||
                    direction == MapDirection.WEST ||
                    direction == MapDirection.NORTHWEST);
        }
    }

    @Test
    public void testUnitVector() {
        assertEquals(new Vector2d(0, 1), MapDirection.NORTH.toUnitVector());
        assertEquals(new Vector2d(1, 1), MapDirection.NORTHEAST.toUnitVector());
        assertEquals(new Vector2d(1, 0), MapDirection.EAST.toUnitVector());
        assertEquals(new Vector2d(1, -1), MapDirection.SOUTHEAST.toUnitVector());
        assertEquals(new Vector2d(0, -1), MapDirection.SOUTH.toUnitVector());
        assertEquals(new Vector2d(-1, -1), MapDirection.SOUTHWEST.toUnitVector());
        assertEquals(new Vector2d(-1, 0), MapDirection.WEST.toUnitVector());
        assertEquals(new Vector2d(-1, 1), MapDirection.NORTHWEST.toUnitVector());
    }
}