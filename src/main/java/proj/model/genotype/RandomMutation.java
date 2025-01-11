package proj.model.genotype;

import proj.simulation.SimulationProperties;

import java.util.Random;

/**
 * Implementation of a mutation strategy where random genes in the array
 * are replaced with new random values.
 * The number of mutations is determined by the simulation properties.
 */
public class RandomMutation implements Mutation {
    private static final Random random = new Random();

    /**
     * Applies a random mutation to the given array of genes. A random number of genes
     * (within the range defined by the simulation properties) is modified. Each mutated
     * gene is replaced by a random value in the range [0, 7].
     *
     * @param Genotype               the array of genes to mutate
     * @param simulationProperties the properties of the simulation containing mutation configuration
     */
    @Override
    public void applyMutation(int[] Genotype, SimulationProperties simulationProperties) {
        if (Genotype.length == 0) return;

        int minMutations = simulationProperties.getMinimumNumberOfMutations();
        int maxMutations = simulationProperties.getMaximumNumberOfMutations();

        // Determine the number of mutations (at least one mutation is enforced)
        int numberOfMutations = minMutations + random.nextInt(maxMutations - minMutations + 1);

        for (int i = 0; i < numberOfMutations; i++) {
            // Select a random index in the array
            int geneIndex = random.nextInt(Genotype.length);
            // Replace the gene at the selected index with a new random value in [0, 7]
            Genotype[geneIndex] = random.nextInt(8);
        }
    }
}
