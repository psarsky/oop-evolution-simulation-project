package proj.model.elements;

import proj.model.genotype.Genotype;
import proj.model.genotype.Mutation;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.*;

/**
 * Represents an Animal, a dynamic element in the simulation world.
 * Animals possess a unique ID, position, direction, energy level, age, genotype,
 * and lineage tracking (children, descendants). They perform actions like moving,
 * eating plants, and reproducing based on simulation rules and their internal state.
 * The animal's age and energy are updated during simulation steps, and its birth/death
 * days are recorded relative to the simulation's day counter.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>, <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public class Animal implements WorldElement {

    private static long nextId = 0; // Static counter for generating unique IDs
    private final long id;         // Unique identifier for this animal instance

    // --- Configuration & Genetics (mostly final or derived) ---
    private final Genotype genotype;                    // The animal's genetic makeup defining behavior.
    private final List<Animal> children;                // Direct offspring produced by this animal.
    private final int energyToReproduce;                // Energy required for this animal to initiate reproduction.
    private final int energyToPassToChild;              // Energy contributed by this parent to an offspring.
    private final int energyCostToMove;                 // Energy cost per movement action.
    private final int birthDate;                        // Simulation day number when the animal was born.

    // --- Dynamic State ---
    private PositionDirectionTuple positionDirection;   // Current position and facing direction.
    private final int[] genes;                          // Cached copy of the gene sequence from genotype for quick access.
    private int geneIndex;                              // Index of the currently active gene in the sequence.
    private int energy;                                 // Current energy level.
    private int age;                                    // Age in simulation days (incremented each day/move).
    private int deathDate = -1;                         // Simulation day number when the animal died (-1 if alive).
    private int plantsEaten = 0;                        // Counter for plants consumed.
    private int childrenMade = 0;                       // Counter for direct offspring produced.

    private static final Random random = new Random(); // For initializing gene index

    /**
     * Constructs a new {@code Animal}. Initializes state based on configuration, genotype,
     * position, and the specified birth day. Assigns a unique ID. Age starts at 0.
     *
     * @param position             The initial position {@link Vector2d}. Cannot be null.
     * @param simulationProperties The {@link SimulationProperties} object defining simulation rules (energy, costs, etc.). Cannot be null.
     * @param genotype             The {@link Genotype} containing the animal's genes and mutation strategy. Cannot be null.
     * @param birthDay             The simulation day number this animal is born on (e.g., 0 for initial population).
     */
    public Animal(Vector2d position, SimulationProperties simulationProperties, Genotype genotype, int birthDay) {
        // Assign unique ID thread-safely
        synchronized (Animal.class) {
            this.id = nextId++;
        }

        // Validate inputs
        this.genotype = Objects.requireNonNull(genotype, "Genotype cannot be null");
        Objects.requireNonNull(position, "Position cannot be null");
        Objects.requireNonNull(simulationProperties, "SimulationProperties cannot be null");

        // Initialize final fields from properties
        this.energyToReproduce = simulationProperties.getEnergyNeededToReproduce();
        this.energyToPassToChild = simulationProperties.getEnergyToPassToChild();
        this.energyCostToMove = simulationProperties.getEnergyCostToMove();
        this.birthDate = birthDay; // Set birth date from parameter

        // Initialize dynamic state
        this.positionDirection = new PositionDirectionTuple(position, MapDirection.getRandomDirection());
        this.genes = this.genotype.getGenes(); // Get gene sequence
        // Ensure gene index is valid
        this.geneIndex = (genes.length > 0) ? random.nextInt(genes.length) : 0;
        this.energy = simulationProperties.getStartEnergy();
        this.age = 0; // Age starts at 0 on the day it's born
        this.children = new ArrayList<>(); // Initialize empty children list
        this.deathDate = -1; // Initialize as alive
        this.plantsEaten = 0;
        this.childrenMade = 0;
    }

    // --- Actions ---

    /**
     * Updates the animal's state after performing a move action.
     * Sets the new position and direction, increments age, and deducts energy cost.
     * The validity of the move is assumed to be handled externally.
     *
     * @param newPositionDirection The {@link PositionDirectionTuple} with the final position and direction.
     */
    public void move(PositionDirectionTuple newPositionDirection) {
        this.positionDirection = newPositionDirection;
        this.age++; // Increment age each time the animal moves (or attempts to)
        this.energy = Math.max(0, this.energy - this.energyCostToMove); // Deduct energy, floor at 0
    }

    /**
     * Increases the animal's energy after consuming a plant and increments the counter.
     *
     * @param plantEnergy Energy value gained from the plant (should be positive).
     */
    public void eatPlant(int plantEnergy) {
        if (plantEnergy > 0) {
            this.energy += plantEnergy;
        }
        this.plantsEaten++;
    }

    /**
     * Attempts to reproduce with another animal (mate).
     * Creates a child with the specified `current_day` as its birth date,
     * deducts energy from parents, and updates lineage records.
     * Uses configuration properties and mutation strategy derived from the parent's genotype.
     *
     * @param mate        The other {@link Animal} partner for reproduction. Must not be null.
     * @param current_day The current simulation day, which will be the child's `birthDate`.
     * @return The newly created offspring {@link Animal}, or {@code null} if reproduction fails (e.g., insufficient energy).
     */
    public Animal reproduce(Animal mate, int current_day) {
        Objects.requireNonNull(mate, "Mate cannot be null for reproduction.");
        // 1. Check energy requirements for both parents to initiate
        if (this.energy < this.energyToReproduce || mate.energy < this.energyToReproduce) {
            return null;
        }
        // 2. Check if parents can afford the energy cost to pass to child
        if (this.energy < this.energyToPassToChild || mate.energy < this.energyToPassToChild) {
            return null;
        }

        // 3. Deduct energy cost from parents
        this.energy -= this.energyToPassToChild;
        mate.energy -= this.energyToPassToChild;

        // 4. Calculate child's starting energy
        int childEnergy = this.energyToPassToChild + mate.energyToPassToChild; // Assumes symmetric cost

        // 5. Get necessary info from own genotype (properties, mutation strategy)
        SimulationProperties props = genotype.getSimulationProperties();
        Mutation mutationStrategy = genotype.getMutation();

        // 6. Create child's genotype
        Genotype childGenotype = new Genotype(this, mate, props, mutationStrategy);

        // 7. Create the child Animal instance, setting its birth date to current_day
        Animal child = new Animal(this.positionDirection.position(), props, childGenotype, current_day);
        child.setEnergy(childEnergy); // Set child's initial energy

        // 8. Update lineage records for both parents
        this.addChildToList(child);
        mate.addChildToList(child);

        return child; // Return the newly created child
    }


    /** Adds a child to this animal's offspring list and increments counter. Protected access. */
    protected void addChildToList(Animal child) {
        // Consider thread safety if accessed concurrently, though typically only called from reproduce
        this.children.add(child);
        this.childrenMade++;
    }

    // --- Getters ---

    /** Gets the unique animal ID. */
    public long getId() { return this.id; }
    /** Gets the current position. */
    @Override public Vector2d getPos() { return this.positionDirection.position(); }
    /** Gets the element type (ANIMAL). */
    @Override public ElementType getElementType() { return ElementType.ANIMAL; }
    /** Gets the current direction. */
    public MapDirection getDirection() { return this.positionDirection.direction(); }
    /** Gets the current energy level. */
    public int getEnergy() { return this.energy; }
    /** Gets a copy of the gene sequence. */
    public int[] getGenes() { return Arrays.copyOf(this.genes, this.genes.length); }
    /** Gets the index of the currently active gene. */
    public int getActiveGeneIndex() { return this.geneIndex; }
    /** Gets the simulation day of birth. */
    public int getBirthDate() { return this.birthDate; }
    /** Gets the simulation day of death (-1 if alive). */
    public int getDeathDate() { return this.deathDate; }
    /** Checks if the animal is alive. */
    public boolean isAlive() { return this.deathDate == -1; }
    /** Gets the count of direct children produced. */
    public int getChildrenMade() { return this.childrenMade; }
    /** Recursively counts all descendants. Caution: potentially expensive. */
    public int getDescendantsCount() {
        int count = children.size();
        // Iterate over snapshot for safety against concurrent modification (though unlikely here)
        List<Animal> childrenSnapshot = new ArrayList<>(children);
        for (Animal child : childrenSnapshot) {
            count += child.getDescendantsCount(); // Recursive call
        }
        return count;
    }
    /** Gets the total count of plants eaten. */
    public int getPlantsEaten() { return this.plantsEaten; }
    /** Gets the current age in days (based on internal counter incremented by `move()`). */
    public int getAge() { return this.age; }

    // --- Setters (Protected: limit external modification) ---

    /** Sets energy, ensuring it's non-negative. Protected access. */
    public void setEnergy(int energy) { this.energy = Math.max(0, energy); }
    /** Sets death date if not already set. Protected access. */
    public void setDeathDate(int date) { if (this.deathDate == -1) this.deathDate = date; }
    /** Sets the active gene index, wrapping around if necessary using `floorMod`. Protected access. */
    public void setGeneIndex(int newGeneIndex) {
        if (this.genes.length == 0) this.geneIndex = 0;
        else this.geneIndex = Math.floorMod(newGeneIndex, this.genes.length);
    }

    // --- Object Overrides ---
    /** Equality based solely on unique ID. */
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; return id == ((Animal) o).id; }
    /** Hash code based solely on unique ID. */
    @Override public int hashCode() { return Long.hashCode(id); }
    /** Simple string representation (direction symbol for console maps). */
    @Override public String toString() { return this.positionDirection.direction().toString(); }
}