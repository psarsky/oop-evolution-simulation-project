/*
Todo:
- Add JavaFX display properties
- Add alternative movement variant
*/

package proj.model.elements;

import proj.model.genotype.Genotype;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Animal class represents a single animal in the simulation.
 * Animals move, consume energy, eat plants, and reproduce based on their genotype and simulation properties.
 */
public class Animal implements WorldElement {
    private final static Random random = new Random();
    private final Genotype genotype;
    private final List<Animal> children = new ArrayList<>(); // List of all offspring produced by this animal.
    private final int energyToReproduce; // Energy required for the animal to reproduce.
    private final int energyToPassToChild; // Energy passed to offspring during reproduction.
    private final int energyCostToMove; // Energy cost incurred by the animal during movement.
    private final int birthDate; // The day the animal was born.
    private PositionDirectionTuple positionDirection; // Represents the animal's current position and direction on the map.
    private int geneIndex; // Index of last used gene from the animal's genotype.
    private int energy; // Current energy level of the animal.
    private int age; // Current age of the animal in simulation days.
    private int deathDate; // Day the animal died (or -1 if still alive).
    private int plantsEaten; // Total number of plants consumed by the animal.
    private int childrenMade; // Total number of offspring produced by the animal.
    private int[] genes; // Genetic sequence defining the animal's behavior and traits.

    /**
     * Constructs a new Animal object with initial parameters.
     *
     * @param position                  Initial position of the animal on the map.
     * @param genotype                  Genotype object containing the animal's genes.
     * @param simulationProperties      Properties defining the simulation's configuration.
     */
    public Animal(Vector2d position, SimulationProperties simulationProperties, Genotype genotype) {
        this.genotype = genotype;
        this.energyToReproduce = simulationProperties.getEnergyNeededToReproduce();
        this.energyToPassToChild = simulationProperties.getEnergyToPassToChild();
        this.energyCostToMove = simulationProperties.getEnergyCostToMove();
        this.birthDate = simulationProperties.getDaysElapsed();
        this.positionDirection = new PositionDirectionTuple(position, MapDirection.getRandomDirection());
        this.geneIndex = random.nextInt(simulationProperties.getGenotypeSize());
        this.energy = simulationProperties.getStartEnergy();
        this.age = 0;
        this.deathDate = -1;
        this.plantsEaten = 0;
        this.childrenMade = 0;
        this.genes = this.genotype.getGenes();
    }

    /**
     * Moves the animal based on its current genotype and validates the new position using a MoveValidator.
     *
     * @param newPositionDirection         Tuple containing the new position and direction of the animal.
     */
    public void move(PositionDirectionTuple newPositionDirection) {
        this.positionDirection = newPositionDirection;
        this.age++;
        this.energy = Math.max(0, this.energy - this.energyCostToMove);
    }

    /**
     * Increases the animal's energy by consuming a plant and increments the plants eaten counter.
     *
     * @param plantEnergy               Energy gained by consuming the plant.
     */
    public void eatPlant(int plantEnergy) {
        this.energy += plantEnergy;
        this.plantsEaten++;
    }

    /**
     * Reproduces a new animal with another mate, creating offspring with combined genotypes.
     *
     * @param mate                          Another animal to mate with.
     * @param simulationProperties          Properties of the simulation affecting reproduction.
     * @return                              The newly created offspring, or null if reproduction conditions are not met.
     */
    public Animal reproduce(Animal mate, SimulationProperties simulationProperties) {
        if (this.energy >= this.energyToReproduce && mate.energy >= mate.energyToReproduce) {
            Genotype childGenotype = new Genotype(this, mate, simulationProperties, this.genotype.getMutation());

            int energyForChild = Math.min(this.energyToPassToChild, Math.min(this.energy, mate.energy));

            this.energy -= energyForChild;
            mate.energy -= energyForChild;

            Animal child = new Animal(this.positionDirection.position(), simulationProperties, childGenotype);
            child.setEnergy(2 * energyForChild);

            this.addChildToList(child);
            mate.addChildToList(child);
            return child;
        }
        return null;
    }

    /**
     * Adds a child to the list of offspring and increments the children counter.
     *
     * @param child         The offspring to add.
     */
    public void addChildToList(Animal child) {
        this.children.add(child);
        this.childrenMade++;
    }

    /**
     * Returns a string representation of the animal's direction for debugging or display purposes.
     *
     * @return              The string representation of the animal's current direction.
     */
    @Override
    public String toString() {
        return this.positionDirection.direction().toString();
        // return " "; // for testing purposes, less visual clutter
    }

    // Getters

    /**
     * Gets the current position of the animal.
     *
     * @return              The animal's current position as a Vector2d object.
     */
    @Override
    public Vector2d getPos() {
        return this.positionDirection.position();
    }

    public MapDirection getDir() {
        return this.positionDirection.direction();
    }

    /**
     * Gets the current energy level of the animal.
     *
     * @return              The animal's energy level.
     */
    public int getEnergy() {
        return this.energy;
    }

    /**
     * Gets the gene array of the animal.
     *
     * @return              The animal's gene array.
     */
    public int[] getGenes() {
        return this.genes;
    }
    public ElementType getElementType() {return ElementType.ANIMAL;}
    public int getBirthDate() {return this.birthDate;}
    public int getDeathDate() {return this.deathDate;}
    public int getChildrenMade() {return this.childrenMade;}
    public int getPlantsEaten() {return this.plantsEaten;}
    public int getAge() {return this.age;}

    // Setters

    /**
     * Sets the energy level of the animal.
     *
     * @param energy            The new energy level.
     */
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    /**
     * Sets the genes of the animal.
     *
     * @param genes          The new genes array.
     */
    public void setGenes(int[] genes) {
        this.genes = genes;
    }

    /**
     * Sets the death date of the animal.
     *
     * @param date          The day the animal died.
     */
    public void setDeathDate(int date) {
        this.deathDate = date;
    }

    public int getGeneIndex() {return this.geneIndex;}

    public void setGeneIndex(int newGeneIndex) {this.geneIndex = newGeneIndex;}
}