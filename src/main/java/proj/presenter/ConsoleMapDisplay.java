package proj.presenter;

import proj.model.maps.AbstractWorldMap;

public class ConsoleMapDisplay implements MapChangeListener {
    private int count = 0;

    @Override
    public synchronized void mapChanged(AbstractWorldMap worldMap, String message) {
        System.out.println("---------------------------------------------\n");
        System.out.println("Map ID: " + worldMap.getID());
        System.out.println("Update " + ++count + ": " + message);
        System.out.println(worldMap);
    }
}
