package proj.app;

import proj.simulation.Simulation;

/**
 * Controller class for managing simulation execution.
 * Responsible for starting, pausing, and controlling the simulation.
 */
public class SimulationController {
    private final Simulation simulation;


    /**
     * Creates a new SimulationController.
     *
     * @param simulation The simulation to control
     */
    public SimulationController(Simulation simulation, StatisticsManager statisticsManager) {
        this.simulation = simulation;

        // Listen for day end events and save statistics
        simulation.addDayEndListener(() -> {
            if (statisticsManager.isCollectingData()) {
                statisticsManager.saveCurrentDayStatistics();
            }
        });
    }

    /**
     * Starts the simulation.
     */
    public void startSimulation() {
        simulation.start();
    }

    /**
     * Toggles the pause state of the simulation.
     */
    public void togglePause() {
        simulation.togglePause();
    }
}