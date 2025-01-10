package proj.model.elements;

import proj.util.Vector2d;

public interface WorldElement {

    default boolean isAt(Vector2d position) {return this.getPos().equals(position);}

    Vector2d getPos();

    ElementType getElementType();
}
