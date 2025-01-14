package proj.model.movement;

import proj.model.elements.Animal;
import proj.model.maps.MoveValidator;

public interface Movement {
    void move(Animal animal, MoveValidator validator);
}
