package proj.model.vegetation;

import proj.util.Vector2d;
import java.util.Objects;

/**
 * Implements the "Forested Equator" vegetation variant.
 * In this variant, a horizontal band across the center of the map (the "equator")
 * is designated as the preferred area for plant growth. Plants are more likely
 * to spawn and potentially thrive within this region compared to areas outside it.
 * Extends {@link AbstractVegetationVariant}.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public class ForestedEquator extends AbstractVegetationVariant {

    private final Vector2d equatorLowerLeft;    // Lower-left corner (inclusive) of the equatorial region.
    private final Vector2d equatorUpperRight;   // Upper-right corner (inclusive) of the equatorial region.

    /**
     * Constructs a {@code ForestedEquator} variant. Calculates the boundaries of the
     * equatorial region based on the provided map dimensions and equator height.
     *
     * @param equatorHeight The desired height (number of rows) of the equatorial band. Must be non-negative and not exceed map height.
     * @param mapWidth      The total width of the map. Must be positive.
     * @param mapHeight     The total height of the map. Must be positive.
     * @throws IllegalArgumentException if dimensions or equatorHeight are invalid.
     */
    public ForestedEquator(int equatorHeight, int mapWidth, int mapHeight) {
        if (mapWidth <= 0 || mapHeight <= 0) {
            throw new IllegalArgumentException("Map dimensions must be positive.");
        }
        if (equatorHeight < 0 || equatorHeight > mapHeight) {
            throw new IllegalArgumentException("Equator height must be between 0 and map height (inclusive).");
        }

        // Calculate vertical start (y-coordinate) of the equator, centering it
        int equatorStartY = Math.max(0, (mapHeight - equatorHeight) / 2);
        // Calculate vertical end (y-coordinate) - ensure it doesn't exceed map height
        // The end Y is startY + height - 1 (inclusive)
        int equatorEndY = Math.min(mapHeight - 1, equatorStartY + equatorHeight - 1);
        // Handle case where equatorHeight is 0
        if (equatorHeight == 0) {
            equatorStartY = -1; // Ensure no position matches
            equatorEndY = -1;
        }


        // Define the bounding box of the equator
        this.equatorLowerLeft = new Vector2d(0, equatorStartY);
        this.equatorUpperRight = new Vector2d(mapWidth - 1, equatorEndY);
    }

    /**
     * Determines if a given position lies within the calculated equatorial region (inclusive).
     * This defines the "preferred" area for this variant.
     *
     * @param position A non-null {@link Vector2d} representing the position to check.
     * @return {@code true} if the position's y-coordinate is within the equator's start and end rows, {@code false} otherwise.
     */
    @Override
    boolean isPreferred(Vector2d position) {
        Objects.requireNonNull(position, "Position cannot be null");
        // Check if position is within the pre-calculated bounds
        // 'precedes' checks <= for both x and y, 'follows' checks >= for both x and y.
        // Since equator spans full width, we only need to check Y against the stored bounds.
        return position.y() >= this.equatorLowerLeft.y() && position.y() <= this.equatorUpperRight.y();
        // return this.equatorLowerLeft.precedes(position) && this.equatorUpperRight.follows(position); // Alternative using Vector2d methods
    }

    /**
     * Classifies the field type based on whether it falls within the equator zone.
     *
     * @param position A non-null {@link Vector2d} representing the position to classify.
     * @return "Equator" if the position is within the preferred equatorial region, "Non-Equator" otherwise.
     */
    @Override
    String getFieldType(Vector2d position) {
        return isPreferred(position) ? "Equator" : "Non-Equator";
    }
}