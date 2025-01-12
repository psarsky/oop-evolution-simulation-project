package proj.util;

/**
 * Represents a rectangular boundary defined by two corner points:
 * the lower-left corner and the upper-right corner.
 * This class can be used to define areas or limits within the simulation world.
 *
 * @param lowerLeft         The lower-left corner of the boundary as a Vector2d
 * @param upperRight        The upper-right corner of the boundary as a Vector2d
 */
public record Boundary(Vector2d lowerLeft, Vector2d upperRight) {}