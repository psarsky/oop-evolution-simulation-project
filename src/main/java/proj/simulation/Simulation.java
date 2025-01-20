/*
todo:
add csv logging
 */

package proj.simulation;


import proj.model.elements.Animal;
import proj.model.genotype.Genotype;
import proj.model.genotype.Mutation;
import proj.model.maps.AbstractWorldMap;
import proj.util.RandomPositionGenerator;
import proj.util.Vector2d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code Simulation} class represents the core simulation engine, managing the lifecycle of
 * animals, plants, and other elements on the simulation map.
 * <p>
 * This class implements {@link Runnable} to enable running the simulation in a separate thread.
 * It manages tasks such as animal movement, reproduction, energy consumption, and plant spawning.
 * </p>
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>, <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public class Simulation implements Runnable {
    private final AbstractWorldMap map;
    private final List<Animal> animals;
    private final SimulationProperties simulationProperties;
    private final List<Animal> deadAnimals;
    private boolean running;

    /**
     * Constructs a new {@code Simulation} instance with the specified map, properties, and mutation strategy.
     *
     * @param map                  An {@link AbstractWorldMap} object representing the map to simulate.
     * @param simulationProperties A {@link SimulationProperties} object defining the simulation's parameters.
     * @param mutation             A {@link Mutation} object defining the strategy for modifying genotypes.
     */
    public Simulation(AbstractWorldMap map, SimulationProperties simulationProperties, Mutation mutation) {
        this.map = map;
        this.animals = new ArrayList<>();
        this.simulationProperties = simulationProperties;
        this.deadAnimals = new ArrayList<>();
        this.running = true;

        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(this.simulationProperties.getWidth(), this.simulationProperties.getHeight(), this.simulationProperties.getAnimalCount());
        Genotype genotype = new Genotype(simulationProperties, mutation);
        for(Vector2d animalPosition : randomPositionGenerator) {
            Animal animal = new Animal(animalPosition, this.simulationProperties, genotype);
            this.animals.add(animal);
            this.map.placeAnimal(animal.getPos(), animal);
            this.map.notifyObservers("New animal placed at " + animal.getPos() + ".");
        }

        int plantsToAdd = this.simulationProperties.getPlantCount();
        for (int i = 0; i < plantsToAdd; i++) {
            this.map.spawnPlant();
        }
        this.map.notifyObservers("Initial plants placed.");
    }

    /**
     * Starts the simulation, running until there are no animals left or the simulation is paused.
     */
    @Override
    public void run() {
        while (!this.animals.isEmpty()) {
            if (this.running) {
                removeDeadAnimals();
                updateWorldElements();
                moveAnimals();
                eat();
                reproduce();
                this.simulationProperties.incrementDaysElapsed();
                this.map.notifyObservers("Day " + this.simulationProperties.getDaysElapsed() + " elapsed.");
            }
            try {
                Thread.sleep(this.simulationProperties.getSimulationStep());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // day management methods

    /**
     * Removes animals with zero or negative energy, recording their death details.
     */
    public void removeDeadAnimals() {
        List<Animal> deadAnimals = new ArrayList<>();
        for (Animal animal : this.animals) {
            if (animal.getEnergy() <= 0) {
                animal.setDeathDate(this.simulationProperties.getDaysElapsed());
                this.map.removeAnimal(animal);
                deadAnimals.add(animal);
            }
        }
        this.animals.removeAll(deadAnimals);
        this.deadAnimals.addAll(deadAnimals);
    }

    /**
     * Updates all inanimate elements on the map.
     */
    public void updateWorldElements() {this.map.updateWorldElements();}

    /**
     * Moves all animals present on the map.
     */
    public void moveAnimals() {this.animals.forEach(this.map::move);}

    /**
     * Allows animals to eat plants if available at their position, prioritizing stronger animals.
     */
    public void eat() {
        for (Vector2d position : this.map.getAnimals().keySet()) {
            if (this.map.getPlants().containsKey(position)) {
                List<Animal> animalList = this.map.getAnimals().get(position);
                if (!animalList.isEmpty()) {
                    animalList.sort(Comparator.comparingInt(Animal::getEnergy).reversed());
                    Animal strongestAnimal = animalList.getFirst();
                    strongestAnimal.eatPlant(this.simulationProperties.getPlantEnergy());
                    this.map.getPlants().remove(position);
                    this.map.getFreePlantPositions().add(position);
                }
            }
        }
    }

    /**
     * Manages animal reproduction, creating offspring if energy requirements are met.
     */
    public void reproduce() {
        this.map.getAnimals().values().forEach(animalList -> {
            if (animalList.size() > 1) {
                animalList.sort(Comparator.comparingInt(Animal::getEnergy).reversed());
                Animal parent1 = animalList.get(0);
                Animal parent2 = animalList.get(1);

                if (parent1.getEnergy() > this.simulationProperties.getEnergyNeededToReproduce() &&
                        parent2.getEnergy() > this.simulationProperties.getEnergyNeededToReproduce()) {
                    Animal child = parent1.reproduce(parent2, this.simulationProperties);
                    if (child != null) {
                        animalList.add(child);
                        this.animals.add(child);
                        this.map.notifyObservers("New child placed at " + child.getPos() + ".");
                    }
                }
            }
        });
    }

    // utilities

    /**
     * Toggles the running state of the simulation between paused and active.
     */
    public void togglePause() {this.running = !this.running;}

    /**
     * Returns a string representation of the map.
     *
     * @return The {@link String} representation of the map.
     */
    @Override
    public String toString() {return map.toString();}

    /**
     * Gets the list of all alive animals in the simulation.
     *
     * @return A {@link List} of {@link Animal} objects.
     */
    public List<Animal> getAnimals() {return this.animals;}

    /**
     * Gets the list of all dead animals in the simulation.
     *
     * @return A {@link List} of {@link Animal} objects.
     */
    public List<Animal> getDeadAnimals() {return this.deadAnimals;}

    /**
     * Gets the current state of the simulation.
     *
     * @return  {@code true} if the simulation is running, {@code false} if otherwise.
     */
    public boolean isRunning() {return this.running;}

    public AbstractWorldMap getMap() {return this.map;}
}
