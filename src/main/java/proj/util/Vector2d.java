package proj.util;

import java.util.Objects;

/**
 * Represents an immutable 2D vector with integer coordinates (x, y).
 * Provides common vector operations like addition, subtraction, comparison,
 * and calculation of corners (upperRight, lowerLeft).
 * Implemented as a record for conciseness and immutability.
 *
 * @param x The horizontal coordinate of the vector.
 * @param y The vertical coordinate of the vector.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>, <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public record Vector2d(int x, int y) {

    // Record implicitly defines a constructor, getters (x(), y()), equals(), hashCode(), and toString().

    /**
     * Checks if this vector's coordinates are less than or equal to the
     * corresponding coordinates of another vector. Useful for boundary checks.
     *
     * @param other The non-null {@link Vector2d} to compare against.
     * @return {@code true} if this.x <= other.x AND this.y <= other.y, {@code false} otherwise.
     */
    public boolean precedes(Vector2d other) {
        Objects.requireNonNull(other, "Other vector cannot be null for precedes comparison.");
        return this.x <= other.x && this.y <= other.y;
    }

    /**
     * Checks if this vector's coordinates are greater than or equal to the
     * corresponding coordinates of another vector. Useful for boundary checks.
     *
     * @param other The non-null {@link Vector2d} to compare against.
     * @return {@code true} if this.x >= other.x AND this.y >= other.y, {@code false} otherwise.
     */
    public boolean follows(Vector2d other) {
        Objects.requireNonNull(other, "Other vector cannot be null for follows comparison.");
        return this.x >= other.x && this.y >= other.y;
    }

    /**
     * Adds another vector to this vector, returning a new {@code Vector2d}
     * representing the component-wise sum. (This vector remains unchanged).
     *
     * @param other The non-null {@link Vector2d} to add to this one.
     * @return A new {@link Vector2d} representing the sum (this.x + other.x, this.y + other.y).
     */
    public Vector2d add(Vector2d other) {
        Objects.requireNonNull(other, "Cannot add a null vector.");
        return new Vector2d(this.x + other.x, this.y + other.y);
    }

    /**
     * Subtracts another vector from this vector, returning a new {@code Vector2d}
     * representing the component-wise difference. (This vector remains unchanged).
     *
     * @param other The non-null {@link Vector2d} to subtract from this one.
     * @return A new {@link Vector2d} representing the difference (this.x - other.x, this.y - other.y).
     */
    public Vector2d subtract(Vector2d other) {
        Objects.requireNonNull(other, "Cannot subtract a null vector.");
        return new Vector2d(this.x - other.x, this.y - other.y);
    }

    /**
     * Calculates the upper-right corner of the bounding box defined by this vector and another vector.
     * The resulting vector's coordinates are the maximum of the corresponding coordinates of the two input vectors.
     *
     * @param other The non-null other {@link Vector2d} defining the bounding box.
     * @return A new {@link Vector2d} representing the upper-right corner (max(this.x, other.x), max(this.y, other.y)).
     */
    public Vector2d upperRight(Vector2d other) {
        Objects.requireNonNull(other, "Other vector cannot be null for upperRight calculation.");
        return new Vector2d(Math.max(this.x, other.x), Math.max(this.y, other.y));
    }

    /**
     * Calculates the lower-left corner of the bounding box defined by this vector and another vector.
     * The resulting vector's coordinates are the minimum of the corresponding coordinates of the two input vectors.
     *
     * @param other The non-null other {@link Vector2d} defining the bounding box.
     * @return A new {@link Vector2d} representing the lower-left corner (min(this.x, other.x), min(this.y, other.y)).
     */
    public Vector2d lowerLeft(Vector2d other) {
        Objects.requireNonNull(other, "Other vector cannot be null for lowerLeft calculation.");
        return new Vector2d(Math.min(this.x, other.x), Math.min(this.y, other.y));
    }

    /**
     * Returns the vector opposite to this one (negated components).
     *
     * @return A new {@link Vector2d} representing the opposite vector (-x, -y).
     */
    public Vector2d opposite() {
        return new Vector2d(-this.x, -this.y);
    }

    /**
     * Returns a string representation of the vector in the format "(x,y)".
     * This overrides the default record toString() only slightly for formatting.
     *
     * @return A {@link String} representing this vector, e.g., "(5,3)".
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + ")"; // Custom format
        // return "Vector2d[x=" + x + ", y=" + y + "]"; // Default record format
    }

    // equals() and hashCode() are automatically generated by the record based on x and y.
}