/*
todo:
add csv logging properties
 */

package proj.simulation;

import proj.model.genotype.MutationVariant;
import proj.model.maps.MapVariant;
import proj.model.movement.MovementVariant;
import proj.model.vegetation.VegetationVariant;

/**
 * A class that encapsulates the configuration properties for a simulation.
 * Includes parameters for map settings, movement rules, energy management, mutation properties,
 * and initial simulation state.
 */
public class SimulationProperties {
    private static int genotypeSize; // Size of the genotype array for animals
    private final MovementVariant movementVariant; // Variant of movement logic for animals
    private final MutationVariant mutationVariant; // Variant of mutation logic
    private final MapVariant mapVariant; // Type of map (e.g., bounded, unbounded)
    private final VegetationVariant vegetationVariant; // Variant of vegetation growth
    private final int width; // Width of the simulation map
    private final int height; // Height of the simulation map
    private final int equatorHeight; // Height of the equatorial region on the map
    private final int animalCount; // Initial number of animals in the simulation
    private final int plantCount; // Initial number of plants in the simulation
    private final int plantsPerDay; // Number of plants generated per day
    private final int startEnergy; // Initial energy level for animals
    private final int plantEnergy; // Energy gained from eating a plant
    private final int energyNeededToReproduce; // Minimum energy required for reproduction
    private final int energyToPassToChild; // Energy passed from parent to child during reproduction
    private final int energyCostToMove; // Energy cost of one movement
    private final int simulationStep; // Simulation step interval in milliseconds
    private final int minimumNumberOfMutations; // Minimum number of mutations for a child genotype
    private final int maximumNumberOfMutations; // Maximum number of mutations for a child genotype
    private final int waterViolence; // Percentage value determining the aggression of water flow
    private int daysElapsed; // Counter for the number of days passed in the simulation

    /**
     * Constructor to initialize all simulation properties.
     *
     * @param genotypeSize              Size of the genotype array for animals
     * @param movementVariant           Movement behavior variant
     * @param mutationVariant           Mutation behavior variant
     * @param mapVariant                Map type
     * @param vegetationVariant         Vegetation growth behavior variant
     * @param width                     Map width
     * @param height                    Map height
     * @param equatorHeight             Height of the equatorial region
     * @param animalCount               Initial number of animals
     * @param plantCount                Initial number of plants
     * @param plantsPerDay              Number of plants generated daily
     * @param startEnergy               Initial energy level for animals
     * @param plantEnergy               Energy gained from consuming plants
     * @param energyNeededToReproduce   Minimum energy required for reproduction
     * @param energyToPassToChild       Energy passed to offspring during reproduction
     * @param energyCostToMove          Energy cost of a single move
     * @param simulationStep            Simulation step interval
     * @param minimumNumberOfMutations  Minimum number of mutations for a child genotype
     * @param maximumNumberOfMutations  Maximum number of mutations for a child genotype
     */
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
                                int maximumNumberOfMutations,
                                int waterViolence) {
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
        this.waterViolence = waterViolence;
        this.daysElapsed = 0;
    }

    /**
     * Increments the counter for the number of days elapsed in the simulation.
     */
    public void incrementDaysElapsed() {
        this.daysElapsed++;
    }

    // Getters
    public int getGenotypeSize() {return genotypeSize;} // Return the size of the genotype array
    public MovementVariant getMovementVariant() {return this.movementVariant;} // Return the movement behavior variant
    public MutationVariant getMutationVariant() {return this.mutationVariant;} // Return the mutation behavior variant
    public MapVariant getMapVariant() {return this.mapVariant;} // Return the map type variant
    public VegetationVariant getVegetationVariant() {return this.vegetationVariant;} // Return the vegetation growth behavior variant
    public int getWidth() {return this.width;} // Return the map width
    public int getHeight() {return this.height;} // Return the map height
    public int getEquatorHeight() {return this.equatorHeight;} // Return the equator region height
    public int getAnimalCount() {return this.animalCount;} // Return the initial number of animals
    public int getPlantCount() {return this.plantCount;} // Return the initial number of plants
    public int getPlantsPerDay() {return this.plantsPerDay;} // Return the number of plants generated daily
    public int getStartEnergy() {return this.startEnergy;} // Return the starting energy level of animals
    public int getPlantEnergy() {return this.plantEnergy;} // Return the energy gained from consuming plants
    public int getEnergyNeededToReproduce() {return this.energyNeededToReproduce;} // Return the minimum energy required for reproduction
    public int getEnergyToPassToChild() {return this.energyToPassToChild;} // Return the energy passed to offspring during reproduction
    public int getEnergyCostToMove() {return this.energyCostToMove;} // Return the energy cost of a single move
    public int getSimulationStep() {return this.simulationStep;} // Return the simulation step interval
    public int getMinimumNumberOfMutations() {return this.minimumNumberOfMutations;} // Return the minimum number of mutations for offspring
    public int getMaximumNumberOfMutations() {return this.maximumNumberOfMutations;} // Return the maximum number of mutations for offspring
    public int getWaterViolence() {return this.waterViolence;} // Return the water violence value
    public int getDaysElapsed() {return this.daysElapsed;} // Return the number of days elapsed in the simulation
}