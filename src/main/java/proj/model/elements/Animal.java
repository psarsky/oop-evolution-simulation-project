/*
todo:
add JavaFX display properties
add alt movement variant
*/

package proj.model.elements;

import proj.model.maps.MoveValidator;
import proj.model.movement.MovementVariant;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.*;

public class Animal implements WorldElement {
    private final MovementVariant movementVariant;
    private final int energyToReproduce;
    private final int energyToPassToChild;
    private final int birthDate;
    private PositionDirectionTuple positionDirection;
    private int energy;
    private int age;
    private int deathDate;
    private int plantsEaten;
    private int childrenMade;
    private int[] genotype;
    private final List<Animal> children = new ArrayList<>();

    // Constructor
    public Animal(Vector2d position, SimulationProperties simulationProperties) {
        this.movementVariant = simulationProperties.getMovementVariant();
        this.energyToReproduce = simulationProperties.getEnergyToReproduce();
        this.energyToPassToChild = simulationProperties.getEnergyToPassToChild();
        this.birthDate = simulationProperties.getDaysElapsed();
        this.positionDirection = new PositionDirectionTuple(position, MapDirection.getRandomDirection());
        this.energy = simulationProperties.getStartEnergy();
        this.age = 0;
        this.deathDate = -1;
        this.plantsEaten = 0;
        this.childrenMade = 0;
        this.genotype = Genotype.generateRandomGenes();
    }

    // Movement dictated by genome
    public void move(MoveValidator validator) {
        Random random = new Random();
        int geneIndex = random.nextInt(this.genotype.length);
        int rotationAngle = this.genotype[geneIndex];
        MapDirection newDirection = this.positionDirection.direction().rotate(rotationAngle);
        Vector2d newPosition = this.positionDirection.position().add(this.positionDirection.direction().toUnitVector());
        this.positionDirection = validator.correctPosition(this.positionDirection.position(), newPosition, newDirection);
        this.age++;
        this.energy = Math.max(0, this.energy - 1);
    }

    // Eating plants
    public void eatPlant(int plantEnergy) {
        this.energy += plantEnergy;
        this.plantsEaten++;
    }

    // Reproduction
    public Animal reproduce(Animal mate, SimulationProperties simulationProperties) {
        if (this.energy >= this.energyToReproduce && mate.energy >= mate.energyToReproduce) {
            int[] childGenome = Genotype.createGenesFromParents(this, mate, simulationProperties);
            int energyForChild = Math.min(this.energyToPassToChild, Math.min(this.energy, mate.energy));

            this.energy -= energyForChild;
            mate.energy -= energyForChild;

            Animal child = new Animal(this.positionDirection.position(), simulationProperties);
            child.setGenotype(childGenome);
            child.setEnergy(2 * energyForChild);
            this.children.add(child);
            mate.children.add(child);

            this.childrenMade++;
            mate.childrenMade++;

            return child;
        }
        return null;
    }

    // children
    public void addChildToList(Animal child) {
        this.children.add(child);
        this.childrenMade++;
    }

    /*
    // Display properties
    @Override
    public String toString() {
        return String.format("Animal at %s facing %s, energy: %d, age: %d, children: %d, plants eaten: %d",
                this.position, this.direction, this.energy, this.age, this.childrenMade, this.plantsEaten);
    }
    */
    @Override
    public String toString() {
        return this.positionDirection.direction().toString();
        // return " "; // for testing purposes - less visual clutter
    }

    // Getters
    @Override
    public ElementType getElementType() {return ElementType.ANIMAL;}
    @Override
    public Vector2d getPos() {return this.positionDirection.position();}

    // Setters
    public void setEnergy(int energy) {this.energy = energy;}
    public void setGenotype(int[] genotype) {this.genotype = genotype;}
}
