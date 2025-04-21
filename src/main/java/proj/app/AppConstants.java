package proj.app;

import javafx.scene.paint.Color;
import java.time.format.DateTimeFormatter;

/**
 * Holds shared application constants to avoid magic numbers and strings
 * scattered throughout the codebase. This promotes maintainability and
 * consistency.
 */
public final class AppConstants {

    // --- Simulation State & Rendering ---
    /** Maximum number of simulation state snapshots buffered for the UI rendering queue. */
    public static final int MAX_STATE_QUEUE_SIZE = 5;
    /** Interval in milliseconds at which the SimulationStateProducer attempts to create a new snapshot. */
    public static final long SIMULATION_STATE_PRODUCER_INTERVAL_MS = 50; // ~20 snapshots/sec
    /** Target interval in nanoseconds between UI rendering frames (aims for ~60 FPS). */
    public static final long UI_RENDER_INTERVAL_NANOS = 16_666_666L; // Target ~60 FPS UI updates

    // --- Statistics ---
    /** The number of most popular genotypes to track and display in statistics. */
    public static final int TOP_GENOTYPE_COUNT = 3;
    /** The name of the main directory where all simulation statistics runs are saved. */
    public static final String STATS_DIR_NAME = "statistics";
    /** The format string used for naming daily statistics files (includes zero-padding for day number). */
    public static final String STATS_DAILY_FILENAME_FORMAT = "day_%05d.json";
    /** The format string used for naming manually saved snapshot files (includes config name, day, timestamp). */
    public static final String STATS_SNAPSHOT_FILENAME_FORMAT = "snapshot_%s_day%d_%s.json";
    /** The DateTimeFormatter pattern used for timestamps in statistics directory names. */
    public static final DateTimeFormatter STATS_DIR_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    /** The DateTimeFormatter pattern used for timestamps in manual snapshot filenames. */
    public static final DateTimeFormatter STATS_SNAPSHOT_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    /** The file extension pattern used for JSON file choosers. */
    public static final String JSON_FILE_EXTENSION = "*.json";
    /** The description used for the JSON file type in file choosers. */
    public static final String JSON_FILE_DESCRIPTION = "JSON files (*.json)";

    // --- Simulation Speed Control (LifecycleManager) ---
    /** Minimum value for the simulation speed control slider. */
    public static final double SPEED_SLIDER_MIN = 0.0;
    /** Maximum value for the simulation speed control slider. */
    public static final double SPEED_SLIDER_MAX = 100.0;
    /** Default value for the simulation speed control slider (representing nominal speed). */
    public static final double SPEED_SLIDER_DEFAULT = 50.0;
    /** Minimum allowed delay (fastest speed) between simulation steps in milliseconds. Must match SimulationController's internal bound. */
    public static final long MIN_STEP_DELAY_MS = 10;
    /** Maximum allowed delay (slowest speed) between simulation steps in milliseconds. Must match SimulationController's internal bound. */
    public static final long MAX_STEP_DELAY_MS = 1000; // Adjust if SimulationController uses different max

    // --- UI Defaults & Limits ---
    /** Maximum number of simulation run titles displayed in the 'Recent Simulations' list in the main window. */
    public static final int MAX_RECENT_SIMULATIONS = 10;
    /** Padding in pixels inside the ScrollPane containing the simulation canvas. */
    public static final double CANVAS_PADDING = 2.0;

    // --- Map Renderer Visuals ---
    // These define default visual properties for elements drawn directly onto the canvas.
    // For more complex styling, consider alternative rendering approaches if possible (e.g., using Nodes instead of Canvas).

    /** Color used for drawing the map grid lines. */
    public static final Color COLOR_GRID = Color.GRAY;
    /** Line width used for drawing the map grid lines. */
    public static final double GRID_LINE_WIDTH = 0.5;
    /** Fill color used for the equator region background (LightGreen with transparency). */
    public static final Color COLOR_EQUATOR_FILL = Color.rgb(144, 238, 144, 0.4);
    /** Fill color used for water tiles (LightBlue with transparency). */
    public static final Color COLOR_WATER_FILL = Color.rgb(173, 216, 230, 0.6);
    /** Fill color used for plant elements. */
    public static final Color COLOR_PLANT_FILL = Color.FORESTGREEN;
    /** The ratio of the plant's size (diameter) relative to the cell's smaller dimension. */
    public static final double PLANT_SIZE_RATIO = 0.7;

    /** Border color used to highlight the currently selected animal. */
    public static final Color COLOR_SELECTED_ANIMAL_BORDER = Color.YELLOW;
    /** Border width used to highlight the currently selected animal. */
    public static final double SELECTED_ANIMAL_BORDER_WIDTH = 3.0;
    /** Border color used to highlight animals with the most dominant genotype. */
    public static final Color COLOR_TOP_GENOTYPE_1_BORDER = Color.MAGENTA;
    /** Border color used to highlight animals with the second most dominant genotype. */
    public static final Color COLOR_TOP_GENOTYPE_2_BORDER = Color.BLACK;
    /** Border color used to highlight animals with the third most dominant genotype. */
    public static final Color COLOR_TOP_GENOTYPE_3_BORDER = Color.BLUE;
    /** Border width used to highlight animals with dominant genotypes. */
    public static final double GENOTYPE_BORDER_WIDTH = 2.0;

    /**
     * Private constructor to prevent instantiation of this utility class.
     * Throws {@link IllegalStateException} if an attempt is made via reflection.
     */
    private AppConstants() {
        throw new IllegalStateException("Utility class AppConstants cannot be instantiated.");
    }
}