package proj.model.vegetation;

import org.junit.jupiter.api.Test;
import proj.util.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class ForestedEquatorTest {

    @Test
    public void testIsPreferable() {
        ForestedEquator forestedEquator = new ForestedEquator(2, 10, 10);
        assertTrue(forestedEquator.preferred(new Vector2d(0, 5)));
        assertTrue(forestedEquator.preferred(new Vector2d(9, 5)));
        assertFalse(forestedEquator.preferred(new Vector2d(0, 4)));
        assertTrue(forestedEquator.preferred(new Vector2d(9, 6)));
    }

    @Test
    public void testGroundType(){
        ForestedEquator forestedEquator = new ForestedEquator(2, 10, 10);
        assertEquals("equator", forestedEquator.getFieldType(new Vector2d(0, 5)));
        assertEquals("equator", forestedEquator.getFieldType(new Vector2d(9, 5)));
        assertEquals("non-equator", forestedEquator.getFieldType(new Vector2d(0, 4)));
        assertEquals("equator", forestedEquator.getFieldType(new Vector2d(9, 6)));
    }
}