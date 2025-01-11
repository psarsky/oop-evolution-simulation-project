package proj.model.vegetation;

import proj.util.Vector2d;

public class ForestedEquator extends AbstractVegetationVariant{
    private final Vector2d equatorLowerLeft;
    private final Vector2d equatorUpperRight;

    // constructor
    public ForestedEquator(int equatorHeight, int mapWidth, int mapHeight) {
        this.equatorLowerLeft = new Vector2d(0, (mapHeight - equatorHeight) / 2 + 1);
        this.equatorUpperRight = new Vector2d(mapWidth - 1, (this.equatorLowerLeft.y() + equatorHeight - 1));
    }

    // utilities
    @Override
    public boolean preferred(Vector2d position) {
        return this.equatorLowerLeft.precedes(position)
                && this.equatorUpperRight.follows(position);
    }
}
