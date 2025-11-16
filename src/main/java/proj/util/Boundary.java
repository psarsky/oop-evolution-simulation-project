package proj.util;

import java.util.Objects;

/**
 * Represents an immutable rectangular boundary defined by two corner points:
 * the lower-left and upper-right corners, using {@link Vector2d} objects.
 * Useful for defining map limits or areas of interest.
 * Implemented as a record for conciseness and inherent immutability.
 *
 * @param lowerLeft  The non-null {@link Vector2d} representing the lower-left corner (inclusive) of the boundary.
 * @param upperRight The non-null {@link Vector2d} representing the upper-right corner (inclusive) of the boundary.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public record Boundary(Vector2d lowerLeft, Vector2d upperRight) {
    /**
     * Canonical constructor for the Boundary record.
     * Ensures that the provided corner vectors are not null.
     * Does not enforce lowerLeft.precedes(upperRight), but this is usually expected.
     *
     * @param lowerLeft The lower-left corner vector.
     * @param upperRight The upper-right corner vector.
     * @throws NullPointerException if lowerLeft or upperRight is null.
     */
    public Boundary {
        Objects.requireNonNull(lowerLeft, "LowerLeft boundary vector cannot be null");
        Objects.requireNonNull(upperRight, "UpperRight boundary vector cannot be null");
        // Optional validation: if (!lowerLeft.precedes(upperRight)) throw new IllegalArgumentException("LowerLeft must precede UpperRight");
    }
}