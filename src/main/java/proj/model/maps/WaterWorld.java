package proj.model.maps;


import proj.model.elements.Water;
import proj.model.elements.WorldElement;
import proj.simulation.SimulationProperties;
import proj.util.MapDirection;
import proj.util.PositionDirectionTuple;
import proj.util.RandomPositionGenerator;
import proj.util.Vector2d;

import java.util.*;

public class WaterWorld extends AbstractWorldMap {
    private final HashMap<Vector2d, Water> waterFields = new HashMap<>();

    // constructor
    public WaterWorld(SimulationProperties simulationProperties) {
        super(simulationProperties);
        RandomPositionGenerator randomPositionGeneratorWater = new RandomPositionGenerator(this.width, this.height, this.width * this.height / 10);
        for(Vector2d pos : randomPositionGeneratorWater) {
            this.waterFields.put(pos, new Water(pos));
        }
    }

    // simulation methods
    public void waterFlow(boolean highTide){
        int waterCount = this.waterFields.size();
        int waterToChangeCount = waterCount / 5;
        
        if (waterToChangeCount == 0) waterToChangeCount = 1;

        List<Vector2d> selectedWaterFields = getSelectedWaterFields();
        Collections.shuffle(selectedWaterFields);

        for (int i = 0; i < waterToChangeCount; i++) {
            Vector2d currentPosition = selectedWaterFields.get(i);

            if (highTide) {
                MapDirection direction = MapDirection.getRandomDirection();
                Vector2d newPosition = currentPosition.add(direction.toUnitVector());
                
                if (this.waterFields.containsKey(newPosition)) {
                    for (int j = 0; j < 8; j++) {
                        if (this.waterFields.containsKey(newPosition)) {
                            direction = direction.next();
                            newPosition = currentPosition.add(direction.toUnitVector());
                        }
                    }
                }

                if (plants.containsKey(newPosition)) {
                    plants.remove(newPosition);
                    this.waterFields.put(newPosition, new Water(newPosition));
                }
                else if (!this.waterFields.containsKey(newPosition)) {
                    this.waterFields.put(newPosition, new Water(newPosition));
                }

                if (newPosition.x() < 0
                        || newPosition.x() > width - 1
                        || newPosition.y() < 0
                        || newPosition.y() > height - 1) {
                    this.waterFields.remove(newPosition);
                }

                this.animals.forEach((position, animalList) -> {
                    if (waterFields.containsKey(position)) {
                        animalList.forEach(animal -> animal.setEnergy(0));
                    }
                });

            } else {
                if (this.waterFields.size() > 1) {
                    this.waterFields.remove(currentPosition);
                }
            }
        }
    }

    // returns a list of water fields that are adjacent to at least one non-water field
    private List<Vector2d> getSelectedWaterFields() {
        List<Vector2d> selectedWaterFields = new ArrayList<>(this.waterFields.keySet());
        selectedWaterFields.forEach(waterField -> {
            boolean surrounded = this.waterFields.containsKey(waterField.add(new Vector2d(1, 0)))
                    && this.waterFields.containsKey(waterField.add(new Vector2d(-1, 0)))
                    && this.waterFields.containsKey(waterField.add(new Vector2d(0, 1)))
                    && this.waterFields.containsKey(waterField.add(new Vector2d(0, -1)));
            if (surrounded) {
                selectedWaterFields.remove(waterField);
            }
        });
        return selectedWaterFields;
    }


    // utilities
    @Override
    public PositionDirectionTuple correctPosition(Vector2d oldPosition, Vector2d newPosition, MapDirection direction){
        PositionDirectionTuple newTuple = super.correctPosition(oldPosition, newPosition, direction);
        if(this.waterFields.containsKey(newTuple.position())){
            return new PositionDirectionTuple(oldPosition, direction.opposite());
        }
        return newTuple;
    }

    @Override
    public WorldElement objectAt(Vector2d position){
        if(this.animals.containsKey(position)) {
            if (!this.animals.get(position).isEmpty())
                return this.animals.get(position).getFirst();
        }
        if(this.waterFields.containsKey(position)) return this.waterFields.get(position);
        if(this.plants.containsKey(position)) return this.plants.get(position);
        return null;
    }
}
