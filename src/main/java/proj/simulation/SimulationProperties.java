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
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>, <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public class SimulationProperties {
    private final String configName;                    // Name of the given configuration
    private final int genotypeSize;                     // Size of the genotype array for animals
    private final MovementVariant movementVariant;      // Variant of movement logic for animals
    private final MutationVariant mutationVariant;      // Variant of mutation logic
    private final MapVariant mapVariant;                // Type of map (e.g., Globe, WaterWorld)
    private final VegetationVariant vegetationVariant;  // Variant of vegetation growth
    private final int width;                            // Width of the simulation map
    private final int height;                           // Height of the simulation map
    private final int equatorHeight;                    // Height of the equatorial region on the map
    private final int animalCount;                      // Initial number of animals in the simulation
    private final int plantCount;                       // Initial number of plants in the simulation
    private final int plantsPerDay;                     // Number of plants generated per day
    private final int startEnergy;                      // Initial energy level for animals
    private final int plantEnergy;                      // Energy gained from eating a plant
    private final int energyNeededToReproduce;          // Minimum energy required for reproduction
    private final int energyToPassToChild;              // Energy passed from parent to child during reproduction
    private final int energyCostToMove;                 // Energy cost of one movement
    private final int simulationStep;                   // Simulation step interval in milliseconds
    private final int minimumNumberOfMutations;         // Minimum number of mutations for a child genotype
    private final int maximumNumberOfMutations;         // Maximum number of mutations for a child genotype
    private final int waterViolence;                    // Percentage value determining the aggression of water flow
    private final boolean saveStatisticsFlag;           // Boolean value defining if statistics of the simulation should be saved or not
    private int daysElapsed;                            // Counter for the number of days passed in the simulation

    /**
     * Constructs a new {@code SimulationProperties} object and initializes all given properties.
     *
     * @param configName                Name of the configuration.
     * @param genotypeSize              Size of the genotype array for animals.
     * @param movementVariant           {@link MovementVariant} utilized in the simulation.
     * @param mutationVariant           {@link MutationVariant} utilized in the simulation.
     * @param mapVariant                {@link MapVariant} utilized in the simulation.
     * @param vegetationVariant         {@link VegetationVariant} utilized in the simulation.
     * @param width                     Map width.
     * @param height                    Map height.
     * @param equatorHeight             Height of the equatorial region.
     * @param animalCount               Initial number of animals.
     * @param plantCount                Initial number of plants.
     * @param plantsPerDay              Number of plants generated daily.
     * @param startEnergy               Initial energy level for animals.
     * @param plantEnergy               Energy gained from consuming plants.
     * @param energyNeededToReproduce   Minimum energy required for reproduction.
     * @param energyToPassToChild       Energy passed to offspring during reproduction.
     * @param energyCostToMove          Energy cost of a single move.
     * @param simulationStep            Simulation step interval in milliseconds.
     * @param minimumNumberOfMutations  Minimum number of mutations for a child genotype.
     * @param maximumNumberOfMutations  Maximum number of mutations for a child genotype.
     * @param waterViolence             Value determining the aggression of water flow.
     * @param saveStatisticsFlag        Boolean value defining if statistics of the simulation should be saved or not
     */
    public SimulationProperties(String configName,
                                int genotypeSize,
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
                                int waterViolence,
                                boolean saveStatisticsFlag) {
        this.configName = configName;
        this.genotypeSize = genotypeSize;
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
        this.saveStatisticsFlag = saveStatisticsFlag;
        this.daysElapsed = 0;
    }

    /**
     * Increments the counter for the number of days elapsed in the simulation.
     */
    public void incrementDaysElapsed() {this.daysElapsed++;}

    
    // Getters

    /**
     * Gets the name of the configuration.
     *
     * @return The configuration name.
     */
    public String getConfigName() {return this.configName;}

    /**
     * Gets the size of the genotype array.
     *
     * @return The genotype size.
     */
    public int getGenotypeSize() {return genotypeSize;}

    /**
     * Gets the movement behavior variant used in the simulation.
     *
     * @return The {@link MovementVariant} representing movement behavior.
     */
    public MovementVariant getMovementVariant() {return this.movementVariant;}

    /**
     * Gets the mutation behavior variant used in the simulation.
     *
     * @return The {@link MutationVariant} representing mutation behavior.
     */
    public MutationVariant getMutationVariant() {return this.mutationVariant;}

    /**
     * Gets the map type used in the simulation.
     *
     * @return The {@link MapVariant} representing the map type.
     */
    public MapVariant getMapVariant() {return this.mapVariant;}

    /**
     * Gets the vegetation growth behavior variant used in the simulation.
     *
     * @return The {@link VegetationVariant} representing vegetation growth behavior.
     */
    public VegetationVariant getVegetationVariant() {return this.vegetationVariant;}

    /**
     * Gets the width of the simulation map.
     *
     * @return The map width.
     */
    public int getWidth() {return this.width;}

    /**
     * Gets the height of the simulation map.
     *
     * @return The map height.
     */
    public int getHeight() {return this.height;}

    /**
     * Gets the height of the equator region on the map.
     *
     * @return The equator region height.
     */
    public int getEquatorHeight() {return this.equatorHeight;}

    /**
     * Gets the initial number of animals in the simulation.
     *
     * @return The initial animal count.
     */
    public int getAnimalCount() {return this.animalCount;}

    /**
     * Gets the initial number of plants on the simulation map.
     *
     * @return The initial plant count.
     */
    public int getPlantCount() {return this.plantCount;}

    /**
     * Gets the number of plants generated daily in the simulation.
     *
     * @return The daily plant generation count.
     */
    public int getPlantsPerDay() {return this.plantsPerDay;}

    /**
     * Gets the starting energy level of animals.
     *
     * @return The starting energy level.
     */
    public int getStartEnergy() {return this.startEnergy;}

    /**
     * Gets the energy gained by animals from consuming plants.
     *
     * @return The energy gained per plant.
     */
    public int getPlantEnergy() {return this.plantEnergy;}

    /**
     * Gets the minimum energy level required for animals to reproduce.
     *
     * @return The energy threshold for reproduction.
     */
    public int getEnergyNeededToReproduce() {return this.energyNeededToReproduce;}

    /**
     * Gets the amount of energy passed from parents to offspring during reproduction.
     *
     * @return The energy passed to offspring.
     */
    public int getEnergyToPassToChild() {return this.energyToPassToChild;}

    /**
     * Gets the energy cost incurred by animals for a single move.
     *
     * @return The energy cost per move.
     */
    public int getEnergyCostToMove() {return this.energyCostToMove;}

    /**
     * Gets the time interval for each simulation step, in milliseconds.
     *
     * @return The simulation step interval.
     */
    public int getSimulationStep() {return this.simulationStep;}

    /**
     * Gets the minimum number of mutations that offspring can inherit.
     *
     * @return The minimum number of mutations.
     */
    public int getMinimumNumberOfMutations() {return this.minimumNumberOfMutations;}

    /**
     * Gets the maximum number of mutations that offspring can inherit.
     *
     * @return The maximum number of mutations.
     */
    public int getMaximumNumberOfMutations() {return this.maximumNumberOfMutations;}

    /**
     * Gets the water violence value, representing the environmental hazard that water movement poses.
     *
     * @return The water violence value.
     */
    public int getWaterViolence() {return this.waterViolence;}

    /**
     * Gets the boolean value defining if statistics of a simulation with this configuration should be stored.
     *
     * @return The boolean value if statistics are to be saved.
     */
    public boolean getSaveStatisticsFlag() {return this.saveStatisticsFlag;}

    /**
     * Gets the total number of days that have elapsed in the simulation.
     *
     * @return The number of days elapsed.
     */
    public int getDaysElapsed() {return this.daysElapsed;}
}