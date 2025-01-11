package proj.model.maps;

import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.model.elements.WorldElement;
import proj.presenter.MapChangeListener;
import proj.presenter.MapVisualizer;
import proj.simulation.SimulationProperties;
import proj.util.Boundary;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.*;

public abstract class AbstractWorldMap implements MoveValidator {
    protected final List<MapChangeListener> observers;
    protected final MapVisualizer mapVisualizer;
    protected final UUID id;
    protected HashMap<Vector2d, List<Animal>> animals;
    protected HashMap<Vector2d, Plant> plants;
    protected List<Vector2d> freePlantPositions;
    protected int width;
    protected int height;

    // constructor
    public AbstractWorldMap(SimulationProperties simulationProperties) {
        this.observers = new ArrayList<>();
        this.mapVisualizer = new MapVisualizer(this);
        this.id = UUID.randomUUID();
        this.animals = new HashMap<>();
        this.plants = new HashMap<>();
        this.freePlantPositions = new ArrayList<>();
        this.width = simulationProperties.getWidth();
        this.height = simulationProperties.getHeight();

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                Vector2d position = new Vector2d(x, y);
                this.freePlantPositions.add(position);
            }
        }
    }

    public void placeAnimal(Vector2d position, Animal animal) {
        if (this.animals.containsKey(position)) {
            this.animals.get(position).add(animal);
        }
        else {
            List<Animal> animalList = new ArrayList<>();
            animalList.add(animal);
            this.animals.put(position, animalList);
        }
    }

    public void removeAnimal(Animal animal) {
        this.animals.get(animal.getPos()).remove(animal);
        notifyObservers("Animal died at " + animal.getPos() + ".");
    }

    public void placePlant(Vector2d position, Plant plant) {
        this.plants.put(position, plant);
        this.freePlantPositions.remove(position);
    }

    public void move(Animal animal) {
        Vector2d oldPos = animal.getPos();
        this.animals.get(animal.getPos()).remove(animal);
        animal.move(this);
        placeAnimal(animal.getPos(), animal);
        notifyObservers("Animal moved from " + oldPos + " to " + animal.getPos() + ".");
    }

    public WorldElement objectAt(Vector2d position) {
        if (this.animals.containsKey(position)) {
            if (!this.animals.get(position).isEmpty())
                return this.animals.get(position).getFirst();
        }
        if (this.plants.containsKey(position)) return this.plants.get(position);
        return null;
    }

    // observers
    public void addObserver(MapChangeListener observer) {this.observers.add(observer);}
    public void notifyObservers(String message) {
        for (MapChangeListener observer : this.observers) {
            observer.mapChanged(this, message);
        }
    }

    @Override
    public PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction) {
        int newX = newPosition.x() % this.width;
        int newY = newPosition.y();
        MapDirection newDirection = direction;
        if (newY < 0) {
            newY = 1;
            newDirection = switch(direction) {
                case SOUTH -> MapDirection.NORTH;
                case SOUTHEAST -> MapDirection.NORTHEAST;
                case SOUTHWEST -> MapDirection.NORTHWEST;
                default -> direction;
            };
        }
        if (newY >= this.height) {
            newY = this.height - 2;
            newDirection = switch(direction) {
                case NORTH -> MapDirection.SOUTH;
                case NORTHEAST -> MapDirection.SOUTHEAST;
                case NORTHWEST -> MapDirection.SOUTHWEST;
                default -> direction;
            };
        }
        return new PositionDirectionTuple(new Vector2d(newX, newY), newDirection);
    }

    @Override
    public String toString() {
        return this.mapVisualizer.draw(getCurrentBounds().lowerLeft(), getCurrentBounds().upperRight());
    }

    // getters
    public HashMap<Vector2d, List<Animal>> getAnimals() {return this.animals;}
    public HashMap<Vector2d, Plant> getPlants() {return this.plants;}
    public int getPlantsCount() {return this.plants.size();}
    public List<Vector2d> getFreePositionsForPlants() {return this.freePlantPositions;}
    public UUID getID() {return this.id;}
    public Boundary getCurrentBounds() {return new Boundary(new Vector2d(0, 0), new Vector2d(this.width - 1, this.height - 1));}
}
