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
 * Główna klasa symulacji, zarządza zwierzętami, roślinami i cyklem życia.
 * Implementuje {@link Runnable}, co pozwala na uruchomienie w oddzielnym wątku.
 */
public class Simulation implements Runnable {
    private final AbstractWorldMap map;
    private final List<Animal> animals;
    private final SimulationProperties simulationProperties;
    private final List<Animal> deadAnimals;
    private volatile boolean running = true;  // Czy symulacja jest włączona?
    private volatile boolean stopped = false; // Czy symulacja została zatrzymana?
    private final List<Runnable> dayEndListeners = new ArrayList<>(); // Listenerzy końca dnia

    /**
     * Konstruktor inicjalizujący symulację.
     */
    public Simulation(AbstractWorldMap map, SimulationProperties simulationProperties, Mutation mutation) {
        this.map = map;
        this.animals = new ArrayList<>();
        this.simulationProperties = simulationProperties;
        this.deadAnimals = new ArrayList<>();

        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(
                this.simulationProperties.getWidth(),
                this.simulationProperties.getHeight(),
                this.simulationProperties.getAnimalCount()
        );

        for (Vector2d animalPosition : randomPositionGenerator) {
            Genotype genotype = new Genotype(simulationProperties, mutation);
            Animal animal = new Animal(animalPosition, this.simulationProperties, genotype);
            this.animals.add(animal);
            this.map.placeAnimal(animal.getPos(), animal);
            this.map.notifyObservers("Nowe zwierzę na " + animal.getPos() + ".");
        }

        int plantsToAdd = this.simulationProperties.getPlantCount();
        for (int i = 0; i < plantsToAdd; i++) {
            this.map.spawnPlant();
        }
        this.map.notifyObservers("Początkowe rośliny rozmieszczone.");
    }

    /**
     * Dodaje listenera końca dnia.
     *
     * @param listener Listener do dodania
     */
    public void addDayEndListener(Runnable listener) {
        dayEndListeners.add(listener);
    }

    /**
     * Uruchamia symulację w nowym wątku.
     */
    public void start() {
        stopped = false;
        Thread simulationThread = new Thread(this);
        simulationThread.start();
    }

    /**
     * Zatrzymuje symulację całkowicie.
     */
    public void stop() {
        stopped = true;
    }

    /**
     * Wstrzymuje symulację bez możliwości wznowienia (jeśli potrzebne osobno).
     */
    public void pause() {
        running = false;
    }

    /**
     * Przełącza stan symulacji między pauzą a wznowieniem.
     */
    public void togglePause() {
        running = !running;
    }

    /**
     * Główna pętla symulacji.
     */
    @Override
    public void run() {
        while (!animals.isEmpty() && !stopped) { // Sprawdza, czy symulacja ma się zatrzymać
            if (running) {
                synchronized (this) {
                    removeDeadAnimals();
                    updateWorldElements();
                    moveAnimals();
                    eat();
                    reproduce();
                    simulationProperties.incrementDaysElapsed();
                    map.notifyObservers("Dzień " + simulationProperties.getDaysElapsed() + " minął.");

                    // Powiadomienie listenerów o końcu dnia
                    notifyDayEndListeners();
                }
            }
            try {
                Thread.sleep(simulationProperties.getSimulationStep());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Powiadamia wszystkich zarejestrowanych listenerów o końcu dnia.
     */
    private void notifyDayEndListeners() {
        for (Runnable listener : dayEndListeners) {
            listener.run();
        }
    }

    // Reszta metod pozostaje bez zmian...
    public synchronized void removeDeadAnimals() {
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

    public void updateWorldElements() {
        this.map.updateWorldElements();
    }

    public void moveAnimals() {
        this.animals.forEach(this.map::move);
    }

    public void eat() {
        for (Vector2d position : this.map.getAnimals().keySet()) {
            if (this.map.getPlants().containsKey(position)) {
                List<Animal> animalList = this.map.getAnimals().get(position);
                if (!animalList.isEmpty()) {
                    animalList.sort(Comparator.comparingInt(Animal::getEnergy).reversed());
                    Animal strongestAnimal = animalList.getFirst();
                    synchronized (this) {
                        strongestAnimal.eatPlant(this.simulationProperties.getPlantEnergy());
                        this.map.getPlants().remove(position);
                        this.map.getFreePlantPositions().add(position);
                    }
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
                    synchronized (this) {
                        Animal child = parent1.reproduce(parent2, this.simulationProperties);
                        if (child != null) {
                            animalList.add(child);
                            this.animals.add(child);
                            this.map.notifyObservers("Nowe zwierzę urodziło się na " + child.getPos() + ".");
                        }
                    }
                }
            }
        });
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public List<Animal> getAnimals() {
        return this.animals;
    }

    public List<Animal> getDeadAnimals() {
        return this.deadAnimals;
    }

    public boolean isRunning() {
        return this.running;
    }

    public AbstractWorldMap getMap() {
        return this.map;
    }
}