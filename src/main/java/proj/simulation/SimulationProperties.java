package proj.simulation;

import proj.model.genotype.MutationVariant;
import proj.model.maps.MapVariant;
import proj.model.movement.MovementVariant;
import proj.model.vegetation.VegetationVariant;
import java.util.Objects;

/**
 * Encapsulates all **immutable configuration parameters** for initializing a simulation run.
 * This class holds properties defining the map characteristics, element behaviors
 * (movement, mutation, vegetation), energy rules, initial population counts,
 * and simulation control settings like the suggested step delay. It ensures that once created,
 * these configuration values cannot be changed for a given simulation instance.
 * <p>
 * Note: The current simulation day (runtime state) is tracked separately in the {@link Simulation} class.
 * </p>
 *
 * @author <a href="https://github.com/psarsky">psarsky</a>, <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public class SimulationProperties {

    // --- Configuration Fields (Immutable after construction) ---
    private final String configName;                    // Unique name identifying this configuration set.
    private final int genotypeSize;                     // The fixed number of genes in each animal's genotype.
    private final MovementVariant movementVariant;      // The selected strategy for animal movement behavior.
    private final MutationVariant mutationVariant;      // The selected strategy for gene mutation during reproduction.
    private final MapVariant mapVariant;                // The selected map topology (e.g., Globe, WaterWorld).
    private final VegetationVariant vegetationVariant;  // The selected strategy for vegetation growth and distribution.
    private final int width;                            // The width of the simulation map grid (number of columns).
    private final int height;                           // The height of the simulation map grid (number of rows).
    private final int equatorHeight;                    // The height (in rows) of the equatorial zone (relevant for ForestedEquator).
    private final int animalCount;                      // The initial number of animals to spawn at the start of the simulation.
    private final int plantCount;                       // The initial number of plants to spawn at the start of the simulation.
    private final int plantsPerDay;                     // The number of new plants the simulation attempts to spawn each day.
    private final int startEnergy;                      // The initial energy level assigned to newly spawned animals.
    private final int plantEnergy;                      // The amount of energy an animal gains upon consuming a plant.
    private final int energyNeededToReproduce;          // The minimum energy level an animal must possess to initiate reproduction.
    private final int energyToPassToChild;              // The amount of energy deducted from *each* parent and contributing to the child's starting energy during reproduction.
    private final int energyCostToMove;                 // The amount of energy deducted from an animal for each movement action it takes.
    private final int simulationStep;                   // The suggested initial time delay (in milliseconds) between simulation steps/days, primarily for visualization control.
    private final int minimumNumberOfMutations;         // The minimum number of gene mutations applied to a child's genotype during reproduction.
    private final int maximumNumberOfMutations;         // The maximum number of gene mutations applied to a child's genotype during reproduction.
    private final int waterViolence;                    // A factor (typically 0-100) influencing water dynamics, relevant for WaterWorld map variant.
    private final boolean saveStatisticsFlag;           // A flag indicating whether daily simulation statistics should be automatically saved to files.

    /**
     * Constructs a new, immutable {@code SimulationProperties} object, initializing all configuration parameters.
     * Performs validation checks on numerical inputs to ensure they fall within acceptable ranges.
     *
     * @param configName                Unique name for the configuration (non-null, non-empty).
     * @param genotypeSize              Size of the genotype (must be positive).
     * @param movementVariant           Selected {@link MovementVariant} (non-null).
     * @param mutationVariant           Selected {@link MutationVariant} (non-null).
     * @param mapVariant                Selected {@link MapVariant} (non-null).
     * @param vegetationVariant         Selected {@link VegetationVariant} (non-null).
     * @param width                     Map width (must be positive).
     * @param height                    Map height (must be positive).
     * @param equatorHeight             Height of the equatorial region (non-negative, <= height).
     * @param animalCount               Initial number of animals (non-negative).
     * @param plantCount                Initial number of plants (non-negative).
     * @param plantsPerDay              Number of plants generated daily (non-negative).
     * @param startEnergy               Initial energy for animals (must be positive).
     * @param plantEnergy               Energy gained from plants (must be positive).
     * @param energyNeededToReproduce   Energy threshold for reproduction (must be positive).
     * @param energyToPassToChild       Energy passed to offspring per parent (must be positive, <= energyNeededToReproduce).
     * @param energyCostToMove          Energy cost per move (non-negative).
     * @param simulationStep            Initial simulation step interval suggestion in ms (must be positive).
     * @param minimumNumberOfMutations  Minimum mutations (non-negative, <= maxMutations, <= genotypeSize).
     * @param maximumNumberOfMutations  Maximum mutations (non-negative, >= minMutations, <= genotypeSize).
     * @param waterViolence             Water dynamics factor (typically 0-100).
     * @param saveStatisticsFlag        Whether to auto-save daily stats.
     * @throws NullPointerException if any variant or configName is null.
     * @throws IllegalArgumentException if numerical parameters are out of valid range.
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

        // --- Input Validation ---
        if (configName == null || configName.trim().isEmpty()) throw new IllegalArgumentException("Config name cannot be empty.");
        if (genotypeSize <= 0) throw new IllegalArgumentException("Genotype size must be positive.");
        if (width <= 0) throw new IllegalArgumentException("Map width must be positive.");
        if (height <= 0) throw new IllegalArgumentException("Map height must be positive.");
        if (equatorHeight < 0 || equatorHeight > height) throw new IllegalArgumentException("Equator height must be between 0 and map height.");
        if (animalCount < 0) throw new IllegalArgumentException("Initial animal count cannot be negative.");
        if (plantCount < 0) throw new IllegalArgumentException("Initial plant count cannot be negative.");
        if (plantsPerDay < 0) throw new IllegalArgumentException("Plants per day cannot be negative.");
        if (startEnergy <= 0) throw new IllegalArgumentException("Start energy must be positive.");
        if (plantEnergy <= 0) throw new IllegalArgumentException("Plant energy must be positive.");
        if (energyNeededToReproduce <= 0) throw new IllegalArgumentException("Energy needed to reproduce must be positive.");
        // Energy passed can be equal to energy needed if parent has exactly enough (child gets double this)
        if (energyToPassToChild <= 0 || energyToPassToChild > startEnergy) throw new IllegalArgumentException("Energy to pass must be positive and ideally not exceed start energy.");
        // Weaker check for energy pass vs needed: if (energyToPassToChild <= 0) throw new IllegalArgumentException("Energy to pass must be positive.");
        if (energyCostToMove < 0) throw new IllegalArgumentException("Energy cost to move cannot be negative.");
        if (simulationStep <= 0) throw new IllegalArgumentException("Simulation step delay must be positive.");
        if (minimumNumberOfMutations < 0 || minimumNumberOfMutations > genotypeSize) throw new IllegalArgumentException("Minimum mutations out of range [0, genotypeSize].");
        if (maximumNumberOfMutations < minimumNumberOfMutations || maximumNumberOfMutations > genotypeSize) throw new IllegalArgumentException("Maximum mutations out of range [minMutations, genotypeSize].");

        // --- Field Assignment ---
        this.configName = configName.trim();
        this.genotypeSize = genotypeSize;
        this.movementVariant = Objects.requireNonNull(movementVariant, "MovementVariant cannot be null.");
        this.mutationVariant = Objects.requireNonNull(mutationVariant, "MutationVariant cannot be null.");
        this.mapVariant = Objects.requireNonNull(mapVariant, "MapVariant cannot be null.");
        this.vegetationVariant = Objects.requireNonNull(vegetationVariant, "VegetationVariant cannot be null.");
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
        this.waterViolence = waterViolence; // Assume valid range handled elsewhere if needed
        this.saveStatisticsFlag = saveStatisticsFlag;
    }

    // --- Getters for accessing configuration properties ---

    /** Gets the unique name of this configuration set. */
    public String getConfigName() { return this.configName; }
    /** Gets the number of genes in an animal's genotype. */
    public int getGenotypeSize() { return genotypeSize; }
    /** Gets the selected movement behavior variant. */
    public MovementVariant getMovementVariant() { return this.movementVariant; }
    /** Gets the selected mutation strategy variant. */
    public MutationVariant getMutationVariant() { return this.mutationVariant; }
    /** Gets the selected map topology variant. */
    public MapVariant getMapVariant() { return this.mapVariant; }
    /** Gets the selected vegetation growth variant. */
    public VegetationVariant getVegetationVariant() { return this.vegetationVariant; }
    /** Gets the width of the simulation map grid. */
    public int getWidth() { return this.width; }
    /** Gets the height of the simulation map grid. */
    public int getHeight() { return this.height; }
    /** Gets the height of the equator zone. */
    public int getEquatorHeight() { return this.equatorHeight; }
    /** Gets the initial number of animals to spawn. */
    public int getAnimalCount() { return this.animalCount; }
    /** Gets the initial number of plants to spawn. */
    public int getPlantCount() { return this.plantCount; }
    /** Gets the number of new plants attempted to spawn each day. */
    public int getPlantsPerDay() { return this.plantsPerDay; }
    /** Gets the initial energy level for newly spawned animals. */
    public int getStartEnergy() { return this.startEnergy; }
    /** Gets the energy gained by an animal upon eating a plant. */
    public int getPlantEnergy() { return this.plantEnergy; }
    /** Gets the minimum energy an animal needs to initiate reproduction. */
    public int getEnergyNeededToReproduce() { return this.energyNeededToReproduce; }
    /** Gets the energy cost deducted from *each* parent during reproduction. */
    public int getEnergyToPassToChild() { return this.energyToPassToChild; }
    /** Gets the energy cost deducted for each movement action. */
    public int getEnergyCostToMove() { return this.energyCostToMove; }
    /** Gets the suggested initial time delay (in milliseconds) between simulation steps. */
    public int getSimulationStep() { return this.simulationStep; }
    /** Gets the minimum number of gene mutations applied to a child's genotype. */
    public int getMinimumNumberOfMutations() { return this.minimumNumberOfMutations; }
    /** Gets the maximum number of gene mutations applied to a child's genotype. */
    public int getMaximumNumberOfMutations() { return this.maximumNumberOfMutations; }
    /** Gets the factor influencing water dynamics (relevant for WaterWorld). */
    public int getWaterViolence() { return this.waterViolence; }
    /** Gets the flag indicating if daily statistics should be automatically saved. */
    public boolean getSaveStatisticsFlag() { return this.saveStatisticsFlag; }
}