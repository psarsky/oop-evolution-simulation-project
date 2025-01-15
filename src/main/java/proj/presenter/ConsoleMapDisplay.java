package proj.presenter;

import proj.model.maps.AbstractWorldMap;

/**
 * The ConsoleMapDisplay class implements the {@link MapChangeListener} interface
 * to display updates of the map in the console. Each change in the map triggers
 * a synchronized method to print the current state of the map along with a message.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class ConsoleMapDisplay implements MapChangeListener {
    private int count = 0; // Counter to track the number of map updates

    /**
     * Handles map updates and prints the map's current state to the console.
     * This method is synchronized to ensure thread safety in concurrent environments.
     *
     * @param worldMap          The map that has been modified.
     * @param message           A message describing the nature of the change.
     */
    @Override
    public synchronized void mapChanged(AbstractWorldMap worldMap, String message) {
        System.out.println("---------------------------------------------\n");
        System.out.println("Map ID: " + worldMap.getID());
        System.out.println("Update " + ++count + ": " + message);
        System.out.println(worldMap);
    }
}