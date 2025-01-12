package proj.presenter;

import proj.model.maps.AbstractWorldMap;
import proj.util.Vector2d;

/**
 * The MapVisualizer converts the {@link AbstractWorldMap} into a string
 * representation for easier visualization and debugging.
 *
 * This class provides methods to draw a specific region of the map and represents
 * its contents in a textual format, including grid lines, objects, and empty cells.
 *
 * @author apohllo, idzik
 */
public class MapVisualizer {
    // Constants for visualizing empty cells and grid lines
    private static final String EMPTY_CELL = " ";
    private static final String FRAME_SEGMENT = "-";
    private static final String CELL_SEGMENT = "|";
    private final AbstractWorldMap map;

    /**
     * Initializes the MapVisualizer with an instance of the map to visualize.
     *
     * @param map The map instance that will be visualized.
     */
    public MapVisualizer(AbstractWorldMap map) {
        this.map = map;
    }

    /**
     * Converts the selected region of the map into a string representation.
     * The region is defined by two corners: the lower left and the upper right.
     * It is assumed that map indices will be represented with up to two characters.
     *
     * @param lowerLeft  The lower-left corner of the region to be drawn.
     * @param upperRight The upper-right corner of the region to be drawn.
     * @return A string representation of the selected region of the map.
     */
    public String draw(Vector2d lowerLeft, Vector2d upperRight) {
        StringBuilder builder = new StringBuilder();
        for (int i = upperRight.y() + 1; i >= lowerLeft.y() - 1; i--) {
            // Add the header row for the map
            if (i == upperRight.y() + 1) {
                builder.append(drawHeader(lowerLeft, upperRight));
            }
            // Add the row number at the beginning of each row
            builder.append(String.format("%3d: ", i));
            for (int j = lowerLeft.x(); j <= upperRight.x() + 1; j++) {
                // Draw the frame for rows outside the defined map region
                if (i < lowerLeft.y() || i > upperRight.y()) {
                    builder.append(drawFrame(j <= upperRight.x()));
                } else {
                    // Draw the cell segment and its content (object or empty cell)
                    builder.append(CELL_SEGMENT);
                    if (j <= upperRight.x()) {
                        builder.append(drawObject(new Vector2d(j, i)));
                    }
                }
            }
            // Add a new line at the end of the row
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    /**
     * Draws a horizontal frame segment for the grid. Appends a separator if
     * it's not the last segment in the row.
     *
     * @param innerSegment          Indicates whether the segment is part of the inner grid.
     * @return                      A string representing a frame segment.
     */
    private String drawFrame(boolean innerSegment) {
        if (innerSegment) {
            return FRAME_SEGMENT + FRAME_SEGMENT;
        } else {
            return FRAME_SEGMENT;
        }
    }

    /**
     * Draws the header row of the map, showing the column indices.
     *
     * @param lowerLeft         The lower-left corner of the region.
     * @param upperRight        The upper-right corner of the region.
     * @return                  A string representing the header row of the map.
     */
    private String drawHeader(Vector2d lowerLeft, Vector2d upperRight) {
        StringBuilder builder = new StringBuilder();
        builder.append(" y\\x ");
        // Add column indices to the header
        for (int j = lowerLeft.x(); j < upperRight.x() + 1; j++) {
            builder.append(String.format("%2d", j));
        }
        builder.append(System.lineSeparator());
        return builder.toString();
    }

    /**
     * Draws the object present at a specific position on the map.
     * If there is no object at the given position, an empty cell is returned.
     *
     * @param currentPosition           The position to check for an object.
     * @return                          A string representation of the object at the given position or an empty cell.
     */
    private String drawObject(Vector2d currentPosition) {
        Object object = this.map.objectAt(currentPosition);
        if (object != null) {
            return object.toString();
        }
        return EMPTY_CELL;
    }
}
