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
    private static int genotypeSize;
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
    private final int energyNeededToReproduce;
    private final int energyToPassToChild;
    private final int energyCostToMove;
    private final int simulationStep;
    private final int minimumNumberOfMutations;
    private final int maximumNumberOfMutations;

    private int daysElapsed;

    // constructor
    public SimulationProperties(int genotypeSize,
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
                                int energyNeededToReproduce,
                                int energyToPassToChild,
                                int energyCostToMove,
                                int simulationStep,
                                int minimumNumberOfMutations,
                                int maximumNumberOfMutations) {
        SimulationProperties.genotypeSize = genotypeSize;
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
        this.energyNeededToReproduce = energyNeededToReproduce;
        this.energyToPassToChild = energyToPassToChild;
        this.energyCostToMove = energyCostToMove;
        this.simulationStep = simulationStep;
        this.minimumNumberOfMutations = minimumNumberOfMutations;
        this.maximumNumberOfMutations = maximumNumberOfMutations;
        this.daysElapsed = 0;
    }

    // Increment days elapsed
    public void incrementDaysElapsed() {this.daysElapsed++;}

    // Getters
    public int getGenotypeSize() {return genotypeSize;}
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
    public int getEnergyNeededToReproduce() {return this.energyNeededToReproduce;}
    public int getEnergyToPassToChild() {return this.energyToPassToChild;}
    public int getEnergyCostToMove() {return this.energyCostToMove;}
    public int getSimulationStep() {return this.simulationStep;}
    public int getMinimumNumberOfMutations() {return this.minimumNumberOfMutations;}
    public int getMaximumNumberOfMutations() {return this.maximumNumberOfMutations;}
    public int getDaysElapsed() {return this.daysElapsed;}
}
