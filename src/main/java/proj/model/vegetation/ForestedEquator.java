package proj.model.vegetation;

import proj.util.Vector2d;

/**
 * Represents the "Forested Equator" variant of vegetation.
 * This class defines a central equatorial region on the map, where vegetation is more likely to grow.
 */
public class ForestedEquator extends AbstractVegetationVariant {
    private final Vector2d equatorLowerLeft; // Lower-left corner of the equatorial region
    private final Vector2d equatorUpperRight; // Upper-right corner of the equatorial region

    /**
     * Constructs a "Forested Equator" variant with the specified equator height and map dimensions.
     *
     * @param equatorHeight     the height of the equatorial region
     * @param mapWidth          the width of the map
     * @param mapHeight         the height of the map
     */
    public ForestedEquator(int equatorHeight, int mapWidth, int mapHeight) {
        // Calculate the bounds of the equatorial region
        this.equatorLowerLeft = new Vector2d(0, (mapHeight - equatorHeight) / 2 + 1);
        this.equatorUpperRight = new Vector2d(mapWidth - 1, (this.equatorLowerLeft.y() + equatorHeight - 1));
    }

    /**
     * Determines if a given position is within the preferred equatorial region.
     *
     * @param position          the position to check
     * @return                  true if the position is within the equatorial region, false otherwise
     */
    @Override
    public boolean preferred(Vector2d position) {
        // Check if the position is within the bounds of the equatorial region
        return this.equatorLowerLeft.precedes(position)
                && this.equatorUpperRight.follows(position);
    }

    /**
     * Returns field type (equator / non-equator).
     *
     * @param position          the position to check
     * @return                  "equator" if the position is within the equatorial region, "non-equator" otherwise
     */
    @Override
    public String getFieldType(Vector2d position) {return preferred(position) ? "equator" : "non-equator";}
}