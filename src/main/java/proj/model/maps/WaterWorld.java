package proj.model.maps;

import proj.model.elements.Animal;
import proj.model.elements.ElementType; // Required for type check
import proj.model.elements.Water;
import proj.model.elements.WorldElement;
import proj.model.movement.AbstractMovementVariant;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.RandomPositionGenerator;
import proj.util.Vector2d;

import java.util.*;
// import java.util.stream.Collectors; // Keep if needed for alternative logic

/**
 * Represents a "Water World" map variant characterized by dynamic water coverage.
 * Extends {@link AbstractWorldMap} to manage water tiles (`waterFields`), simulate
 * tidal water flow based on the current simulation day and a "violence" factor,
 * and implement specific interactions where water affects plants, animals (energy drain),
 * and movement (blocks entry). Water placement occurs first during construction,
 * followed by the calculation of initial free land positions.
 * Synchronization is used extensively to manage concurrent state access and modification.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>, <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public class WaterWorld extends AbstractWorldMap {

    // Internal map holding water tile positions. Access requires synchronization.
    private final Map<Vector2d, Water> waterFields = new HashMap<>();
    private static final Random waterRandom = new Random(); // Random generator specific to water logic

    /**
     * Constructs a {@code WaterWorld} map. Initializes base map features,
     * then populates initial water fields based on simulation properties,
     * and finally calculates the initial free positions excluding water by calling
     * the now mandatory {@link #initializeFreePositions()} method.
     *
     * @param simulationProperties A {@link SimulationProperties} object defining parameters (map size, water violence, etc.). Must not be null.
     * @param vegetation           An {@link AbstractVegetationVariant} object defining vegetation rules. Must not be null.
     * @param movement             An {@link AbstractMovementVariant} object defining animal movement rules. Must not be null.
     */
    public WaterWorld(SimulationProperties simulationProperties, AbstractVegetationVariant vegetation, AbstractMovementVariant movement) {
        super(simulationProperties, vegetation, movement); // Calls base constructor (DOES NOT call initializeFreePositions)

        initializeWaterFields();        // Initialize water specific to this class FIRST
        initializeFreePositions();      // NOW call base method to populate list, correctly excluding water
    }

    /**
     * Populates the initial water fields on the map based on simulation properties.
     * Places a number of water tiles (e.g., 10% of map area) at unique random positions.
     * Synchronized modification of `waterFields`.
     */
    private void initializeWaterFields() {
        int waterTileCount = (simulationProperties.getWidth() * simulationProperties.getHeight()) / 10; // Example: 10% water
        RandomPositionGenerator waterPosGen = new RandomPositionGenerator(
                simulationProperties.getWidth(), simulationProperties.getHeight(), waterTileCount);

        synchronized (waterFields) { // Synchronize write access
            waterFields.clear(); // Ensure it's empty before adding
            for (Vector2d pos : waterPosGen) {
                // Basic bounds check (though generator should be within bounds)
                if (pos.x() >= 0 && pos.x() < simulationProperties.getWidth() &&
                        pos.y() >= 0 && pos.y() < simulationProperties.getHeight()) {
                    this.waterFields.put(pos, new Water(pos));
                }
            }
        }
        System.out.println("Initialized " + waterFields.size() + " water tiles.");
    }

    /**
     * Overrides the base map update method to include water-specific dynamics (tidal flow)
     * after the standard updates (like plant spawning) are performed.
     * Passes the `currentDay` to the water flow logic for tidal calculations. This method is synchronized.
     *
     * @param currentDay The current simulation day number.
     */
    @Override
    public synchronized void updateWorldElements(int currentDay) {
        super.updateWorldElements(currentDay); // Call base method first (spawns plants)

        // --- Water Flow Simulation ---
        boolean highTide = (currentDay % 10) < 5; // Determine tide based on day (e.g., 5 days high, 5 low)
        int waterViolence = this.simulationProperties.getWaterViolence(); // Get configured violence factor
        waterFlow(highTide, waterViolence); // Simulate water movement (calls synchronized method)
    }

    /**
     * Simulates the ebb and flow of water based on tidal state and violence factor.
     * High tide: Water attempts to expand to adjacent non-water tiles, removing plants and potentially harming animals.
     * Low tide: A portion of water tiles (especially those on the edge) may recede.
     * Modifies internal state (`waterFields`, `plants`, `animals` energy, `freePlantPositions`).
     * This method is synchronized as it heavily modifies shared map state.
     *
     * @param highTide      {@code true} if it's currently high tide (expansion phase), {@code false} for low tide (recession phase).
     * @param waterViolence A percentage (0-100) influencing the rate of expansion vs. recession. Higher values favor expansion.
     */
    public synchronized void waterFlow(boolean highTide, int waterViolence) {
        int currentWaterCount = this.waterFields.size();
        if (currentWaterCount == 0) return; // Nothing to flow

        // Calculate number of water tiles targeted for change
        double lowTideRecessionFactor = 1.0 + (double) Math.max(0, Math.min(100, waterViolence)) / 100.0; // Range [1.0, 2.0]
        int baseChangeCount = Math.max(1, currentWaterCount / 5); // Base rate ~20%
        int waterToChangeCount = highTide ? baseChangeCount : (int) Math.round(baseChangeCount / lowTideRecessionFactor);
        waterToChangeCount = Math.max(1, waterToChangeCount); // Ensure at least 1 attempt

        // Identify water tiles eligible for change (those adjacent to non-water)
        List<Vector2d> changeableWaterFields = getChangeableWaterFields(highTide); // This accesses internal state safely
        if (changeableWaterFields.isEmpty()) return; // No eligible tiles

        Collections.shuffle(changeableWaterFields, waterRandom); // Randomize the order of processing

        // Limit changes to available candidates or calculated count
        waterToChangeCount = Math.min(changeableWaterFields.size(), waterToChangeCount);

        // Apply changes to the selected tiles
        for (int i = 0; i < waterToChangeCount; i++) {
            Vector2d currentPosition = changeableWaterFields.get(i);

            if (highTide) {
                // --- High Tide: Attempt to Expand Water ---
                List<Vector2d> potentialTargets = findExpansionTargets(currentPosition); // Find valid adjacent land tiles
                if (!potentialTargets.isEmpty()) {
                    Vector2d newWaterPos = potentialTargets.get(waterRandom.nextInt(potentialTargets.size())); // Pick one randomly
                    // Add new water tile (overwrites if occupied by plant/animal implicitly via objectAt checks later)
                    this.waterFields.put(newWaterPos, new Water(newWaterPos));
                    // Remove plant if present at the new water position
                    this.plants.remove(newWaterPos); // remove is safe on HashMap even if key missing
                    // Remove from free positions list (synchronized list access)
                    synchronized(freePlantPositions){ freePlantPositions.remove(newWaterPos); }
                    // Harm any animals now standing in the new water tile
                    harmAnimalsAt(newWaterPos); // Handles animal energy update
                    // notifyObservers("Water expanded to " + newWaterPos); // Optional detailed log
                }
            } else {
                // --- Low Tide: Attempt to Recede Water ---
                // Only recede if more than a minimum amount of water exists and tile is still water
                if (this.waterFields.size() > 1 && this.waterFields.containsKey(currentPosition)) {
                    this.waterFields.remove(currentPosition); // Remove water tile
                    // Check if position becomes free *after* water removal (no animals or plants)
                    if (objectAt(currentPosition) == null) {
                        synchronized(freePlantPositions) { // Add back safely
                            if (!freePlantPositions.contains(currentPosition)) {
                                freePlantPositions.add(currentPosition);
                            }
                        }
                    }
                    // notifyObservers("Water receded from " + currentPosition); // Optional detailed log
                }
            }
        }
    }

    /**
     * Identifies water tiles eligible for changing state (adjacent to non-water tiles).
     * Helper method for `waterFlow`. Requires external synchronization or call from synchronized context.
     * @param highTide True if looking for expansion sources, false for recession candidates.
     * @return A list of eligible water tile positions.
     */
    private List<Vector2d> getChangeableWaterFields(boolean highTide) {
        List<Vector2d> candidates = new ArrayList<>();
        Set<Vector2d> currentWaterPositions = this.waterFields.keySet(); // Access internal state

        for (Vector2d waterPos : currentWaterPositions) {
            boolean adjacentToLand = false;
            for (MapDirection dir : MapDirection.values()) {
                Vector2d adjacentPos = waterPos.add(dir.toUnitVector());
                // Check within bounds
                if (adjacentPos.x() >= 0 && adjacentPos.x() < simulationProperties.getWidth() &&
                        adjacentPos.y() >= 0 && adjacentPos.y() < simulationProperties.getHeight()) {
                    // If adjacent tile is NOT water, then this water tile is on the edge
                    if (!currentWaterPositions.contains(adjacentPos)) {
                        adjacentToLand = true;
                        break;
                    }
                } else { // Out of bounds
                    // Treat out-of-bounds as "land" only for recession eligibility
                    if (!highTide) {
                        adjacentToLand = true;
                        break;
                    }
                }
            }
            if (adjacentToLand) {
                candidates.add(waterPos); // Add edge water tiles
            }
        }
        return candidates;
    }


    /**
     * Finds valid adjacent positions for water expansion from a source tile.
     * Excludes out-of-bounds and existing water tiles.
     * Helper method for `waterFlow`. Requires external synchronization or call from synchronized context.
     * @param sourceWaterPos The water tile trying to expand.
     * @return List of valid {@link Vector2d} target positions.
     */
    private List<Vector2d> findExpansionTargets(Vector2d sourceWaterPos) {
        List<Vector2d> targets = new ArrayList<>();
        Set<Vector2d> currentWaterPositions = this.waterFields.keySet(); // Access internal state

        for (MapDirection dir : MapDirection.values()) {
            Vector2d potentialTarget = sourceWaterPos.add(dir.toUnitVector());
            // Check bounds and if not already water
            if (potentialTarget.x() >= 0 && potentialTarget.x() < simulationProperties.getWidth() &&
                    potentialTarget.y() >= 0 && potentialTarget.y() < simulationProperties.getHeight() &&
                    !currentWaterPositions.contains(potentialTarget))
            {
                targets.add(potentialTarget);
            }
        }
        return targets;
    }

    /**
     * Applies harm (sets energy to 0) to any animals located at the specified position.
     * Called when water floods a tile. This method is synchronized.
     * @param position The position where water has appeared.
     */
    private synchronized void harmAnimalsAt(Vector2d position) {
        List<Animal> animalsAtPos = this.animals.get(position); // Access internal map
        if (animalsAtPos != null && !animalsAtPos.isEmpty()) {
            // Create copy to iterate over safely while potentially modifying energy
            List<Animal> animalsToHarm = new ArrayList<>(animalsAtPos);
            System.out.println("Water harming " + animalsToHarm.size() + " animals at " + position);
            for (Animal animal : animalsToHarm) {
                animal.setEnergy(0); // Example: Instant energy drain / drowning
            }
            // Dead animals are removed later by the main simulation loop's removeDeadAnimals step
        }
    }

    /**
     * Overrides position correction to prevent movement *into* water tiles.
     * Calls the base boundary correction first, then checks the result against internal water fields.
     * If the target is water, returns the original position and opposite direction. Synchronized method.
     *
     * @param oldPosition   The entity's valid position before the attempted move.
     * @param newPosition   The calculated position after applying movement vector.
     * @param direction     The intended direction of movement.
     * @return A {@link PositionDirectionTuple} containing the final valid position and direction.
     */
    @Override
    public synchronized PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction) {
        // Apply standard boundary correction (wrapping/bouncing) from the base class
        PositionDirectionTuple correctedTuple = super.correctPosition(oldPosition, newPosition, direction);
        Vector2d finalPosition = correctedTuple.position();

        // Check if the resulting position is a water tile (access internal map safely)
        if (this.waterFields.containsKey(finalPosition)) {
            // Collision with water: stay in old position, reverse original direction
            return new PositionDirectionTuple(oldPosition, direction.opposite());
        }

        // Otherwise, the boundary-corrected position is valid (not water)
        return correctedTuple;
    }

    /**
     * Overrides object lookup to include checking for water tiles with priority.
     * Order: Animal > Water > Plant > null. Synchronized method.
     *
     * @param position A {@link Vector2d} position to check.
     * @return The primary {@link WorldElement} at the position, or {@code null} if empty.
     */
    @Override
    public synchronized WorldElement objectAt(Vector2d position) {
        // Access internal maps safely within synchronized method
        List<Animal> animalsAtPos = this.animals.get(position);
        if (animalsAtPos != null && !animalsAtPos.isEmpty()) return animalsAtPos.getFirst();
        if (this.waterFields.containsKey(position)) return this.waterFields.get(position); // Check water before plant
        if (this.plants.containsKey(position)) return this.plants.get(position);
        return null; // Empty
    }

    /**
     * Gets an unmodifiable view of the map of current water fields.
     * Provides read-only, thread-safe access. Synchronized method.
     * @return An unmodifiable {@link Map} representing water positions.
     */
    public synchronized Map<Vector2d, Water> getWaterFields() {
        return Collections.unmodifiableMap(this.waterFields);
    }

    /**
     * Internal helper to access the modifiable water map.
     * **Should only be called from synchronized contexts** within this class or its base class
     * during initialization phases where direct access is needed before `initializeFreePositions`.
     * @return The internal, modifiable water fields map.
     */
    protected synchronized Map<Vector2d, Water> getWaterFieldsInternal() {
        return this.waterFields; // Used by initializeFreePositions in AbstractWorldMap
    }
}