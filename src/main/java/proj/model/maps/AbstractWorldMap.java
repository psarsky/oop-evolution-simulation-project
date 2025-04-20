package proj.model.maps;

import proj.model.elements.Animal;
import proj.model.elements.ElementType; // Required for checking element types
import proj.model.elements.Plant;
import proj.model.elements.WorldElement;
import proj.model.movement.AbstractMovementVariant;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.presenter.MapChangeListener;
import proj.presenter.MapVisualizer;
import proj.simulation.SimulationProperties;
import proj.util.Boundary;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.*; // Need List, Map, ArrayList, HashMap, Set, HashSet, Collections, UUID, Random, Objects

/**
 * Represents an abstract world map for an evolution simulation.
 * Manages the placement, storage, and movement of simulation elements like
 * {@link Animal}s and {@link Plant}s. It defines boundaries, handles position correction,
 * and uses specific {@link AbstractVegetationVariant} and {@link AbstractMovementVariant} strategies.
 * It also allows {@link MapChangeListener} observers to be notified of map changes.
 * Subclasses (e.g., {@link Globe}, {@link WaterWorld}) implement specific map topologies
 * and must call {@link #initializeFreePositions()} in their constructors after specific features are set up.
 * Synchronization is used on methods modifying shared state.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public abstract class AbstractWorldMap implements MoveValidator {

    private static final Random random = new Random(); // Random generator for internal use
    protected final List<MapChangeListener> observers;      // List of observers
    protected final MapVisualizer mapVisualizer;            // Helper for text visualization
    protected final AbstractVegetationVariant vegetation;   // Vegetation spawning strategy
    protected final AbstractMovementVariant movement;       // Animal movement strategy
    protected final UUID id;                                // Unique ID for this map instance
    protected final SimulationProperties simulationProperties; // Simulation configuration

    // Internal state storage for map elements. Access needs to be thread-safe.
    // Using standard HashMaps and a synchronized List, with access controlled by synchronized methods.
    protected final Map<Vector2d, List<Animal>> animals;    // Position -> List of Animals at that position
    protected final Map<Vector2d, Plant> plants;            // Position -> Plant at that position
    protected final List<Vector2d> freePlantPositions;      // List of positions currently free for plant spawning

    /**
     * Base constructor for AbstractWorldMap. Initializes common fields but delegates
     * the calculation of initial free plant positions to subclass constructors
     * via the mandatory call to {@link #initializeFreePositions()}.
     *
     * @param simulationProperties Simulation configuration settings. Must not be null.
     * @param vegetation           Vegetation spawning strategy. Must not be null.
     * @param movement             Animal movement strategy. Must not be null.
     */
    public AbstractWorldMap(SimulationProperties simulationProperties, AbstractVegetationVariant vegetation, AbstractMovementVariant movement) {
        this.simulationProperties = Objects.requireNonNull(simulationProperties, "SimulationProperties cannot be null");
        this.vegetation = Objects.requireNonNull(vegetation, "Vegetation variant cannot be null");
        this.movement = Objects.requireNonNull(movement, "Movement variant cannot be null");

        this.id = UUID.randomUUID();
        this.observers = new ArrayList<>(); // Consider thread-safe list if modified concurrently
        this.mapVisualizer = new MapVisualizer(this);
        this.animals = new HashMap<>(); // Requires synchronized access for modification/iteration
        this.plants = new HashMap<>();  // Requires synchronized access for modification/iteration
        this.freePlantPositions = Collections.synchronizedList(new ArrayList<>()); // Use synchronized wrapper

        // NOTE: initializeFreePositions() is NOT called here. Subclasses MUST call it.
    }

    /**
     * Populates the list of positions eligible for initial plant spawning (`freePlantPositions`).
     * This method clears the current list and adds all positions on the map grid
     * that are not initially occupied by any world element (checked via `objectAt`).
     * **Crucially, this method must be called by subclass constructors *after* any specific
     * map features (like water tiles in {@link WaterWorld}) have been initialized.**
     * This method synchronizes access to relevant internal state.
     */
    protected void initializeFreePositions() {
        // Synchronize on 'this' map instance to ensure a consistent view of animals, plants, and potentially water
        // while iterating through all possible positions.
        synchronized (this) {
            synchronized (freePlantPositions) { // Also synchronize direct modification of the list
                freePlantPositions.clear(); // Start with an empty list
                int width = this.simulationProperties.getWidth();
                int height = this.simulationProperties.getHeight();

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        Vector2d position = new Vector2d(x, y);
                        // objectAt() checks for animals, plants, and (in WaterWorld) water.
                        // It needs to be called within the synchronized(this) block for consistency.
                        if (objectAt(position) == null) {
                            this.freePlantPositions.add(position);
                        }
                    }
                }
            } // End sync freePlantPositions
        } // End sync this
        System.out.println("Initialized " + freePlantPositions.size() + " free plant positions for map " + this.id);
    }

    // --- Element Placement and Removal (synchronized methods ensure thread safety) ---

    /**
     * Places an animal at a specified position on the map. Manages the internal animals map
     * and updates the `freePlantPositions` list accordingly. Synchronized method.
     *
     * @param position The {@link Vector2d} position to place the animal.
     * @param animal   The {@link Animal} to place.
     */
    public synchronized void placeAnimal(Vector2d position, Animal animal) {
        this.animals.computeIfAbsent(position, k -> new ArrayList<>()).add(animal);
        // Position occupied by animal is not free for plants
        synchronized(freePlantPositions) { // Ensure safe removal from list
            freePlantPositions.remove(position);
        }
        // notifyObservers("Animal " + animal.getId() + " placed at " + position); // Optional log
    }

    /**
     * Removes a specified animal from the map. Updates the internal animals map
     * and potentially adds the position back to `freePlantPositions` if it becomes completely empty.
     * Synchronized method.
     *
     * @param animal The {@link Animal} to remove.
     */
    public synchronized void removeAnimal(Animal animal) {
        Vector2d pos = animal.getPos();
        List<Animal> animalsAtPos = this.animals.get(pos);
        if (animalsAtPos != null && animalsAtPos.remove(animal)) { // If animal was found and removed
            if (animalsAtPos.isEmpty()) { // If this was the last animal at this position
                this.animals.remove(pos); // Clean up the map entry
                // Check if the position is now truly empty (no plants or water) using objectAt
                if (objectAt(pos) == null) {
                    synchronized(freePlantPositions) { // Add back to free list safely
                        if (!freePlantPositions.contains(pos)) { // Avoid duplicates
                            freePlantPositions.add(pos);
                        }
                    }
                }
            }
            notifyObservers("Animal " + animal.getId() + " removed from " + pos);
        } else {
            // Log warning if animal wasn't found where expected
            System.err.println("Warning: Attempted to remove animal ID " + animal.getId() + " from " + pos + ", but it wasn't found there.");
        }
    }

    /**
     * Places a plant at a specified position if it's not already occupied by another plant.
     * Updates the internal plants map and removes the position from `freePlantPositions`.
     * Synchronized method.
     *
     * @param position The {@link Vector2d} position to place the plant.
     * @param plant    The {@link Plant} to place.
     */
    public synchronized void placePlant(Vector2d position, Plant plant) {
        // Check internal map before placing
        if (!this.plants.containsKey(position)) {
            this.plants.put(position, plant);
            synchronized(freePlantPositions) { // Ensure safe removal from list
                this.freePlantPositions.remove(position);
            }
            // notifyObservers("Plant placed at " + position); // Optional log
        } else {
            // This indicates a potential logic error elsewhere if called with an occupied position
            System.err.println("Warning: Attempted to place plant at " + position + " which already has a plant.");
        }
    }

    /**
     * Removes a plant from the specified position. Updates the internal plants map
     * and potentially adds the position back to `freePlantPositions` if it becomes completely empty.
     * Synchronized method.
     *
     * @param position The {@link Vector2d} position from which to remove the plant.
     * @return {@code true} if a plant was found and removed, {@code false} otherwise.
     */
    public synchronized boolean removePlant(Vector2d position) {
        // Check internal map before removing
        if (this.plants.containsKey(position)) {
            this.plants.remove(position);
            // Check if the position is now truly empty (no animals or water) using objectAt
            if (objectAt(position) == null) {
                synchronized (freePlantPositions) { // Add back safely
                    if (!freePlantPositions.contains(position)) { // Avoid duplicates
                        freePlantPositions.add(position);
                    }
                }
            }
            notifyObservers("Plant removed from " + position);
            return true;
        }
        // Log warning if no plant was found? Optional.
        // System.err.println("Warning: Attempted to remove plant from " + position + " but none was found.");
        return false;
    }

    // --- Simulation Step Logic (synchronized methods) ---

    /**
     * Processes the movement of a single animal for one simulation step.
     * Handles removal from the old position, calculation of the new position
     * (using movement variant and map validator), and placement at the new position.
     * Manages updates to the `freePlantPositions` list accordingly. Synchronized method.
     *
     * @param animal The {@link Animal} to move.
     */
    public synchronized void move(Animal animal) {
        Vector2d oldPos = animal.getPos();
        List<Animal> animalsAtOldPos = this.animals.get(oldPos);
        boolean removedFromOld = false;

        // --- Remove from old position ---
        if (animalsAtOldPos != null) {
            removedFromOld = animalsAtOldPos.remove(animal);
            if (animalsAtOldPos.isEmpty()) { // If list becomes empty
                this.animals.remove(oldPos); // Remove map entry
                // Check if old position becomes free *after* removal
                if (objectAt(oldPos) == null) {
                    synchronized(freePlantPositions) { if (!freePlantPositions.contains(oldPos)) freePlantPositions.add(oldPos); }
                }
            }
        }
        // Warn if animal wasn't where expected?
        // if (!removedFromOld) { System.err.println("Warning: Animal " + animal.getId() + " not found at old position " + oldPos + " during move start."); }

        // --- Calculate and apply move ---
        // movement.move updates the animal's internal position/direction after validation
        this.movement.move(animal, this);

        // --- Place at new position ---
        // placeAnimal adds to the new list and removes the new position from freePlantPositions
        placeAnimal(animal.getPos(), animal);

        // notifyObservers("Animal " + animal.getId() + " moved from " + oldPos + " to " + animal.getPos()); // Optional log
    }

    /**
     * Attempts to spawn a single plant at a valid, randomly selected free position.
     * Validation includes checking vegetation rules and ensuring the chosen position
     * is currently empty (no animal, plant, or water) before placing the plant.
     * Limits the number of attempts to find a suitable position. Synchronized method.
     *
     * @return {@code true} if a plant was successfully spawned and placed, {@code false} otherwise.
     */
    public synchronized boolean spawnPlant() {
        // Check if there are any positions available at all
        synchronized(freePlantPositions) {
            if (this.freePlantPositions.isEmpty()) return false;
        }

        // Limit attempts to find a suitable random spot
        int maxAttempts = Math.max(10, freePlantPositions.size() / 2 + 1); // Heuristic limit

        for (int i = 0; i < maxAttempts; i++) {
            Vector2d potentialPosition;
            // Get a random position safely from the synchronized list
            synchronized (freePlantPositions) {
                if (freePlantPositions.isEmpty()) return false; // Check again, list might empty during attempts
                potentialPosition = this.freePlantPositions.get(random.nextInt(freePlantPositions.size()));
            }

            // Check vegetation rules first (less expensive check)
            if (this.vegetation.validatePlantPosition(potentialPosition)) {
                // If vegetation allows, perform final check for occupancy using objectAt (synchronized)
                // This ensures atomicity between checking and placing within the broader map lock (if called from advanceDay)
                // or relies on objectAt's internal sync if called independently.
                if (objectAt(potentialPosition) == null) {
                    // Position is valid and empty, place the plant
                    placePlant(potentialPosition, new Plant(potentialPosition)); // placePlant is synchronized
                    return true; // Success!
                }
                // else: Position was in free list but objectAt found something (e.g., animal moved there just now)
            }
            // else: Vegetation rules rejected this position
        }
        // Failed to find a suitable position after multiple attempts
        // System.out.println("spawnPlant: Failed to find valid position after " + maxAttempts + " attempts."); // Optional log
        return false;
    }


    /**
     * Updates passive world elements for one simulation day, primarily by attempting
     * to spawn a configured number of new plants. Subclasses (like {@link WaterWorld})
     * override this method to add their specific update logic (e.g., water flow),
     * ensuring they call `super.updateWorldElements(currentDay)` to include plant spawning.
     * Accepts the `currentDay` for potential use in subclass logic. Synchronized method.
     *
     * @param currentDay The current simulation day number.
     */
    public synchronized void updateWorldElements(int currentDay) {
        int plantsToSpawn = this.simulationProperties.getPlantsPerDay();
        int plantsSpawned = 0;
        for (int i = 0; i < plantsToSpawn; i++) {
            if (spawnPlant()) { // spawnPlant is synchronized and returns success/failure
                plantsSpawned++;
            } else {
                // Optional: Break early if spawning fails repeatedly (map likely full)
                // if (i > plantsToSpawn / 2) break; // Example heuristic
            }
        }
        // Optionally notify observers about plant spawning
        if (plantsSpawned > 0) {
            notifyObservers(plantsSpawned + " new plant(s) spawned on day " + currentDay + ".");
        }
        // Subclasses add their specific updates after calling super.updateWorldElements(currentDay)
    }

    // --- Map State Querying (synchronized methods ensure consistent reads) ---

    /**
     * Safely retrieves the primary world element at a given position.
     * Priority: Animal > Plant (Water check handled in WaterWorld override). Returns null if empty.
     * Synchronized method.
     * @param position The position to query.
     * @return The {@link WorldElement} found, or null.
     */
    public synchronized WorldElement objectAt(Vector2d position) {
        // Access internal maps within synchronized context
        List<Animal> animalsAtPos = this.animals.get(position);
        if (animalsAtPos != null && !animalsAtPos.isEmpty()) {
            return animalsAtPos.getFirst(); // Return first animal if multiple exist
        }
        if (this.plants.containsKey(position)) {
            return this.plants.get(position);
        }
        // WaterWorld's override will check for water here if applicable.
        return null; // Position is empty or only contains water (checked by override)
    }

    /**
     * Gets an unmodifiable view of the map associating positions with lists of animals.
     * Provides read-only, thread-safe access to the current animal layout. Synchronized method.
     * @return An unmodifiable {@link Map} of {@link Vector2d} to {@link List} of {@link Animal}.
     */
    public synchronized Map<Vector2d, List<Animal>> getAnimals() {
        // Return an unmodifiable view to prevent external structural modification
        return Collections.unmodifiableMap(this.animals);
    }

    /**
     * Gets an unmodifiable view of the map associating positions with plants.
     * Provides read-only, thread-safe access to the current plant layout. Synchronized method.
     * @return An unmodifiable {@link Map} of {@link Vector2d} to {@link Plant}.
     */
    public synchronized Map<Vector2d, Plant> getPlants() {
        // Return an unmodifiable view
        return Collections.unmodifiableMap(this.plants);
    }

    /**
     * Gets a defensive copy of the list of positions currently considered free for plant spawning.
     * Provides a snapshot of the free positions at the time of calling. Synchronized method.
     * @return A new {@link ArrayList} containing the free {@link Vector2d} positions.
     */
    public List<Vector2d> getFreePlantPositions() {
        synchronized (freePlantPositions) { // Synchronize access to the list
            return new ArrayList<>(this.freePlantPositions); // Return a copy
        }
    }

    /** Gets the unique identifier of this map instance. */
    public UUID getID() { return this.id; }

    /** Calculates and returns the map boundaries based on configuration. */
    public Boundary getCurrentBounds() {
        return new Boundary(
                new Vector2d(0, 0),
                new Vector2d(simulationProperties.getWidth() - 1, simulationProperties.getHeight() - 1)
        );
    }

    // --- Observer Pattern (synchronized methods ensure thread safety) ---

    /** Adds a map change listener if not already present. Synchronized method. */
    public synchronized void addObserver(MapChangeListener observer) {
        if (observer != null && !this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /** Removes a map change listener if present. Synchronized method. */
    public synchronized void removeObserver(MapChangeListener observer) {
        this.observers.remove(observer);
    }

    /** Notifies all registered observers of a map change event. Iterates over a copy for safety. Synchronized method. */
    protected synchronized void notifyObservers(String message) {
        // Create copy to prevent ConcurrentModificationException if observer unsubscribes during notification
        List<MapChangeListener> observersCopy = new ArrayList<>(this.observers);
        for (MapChangeListener observer : observersCopy) {
            try {
                observer.mapChanged(this, message);
            } catch (Exception e) {
                // Log error but continue notifying other observers
                System.err.println("Error notifying map observer " + observer.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --- MoveValidator Implementation ---

    /**
     * Default position correction: Horizontal wrapping, vertical bouncing with direction reflection.
     * Called by movement logic before finalizing a move. Does not require synchronization itself
     * as it only reads immutable properties and parameters.
     * @param oldPosition The entity's position before the move (not used here).
     * @param newPosition The calculated position after applying movement vector.
     * @param direction   The intended direction of movement.
     * @return A {@link PositionDirectionTuple} with corrected position and direction.
     */
    @Override
    public PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction) {
        int mapWidth = this.simulationProperties.getWidth();
        int mapHeight = this.simulationProperties.getHeight();
        int correctedX = newPosition.x();
        int correctedY = newPosition.y();
        MapDirection correctedDirection = direction;

        // Horizontal Wrap
        if (mapWidth > 0) correctedX = Math.floorMod(newPosition.x(), mapWidth);
        else correctedX = 0;

        // Vertical Bounce & Reflect
        if (correctedY < 0) {
            correctedY = 0; // Bounce back to edge
            correctedDirection = switch (direction) { // Reverse N/S component
                case NORTH, NORTHEAST, NORTHWEST -> direction.opposite(); // Should be SOUTH variants
                case SOUTH -> MapDirection.NORTH;
                case SOUTHEAST -> MapDirection.NORTHEAST;
                case SOUTHWEST -> MapDirection.NORTHWEST;
                default -> direction; // E, W unchanged
            };
        } else if (correctedY >= mapHeight) {
            correctedY = mapHeight - 1; // Bounce back to edge
            correctedDirection = switch (direction) { // Reverse N/S component
                case SOUTH, SOUTHEAST, SOUTHWEST -> direction.opposite(); // Should be NORTH variants
                case NORTH -> MapDirection.SOUTH;
                case NORTHEAST -> MapDirection.SOUTHEAST;
                case NORTHWEST -> MapDirection.SOUTHWEST;
                default -> direction; // E, W unchanged
            };
        }
        return new PositionDirectionTuple(new Vector2d(correctedX, correctedY), correctedDirection);
    }

    // --- Object Overrides ---
    /** Returns string representation generated by MapVisualizer. */
    @Override public String toString() { Boundary b = getCurrentBounds(); return mapVisualizer.draw(b.lowerLeft(), b.upperRight()); }
    /** Equality based on unique map ID. */
    @Override public boolean equals(Object o) { if(this==o)return true;if(o==null||getClass()!=o.getClass())return false;return id.equals(((AbstractWorldMap)o).id); }
    /** Hash code based on unique map ID. */
    @Override public int hashCode() { return id.hashCode(); }
}