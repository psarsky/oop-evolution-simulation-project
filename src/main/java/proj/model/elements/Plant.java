/*
todo:
add JavaFX display properties
*/

package proj.model.elements;

import proj.util.Vector2d;

public class Plant implements WorldElement {
    private final Vector2d position;

    // constructor
    public Plant(Vector2d position) {this.position = position;}

    @Override
    public String toString() {
        return "*";
        // return " "; // for testing purposes - less visual clutter
    }

    // getters
    @Override
    public Vector2d getPos() {return this.position;}
    @Override
    public ElementType getElementType() {return ElementType.PLANT;}
}
