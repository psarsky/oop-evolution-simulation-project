package proj.model.genotype;

import proj.model.elements.Animal;
import proj.simulation.SimulationProperties;
import proj.util.PositionDirectionTuple;
import proj.util.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GenotypeTest {

    private SimulationProperties simulationProperties;
    private Genotype genotype;
    private Animal animal;

    @BeforeEach
    void setUp() {
        // Creating a SimulationProperties instance
        simulationProperties = new SimulationProperties(
                10,  // genotypeSize
                null,  // movementVariant
                null,  // mutationVariant
                null,  // mapVariant
                null,  // vegetationVariant
                100,  // width
                100,  // height
                20,  // equatorHeight
                10,  // animalCount
                20,  // plantCount
                5,   // plantsPerDay
                100, // startEnergy
                10,  // plantEnergy
                50,  // energyNeededToReproduce
                20,  // energyToPassToChild
                1,   // energyCostToMove
                1000, // simulationStep
                1,   // minimumNumberOfMutations
                2,   // maximumNumberOfMutations
                0    // waterViolence
        );

        // Genotype and Animal Initialization
        genotype = new Genotype(simulationProperties, new RandomMutation());
        animal = new Animal(new Vector2d(0, 0), simulationProperties, genotype);
    }

    @Test
    void testAnimalGenotypeInitialization() {
        // Test if the genotype is initialized correctly
        assertNotNull(animal.getGenes(), "Genes should not be null");
        assertEquals(10, animal.getGenes().length, "Genotype size should be 10");
        for (int gene : animal.getGenes()) {
            assertTrue(gene >= 0 && gene <= 7, "Gene should be between 0 and 7");
        }
    }

    @Test
    void testDeterministicReproduction() {
        // Mock randomness to control behavior
        Random mockRandom = Mockito.mock(Random.class);
        Mockito.when(mockRandom.nextBoolean()).thenReturn(true); // Always choose parent 1 for random splits

        // Create SimulationProperties with minimumNumberOfMutations and maximumNumberOfMutations set to 0 in order
        // to test only the gene inheritance by child
        SimulationProperties simulationPropertiesReproduce = new SimulationProperties(
                10,  // genotypeSize
                null,  // movementVariant
                null,  // mutationVariant
                null,  // mapVariant
                null,  // vegetationVariant
                100,  // width
                100,  // height
                20,  // equatorHeight
                10,  // animalCount
                20,  // plantCount
                5,   // plantsPerDay
                100, // startEnergy
                10,  // plantEnergy
                50,  // energyNeededToReproduce
                20,  // energyToPassToChild
                1,   // energyCostToMove
                1000, // simulationStep
                0,   // minimumNumberOfMutations
                0,   // maximumNumberOfMutations
                0    // waterViolence
        );

        // Simulate the partner animal
        Animal mate = new Animal(new Vector2d(1, 1), simulationPropertiesReproduce, genotype);
        mate.setEnergy(100); // Set energy for partner

        // Set energy for parent 1 (animal)
        animal.setEnergy(60);

        // Get parent genotypes
        int[] parent1Genes = animal.getGenes();
        int[] parent2Genes = mate.getGenes();

        // Perform reproduction
        Animal offspring = animal.reproduce(mate, simulationPropertiesReproduce);
        assertNotNull(offspring, "Offspring should not be null when both animals have enough energy");

        // Check energy reduction after reproduction
        assertEquals(40, animal.getEnergy(), "Parent's energy should decrease after reproduction");
        assertEquals(80, mate.getEnergy(), "Mate's energy should decrease after reproduction");

        // Child's genotype
        int[] childGenes = offspring.getGenes();
        assertEquals(parent1Genes.length, childGenes.length, "Child's genotype size should match parent's genotype size");

        // Calculate expected split point based on parent's energy
        int totalEnergy = 60 + 100;
        double splitRatio = 60.0 / totalEnergy;
        int expectedSplitIndex = (int) Math.round(simulationPropertiesReproduce.getGenotypeSize() * splitRatio);

        // Verify child's genotype inheritance
        for (int i = 0; i < childGenes.length; i++) {
            if (i < expectedSplitIndex) {
                assertEquals(parent1Genes[i], childGenes[i], "Child's genes should match parent 1 in left segment");
            } else {
                assertEquals(parent2Genes[i], childGenes[i], "Child's genes should match parent 2 in right segment");
            }
        }
    }

    @Test
    void testAnimalCannotReproduceWithLowEnergy() {
        // Set energy low for the animal
        animal.setEnergy(30);

        // Initialize mate
        Animal mate = new Animal(new Vector2d(1, 1), simulationProperties, genotype);
        mate.setEnergy(30);

        // Low Energy Reproduction Testing
        Animal offspring = animal.reproduce(mate, simulationProperties);
        assertNull(offspring, "Offspring should be null when one or both animals lack energy");
    }

    @Test
    void testAnimalAgeIncrement() {
        // Testing if an animal's age increases after movement
        int initialAge = animal.getAge();
        animal.move(new PositionDirectionTuple(new Vector2d(1, 1), animal.getDirection()));
        assertEquals(initialAge + 1, animal.getAge(), "Animal's age should increment by 1 after move");
    }

    @Test
    void testEnergyReductionAfterMove() {
        // Testing if energy decreases after movement
        int initialEnergy = animal.getEnergy();
        animal.move(new PositionDirectionTuple(new Vector2d(1, 1), animal.getDirection()));
        assertEquals(initialEnergy - simulationProperties.getEnergyCostToMove(), animal.getEnergy(), "Animal's energy should decrease after moving");
    }

    @Test
    void testEnergyIncreaseAfterEatingPlant() {
        // Testing if energy increases after eating a plant
        int initialEnergy = animal.getEnergy();
        animal.eatPlant(20);
        assertEquals(initialEnergy + 20, animal.getEnergy(), "Animal's energy should increase by plant's energy");
    }

    @Test
    void testAnimalReproduceAddsChild() {
        // Testing if a child is added to the children list
        Animal mate = new Animal(new Vector2d(1, 1), simulationProperties, genotype);
        mate.setEnergy(100);

        animal.reproduce(mate, simulationProperties);

        assertEquals(1, animal.getChildrenMade(), "Animal should have one child after reproduction");
    }

    @Test
    void testHashCodeForDifferentGenotypes() {
        // Testing if hashes are different for different genotypes
        Genotype genotype1 = new Genotype(simulationProperties, new RandomMutation());
        Genotype genotype2 = new Genotype(simulationProperties, new RandomMutation());

        assertNotEquals(genotype1.hashCode(), genotype2.hashCode(), "Hash codes should differ for different genotypes");
    }

    @Test
    void testToStringRepresentation() {
        // Testing if the conversion of the genes to string has the right format
        Genotype genotype = new Genotype(simulationProperties, new RandomMutation());
        String representation = genotype.toString();

        assertNotNull(representation, "String representation should not be null");
        assertTrue(representation.startsWith("[") && representation.endsWith("]"),
                "String representation should be in array format");
    }
}
