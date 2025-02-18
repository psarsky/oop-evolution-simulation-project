package proj.model.maps;

import proj.model.elements.Water;
import proj.model.elements.WorldElement;
import proj.model.movement.AbstractMovementVariant;
import proj.model.vegetation.AbstractVegetationVariant;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.RandomPositionGenerator;
import proj.util.Vector2d;

import java.util.*;

/**
 * Represents a map with a water-world topology.
 * Water occupies a portion of the map and interacts dynamically with other elements,
 * such as plants and animals, based on tides or other conditions.
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>, <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public class WaterWorld extends AbstractWorldMap {
    private final HashMap<Vector2d, Water> waterFields = new HashMap<>();

    /**
     * Constructor for the {@code WaterWorld} map.
     * Initializes the map with water fields distributed randomly based on simulation properties.
     *
     * @param simulationProperties  A {@link SimulationProperties} object defining the simulation parameters.
     * @param vegetation            An {@link AbstractVegetationVariant} object defining vegetation rules.
     * @param movement              An {@link AbstractMovementVariant} object defining movement rules.
     */
    public WaterWorld(SimulationProperties simulationProperties, AbstractVegetationVariant vegetation, AbstractMovementVariant movement) {
        super(simulationProperties, vegetation, movement);
        RandomPositionGenerator randomPositionGeneratorWater =
                new RandomPositionGenerator(this.simulationProperties.getWidth(), this.simulationProperties.getHeight(), this.simulationProperties.getWidth() * this.simulationProperties.getHeight() / 10);

        // Populate water fields at random positions
        for (Vector2d pos : randomPositionGeneratorWater) {
            this.waterFields.put(pos, new Water(pos));
        }
    }

    /**
     * Updates all inanimate elements on the map, including {@link Water} objects.
     */
    @Override
    public void updateWorldElements() {
        super.updateWorldElements();
        boolean highTide = this.simulationProperties.getDaysElapsed() % 10 < 5;
        waterFlow(highTide, this.simulationProperties.getWaterViolence());
        generateFreePlantPositions();
    }

    /**
     * Simulates the flow of water based on tidal conditions.
     * During high tide, water expands to new areas, potentially replacing plants and affecting animals.
     * During low tide, water retreats from some areas.
     *
     * @param highTide      A boolean value determining whether the simulation is currently in high tide.
     * @param waterViolence A percentage value determining the behavior of water. </br>
     *                      0% - The volume of water receding during low tide is slightly higher than the volume that returns during high tide. </br>
     *                      100% - The volume of water receding during low tide is two times lower than the volume that returns during high tide.
     */
    public void waterFlow(boolean highTide, int waterViolence) {
        int waterCount = this.waterFields.size();
        int waterToChangeCount = highTide ? waterCount / 5 : waterCount / (5 * (100 + waterViolence) / 100);

        waterToChangeCount = Math.max(waterToChangeCount, 1);

        List<Vector2d> selectedWaterFields = getSelectedWaterFields();
        Collections.shuffle(selectedWaterFields);

        waterToChangeCount = Math.min(selectedWaterFields.size(), waterToChangeCount);

        for (int i = 0; i < waterToChangeCount; i++) {
            Vector2d currentPosition = selectedWaterFields.get(i);

            if (highTide) {
                // Expand water fields during high tide
                MapDirection direction = MapDirection.getRandomDirection();
                Vector2d newPosition = currentPosition.add(direction.toUnitVector());

                // Ensure the new position is not already water
                for (int j = 0; j < 8 && this.waterFields.containsKey(newPosition); j++) {
                    direction = direction.next();
                    newPosition = currentPosition.add(direction.toUnitVector());
                }

                // Replace plants or add new water if valid
                this.plants.remove(newPosition);
                this.waterFields.put(newPosition, new Water(newPosition));

                // Remove water if it expands out of bounds
                if (newPosition.x() < 0 || newPosition.x() > simulationProperties.getWidth() - 1 ||
                        newPosition.y() < 0 || newPosition.y() > simulationProperties.getHeight() - 1) {
                    this.waterFields.remove(newPosition);
                }

                // Reduce energy of animals on water fields
                this.animals.forEach((position, animalList) -> {
                    if (waterFields.containsKey(position)) {
                        animalList.forEach(animal -> animal.setEnergy(0));
                    }
                });

            } else {
                // Remove some water fields during low tide
                if (this.waterFields.size() > 1) {
                    this.waterFields.remove(currentPosition);
                }
            }
        }
    }

    /**
     * Retrieves a list of water fields that are adjacent to at least one non-water field.
     *
     * @return A {@link List} of {@link Vector2d} objects representing the positions of selected water fields.
     */
    private List<Vector2d> getSelectedWaterFields() {
        List<Vector2d> selectedWaterFields = new ArrayList<>(this.waterFields.keySet());
        List<Vector2d> selectedWaterFieldsCopy = new ArrayList<>(this.waterFields.keySet());

        selectedWaterFieldsCopy.forEach(waterField -> {
            boolean surrounded = this.waterFields.containsKey(waterField.add(new Vector2d(1, 0))) &&
                    this.waterFields.containsKey(waterField.add(new Vector2d(-1, 0))) &&
                    this.waterFields.containsKey(waterField.add(new Vector2d(0, 1))) &&
                    this.waterFields.containsKey(waterField.add(new Vector2d(0, -1)));
            if (surrounded) {
                selectedWaterFields.remove(waterField);
            }
        });
        return selectedWaterFields;
    }

    /**
     * Updates the list of positions where plants can grow by excluding water fields.
     */
    public synchronized void generateFreePlantPositions() {
        this.freePlantPositions.clear();
        for (int x = 0; x < this.simulationProperties.getWidth(); x++) {
            for (int y = 0; y < this.simulationProperties.getHeight(); y++) {
                Vector2d position = new Vector2d(x, y);
                if (!this.waterFields.containsKey(position)) this.freePlantPositions.add(position);
            }
        }
    }

    /**
     * Corrects the position of an entity if it moves into a water field.
     * If the position is within a water field, it redirects the entity to its previous position
     * and reverses its direction.
     *
     * @param oldPosition   The entity's position before the move ({@link Vector2d}).
     * @param newPosition   The intended position after the move ({@link Vector2d}).
     * @param direction     The intended direction of movement ({@link MapDirection}).
     * @return              A {@link PositionDirectionTuple} containing the corrected position and direction.
     */
    @Override
    public synchronized PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction) {
        PositionDirectionTuple newTuple = super.correctPosition(oldPosition, newPosition, direction);
        if (this.waterFields.containsKey(newTuple.position())) {
            return new PositionDirectionTuple(oldPosition, direction.opposite());
        }
        return newTuple;
    }

    /**
     * Retrieves the object (animal, plant, or water) located at the specified position.
     *
     * @param position  A {@link Vector2d} object defining the position to check.
     * @return          The {@link WorldElement} object at the position, or null if none exists.
     */
    @Override
    public synchronized WorldElement objectAt(Vector2d position) {
        if (this.animals.containsKey(position)) {
            if (!this.animals.get(position).isEmpty())
                return this.animals.get(position).getFirst();
        }
        if (this.waterFields.containsKey(position)) return this.waterFields.get(position);
        if (this.plants.containsKey(position)) return this.plants.get(position);
        return null;
    }

    /**
     * Gets a hash map with all water fields present on the map and their positions.
     *
     * @return  A {@link HashMap} containing ({@link Vector2d}, {@link Water}) pairs
     *          representing map positions and water objects.
     */
    public HashMap<Vector2d, Water> getWaterFields() {return this.waterFields;}
}