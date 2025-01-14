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

public class Simulation implements Runnable {
    private final AbstractWorldMap map;
    private final List<Animal> animals;
    private final SimulationProperties simulationProperties;
    private final List<Animal> deadAnimals;
    private final Genotype genotype;
    private boolean running;

    // constructor
    public Simulation(AbstractWorldMap map, SimulationProperties simulationProperties, Mutation mutation) {
        this.map = map;
        this.animals = new ArrayList<>();
        this.simulationProperties = simulationProperties;
        this.deadAnimals = new ArrayList<>();
        this.genotype = new Genotype(simulationProperties, mutation);
        this.running = true;

        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(this.simulationProperties.getWidth(), this.simulationProperties.getHeight(), this.simulationProperties.getAnimalCount());
        for(Vector2d animalPosition : randomPositionGenerator) {
            Animal animal = new Animal(animalPosition, this.simulationProperties, this.genotype);
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

    public void updateWorldElements() {this.map.updateWorldElements();}

    public void moveAnimals() {this.animals.forEach(this.map::move);}

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
    public void togglePause() {this.running = !this.running;}

    @Override
    public String toString() {return map.toString();}
}
