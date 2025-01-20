package proj.model.maps;

import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.model.elements.WorldElement;
import proj.model.movement.AbstractMovementVariant;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.presenter.MapChangeListener;
import proj.presenter.MapVisualizer;
import proj.simulation.SimulationProperties;
import proj.util.Boundary;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;

import java.util.*;

/**
 * Represents an abstract world map for a simulation.
 * Manages the placement and movement of animals and plants,
 * while notifying observers about changes.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>
 */
public abstract class AbstractWorldMap implements MoveValidator {
    private static final Random random = new Random();
    protected final List<MapChangeListener> observers;      // List of observers notified when the map changes.
    protected final MapVisualizer mapVisualizer;            // Visualizer responsible for rendering the map.
    protected final AbstractVegetationVariant vegetation;   // Vegetation variant utilized in the map.
    protected final AbstractMovementVariant movement;       // Movement variant utilized in the map.
    protected final UUID id;                                // Unique identifier for the map instance.
    protected HashMap<Vector2d, List<Animal>> animals;      // Mapping of positions to animals.
    protected HashMap<Vector2d, Plant> plants;              // Mapping of positions to plants.
    protected List<Vector2d> freePlantPositions;            // List of free positions available for plants.
    protected SimulationProperties simulationProperties;    // Properties os the simulation in which the map is initialized.

    /**
     * Constructs an {@code AbstractWorldMap} using simulation properties.
     * Initializes the dimensions, animals, plants, and free positions for plants.
     *
     * @param simulationProperties  A {@link SimulationProperties} object defining the simulation parameters.
     * @param vegetation            An {@link AbstractVegetationVariant} object defining vegetation rules.
     * @param movement              An {@link AbstractMovementVariant} object defining movement rules.
     */
    public AbstractWorldMap(SimulationProperties simulationProperties, AbstractVegetationVariant vegetation, AbstractMovementVariant movement) {
        this.observers = new ArrayList<>();
        this.mapVisualizer = new MapVisualizer(this);
        this.vegetation = vegetation;
        this.movement = movement;
        this.id = UUID.randomUUID();
        this.animals = new HashMap<>();
        this.plants = new HashMap<>();
        this.freePlantPositions = new ArrayList<>();
        this.simulationProperties = simulationProperties;

        // Populate the list of free positions with all positions on the map.
        for (int x = 0; x < this.simulationProperties.getWidth(); x++) {
            for (int y = 0; y < this.simulationProperties.getHeight(); y++) {
                Vector2d position = new Vector2d(x, y);
                this.freePlantPositions.add(position);
            }
        }
    }

    /**
     * Places an animal at a given position on the map.
     * If there are already animals at the position, the new one is added to the list.
     *
     * @param position  A {@link Vector2d} object defining the position to place the animal.
     * @param animal    An {@link Animal} object to place.
     */
    public synchronized void placeAnimal(Vector2d position, Animal animal) {
        if (this.animals.containsKey(position)) {
            this.animals.get(position).add(animal);
        } else {
            List<Animal> animalList = new ArrayList<>();
            animalList.add(animal);
            this.animals.put(position, animalList);
        }
    }

    /**
     * Removes an animal from its current position on the map.
     * Notifies observers that the animal has died.
     *
     * @param animal An {@link Animal} object to remove.
     */
    public synchronized void removeAnimal(Animal animal) {
        this.animals.get(animal.getPos()).remove(animal);
        notifyObservers("Animal died at " + animal.getPos() + ".");
    }

    /**
     * Moves an animal to a new position on the map.
     * Updates the animal's position and notifies observers of the change.
     *
     * @param animal An {@link Animal} object to move.
     */
    public synchronized void move(Animal animal) {
        Vector2d oldPos = animal.getPos();
        this.animals.get(animal.getPos()).remove(animal);
        this.movement.move(animal, this);
        placeAnimal(animal.getPos(), animal);
        notifyObservers("Animal moved from " + oldPos + " to " + animal.getPos() + ".");
    }

    /**
     * Places a plant at a given position on the map.
     * Removes the position from the list of free plant positions.
     *
     * @param position  A {@link Vector2d} object defining the position to place the plant.
     * @param plant     A {@link Plant} object to place.
     */
    public synchronized void placePlant(Vector2d position, Plant plant) {
        this.plants.put(position, plant);
        this.freePlantPositions.remove(position);
    }

    /**
     * Spawns a plant on a random position on the map, according to
     * vegetation logic defined by the vegetation variant.
     */
    public void spawnPlant() {
        while (!this.freePlantPositions.isEmpty()) {
            Vector2d plantPosition = this.freePlantPositions.get(random.nextInt(this.freePlantPositions.size()));
            if (this.vegetation.validatePlantPosition(plantPosition)) {
                Plant plant = new Plant(plantPosition);
                placePlant(plantPosition, plant);
                break;
            }
        }
    }

    /**
     * Updates all inanimate elements on the map (only {@link Plant} objects by default).
     */
    public synchronized void updateWorldElements() {
        for (int i = 0; i < this.simulationProperties.getPlantsPerDay(); i++) {
            spawnPlant();
        }
        notifyObservers("New plants placed.");
    }

    /**
     * Returns the object (animal or plant) located at a given position on the map.
     *
     * @param position  A {@link Vector2d} object defining the position to check.
     * @return          The {@link WorldElement} object at the position, or null if none exists.
     */
    public synchronized WorldElement objectAt(Vector2d position) {
        if (this.animals.containsKey(position)) {
            if (!this.animals.get(position).isEmpty())
                return this.animals.get(position).getFirst();
        }
        if (this.plants.containsKey(position)) return this.plants.get(position);
        return null;
    }

    /**
     * Adds an observer to the list of map change listeners.
     *
     * @param observer The observer to add ({@link MapChangeListener}).
     */
    public void addObserver(MapChangeListener observer) {
        this.observers.add(observer);
    }

    /**
     * Notifies all observers of a change in the map.
     *
     * @param message The message describing the change ({@link String}).
     */
    public void notifyObservers(String message) {
        for (MapChangeListener observer : this.observers) {
            observer.mapChanged(this, message);
        }
    }

    /**
     * Corrects an animal's position and direction when it moves outside the map boundaries.
     * Handles wrapping and redirection based on the map's dimensions.
     * Wraps positions horizontally and reflects directions vertically.
     *
     * @param oldPosition   The entity's position before the move ({@link Vector2d}).
     * @param newPosition   The intended position after the move ({@link Vector2d}).
     * @param direction     The intended direction of movement ({@link MapDirection}).
     * @return              A {@link PositionDirectionTuple} containing the corrected position and direction.
     */
    @Override
    public PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction) {
        int newX = (newPosition.x() + this.simulationProperties.getWidth()) % this.simulationProperties.getWidth();
        int newY = newPosition.y();
        MapDirection newDirection = direction;

        // Adjust position and direction if crossing vertical boundaries.
        if (newY < 0) {
            newY = 1;
            newDirection = switch (direction) {
                case SOUTH -> MapDirection.NORTH;
                case SOUTHEAST -> MapDirection.NORTHEAST;
                case SOUTHWEST -> MapDirection.NORTHWEST;
                default -> direction;
            };
        }
        if (newY >= this.simulationProperties.getHeight()) {
            newY = this.simulationProperties.getHeight() - 2;
            newDirection = switch (direction) {
                case NORTH -> MapDirection.SOUTH;
                case NORTHEAST -> MapDirection.SOUTHEAST;
                case NORTHWEST -> MapDirection.SOUTHWEST;
                default -> direction;
            };
        }
        return new PositionDirectionTuple(new Vector2d(newX, newY), newDirection);
    }

    /**
     * Renders the current map as a string representation using the map visualizer.
     *
     * @return The {@link String} representation of the map.
     */
    @Override
    public String toString() {
        return this.mapVisualizer.draw(getCurrentBounds().lowerLeft(), getCurrentBounds().upperRight());
    }

    // Getters

    /**
     * Gets a hash map of all animals on the map with the positions that they occupy.
     *
     * @return  A {@link HashMap} containing ({@link Vector2d}, {@link List}) pairs
     *          representing a map position and a list of animals occupying the position.
     */
    public HashMap<Vector2d, List<Animal>> getAnimals() {return this.animals;}

    /**
     * Gets a hash map of all plants on the map with the positions that they occupy.
     *
     * @return  A {@link HashMap} containing ({@link Vector2d}, {@link Plant}) pairs
     *          representing a map position and a plant occupying the position.
     */
    public HashMap<Vector2d, Plant> getPlants() {return this.plants;}

    /**
     * Gets a list of all positions on the map that are free for plants to grow on.
     *
     * @return  A {@link List} containing {@link Vector2d} objects representing the free positions.
     */
    public List<Vector2d> getFreePlantPositions() {return this.freePlantPositions;}

    /**
     * Gets the map's unique ID.
     *
     * @return  A {@link UUID} object.
     */
    public UUID getID() {return this.id;}

    /**
     * Gets the map's boundaries.
     *
     * @return  A {@link Boundary} object.
     */
    public Boundary getCurrentBounds() {
        return new Boundary(new Vector2d(0, 0), new Vector2d(this.simulationProperties.getWidth() - 1, this.simulationProperties.getHeight() - 1));
    }
}