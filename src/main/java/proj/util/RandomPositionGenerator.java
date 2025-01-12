package proj.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * The RandomPositionGenerator class generates a list of random, unique positions
 * within a defined width and height. It implements {@link Iterable}, allowing
 * iteration over the generated positions.
 */
public class RandomPositionGenerator implements Iterable<Vector2d> {
    private final List<Vector2d> positions = new ArrayList<>();

    /**
     * Constructs a RandomPositionGenerator to generate random positions within
     * a specified area and count.
     *
     * @param maxWidth          The maximum width of the area.
     * @param maxHeight         The maximum height of the area.
     * @param count             The number of random positions to generate.
     */
    public RandomPositionGenerator(int maxWidth, int maxHeight, int count) {
        int[] indices = new int[maxWidth * maxHeight];
        for (int i = 0; i < indices.length; i++) indices[i] = i;

        Random rand = new Random();
        for (int i = indices.length - 1; i > indices.length - count - 2; i--) {
            int j = rand.nextInt(i); // Random index for swapping
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
        }

        for (int i = indices.length - 1; i > indices.length - count - 2; i--) {
            this.positions.add(new Vector2d(indices[i] % maxWidth, indices[i] / maxWidth));
        }
    }

    // Getters

    /**
     * Returns an iterator to iterate over the generated random positions.
     *
     * @return          An iterator for the random positions.
     */
    public Iterator<Vector2d> iterator() {
        return new RandomPositionIterator(this);
    }

    /**
     * Returns the first position in the list of generated positions.
     *
     * @return          The first {@link Vector2d} position.
     */
    public Vector2d getHead() {
        return this.positions.get(0);
    }

    /**
     * Returns the last position in the list of generated positions.
     *
     * @return          The last {@link Vector2d} position.
     */
    public Vector2d getTail() {
        return this.positions.get(this.positions.size() - 1);
    }

    /**
     * Returns the index of the specified position in the list.
     *
     * @param pos           The position to find.
     * @return              The index of the specified position, or -1 if not found.
     */
    public int getIndex(Vector2d pos) {
        return this.positions.indexOf(pos);
    }

    /**
     * Returns the position at the specified index in the list.
     *
     * @param index         The index of the position.
     * @return              The {@link Vector2d} at the specified index.
     */
    public Vector2d getPos(int index) {
        return this.positions.get(index);
    }
}

/**
 * The RandomPositionIterator class provides an iterator for the
 * {@link RandomPositionGenerator}, allowing traversal of the generated positions.
 */
class RandomPositionIterator implements Iterator<Vector2d> {
    private final RandomPositionGenerator generator;
    private Vector2d current;

    /**
     * Constructs a RandomPositionIterator for the specified generator.
     *
     * @param generator         The {@link RandomPositionGenerator} to iterate over.
     */
    RandomPositionIterator(RandomPositionGenerator generator) {
        this.current = generator.getHead();
        this.generator = generator;
    }

    /**
     * Returns the next position in the iteration.
     *
     * @return          The next {@link Vector2d} position.
     */
    public Vector2d next() {
        Vector2d data = this.current;
        int index = this.generator.getIndex(this.current) + 1;
        this.current = this.generator.getPos(index);
        return data;
    }

    /**
     * Checks if there are more positions to iterate over.
     *
     * @return          {@code true} if there are more positions; {@code false} otherwise.
     */
    public boolean hasNext() {
        return !this.current.equals(this.generator.getTail());
    }

    /**
     * This operation is not supported for this iterator.
     *
     * @throws          UnsupportedOperationException if invoked.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
