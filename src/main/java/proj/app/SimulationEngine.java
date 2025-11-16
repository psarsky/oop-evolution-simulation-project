// ============================================================
// SOURCE FILE: proj/app/SimulationController.java
// ============================================================

package proj.app;

import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages the execution lifecycle of a {@link Simulation} instance.
 * This controller runs the simulation loop in a separate thread, allowing the main application
 * (e.g., UI) to remain responsive. It handles starting, stopping, pausing, and resuming
 * the simulation based on external commands. It also allows dynamic adjustment of the
 * time delay between simulation steps via the {@link #setStepDelay(long)} method.
 */
public class SimulationEngine implements Runnable {

    private final Simulation simulation; // The simulation logic instance to control
    private final SimulationProperties simulationProperties; // Configuration properties

    private volatile boolean running = false; // Is the simulation loop actively advancing days? (vs paused)
    private volatile boolean stopped = false; // Has a permanent stop been requested?
    private Thread simulationThread;      // The background thread executing the simulation loop

    // Use AtomicLong for thread-safe updates and reads of the step delay from UI thread
    private final AtomicLong currentStepDelay;
    // Define reasonable bounds for the delay to prevent excessively fast or slow steps
    private static final long MIN_STEP_DELAY_MS = 10;   // Minimum delay (fastest speed) in milliseconds
    private static final long MAX_STEP_DELAY_MS = 2000; // Maximum delay (slowest speed) in milliseconds


    /**
     * Constructs a SimulationController. Initializes the step delay based on properties,
     * clamping the value within defined minimum and maximum bounds.
     *
     * @param simulation           The {@link Simulation} instance containing the core logic and state. Must not be null.
     * @param simulationProperties The {@link SimulationProperties} containing configuration like initial step delay. Must not be null.
     */
    public SimulationEngine(Simulation simulation, SimulationProperties simulationProperties) {
        this.simulation = Objects.requireNonNull(simulation, "Simulation cannot be null");
        this.simulationProperties = Objects.requireNonNull(simulationProperties, "SimulationProperties cannot be null");
        // Initialize delay from properties, ensuring it's within the allowed bounds
        long initialDelay = Math.max(MIN_STEP_DELAY_MS, Math.min(MAX_STEP_DELAY_MS, simulationProperties.getSimulationStep()));
        this.currentStepDelay = new AtomicLong(initialDelay);
    }

    /**
     * Starts the simulation execution in a new background thread.
     * If the simulation thread is already running or has been stopped, this method does nothing.
     * The simulation begins in a running state immediately after the thread starts.
     */
    public synchronized void start() {
        if (simulationThread == null || !simulationThread.isAlive()) {
            if (stopped) { // Do not restart if explicitly stopped
                System.out.println("SimulationController: Start ignored, simulation was already stopped.");
                return;
            }
            stopped = false;
            running = true; // Start in running state
            String threadName = "SimControl-" + simulationProperties.getConfigName();
            simulationThread = new Thread(this, threadName);
            simulationThread.setDaemon(true); // Allow JVM to exit even if this thread is running
            simulationThread.start();
            System.out.println("SimulationController started thread: " + threadName);
        } else {
            System.out.println("SimulationController: Start ignored, thread already running.");
        }
    }

    /**
     * Signals the simulation loop to stop execution permanently.
     * Sets the `stopped` flag and interrupts the simulation thread to potentially wake it from sleep.
     * The thread will exit gracefully after completing its current step or sleep cycle.
     */
    public synchronized void stop() {
        if (!stopped) {
            System.out.println("SimulationController stop requested for thread: " + (simulationThread != null ? simulationThread.getName() : "N/A"));
            stopped = true;
            running = false; // Ensure it's not considered running after stop request
            // Interrupting can speed up shutdown if thread is sleeping
            if (simulationThread != null && simulationThread.isAlive()) {
                simulationThread.interrupt();
            }
        }
    }

    /**
     * Toggles the simulation's execution state between running and paused.
     * If the simulation has been permanently stopped, this method has no effect.
     */
    public synchronized void togglePause() {
        if (!stopped) {
            running = !running;
            System.out.println("SimulationController " + (running ? "resumed" : "paused") + ".");
        } else {
            System.out.println("SimulationController: Toggle pause ignored, simulation is stopped.");
        }
    }

    /**
     * Checks if the simulation is currently configured to actively run (i.e., not paused and not stopped).
     * Note: Even if true, the simulation thread might be momentarily sleeping between steps.
     *
     * @return {@code true} if the simulation is not paused and not stopped, {@code false} otherwise.
     */
    public synchronized boolean isRunning() {
        return running && !stopped;
    }

    /**
     * Checks if the simulation has been permanently stopped via the {@link #stop()} method.
     *
     * @return {@code true} if stop() has been called, {@code false} otherwise.
     */
    public synchronized boolean isStopped() {
        return stopped;
    }

    /**
     * Dynamically sets the time delay (in milliseconds) between simulation steps.
     * The provided value is automatically clamped between the predefined minimum
     * ({@value #MIN_STEP_DELAY_MS}) and maximum ({@value #MAX_STEP_DELAY_MS}) delay bounds.
     * This method is thread-safe and designed to be called from external threads (like the UI thread).
     *
     * @param newDelayMs The desired delay between simulation steps in milliseconds.
     */
    public void setStepDelay(long newDelayMs) {
        long clampedDelay = Math.max(MIN_STEP_DELAY_MS, Math.min(MAX_STEP_DELAY_MS, newDelayMs));
        this.currentStepDelay.set(clampedDelay);
        // System.out.println("Simulation step delay set to: " + clampedDelay + " ms"); // Optional debug log
    }


    /**
     * The main execution loop run by the simulation thread.
     * It repeatedly checks the `stopped` and `running` flags. When running, it calls
     * {@link Simulation#advanceDay()} to perform one simulation step. After each check or step,
     * it sleeps for the duration specified by `currentStepDelay` (which can be updated dynamically).
     * The loop terminates when the `stopped` flag becomes true (either by external call to `stop()`
     * or due to an internal condition like interruption or simulation end).
     */
    @Override
    public void run() {
        System.out.println("Simulation Controller thread [" + Thread.currentThread().getName() + "] executing run().");

        while (!stopped) {
            if (running) {
                try {
                    // --- Execute one simulation step ---
                    simulation.advanceDay();

                    // --- Check if simulation ended naturally (e.g., no animals left) ---
                    if (simulation.getAnimals().isEmpty()) { // Safe to check size of synchronized list
                        System.out.println("Simulation Controller: Stopping run loop - no animals remaining.");
                        stopped = true; // Set stopped flag if simulation ends itself
                        running = false; // Ensure it's not considered running
                        // The dayEndListener in UI controller should handle disabling UI elements
                    }

                } catch (Exception e) {
                    // Catch unexpected errors during the simulation step
                    System.err.println("!!! Critical error during simulation step in thread [" + Thread.currentThread().getName() + "]: " + e.getMessage());
                    e.printStackTrace();
                    stopped = true; // Stop controller on critical error
                    running = false;
                    // Consider notifying the UI thread about the critical error
                }
            } // End if(running)

            // --- Sleep until the next step/check ---
            // Sleep even if paused (running=false) to avoid busy-waiting
            try {
                // Read the current delay safely from AtomicLong
                long delayToUse = currentStepDelay.get();
                Thread.sleep(delayToUse); // Use the potentially updated delay
            } catch (InterruptedException e) {
                // If interrupted (likely by stop() method or external source)
                Thread.currentThread().interrupt(); // Re-set interrupt flag
                // If stop() wasn't called explicitly, treat interrupt as a stop signal
                if (!stopped) {
                    System.out.println("Simulation Controller thread [" + Thread.currentThread().getName() + "] interrupted unexpectedly during sleep, stopping.");
                    stopped = true;
                } else {
                    System.out.println("Simulation Controller thread [" + Thread.currentThread().getName() + "] interrupted during sleep (stop requested).");
                }
                running = false; // Ensure not running after interrupt
            }
        } // End of while (!stopped) loop

        // Final log message when the thread exits the loop
        System.out.println("Simulation Controller thread [" + Thread.currentThread().getName() + "] finished.");
    }

    /**
     * Gets the underlying {@link Simulation} instance managed by this controller.
     * Useful for components that need direct access to simulation state (e.g., statistics, rendering).
     * @return The {@link Simulation} instance.
     */
    public Simulation getSimulation() {
        return simulation;
    }

    /**
     * Gets the background thread executing the simulation loop.
     * Can be used to check thread status or potentially `join()` it. Direct manipulation is discouraged.
     * @return The simulation {@link Thread}, or {@code null} if not started or already terminated.
     */
    public synchronized Thread getSimulationThread() {
        return simulationThread;
    }

    /**
     * Waits indefinitely for the simulation thread managed by this controller to complete its execution.
     * This method blocks the calling thread (e.g., the main application thread) until the simulation
     * stops naturally (e.g., no animals left) or is stopped explicitly via the {@link #stop()} method.
     * This is useful in console applications to prevent the main thread from exiting before the simulation finishes.
     * It has no effect if the simulation thread has not been started or has already finished.
     */
    public void waitUntilFinished() {
        Thread thread = getSimulationThread(); // Get thread reference safely
        if (thread != null && thread.isAlive()) {
            try {
                System.out.println("Main thread waiting for simulation controller [" + thread.getName() + "] to finish...");
                thread.join(); // Blocks until the simulation thread terminates
                System.out.println("Simulation controller [" + thread.getName() + "] finished.");
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted while waiting for simulation controller.");
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        } else {
            System.out.println("Simulation controller thread already finished or not started, no need to wait.");
        }
    }
}