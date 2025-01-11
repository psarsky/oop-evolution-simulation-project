package proj.model.genotype;

import proj.model.elements.Animal;
import proj.simulation.SimulationProperties;

import java.util.Arrays;
import java.util.Random;

/**
 * Represents a genotype, which is a collection of genes describing an animal's movements.
 * Supports mutation and child gene generation based on parent genotypes.
 */
public class Genotype {
    private int[] genes;
    private final MutationVariant mutationVariant;
    private final SimulationProperties simulationProperties;

    /**
     * Constructs a genotype with random genes of the specified size.
     *
     * @param simulationProperties specified properties of the current simulation
     * @param mutationVariant the mutation strategy to apply to the genotype
     */
    public Genotype(SimulationProperties simulationProperties, MutationVariant mutationVariant) {
        this.simulationProperties = simulationProperties;
        this.mutationVariant = mutationVariant;
        this.genes = new int[simulationProperties.getGenotypeSize()];
    }

    /**
     * Initializes the genotype with random gene values in the range [0, 7].
     */
    public void initializeRandomGenes() {
        Random random = new Random();
        for (int i = 0; i < genes.length; i++) {
            genes[i] = random.nextInt(8);
        }
    }

    /**
     * Generates a child's genes based on the genotypes and energies of two parents.
     * The child's genes are composed of sections from each parent's genotype, with
     * the proportions determined by their respective energy levels.
     *
     * @param parent1 the first parent (Animal)
     * @param parent2 the second parent (Animal)
     * @return an array of genes representing the child's genotype
     */
    public void generateChildGenes(Animal parent1, Animal parent2) {
        int[] genotype1 = parent1.getGenotype();
        int[] genotype2 = parent2.getGenotype();

        int energy1 = parent1.getEnergy();
        int energy2 = parent2.getEnergy();

        int totalEnergy = energy1 + energy2;

        // Calculate the proportions of genes from each parent
        double splitRatio = (double) energy1 / totalEnergy;
        int splitIndex = (int) Math.round(simulationProperties.getGenotypeSize() * splitRatio);

        // Randomly decide which parent's genes are on the left/right
        Random random = new Random();
        boolean takeParent1Left = random.nextBoolean();

        if (takeParent1Left) {
            System.arraycopy(genotype1, 0, genes, 0, splitIndex);
            System.arraycopy(genotype2, splitIndex, genes, splitIndex, genes.length - splitIndex);
        } else {
            System.arraycopy(genotype2, 0, genes, 0, splitIndex);
            System.arraycopy(genotype1, splitIndex, genes, splitIndex, genes.length - splitIndex);
        }

        performMutation(genes, simulationProperties);
    }

    /**
     * Applies the mutation strategy to the genotype.
     * This modifies the genes in the genotype.
     */
    public void performMutation(int[] genes, SimulationProperties simulationProperties) {
//        Mutation mutationStrategy = mutationVariant == MutationVariant.RANDOM ? new RandomMutation() : ;
        Mutation mutationStrategy = new RandomMutation();
        mutationStrategy.applyMutation(genes, simulationProperties);
    }

    @Override
    public String toString() {
        return Arrays.toString(genes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(genes);
    }

    public int[] getGenes() {
        return genes;
    }
}

