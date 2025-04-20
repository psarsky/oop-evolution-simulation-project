package proj.presenter;

import proj.model.maps.AbstractWorldMap;

/**
 * Interface for observers that need to be notified when the state of an
 * {@link AbstractWorldMap} changes. Implementing classes can react to map
 * updates, such as element movements, placements, or removals.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public interface MapChangeListener {

    /**
     * Callback method invoked when a change occurs on the observed map.
     *
     * @param worldMap The {@link AbstractWorldMap} instance that has changed.
     * @param message  A descriptive {@link String} message detailing the nature of the change (e.g., "Animal moved", "Plant spawned").
     */
    void mapChanged(AbstractWorldMap worldMap, String message);
}