package proj.model.genotype;

import proj.model.elements.Animal;
import proj.simulation.SimulationProperties;

import java.util.Arrays;
import java.util.Random;

/**
 * Represents a genotype, which is a collection of genes describing an animal's movement behavior.
 * The genotype supports operations such as mutation and child gene generation based on parent genotypes.
 *
 * @author <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public class Genotype {
    private final int[] genes;                                  // Array representing the genes of this genotype
    private final SimulationProperties simulationProperties;    // Simulation configuration properties
    private final Mutation mutation;                            // The gene mutation utilized in the genotype.

    /**
     * Constructs a {@code Genotype} with random genes of the specified size, using the provided simulation properties.
     *
     * @param simulationProperties  Specified properties of the current simulation ({@link SimulationProperties}).
     * @param mutation              The gene mutation utilized in the genotype ({@link Mutation}).
     */
    public Genotype(SimulationProperties simulationProperties, Mutation mutation) {
        this.simulationProperties = simulationProperties;
        this.mutation = mutation;
        this.genes = new int[simulationProperties.getGenotypeSize()];
        initializeRandomGenes();
    }

    /**
     * Constructs a {@code Genotype} with genes of parents with the specified size, using the provided simulation properties
     * and parents' genes.
     *
     * @param parent1               The first parent ({@link Animal}).
     * @param parent2               The second parent ({@link Animal}).
     * @param simulationProperties  Specified properties of the current simulation ({@link SimulationProperties}).
     */
    public Genotype(Animal parent1, Animal parent2, SimulationProperties simulationProperties, Mutation mutation) {
        this.simulationProperties = simulationProperties;
        this.mutation = mutation;
        this.genes = new int[simulationProperties.getGenotypeSize()];
        generateChildGenes(parent1, parent2);
    }

    /**
     * Initializes the genotype with random gene values in the range [0, 7].
     * Each value represents a possible movement direction.
     */
    public void initializeRandomGenes() {
        Random random = new Random();
        for (int i = 0; i < genes.length; i++) {
            genes[i] = random.nextInt(8); // Random direction from 0 to 7
        }
    }

    /**
     * Generates a child's genes based on the genotypes and energies of two parent animals.
     * The child's genes are a combination of sections from each parent's genotype, with
     * the size of each section determined by their respective energy levels.
     *
     * @param parent1 The first parent ({@link Animal}).
     * @param parent2 The second parent ({@link Animal}).
     */
    public void generateChildGenes(Animal parent1, Animal parent2) {
        int[] genotype1 = parent1.getGenes(); // Genotype of parent 1
        int[] genotype2 = parent2.getGenes(); // Genotype of parent 2

        int energy1 = parent1.getEnergy(); // Energy level of parent 1
        int energy2 = parent2.getEnergy(); // Energy level of parent 2

        int totalEnergy = energy1 + energy2;

        // Calculate the split ratio based on the energy levels of the parents
        double splitRatio = (double) energy1 / totalEnergy;
        int splitIndex = (int) Math.round(simulationProperties.getGenotypeSize() * splitRatio);

        // Randomly determine the order of parent's gene contribution
        Random random = new Random();
        boolean takeParent1Left = random.nextBoolean();

        if (takeParent1Left) {
            System.arraycopy(genotype1, 0, genes, 0, splitIndex); // Left part from parent 1
            System.arraycopy(genotype2, splitIndex, genes, splitIndex, genes.length - splitIndex); // Right part from parent 2
        } else {
            System.arraycopy(genotype2, 0, genes, 0, splitIndex); // Left part from parent 2
            System.arraycopy(genotype1, splitIndex, genes, splitIndex, genes.length - splitIndex); // Right part from parent 1
        }

        // Apply mutation to the child's genes
        mutation.applyMutation(genes, simulationProperties);
    }

    /**
     * Returns a string representation of the genotype.
     *
     * @return A {@link String} showing the genes in the genotype.
     */
    @Override
    public String toString() {return Arrays.toString(genes);}

    /**
     * Returns a hash code value for the genotype, based on its genes.
     *
     * @return The hash code value for the genotype.
     */
    @Override
    public int hashCode() {return Arrays.hashCode(genes);}

    // Getters

    /**
     * Gets the array of genes in the genotype.
     *
     * @return The array of genes.
     */
    public int[] getGenes() {return genes;}

    /**
     * Gets the mutation utilized in the genotype.
     *
     * @return {@link Mutation} object.
     */
    public Mutation getMutation() {return this.mutation;}
}
