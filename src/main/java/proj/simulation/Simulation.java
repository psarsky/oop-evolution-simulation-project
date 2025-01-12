/*
todo:
add csv logging
 */

package proj.simulation;


import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.model.genotype.Genotype;
import proj.model.genotype.MutationVariant;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps. MapVariant;
import proj.model.maps.WaterWorld;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.util.RandomPositionGenerator;
import proj.util.Vector2d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Simulation implements Runnable {
    private static final Random random = new Random();
    private final AbstractWorldMap map;
    private final List<Animal> animals;
    private final AbstractVegetationVariant vegetationVariant;
    private final SimulationProperties simulationProperties;
    private final List<Animal> deadAnimals;
    private final Genotype genotype;
    private boolean running;

    // constructor
    public Simulation(AbstractWorldMap map, AbstractVegetationVariant vegetationVariant, SimulationProperties simulationProperties) {
        this.map = map;
        this.animals = new ArrayList<>();
        this.vegetationVariant = vegetationVariant;
        this.simulationProperties = simulationProperties;
        this.deadAnimals = new ArrayList<>();
        this.genotype = new Genotype(simulationProperties);
        this.running = true;

        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(this.simulationProperties.getWidth(), this.simulationProperties.getHeight(), this.simulationProperties.getAnimalCount());
        for(Vector2d animalPosition : randomPositionGenerator) {
            Animal animal = new Animal(animalPosition, this.genotype, this.simulationProperties);
            this.animals.add(animal);
            this.map.placeAnimal(animal.getPos(), animal);
            this.map.notifyObservers("New animal placed at " + animal.getPos() + ".");
        }

        int plantsToAdd = this.simulationProperties.getPlantCount();
        for (int i = 0; i < plantsToAdd; i++) {
            spawnPlant();
        }
        this.map.notifyObservers("Initial plants placed.");
    }

    @Override
    public void run() {
        while (!this.animals.isEmpty()) {
            if (this.running) {
                removeDeadAnimals();

                // update water
                if (this.simulationProperties.getMapVariant() == MapVariant.WATER_WORLD) {
                    boolean highTide = this.simulationProperties.getDaysElapsed() % 10 < 5;
                    ((WaterWorld) this.map).waterFlow(highTide);
                    ((WaterWorld) this.map).generateFreePlantPositions();
                }

                // move animals
                this.animals.forEach(this.map::move);

                eat();
                reproduce();
                growPlants();

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
    private void removeDeadAnimals() {
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

    private void growPlants() {
        int plantsToAdd = this.simulationProperties.getPlantsPerDay();
        for (int i = 0; i < plantsToAdd; i++) {
            spawnPlant();
        }
        this.map.notifyObservers("New plants placed.");
    }

    // utilities
    public void spawnPlant() {
        do {
            Vector2d plantPosition = this.map.getFreePlantPositions().get(random.nextInt(this.map.getFreePlantPositions().size()));
            if (this.vegetationVariant.validatePlantPosition(plantPosition)) {
                Plant plant = new Plant(plantPosition);
                this.map.placePlant(plantPosition, plant);
                break;
            }
        } while (!this.map.getFreePlantPositions().isEmpty());
    }

    public void togglePause() {this.running = !this.running;}

    @Override
    public String toString() {return map.toString();}
}
