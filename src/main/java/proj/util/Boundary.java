package proj.util;

/**
 * Represents a rectangular boundary defined by two corner points:
 * the lower-left corner and the upper-right corner.
 * This class can be used to define areas or limits within the simulation world.
 *
 * @param lowerLeft     A {@link Vector2d} object representing the lower-left corner of the boundary.
 * @param upperRight    A {@link Vector2d} object representing the upper-right corner of the boundary.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public record Boundary(Vector2d lowerLeft, Vector2d upperRight) {}