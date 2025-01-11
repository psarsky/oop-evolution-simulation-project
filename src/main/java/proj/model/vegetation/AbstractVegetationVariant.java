package proj.model.vegetation;

import proj.util.Vector2d;

import java.util.Random;

public abstract class AbstractVegetationVariant {
    private final Random random = new Random();
    public boolean validatePlantPosition(Vector2d position) {return preferred(position) == (this.random.nextInt(5) != 4);}
    abstract boolean preferred(Vector2d position);
}
