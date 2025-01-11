/*
todo:
add JavaFX display properties
add alt movement variant
*/

package proj.model.elements;

import proj.model.genotype.Genotype;
import proj.model.genotype.MutationVariant;
import proj.model.maps.MoveValidator;
import proj.model.movement.MovementVariant;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Animal implements WorldElement {
    private final MovementVariant movementVariant;
    private final int energyToReproduce;
    private final int energyToPassToChild;
    private final int energyCostToMove;
    private final int birthDate;
    private PositionDirectionTuple positionDirection;
    private int energy;
    private int age;
    private int deathDate;
    private int plantsEaten;
    private int childrenMade;
    private final MutationVariant mutationVariant;
    private int[] genotype;
    private final List<Animal> children = new ArrayList<>();

    // Constructor
    public Animal(Vector2d position, Genotype genotype, SimulationProperties simulationProperties) {
        this.movementVariant = simulationProperties.getMovementVariant();
        this.energyToReproduce = simulationProperties.getEnergyNeededToReproduce();
        this.energyToPassToChild = simulationProperties.getEnergyToPassToChild();
        this.energyCostToMove = simulationProperties.getEnergyCostToMove();
        this.birthDate = simulationProperties.getDaysElapsed();
        this.positionDirection = new PositionDirectionTuple(position, MapDirection.getRandomDirection());
        this.energy = simulationProperties.getStartEnergy();
        this.age = 0;
        this.deathDate = -1;
        this.plantsEaten = 0;
        this.childrenMade = 0;
        this.mutationVariant = simulationProperties.getMutationVariant();
        this.genotype = genotype.getGenes();
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
            Genotype childGenotype = new Genotype(simulationProperties, mutationVariant);
            childGenotype.generateChildGenes(this, mate);
            int[] childGenes = childGenotype.getGenes();
            int energyForChild = Math.min(this.energyToPassToChild, Math.min(this.energy, mate.energy));

            this.energy -= energyForChild;
            mate.energy -= energyForChild;

            Animal child = new Animal(this.positionDirection.position(), childGenotype, simulationProperties);
//            child.setGenotype(childGenes);
            child.setEnergy(2 * energyForChild);
            addChildToList(child);
            mate.addChildToList(child);
            return child;
        }
        return null;
    }

    public void addChildToList(Animal child) {
        this.children.add(child);
        this.childrenMade++;
    }

    @Override
    public String toString() {
        return this.positionDirection.direction().toString();
        // return " "; // for testing purposes - less visual clutter
    }

    // Getters
    @Override
    public Vector2d getPos() {return this.positionDirection.position();}
    public int getEnergy() {return this.energy;}
    public int[] getGenotype() {return this.genotype;}

    // Setters
    public void setEnergy(int energy) {this.energy = energy;}
    public void setGenotype(int[] genotype) {this.genotype = genotype;}
    public void setDeathDate(int date) {this.deathDate = date;}
}
