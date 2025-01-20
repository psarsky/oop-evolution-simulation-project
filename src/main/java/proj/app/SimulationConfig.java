package proj.app;

import proj.model.genotype.MutationVariant;
import proj.model.maps.MapVariant;
import proj.model.movement.MovementVariant;
import proj.model.vegetation.VegetationVariant;
import proj.simulation.SimulationProperties;

public class SimulationConfig {
    private int genotypeSize;
    private MovementVariant movementVariant;
    private MutationVariant mutationVariant;
    private MapVariant mapVariant;
    private VegetationVariant vegetationVariant;
    private int width;
    private int height;
    private int equatorHeight;
    private int animalCount;
    private int plantCount;
    private int plantsPerDay;
    private int startEnergy;
    private int plantEnergy;
    private int energyNeededToReproduce;
    private int energyToPassToChild;
    private int energyCostToMove;
    private int simulationStep;
    private int minimumNumberOfMutations;
    private int maximumNumberOfMutations;
    private int waterViolence;
    private boolean saveStatistics;
    private String configName;

    // Constructor with all parameters
    public SimulationConfig(String configName, int genotypeSize, MovementVariant movementVariant,
                            MutationVariant mutationVariant, MapVariant mapVariant,
                            VegetationVariant vegetationVariant, int width, int height,
                            int equatorHeight, int animalCount, int plantCount,
                            int plantsPerDay, int startEnergy, int plantEnergy,
                            int energyNeededToReproduce, int energyToPassToChild,
                            int energyCostToMove, int simulationStep,
                            int minimumNumberOfMutations, int maximumNumberOfMutations,
                            int waterViolence, boolean saveStatistics) {
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
        this.saveStatistics = saveStatistics;
    }

    // Convert to SimulationProperties
    public SimulationProperties toSimulationProperties() {
        return new SimulationProperties(
                genotypeSize, movementVariant, mutationVariant, mapVariant,
                vegetationVariant, width, height, equatorHeight, animalCount,
                plantCount, plantsPerDay, startEnergy, plantEnergy,
                energyNeededToReproduce, energyToPassToChild, energyCostToMove,
                simulationStep, minimumNumberOfMutations, maximumNumberOfMutations,
                waterViolence
        );
    }

    // Getters and setters
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }

    public boolean isSaveStatistics() { return saveStatistics; }
    public void setSaveStatistics(boolean saveStatistics) { this.saveStatistics = saveStatistics; }

    public int getGenotypeSize() { return genotypeSize; }
    public void setGenotypeSize(int genotypeSize) { this.genotypeSize = genotypeSize; }

    public MovementVariant getMovementVariant() { return movementVariant; }
    public void setMovementVariant(MovementVariant movementVariant) { this.movementVariant = movementVariant; }

    public MutationVariant getMutationVariant() { return mutationVariant; }
    public void setMutationVariant(MutationVariant mutationVariant) { this.mutationVariant = mutationVariant; }

    public MapVariant getMapVariant() { return mapVariant; }
    public void setMapVariant(MapVariant mapVariant) { this.mapVariant = mapVariant; }

    public VegetationVariant getVegetationVariant() { return vegetationVariant; }
    public void setVegetationVariant(VegetationVariant vegetationVariant) { this.vegetationVariant = vegetationVariant; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public int getEquatorHeight() { return equatorHeight; }
    public void setEquatorHeight(int equatorHeight) { this.equatorHeight = equatorHeight; }

    public int getAnimalCount() { return animalCount; }
    public void setAnimalCount(int animalCount) { this.animalCount = animalCount; }

    public int getPlantCount() { return plantCount; }
    public void setPlantCount(int plantCount) { this.plantCount = plantCount; }

    public int getPlantsPerDay() { return plantsPerDay; }
    public void setPlantsPerDay(int plantsPerDay) { this.plantsPerDay = plantsPerDay; }

    public int getStartEnergy() { return startEnergy; }
    public void setStartEnergy(int startEnergy) { this.startEnergy = startEnergy; }

    public int getPlantEnergy() { return plantEnergy; }
    public void setPlantEnergy(int plantEnergy) { this.plantEnergy = plantEnergy; }

    public int getEnergyNeededToReproduce() { return energyNeededToReproduce; }
    public void setEnergyNeededToReproduce(int energyNeededToReproduce) { this.energyNeededToReproduce = energyNeededToReproduce; }

    public int getEnergyToPassToChild() { return energyToPassToChild; }
    public void setEnergyToPassToChild(int energyToPassToChild) { this.energyToPassToChild = energyToPassToChild; }

    public int getEnergyCostToMove() { return energyCostToMove; }
    public void setEnergyCostToMove(int energyCostToMove) { this.energyCostToMove = energyCostToMove; }

    public int getSimulationStep() { return simulationStep; }
    public void setSimulationStep(int simulationStep) { this.simulationStep = simulationStep; }

    public int getMinimumNumberOfMutations() { return minimumNumberOfMutations; }
    public void setMinimumNumberOfMutations(int minimumNumberOfMutations) { this.minimumNumberOfMutations = minimumNumberOfMutations; }

    public int getMaximumNumberOfMutations() { return maximumNumberOfMutations; }
    public void setMaximumNumberOfMutations(int maximumNumberOfMutations) { this.maximumNumberOfMutations = maximumNumberOfMutations; }

    public int getWaterViolence() { return waterViolence; }
    public void setWaterViolence(int waterViolence) { this.waterViolence = waterViolence; }
}