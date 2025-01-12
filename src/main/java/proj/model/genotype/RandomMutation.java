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
     * @param Genotype                      The array of genes to mutate
     * @param simulationProperties          The properties of the simulation containing mutation configuration,
     *                               including the minimum and maximum number of mutations allowed.
     */
    @Override
    public void applyMutation(int[] Genotype, SimulationProperties simulationProperties) {
        if (Genotype.length == 0) return; // If there are no genes, no mutation can be applied.

        int minMutations = simulationProperties.getMinimumNumberOfMutations();
        int maxMutations = simulationProperties.getMaximumNumberOfMutations();

        // Determine the number of mutations to apply, ensuring it is within the defined range.
        int numberOfMutations = minMutations + random.nextInt(maxMutations - minMutations + 1);

        // Perform the mutations by randomly selecting indices and modifying their values.
        for (int i = 0; i < numberOfMutations; i++) {
            // Select a random index in the gene array.
            int geneIndex = random.nextInt(Genotype.length);
            // Assign a new random value in the range [0, 7] to the selected gene.
            Genotype[geneIndex] = random.nextInt(8);
        }
    }
}
