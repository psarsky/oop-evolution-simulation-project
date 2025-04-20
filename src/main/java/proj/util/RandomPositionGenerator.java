package proj.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException; // Import for iterator exception
import java.util.Random;

/**
 * Generates a specified number of unique, random {@link Vector2d} positions
 * within a defined rectangular area (width x height).
 * Uses the Fisher-Yates (Knuth) shuffle algorithm on indices to ensure uniqueness
 * and good random distribution without needing to check for collisions repeatedly.
 * Implements {@link Iterable} to allow easy iteration over the generated positions.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class RandomPositionGenerator implements Iterable<Vector2d> {

    private final List<Vector2d> positions; // Stores the generated unique positions

    /**
     * Constructs a {@code RandomPositionGenerator}. Generates `count` unique random positions
     * within the grid defined by `width` and `height`.
     *
     * @param width  The width of the grid (must be positive).
     * @param height The height of the grid (must be positive).
     * @param count  The number of unique random positions to generate (must be non-negative and not exceed width * height).
     * @throws IllegalArgumentException if width, height, or count are invalid.
     */
    public RandomPositionGenerator(int width, int height, int count) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and Height must be positive.");
        }
        int totalPositions = width * height;
        if (count < 0 || count > totalPositions) {
            throw new IllegalArgumentException("Count must be non-negative and cannot exceed total positions (" + totalPositions + ").");
        }

        // --- Generate unique positions using Fisher-Yates shuffle on indices ---
        List<Integer> allIndices = new ArrayList<>(totalPositions);
        for (int i = 0; i < totalPositions; i++) {
            allIndices.add(i);
        }

        // Shuffle the entire list of indices
        Collections.shuffle(allIndices, new Random());

        // Take the first 'count' shuffled indices and convert them to Vector2d
        this.positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int index = allIndices.get(i);
            int x = index % width; // Calculate x from index
            int y = index / width; // Calculate y from index (integer division)
            this.positions.add(new Vector2d(x, y));
        }
        // Ensure the list is unmodifiable if desired, although it's typically just iterated over once.
        // this.positions = Collections.unmodifiableList(this.positions);
    }

    /**
     * Returns an iterator over the generated sequence of unique random positions.
     * The order of positions returned by the iterator is fixed after generation.
     *
     * @return An {@link Iterator} for the generated {@link Vector2d} positions.
     */
    @Override
    public Iterator<Vector2d> iterator() {
        // Return an iterator for the generated list
        return this.positions.iterator();
        // return new RandomPositionIterator(this); // Keep if using the custom iterator class is preferred
    }


    /* --- Optional helper methods (kept from original, potentially less useful now) ---
       These might be less relevant if the primary use is the iterator,
       but kept here for compatibility if they were used elsewhere. */

    /**
     * (Optional) Returns the first position in the generated sequence.
     * @return The first {@link Vector2d} position, or null if count was 0.
     * @deprecated Consider using the iterator pattern instead.
     */
    @Deprecated
    public Vector2d getHead() {
        return this.positions.isEmpty() ? null : this.positions.getFirst();
    }

    /**
     * (Optional) Returns the last position in the generated sequence.
     * @return The last {@link Vector2d} position, or null if count was 0.
     * @deprecated Consider using the iterator pattern instead.
     */
    @Deprecated
    public Vector2d getTail() {
        return this.positions.isEmpty() ? null : this.positions.getLast();
    }

    /**
     * (Optional) Returns the index of a specific position within the generated sequence.
     * @param pos A {@link Vector2d} position to search for.
     * @return The index of the position, or -1 if not found in the generated sequence.
     * @deprecated Relying on index might be fragile; consider if position existence check is needed instead.
     */
    @Deprecated
    public int getIndex(Vector2d pos) {
        return this.positions.indexOf(pos);
    }

    /**
     * (Optional) Returns the position at a specific index in the generated sequence.
     * @param index The index of the desired position (0-based).
     * @return The {@link Vector2d} at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     * @deprecated Consider using the iterator pattern instead.
     */
    @Deprecated
    public Vector2d getPos(int index) {
        return this.positions.get(index); // Throws IndexOutOfBoundsException if invalid
    }

}

/* --- Custom Iterator (Alternative Implementation, likely not needed with Collections.shuffle) ---
   Kept commented out as the standard list iterator is usually sufficient after shuffling.

class RandomPositionIterator implements Iterator<Vector2d> {
    private final List<Vector2d> positions;
    private int currentIndex = 0;

    RandomPositionIterator(RandomPositionGenerator generator) {
        // Get a reference or copy of the generator's positions list
        this.positions = new ArrayList<>(generator.positions); // Use the internal list
    }

    @Override
    public boolean hasNext() {
        return currentIndex < positions.size();
    }

    @Override
    public Vector2d next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more positions in the generator.");
        }
        return positions.get(currentIndex++);
    }

    @Override
    public void remove() {
        // Typically not supported for this type of generator
        throw new UnsupportedOperationException("Remove operation is not supported by this iterator.");
    }
}
*/