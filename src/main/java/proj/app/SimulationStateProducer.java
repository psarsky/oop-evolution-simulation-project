package proj.app;

import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.WaterWorld;
import proj.simulation.Simulation;
import proj.util.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Producer component in the producer-consumer pattern.
 * Periodically creates snapshots of the simulation state and adds them to the queue.
 */
public class SimulationStateProducer implements Runnable {
    private final Simulation simulation;
    private final StatisticsViewModel statisticsViewModel;
    private final SimulationStateQueue stateQueue;
    private volatile boolean running = true;
    private volatile Animal selectedAnimal;
    private static final long UPDATE_INTERVAL_MS = 50; // 50ms update interval

    public SimulationStateProducer(Simulation simulation,
                                   StatisticsViewModel statisticsViewModel,
                                   SimulationStateQueue stateQueue) {
        this.simulation = simulation;
        this.statisticsViewModel = statisticsViewModel;
        this.stateQueue = stateQueue;
    }

    /**
     * Sets the selected animal for including in snapshots.
     *
     * @param animal The currently selected animal
     */
    public void setSelectedAnimal(Animal animal) {
        this.selectedAnimal = animal;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Create a snapshot of the current simulation state
                createAndEnqueueSnapshot();

                // Sleep for a short interval to control production rate
                Thread.sleep(UPDATE_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            } catch (Exception e) {
                System.err.println("Error in simulation state producer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a snapshot of the current simulation state and adds it to the queue.
     */
    private void createAndEnqueueSnapshot() {
        AbstractWorldMap map = simulation.getMap();
        if (map != null) {
            // Get water fields if this is a WaterWorld
            Map<Vector2d, ?> waterFields = null;
            if (map instanceof WaterWorld) {
                synchronized (map) {
                    waterFields = new HashMap<>(((WaterWorld) map).getWaterFields());
                }
            }

            // Update statistics to get current top genotypes
            synchronized (statisticsViewModel) {
                statisticsViewModel.updateStatistics();
            }

            List<String> topGenotypes = statisticsViewModel.getTopGenotypes();

            // Create a deep copy of animals and plants with proper synchronization
            Map<Vector2d, List<Animal>> animalsCopy;
            Map<Vector2d, Plant> plantsCopy;

            synchronized (map) {
                animalsCopy = new HashMap<>();
                map.getAnimals().forEach((key, value) ->
                        animalsCopy.put(key, new ArrayList<>(value))
                );

                plantsCopy = new HashMap<>(map.getPlants());
            }

            // Create the snapshot with all the data
            SimulationStateSnapshot snapshot = new SimulationStateSnapshot(
                    animalsCopy,
                    plantsCopy,
                    waterFields,
                    topGenotypes,
                    selectedAnimal
            );

            // Add to the queue
            stateQueue.enqueue(snapshot);
        }
    }
}