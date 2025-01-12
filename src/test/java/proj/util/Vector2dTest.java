package proj.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class Vector2dTest {
    @Test
    void equalsSameVectors() {
        Vector2d v1 = new Vector2d(1, 2);
        Vector2d v2 = new Vector2d(1, 2);
        assertEquals(v1, v2);
    }

    @Test
    void equalsDifferentVectors() {
        Vector2d v1 = new Vector2d(1, 2);
        Vector2d v2 = new Vector2d(1, -2);
        assertNotEquals(v1, v2);
    }

    @Test
    void equalsOneIsNull() {
        Vector2d v = new Vector2d(1, 2);
        assertNotEquals(v, null);
    }

    @Test
    void toStringTest() {
        Vector2d v1 = new Vector2d(1, 2);
        String v2 = v1.toString();
        assertEquals("(1, 2)", v2);
    }

    @Test
    void precedesSameVectors() {
        Vector2d v1 = new Vector2d(2, 5);
        Vector2d v2 = new Vector2d(2, 5);
        assertTrue(v1.precedes(v2));
    }

    @Test
    void precedesDifferentVectorsCorrect() {
        Vector2d v1 = new Vector2d(1, 3);
        Vector2d v2 = new Vector2d(2, 5);
        assertTrue(v1.precedes(v2));
    }

    @Test
    void precedesDifferentVectorsIncorrect() {
        Vector2d v1 = new Vector2d(2, 5);
        Vector2d v2 = new Vector2d(1, 3);
        assertFalse(v1.precedes(v2));
    }

    @Test
    void precedesNeitherVectorPrecedes() {
        Vector2d v1 = new Vector2d(1, 3);
        Vector2d v2 = new Vector2d(0, 5);
        assertFalse(v1.precedes(v2));
    }

    @Test
    void followsSameVectors() {
        Vector2d v1 = new Vector2d(2, 5);
        Vector2d v2 = new Vector2d(2, 5);
        assertTrue(v1.follows(v2));
    }

    @Test
    void followsDifferentVectorsCorrect() {
        Vector2d v1 = new Vector2d(2, 5);
        Vector2d v2 = new Vector2d(1, 3);
        assertTrue(v1.follows(v2));
    }

    @Test
    void followsDifferentVectorsIncorrect() {
        Vector2d v1 = new Vector2d(1, 3);
        Vector2d v2 = new Vector2d(2, 5);
        assertFalse(v1.follows(v2));
    }

    @Test
    void followsNeitherVectorFollows() {
        Vector2d v1 = new Vector2d(1, 3);
        Vector2d v2 = new Vector2d(0, 5);
        assertFalse(v1.follows(v2));
    }

    @Test
    void upperRightIsV1() {
        Vector2d v1 = new Vector2d(2, 5);
        Vector2d v2 = new Vector2d(1, -3);
        Vector2d expectedResult = new Vector2d(2, 5);
        Vector2d result = v1.upperRight(v2);
        assertEquals(expectedResult, result);
    }

    @Test
    void upperRightIsCombination() {
        Vector2d v1 = new Vector2d(2, 5);
        Vector2d v2 = new Vector2d(1, 10);
        Vector2d expectedResult = new Vector2d(2, 10);
        Vector2d result = v1.upperRight(v2);
        assertEquals(expectedResult, result);
    }

    @Test
    void lowerLeftIsV1() {
        Vector2d v1 = new Vector2d(1, -3);
        Vector2d v2 = new Vector2d(2, 5);
        Vector2d expectedResult = new Vector2d(1, -3);
        Vector2d result = v1.lowerLeft(v2);
        assertEquals(expectedResult, result);
    }

    @Test
    void lowerLeftIsCombination() {
        Vector2d v1 = new Vector2d(2, 5);
        Vector2d v2 = new Vector2d(1, 10);
        Vector2d expectedResult = new Vector2d(1, 5);
        Vector2d result = v1.lowerLeft(v2);
        assertEquals(expectedResult, result);
    }

    @Test
    void add() {
        Vector2d v1 = new Vector2d(2, 5);
        Vector2d v2 = new Vector2d(1, -3);
        Vector2d expectedResult = new Vector2d(3, 2);
        Vector2d result = v1.add(v2);
        assertEquals(expectedResult, result);
    }

    @Test
    void subtract() {
        Vector2d v1 = new Vector2d(2, 5);
        Vector2d v2 = new Vector2d(1, -3);
        Vector2d expectedResult = new Vector2d(1, 8);
        Vector2d result = v1.subtract(v2);
        assertEquals(expectedResult, result);
    }

    @Test
    void opposite() {
        Vector2d v = new Vector2d(2, 5);
        Vector2d expectedResult = new Vector2d(-2, -5);
        Vector2d result = v.opposite();
        assertEquals(expectedResult, result);
    }

    @Test
    void oppositeZeroVector() {
        Vector2d v = new Vector2d(0, 0);
        Vector2d expectedResult = new Vector2d(0, 0);
        Vector2d result = v.opposite();
        assertEquals(expectedResult, result);
    }
}