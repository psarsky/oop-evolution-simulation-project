package proj.presenter;

import proj.model.maps.AbstractWorldMap;

/**
 * The MapChangeListener interface defines a callback for responding to changes in the map.
 * Implementing classes can use this to update their state or perform actions
 * whenever the map undergoes modifications.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public interface MapChangeListener {
    /**
     * Called when the map has changed. This method provides a reference to the
     * updated map and an optional message describing the change.
     *
     * @param worldMap          The map that has been modified.
     * @param message           A message describing the nature of the change.
     */
    void mapChanged(AbstractWorldMap worldMap, String message);
}
