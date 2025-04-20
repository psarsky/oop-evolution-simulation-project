package proj.app.state;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * A thread-safe, bounded, double-ended queue (deque) designed to hold {@link SimulationStateSnapshot} objects.
 * It acts as a buffer between the {@link SimulationStateProducer} thread (which adds snapshots)
 * and the UI rendering thread (which consumes snapshots).
 * <p>
 * When the queue is full, adding a new element (enqueue) causes the oldest element (at the head) to be dropped.
 * Elements are consumed (dequeued) from the head in FIFO order.
 * </p>
 */
public class SimulationStateQueue {

    private final BlockingDeque<SimulationStateSnapshot> stateQueue;
    private final int maxQueueSize;

    /**
     * Constructs a {@code SimulationStateQueue} with a specified maximum capacity.
     *
     * @param maxQueueSize The maximum number of snapshots the queue can hold before dropping the oldest. Must be positive.
     */
    public SimulationStateQueue(int maxQueueSize) {
        if (maxQueueSize <= 0) {
            throw new IllegalArgumentException("maxQueueSize must be positive");
        }
        this.maxQueueSize = maxQueueSize;
        // Use LinkedBlockingDeque which implements BlockingDeque interface
        this.stateQueue = new LinkedBlockingDeque<>(maxQueueSize);
    }

    /**
     * Adds a new state snapshot to the tail (end) of the deque.
     * If the deque is currently full (at max capacity), this operation first removes
     * the element at the head (oldest snapshot) before adding the new snapshot to the tail.
     * This is a non-blocking operation.
     *
     * @param snapshot The {@link SimulationStateSnapshot} to add. If null, the operation is ignored.
     */
    public void enqueue(SimulationStateSnapshot snapshot) {
        if (snapshot == null) {
            return; // Ignore null snapshots
        }
        // If the queue is full, pollFirst() removes the head.
        // This loop ensures space is made *before* adding the new element.
        while (stateQueue.size() >= maxQueueSize) {
            stateQueue.pollFirst(); // Remove the oldest element from the head
        }
        // offerLast adds to the tail, non-blocking, returns false if couldn't add (shouldn't happen here)
        stateQueue.offerLast(snapshot);
    }

    /**
     * Retrieves and removes the state snapshot from the head (beginning) of the deque (FIFO order).
     * If the deque is empty, this method returns {@code null}.
     * This is a non-blocking operation.
     *
     * @return The snapshot at the head of the deque, or {@code null} if the deque is empty.
     */
    public SimulationStateSnapshot dequeue() {
        // pollFirst retrieves and removes the head, returns null if empty
        return stateQueue.pollFirst();
    }

    /**
     * Retrieves, but does not remove, the most recently added snapshot (the element at the tail of the deque).
     * If the deque is empty, this method returns {@code null}.
     * This can be useful for getting the latest state without consuming it, e.g., for immediate redraws.
     *
     * @return The snapshot at the tail of the deque, or {@code null} if the deque is empty.
     */
    public SimulationStateSnapshot peekLast() {
        // peekLast retrieves but does not remove the tail, returns null if empty
        return stateQueue.peekLast();
    }

    /**
     * Checks if the deque currently contains any snapshots.
     *
     * @return {@code true} if the deque is not empty, {@code false} otherwise.
     */
    public boolean hasSnapshots() {
        return !stateQueue.isEmpty();
    }

    /**
     * Returns the current number of snapshots in the queue.
     *
     * @return The number of elements in the queue.
     */
    public int size() {
        return stateQueue.size();
    }
}