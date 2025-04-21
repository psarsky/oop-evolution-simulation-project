package proj.app;

import proj.app.render.MapRenderer;
import proj.app.state.SimulationStateProducer;
import proj.app.state.SimulationStateQueue;
import proj.simulation.Simulation;

/**
 * A simple record to hold the primary components of a simulation instance
 * required for the UI layer. This acts as a container for passing these
 * components together.
 *
 * @param simulation           The core simulation logic instance.
 * @param simulationController The controller managing the simulation thread lifecycle.
 * @param statisticsManager    The manager for calculating and saving simulation statistics.
 * @param stateProducer        The background task producing simulation state snapshots.
 * @param stateQueue           The queue buffering state snapshots for the UI thread.
 * @param mapRenderer          The component responsible for drawing the simulation state onto the canvas.
 */
public record SimulationComponents(
        Simulation simulation,
        SimulationController simulationController,
        StatisticsManager statisticsManager,
        SimulationStateProducer stateProducer,
        SimulationStateQueue stateQueue,
        MapRenderer mapRenderer
) {
    // Records automatically provide a constructor, getters, equals, hashCode, toString,
    // and ensure field immutability (references won't change).
}