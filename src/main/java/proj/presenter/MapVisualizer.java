package proj.presenter;

import proj.model.maps.AbstractWorldMap;
import proj.util.Vector2d;
import java.util.Objects;

/**
 * Utility class responsible for generating a textual representation of an {@link AbstractWorldMap}.
 * It draws a grid corresponding to a specified rectangular region of the map, displaying
 * the elements present at each position using their `toString()` representation.
 * Useful for console output and debugging.
 *
 * @author apohllo, idzik
 */
public class MapVisualizer {

    private static final String EMPTY_CELL = " ";       // Character for empty map cells.
    private static final String FRAME_SEGMENT = "-";    // Character for horizontal frame lines.
    private static final String CELL_SEGMENT = "|";     // Character for vertical cell separators.
    private final AbstractWorldMap map;                 // The map instance to visualize.

    /**
     * Constructs a {@code MapVisualizer} associated with a specific map.
     *
     * @param map The non-null {@link AbstractWorldMap} to be visualized.
     */
    public MapVisualizer(AbstractWorldMap map) {
        this.map = Objects.requireNonNull(map, "Map cannot be null for MapVisualizer");
    }

    /**
     * Generates a string representation of a rectangular region of the map.
     * The output includes row and column headers, grid lines, and the string
     * representation of the element at each cell (or an empty space).
     * Assumes map indices fit within the formatted width.
     *
     * @param lowerLeft  The non-null {@link Vector2d} representing the lower-left corner (inclusive) of the region to draw.
     * @param upperRight The non-null {@link Vector2d} representing the upper-right corner (inclusive) of the region to draw.
     * @return A {@link String} containing the textual representation of the specified map region.
     */
    public String draw(Vector2d lowerLeft, Vector2d upperRight) {
        Objects.requireNonNull(lowerLeft, "LowerLeft boundary cannot be null");
        Objects.requireNonNull(upperRight, "UpperRight boundary cannot be null");
        // Basic validation for boundary sanity (optional)
        // if (!lowerLeft.precedes(upperRight)) { return "Invalid boundaries for drawing."; }

        StringBuilder builder = new StringBuilder();
        // Draw header row (x-coordinates)
        builder.append(drawHeader(lowerLeft, upperRight));

        // Draw map rows from top (upperRight.y) down to bottom (lowerLeft.y)
        for (int y = upperRight.y(); y >= lowerLeft.y(); y--) {
            // Draw row header (y-coordinate)
            builder.append(String.format("%3d: ", y)); // Format Y coordinate
            // Draw cells in the row
            for (int x = lowerLeft.x(); x <= upperRight.x(); x++) {
                builder.append(CELL_SEGMENT); // Vertical separator
                // Get object at (x, y) using map's synchronized method
                Object element = this.map.objectAt(new Vector2d(x, y));
                builder.append(element != null ? element.toString() : EMPTY_CELL); // Use element's toString or empty cell
            }
            builder.append(CELL_SEGMENT); // Closing vertical separator for the row
            builder.append(System.lineSeparator()); // New line after each row
        }

        // Draw bottom frame (optional, could be added similar to header)
        // builder.append(drawFooter(lowerLeft, upperRight));

        return builder.toString();
    }

    /**
     * Creates the header string for the map visualization, displaying column (x) indices.
     *
     * @param lowerLeft  The lower-left corner of the drawing region.
     * @param upperRight The upper-right corner of the drawing region.
     * @return The formatted header string.
     */
    private String drawHeader(Vector2d lowerLeft, Vector2d upperRight) {
        StringBuilder builder = new StringBuilder();
        builder.append(" y\\x "); // Label for coordinate axes
        for (int x = lowerLeft.x(); x <= upperRight.x(); x++) {
            builder.append(String.format("%2s", x)); // Format X coordinate (adjust width if needed)
        }
        builder.append(System.lineSeparator()); // New line after header
        return builder.toString();
    }

    /*  --- Method stubs for potential frame/footer ---
        private String drawFrame(boolean innerSegment) { ... }
        private String drawObject(Vector2d currentPosition) { ... } // Replaced by inline logic in draw()
        private String drawFooter(Vector2d lowerLeft, Vector2d upperRight) { ... }
    */
}