// ============================================================
// SOURCE FILE: proj/simulation/Simulation.java
// ============================================================

package proj.simulation;

import proj.model.elements.Animal;
import proj.model.elements.ElementType;
import proj.model.elements.WorldElement;
import proj.model.genotype.Genotype;
import proj.model.genotype.Mutation;
import proj.model.maps.AbstractWorldMap;
import proj.util.RandomPositionGenerator;
import proj.util.Vector2d;

import java.util.*;
// import java.util.concurrent.ConcurrentHashMap; // Keep if considering finer-grained concurrency

/**
 * Represents the core logic and state container for an evolution simulation instance.
 * It manages the simulation map ({@link AbstractWorldMap}), tracks living and dead
 * {@link Animal}s, manages {@link proj.model.elements.Plant}s via the map, and enforces
 * the rules for daily activities: movement, eating, and reproduction.
 * It maintains the current simulation day counter and notifies registered listeners
 * ({@link Runnable}) at the completion of each day's cycle via {@link #notifyDayEndListeners()}.
 * <p>
 * Synchronization is employed within this class (and expected in the map implementation)
 * to ensure safe state modification during simulation steps, especially when accessed
 * by methods like {@link #advanceDay()} or state getters like {@link #getAnimals()}.
 * </p><p>
 * **Note:** This class defines *what* happens in the simulation but not *how* it is executed.
 * The execution loop, timing, and lifecycle (start/stop/pause) are managed externally,
 * typically by a {@link proj.app.SimulationController}.
 * </p>
 */
public class Simulation {

    private final AbstractWorldMap map;                 // The simulation world map instance.
    // Use synchronized lists for basic thread safety on modifications (add/remove).
    // Iteration requires external synchronization block on the list itself.
    private final List<Animal> animals;                 // List of currently living animals.
    private final List<Animal> deadAnimals;             // List of animals that have died.
    private final SimulationProperties simulationProperties; // Immutable configuration settings.
    private final List<Runnable> dayEndListeners = new ArrayList<>(); // Listeners for day end events.

    // Runtime state: Current day counter. Volatile ensures visibility across threads.
    private volatile int currentDay = 0;

    /**
     * Constructs and initializes the simulation state.
     * Creates the initial population of animals and plants based on the provided properties.
     * The simulation starts at day 0.
     *
     * @param map                  The {@link AbstractWorldMap} instance for this simulation. Must not be null.
     * @param simulationProperties The {@link SimulationProperties} defining configuration parameters. Must not be null.
     * @param mutation             The {@link Mutation} strategy to be used for new genotypes. Must not be null.
     */
    public Simulation(AbstractWorldMap map, SimulationProperties simulationProperties, Mutation mutation) {
        this.map = Objects.requireNonNull(map, "Map cannot be null");
        this.simulationProperties = Objects.requireNonNull(simulationProperties, "SimulationProperties cannot be null");
        Objects.requireNonNull(mutation, "Mutation strategy cannot be null");

        this.animals = Collections.synchronizedList(new ArrayList<>());
        this.deadAnimals = Collections.synchronizedList(new ArrayList<>());
        this.currentDay = 0; // Explicitly start at day 0

        // Initialize simulation content
        initializeAnimals(mutation);
        initializePlants();

        System.out.println("Simulation instance created with ID: " + map.getID());
    }

    /**
     * Initializes the starting animal population on the map.
     * Animals created here are assigned `birthDate` 0. Attempts to place animals
     * at unique random positions, skipping occupied or water tiles.
     * Ensures thread safety by synchronizing on map and animal list during placement.
     *
     * @param mutation The mutation strategy for initial genotypes.
     */
    private void initializeAnimals(Mutation mutation) {
        RandomPositionGenerator positionGenerator = new RandomPositionGenerator(
                this.simulationProperties.getWidth(),
                this.simulationProperties.getHeight(),
                this.simulationProperties.getAnimalCount()
        );

        int animalsToPlace = this.simulationProperties.getAnimalCount();
        int animalsPlaced = 0;

        // Synchronize map access while checking positions and placing animals
        // Synchronize list access when adding animals
        synchronized(this.map) {
            synchronized(this.animals) {
                for (Vector2d animalPosition : positionGenerator) {
                    if (animalsPlaced >= animalsToPlace) break; // Stop if target count reached

                    // Check if the randomly chosen position is suitable (not water, not occupied initially)
                    WorldElement elementAtPos = map.objectAt(animalPosition); // objectAt is synchronized
                    if (elementAtPos == null || elementAtPos.getElementType() != ElementType.WATER) {
                        Genotype genotype = new Genotype(simulationProperties, mutation);
                        // Create animal with birth day 0
                        Animal animal = new Animal(animalPosition, simulationProperties, genotype, 0);
                        this.animals.add(animal); // Add to synchronized list
                        this.map.placeAnimal(animal.getPos(), animal); // map.placeAnimal is synchronized
                        animalsPlaced++;
                    } else {
                        // Log skipped placement
                        // System.out.println("Skipped placing initial animal at " + animalPosition + " (occupied/water).");
                    }
                }
            } // End sync animals
        } // End sync map

        // Log outcome
        if (animalsPlaced < animalsToPlace) {
            System.out.println("Warning: Could only place " + animalsPlaced + " initial animals (requested " + animalsToPlace + "). Map might be constrained.");
        } else {
            System.out.println("Initialized " + animalsPlaced + " animals.");
        }
    }

    /**
     * Initializes the starting plant population on the map by attempting to spawn
     * the configured number of plants using the map's `spawnPlant` method,
     * limiting the number of attempts.
     */
    private void initializePlants() {
        int initialPlantCount = this.simulationProperties.getPlantCount();
        int plantsAdded = 0;
        // map.spawnPlant is internally synchronized
        // Limit attempts to avoid excessive loops if map is full or spawning is difficult
        for (int i = 0; i < initialPlantCount * 3 && plantsAdded < initialPlantCount; i++) {
            if (this.map.spawnPlant()) {
                plantsAdded++;
            }
        }
        System.out.println("Initialized " + plantsAdded + " plants (requested " + initialPlantCount + ").");
    }


    /**
     * Executes all simulation logic for a single day (or step).
     * This is the core method called repeatedly by the {@link proj.app.SimulationController}.
     * It orchestrates the daily sequence: removing the dead, updating the environment (plants/water),
     * moving animals, handling consumption (eating), and handling reproduction. Finally, it increments
     * the internal day counter and notifies any registered day-end listeners.
     * <p>
     * This entire method is synchronized on the `Simulation` instance (`this`) to ensure that
     * all actions within a day occur atomically relative to external accessors (like `getAnimals()`)
     * and prevent race conditions between the daily steps.
     * </p>
     */
    public synchronized void advanceDay() {
        // Check if simulation should continue (at least one animal alive)
        // Accessing synchronized list's isEmpty() is generally safe.
        if (animals.isEmpty()) {
            // Potentially log or set a state indicating simulation ended naturally
            return;
        }

        // --- Perform daily actions in sequence ---
        // Each helper method called operates within this synchronized block
        removeDeadAnimals();          // Check energy, update deathDate, move to dead list, remove from map
        map.updateWorldElements(this.currentDay); // Update map environment (plants, water), pass current day
        moveAnimals();                // Calculate moves, update positions/energy/age via map
        eat();                        // Check plant/animal overlap, update energy, remove plants via map
        reproduce();                  // Check eligibility, create children (with currentDay as birthDate), add via map

        this.currentDay++;            // Increment internal day counter AFTER all actions are done

        notifyDayEndListeners();      // Notify observers about the completed day
        // --- End Daily Steps ---
    }


    /**
     * Identifies and removes animals with zero or less energy from the active simulation.
     * Sets their `deathDate` to the current simulation day (`currentDay`) and moves them
     * from the `animals` list to the `deadAnimals` list. Also removes them from the map.
     * Requires external synchronization (provided by `advanceDay`).
     */
    private void removeDeadAnimals() {
        List<Animal> newlyDead = new ArrayList<>();
        // Safely iterate and remove from the synchronized 'animals' list
        synchronized (animals) {
            Iterator<Animal> iterator = animals.iterator();
            while (iterator.hasNext()) {
                Animal animal = iterator.next();
                if (animal.getEnergy() <= 0) {
                    animal.setDeathDate(this.currentDay); // Record day of death
                    this.map.removeAnimal(animal);        // Remove from map (synchronized map method)
                    newlyDead.add(animal);
                    iterator.remove();                    // Remove from live list using iterator
                }
            }
        }
        // Safely add to the synchronized 'deadAnimals' list
        if (!newlyDead.isEmpty()) {
            synchronized(deadAnimals){
                this.deadAnimals.addAll(newlyDead);
            }
        }
    }

    // updateWorldElements() is now called directly in advanceDay

    /**
     * Manages the movement phase for all living animals.
     * Iterates over a snapshot of the current animal list and instructs the map
     * to process the movement for each eligible animal.
     * Requires external synchronization (provided by `advanceDay`).
     */
    private void moveAnimals() {
        // Create snapshot to avoid issues if map.move indirectly affects the main list
        List<Animal> currentAnimalsSnapshot;
        synchronized (animals) {
            currentAnimalsSnapshot = new ArrayList<>(this.animals);
        }
        // Move each animal from the snapshot
        for(Animal animal : currentAnimalsSnapshot) {
            // Check liveliness again - important if energy could drop to 0 during another animal's move (unlikely here, but safe)
            if(animal.isAlive() && animal.getEnergy() > 0) {
                this.map.move(animal); // map.move is synchronized
            }
        }
    }

    /**
     * Manages the eating phase. Identifies locations with both plants and animals,
     * determines the highest-energy animal(s) at those locations, and allows one
     * to consume the plant by invoking `map.removePlant()`.
     * Requires external synchronization (provided by `advanceDay`).
     */
    private void eat() {
        List<Vector2d> ediblePlantPositions = new ArrayList<>();
        Set<Vector2d> currentPlantPositions;
        Map<Vector2d, List<Animal>> currentAnimalPositions;

        // Get consistent snapshot of map state
        synchronized(this.map) {
            // Use map's getters which return unmodifiable views
            currentPlantPositions = new HashSet<>(this.map.getPlants().keySet());
            currentAnimalPositions = new HashMap<>();
            this.map.getAnimals().forEach((pos, list) -> {
                if (list != null && !list.isEmpty()) {
                    // Create copies only for positions where eating might occur
                    // Optimization: Check for plant presence before copying animal list?
                    if (currentPlantPositions.contains(pos)) {
                        currentAnimalPositions.put(pos, new ArrayList<>(list));
                    }
                }
            });
        }

        // Find overlaps more efficiently
        for (Vector2d plantPos : currentPlantPositions) {
            if (currentAnimalPositions.containsKey(plantPos)) {
                ediblePlantPositions.add(plantPos);
            }
        }

        // Process eating
        for (Vector2d position : ediblePlantPositions) {
            List<Animal> animalsAtPosCopy = currentAnimalPositions.get(position); // Already a copy
            if (animalsAtPosCopy == null || animalsAtPosCopy.isEmpty()) continue;

            // Sort copy to find strongest
            animalsAtPosCopy.sort(Comparator.comparingInt(Animal::getEnergy).reversed()
                    .thenComparingInt(Animal::getAge).reversed()); // Tie-breaker by age
            Animal strongestAnimal = animalsAtPosCopy.getFirst();

            // Attempt removal via map's synchronized method
            boolean plantRemoved = this.map.removePlant(position);
            if (plantRemoved) {
                strongestAnimal.eatPlant(this.simulationProperties.getPlantEnergy());
            }
        }
    }

    /**
     * Manages the reproduction phase. Finds locations with two or more animals,
     * identifies the pair with the highest energy if they meet the threshold,
     * triggers their `reproduce` method (passing `currentDay`), and adds the
     * resulting child to the simulation and map.
     * Requires external synchronization (provided by `advanceDay`).
     */
    private void reproduce() {
        List<Animal> newChildren = new ArrayList<>();
        int energyNeeded = this.simulationProperties.getEnergyNeededToReproduce();
        Map<Vector2d, List<Animal>> potentialMatingSpots;

        // Get consistent snapshot of animal locations suitable for mating
        synchronized(this.map) {
            potentialMatingSpots = new HashMap<>();
            this.map.getAnimals().forEach((pos, list) -> { // Uses unmodifiable view
                if (list != null && list.size() >= 2) { // Need at least 2 animals
                    potentialMatingSpots.put(pos, new ArrayList<>(list)); // Copy list for sorting
                }
            });
        }

        // Process reproduction attempts
        for (Map.Entry<Vector2d, List<Animal>> entry : potentialMatingSpots.entrySet()) {
            List<Animal> animalListCopy = entry.getValue(); // Already a copy

            // Sort the copy by energy descending to find the strongest pair
            animalListCopy.sort(Comparator.comparingInt(Animal::getEnergy).reversed());
            Animal parent1 = animalListCopy.get(0);
            Animal parent2 = animalListCopy.get(1);

            // Check energy requirements
            if (parent1.getEnergy() >= energyNeeded && parent2.getEnergy() >= energyNeeded) {
                // Attempt reproduction, passing the internal currentDay for child's birthDate
                Animal child = parent1.reproduce(parent2, this.currentDay);
                if (child != null) {
                    newChildren.add(child);
                    // Place child on map using map's synchronized method
                    this.map.placeAnimal(child.getPos(), child);
                }
            }
        }

        // Add all newly born children to the main animals list safely
        if (!newChildren.isEmpty()) {
            synchronized (animals) {
                this.animals.addAll(newChildren);
            }
        }
    }


    /**
     * Adds a listener that will be executed sequentially at the end of each simulation day,
     * within the context of the {@link #advanceDay()} method's execution (i.e., in the simulation thread).
     * Ensures the listener is not null and synchronizes access to the listener list.
     *
     * @param listener The non-null {@link Runnable} task to execute.
     */
    public void addDayEndListener(Runnable listener) {
        if (listener != null) {
            synchronized (dayEndListeners) { // Protect list modification
                dayEndListeners.add(listener);
            }
        }
    }

    /** Notifies registered day-end listeners. Creates a copy for safe iteration. */
    private void notifyDayEndListeners() {
        List<Runnable> listenersCopy;
        synchronized(dayEndListeners) { // Create copy safely inside synchronized block
            listenersCopy = new ArrayList<>(this.dayEndListeners);
        }
        // Execute listeners outside the synchronized block
        for (Runnable listener : listenersCopy) {
            try { listener.run(); } catch (Exception e) {
                System.err.println("Error executing day end listener: " + e.getMessage()); e.printStackTrace();
            }
        }
    }

    // --- Getters for State Access ---

    /**
     * Gets a synchronized list view of currently living animals.
     * **Important:** Iteration requires external synchronization on the returned list.
     * @return The synchronized {@link List} of living {@link Animal}s.
     */
    public List<Animal> getAnimals() { return this.animals; }

    /**
     * Gets a synchronized list view of animals that have died.
     * **Important:** Iteration requires external synchronization on the returned list.
     * @return The synchronized {@link List} of dead {@link Animal}s.
     */
    public List<Animal> getDeadAnimals() { return this.deadAnimals; }

    /**
     * Gets the world map. Access its state via its synchronized methods or ensure external synchronization.
     * @return The {@link AbstractWorldMap} instance.
     */
    public AbstractWorldMap getMap() { return this.map; }

    /**
     * Gets the immutable simulation configuration properties.
     * @return The {@link SimulationProperties} instance.
     */
    public SimulationProperties getSimulationProperties() { return simulationProperties; }

    /**
     * Gets the current simulation day number (starting from 0). Thread-safe for reading.
     * @return The current day number.
     */
    public int getCurrentDay() { return this.currentDay; } // Reading volatile is safe

    /**
     * Provides a string representation of the simulation map state. Requires synchronization on the map.
     * @return A {@link String} representing the current map state.
     */
    @Override
    public String toString() {
        synchronized (this.map) { // Synchronize map access for consistent toString
            return map.toString();
        }
    }
}