package proj.app;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread-safe queue for simulation state snapshots.
 * Acts as a buffer between simulation thread and UI rendering thread.
 */
public class SimulationStateQueue {
    // Use a blocking queue with limited capacity to prevent memory issues
    private final BlockingQueue<SimulationStateSnapshot> stateQueue;
    private final int maxQueueSize;

    public SimulationStateQueue(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
        this.stateQueue = new LinkedBlockingQueue<>(maxQueueSize);
    }

    /**
     * Adds a new state snapshot to the queue.
     * If the queue is full, the oldest snapshot is dropped.
     *
     * @param snapshot The state snapshot to add
     */
    public void enqueue(SimulationStateSnapshot snapshot) {
        try {
            // If queue is full, remove oldest snapshot
            if (stateQueue.size() >= maxQueueSize) {
                stateQueue.poll();
            }
            stateQueue.put(snapshot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while adding simulation state to queue");
        }
    }

    /**
     * Gets the next available state snapshot from the queue.
     * If no snapshot is available, returns null.
     *
     * @return The next state snapshot or null if none available
     */
    public SimulationStateSnapshot dequeue() {
        return stateQueue.poll();
    }

    /**
     * Checks if the queue has any snapshots available.
     *
     * @return true if there are snapshots, false otherwise
     */
    public boolean hasSnapshots() {
        return !stateQueue.isEmpty();
    }
}