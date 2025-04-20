// ============================================================
// SOURCE FILE: proj/World.java
// ============================================================
package proj;

import proj.app.SimulationController; // Import the controller
import proj.model.elements.Animal; // Import Animal for listener
import proj.model.genotype.Mutation;
import proj.model.genotype.MutationVariant;
import proj.model.genotype.RandomMutation;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.Globe;
import proj.model.maps.MapVariant;
import proj.model.maps.WaterWorld;
import proj.model.movement.AbstractMovementVariant;
import proj.model.movement.MovementVariant;
import proj.model.movement.OldAgeAintNoPicnic;
import proj.model.movement.PredestinedMovement;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.model.vegetation.ForestedEquator;
import proj.model.vegetation.VegetationVariant;
// import proj.presenter.ConsoleMapDisplay; // Keep commented out if not used
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

import java.util.List; // Import List

/**
 * Main class for running a console-based version of the evolution simulation.
 * Sets up simulation properties, creates the necessary simulation components
 * (map, variants), initializes the simulation logic and controller, and runs it.
 * Primarily used for testing or non-GUI execution.
 */
public class World {

    /**
     * Entry point for the console-based simulation application.
     * Configures simulation parameters, creates and starts the simulation controller,
     * adds a simple day-end console logger, and waits for the simulation to finish.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("START Console Simulation");

        // --- Simulation Configuration ---
        SimulationProperties simulationProperties = new SimulationProperties(
                "console_config",          // configName
                10,                        // genotypeSize
                MovementVariant.PREDESTINED, // movementVariant
                MutationVariant.RANDOM,    // mutationVariant
                MapVariant.GLOBE,          // mapVariant (using Globe for simpler console view)
                VegetationVariant.FORESTED_EQUATOR, // vegetationVariant
                20,                        // width
                10,                        // height
                4,                         // equatorHeight
                10,                        // animalCount
                20,                        // plantCount
                5,                         // plantsPerDay
                50,                        // startEnergy
                20,                        // plantEnergy
                30,                        // energyNeededToReproduce
                15,                        // energyToPassToChild
                1,                         // energyCostToMove
                200,                       // simulationStep (ms delay for console visibility)
                1,                         // minimumNumberOfMutations
                2,                         // maximumNumberOfMutations
                50,                        // waterViolence (N/A for Globe)
                false                      // saveStatisticsFlag
        );

        // 1. Create the Simulation logic instance using the factory method
        Simulation simulation = createSimulationInstance(simulationProperties);

        // 2. Create the SimulationController to manage execution
        SimulationController simulationController = new SimulationController(simulation, simulationProperties);

        // 3. Optional: Add a simple console output listener for day ends
        addConsoleDayEndListener(simulation, simulationProperties); // Pass simulationProperties if needed elsewhere by listener

        // 4. Start the simulation via the controller (runs in a background thread)
        simulationController.start();

        // 5. Wait for the simulation thread to complete its execution
        simulationController.waitUntilFinished();

        // 6. Simulation finished
        System.out.println("STOP Console Simulation");
    }

    /**
     * Factory method to create a configured {@link Simulation} instance based on properties.
     * Selects and instantiates the appropriate map, vegetation, movement, and mutation variants.
     *
     * @param simulationProperties The configuration properties for the simulation.
     * @return A configured {@link Simulation} instance.
     */
    private static Simulation createSimulationInstance(SimulationProperties simulationProperties) {
        // --- Instantiate Variants ---
        AbstractVegetationVariant vegetation = switch(simulationProperties.getVegetationVariant()) {
            case FORESTED_EQUATOR -> new ForestedEquator(
                    simulationProperties.getEquatorHeight(),
                    simulationProperties.getWidth(),
                    simulationProperties.getHeight()
            );
        };
        Mutation mutation = switch(simulationProperties.getMutationVariant()) {
            case RANDOM -> new RandomMutation();
        };
        AbstractMovementVariant movement = switch(simulationProperties.getMovementVariant()) {
            case PREDESTINED -> new PredestinedMovement();
            case OLD_AGE_AINT_NO_PICNIC -> new OldAgeAintNoPicnic();
        };

        // --- Instantiate Map ---
        AbstractWorldMap map = switch(simulationProperties.getMapVariant()) {
            case GLOBE -> new Globe(simulationProperties, vegetation, movement);
            case WATER_WORLD -> new WaterWorld(simulationProperties, vegetation, movement);
        };

        // --- Create Simulation ---
        return new Simulation(map, simulationProperties, mutation);
    }

    /**
     * Adds a simple listener to the simulation that prints basic info to the console at the end of each day.
     * Retrieves the current day directly from the simulation instance.
     *
     * @param simulation The simulation instance to listen to.
     * @param properties The simulation properties (passed in case listener logic needs other config values in future).
     */
    private static void addConsoleDayEndListener(Simulation simulation, SimulationProperties properties) {
        simulation.addDayEndListener(() -> {
            // This lambda runs in the SimulationController thread when notifyDayEndListeners is called.
            // Get current day directly from the simulation object's state
            int day = simulation.getCurrentDay(); // Use the correct getter
            System.out.println("--- Day " + day + " Ended ---");

            // Accessing simulation state (like getAnimals) should be done carefully.
            // The getter returns a synchronized list, but iteration needs external synchronization.
            List<Animal> currentAnimals = simulation.getAnimals();
            int animalCount;
            synchronized (currentAnimals) { // Synchronize block for safe iteration/access
                animalCount = currentAnimals.size();
                // If iterating: for (Animal a : currentAnimals) { ... }
            }
            System.out.println("Animals: " + animalCount);

            // Potentially print map state (consider performance impact)
            // synchronized(simulation.getMap()) { // Sync if map.toString() isn't internally safe
            //     System.out.println(simulation.getMap().toString());
            // }
            System.out.println("---------------");
        });
    }
}