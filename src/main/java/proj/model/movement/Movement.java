package proj.model.movement;

import proj.model.elements.Animal;
import proj.model.maps.MoveValidator;

/**
 * Interface representing a movement variant that is utilized in the animal movement.
 * Implementations of this interface define specific ways to modify movement basing different simulation properties.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public interface Movement {

    /**
     * Defines movement logic according to the selected variant.
     *
     * @param animal    The {@link Animal} to move on the map.
     * @param validator A {@link MoveValidator} object that verifies the validity of the potential new position.
     */
    void move(Animal animal, MoveValidator validator);
}
