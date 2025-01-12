package proj.util;

/**
 * Represents a 2D vector with integer coordinates.
 * Provides utility methods for common vector operations.
 *
 * @param x         The x-coordinate of the vector
 * @param y         The y-coordinate of the vector
 */
public record Vector2d(int x, int y) {

    /**
     * Checks if this vector precedes another vector.
     * A vector is considered to precede another if its x and y coordinates are
     * less than or equal to the corresponding coordinates of the other vector.
     *
     * @param other         The other vector to compare with
     * @return              True if this vector precedes the other vector, false otherwise
     */
    public boolean precedes(Vector2d other) {
        return x <= other.x && y <= other.y;
    }

    /**
     * Checks if this vector follows another vector.
     * A vector is considered to follow another if its x and y coordinates are
     * greater than or equal to the corresponding coordinates of the other vector.
     *
     * @param other         The other vector to compare with
     * @return              True if this vector follows the other vector, false otherwise
     */
    public boolean follows(Vector2d other) {
        return x >= other.x && y >= other.y;
    }

    /**
     * Adds this vector to another vector.
     * The result is a new vector whose x and y coordinates are the sums of the
     * corresponding coordinates of the two vectors.
     *
     * @param other         The vector to add
     * @return              A new vector representing the sum of the two vectors
     */
    public Vector2d add(Vector2d other) {
        return new Vector2d(x + other.x, y + other.y);
    }

    /**
     * Subtracts another vector from this vector.
     * The result is a new vector whose x and y coordinates are the differences
     * between the corresponding coordinates of this vector and the other vector.
     *
     * @param other         The vector to subtract
     * @return              A new vector representing the difference between the two vectors
     */
    public Vector2d subtract(Vector2d other) {
        return new Vector2d(x - other.x, y - other.y);
    }

    /**
     * Returns a vector that represents the upper-right corner of the rectangle
     * defined by this vector and another vector.
     * The x and y coordinates of the resulting vector are the maximum values
     * between the corresponding coordinates of the two vectors.
     *
     * @param other         The other vector
     * @return              A new vector representing the upper-right corner
     */
    public Vector2d upperRight(Vector2d other) {
        return new Vector2d(Math.max(x, other.x), Math.max(y, other.y));
    }

    /**
     * Returns a vector that represents the lower-left corner of the rectangle
     * defined by this vector and another vector.
     * The x and y coordinates of the resulting vector are the minimum values
     * between the corresponding coordinates of the two vectors.
     *
     * @param other         The other vector
     * @return              A new vector representing the lower-left corner
     */
    public Vector2d lowerLeft(Vector2d other) {
        return new Vector2d(Math.min(x, other.x), Math.min(y, other.y));
    }

    /**
     * Returns the opposite vector.
     * The x and y coordinates of the resulting vector are the negations of the
     * corresponding coordinates of this vector.
     *
     * @return          A new vector representing the opposite of this vector
     */
    public Vector2d opposite() {
        return new Vector2d(-x, -y);
    }

    /**
     * Returns a string representation of the vector.
     * The string is formatted as "(x, y)".
     *
     * @return          A string representing this vector
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}