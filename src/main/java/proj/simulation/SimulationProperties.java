/*
todo:
add csv logging properties
 */

package proj.simulation;

import proj.model.genotype.MutationVariant;
import proj.model.maps.MapVariant;
import proj.model.movement.MovementVariant;
import proj.model.vegetation.VegetationVariant;

public class SimulationProperties {
    private static int genesCount;
    private final MovementVariant movementVariant;
    private final MutationVariant mutationVariant;
    private final MapVariant mapVariant;
    private final VegetationVariant vegetationVariant;
    private final int width;
    private final int height;
    private final int equatorHeight;
    private final int animalCount;
    private final int plantCount;
    private final int plantsPerDay;
    private final int startEnergy;
    private final int plantEnergy;
    private final int energyToReproduce;
    private final int energyToPassToChild;
    private final int energyToMove;
    private final int simulationStep;
    private final int minMutation;
    private final int maxMutation;

    private int daysElapsed;

    // constructor
    public SimulationProperties(int genesCount,
                                MovementVariant movementVariant,
                                MutationVariant mutationVariant,
                                MapVariant mapVariant,
                                VegetationVariant vegetationVariant,
                                int width,
                                int height,
                                int equatorHeight,
                                int animalCount,
                                int plantCount,
                                int plantsPerDay,
                                int startEnergy,
                                int plantEnergy,
                                int energyToReproduce,
                                int energyToPassToChild,
                                int energyToMove,
                                int simulationStep,
                                int minMutation,
                                int maxMutation) {
        SimulationProperties.genesCount = genesCount;
        this.movementVariant = movementVariant;
        this.mutationVariant = mutationVariant;
        this.mapVariant = mapVariant;
        this.vegetationVariant = vegetationVariant;
        this.width = width;
        this.height = height;
        this.equatorHeight = equatorHeight;
        this.animalCount = animalCount;
        this.plantCount = plantCount;
        this.plantsPerDay = plantsPerDay;
        this.startEnergy = startEnergy;
        this.plantEnergy = plantEnergy;
        this.energyToReproduce = energyToReproduce;
        this.energyToPassToChild = energyToPassToChild;
        this.energyToMove = energyToMove;
        this.simulationStep = simulationStep;
        this.minMutation = minMutation;
        this.maxMutation = maxMutation;
        this.daysElapsed = 0;
    }

    // Increment days elapsed
    public void incrementDaysElapsed() {this.daysElapsed++;}
    // Reset days elapsed
    public void resetDaysElapsed() {this.daysElapsed = 0;}

    // Getters
    public static int getGenesCount() {return genesCount;}
    public MovementVariant getMovementVariant() {return this.movementVariant;}
    public MutationVariant getMutationVariant() {return this.mutationVariant;}
    public MapVariant getMapVariant() {return this.mapVariant;}
    public VegetationVariant getVegetationVariant() {return this.vegetationVariant;}
    public int getWidth() {return this.width;}
    public int getHeight() {return this.height;}
    public int getEquatorHeight() {return this.equatorHeight;}
    public int getAnimalCount() {return this.animalCount;}
    public int getPlantCount() {return this.plantCount;}
    public int getPlantsPerDay() {return this.plantsPerDay;}
    public int getStartEnergy() {return this.startEnergy;}
    public int getPlantEnergy() {return this.plantEnergy;}
    public int getEnergyToReproduce() {return this.energyToReproduce;}
    public int getEnergyToPassToChild() {return this.energyToPassToChild;}
    public int getEnergyToMove() {return this.energyToMove;}
    public int getSimulationStep() {return this.simulationStep;}
    public int getMinMutation() {return this.minMutation;}
    public int getMaxMutation() {return this.maxMutation;}
    public int getDaysElapsed() {return this.daysElapsed;}
}
