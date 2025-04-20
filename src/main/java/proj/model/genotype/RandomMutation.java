package proj.model.genotype;

import proj.simulation.SimulationProperties;

import java.util.Objects;
import java.util.Random;

/**
 * Implements the {@link Mutation} interface using a random replacement strategy.
 * A random number of genes (between min and max specified in properties) are chosen
 * at random indices and replaced with new random gene values (0-7).
 *
 * @author <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public class RandomMutation implements Mutation {
    private static final Random random = new Random(); // Static random instance for efficiency

    /**
     * Applies random mutations to the provided genotype array.
     * The number of genes to mutate is chosen randomly between the minimum and maximum
     * specified in the {@link SimulationProperties}. Each selected gene index is then
     * assigned a new random value between 0 and 7 (inclusive).
     *
     * @param genes                The integer gene array to mutate in place.
     * @param simulationProperties The {@link SimulationProperties} defining the min/max number of mutations. Must not be null.
     */
    @Override
    public void applyMutation(int[] genes, SimulationProperties simulationProperties) {
        Objects.requireNonNull(simulationProperties, "SimulationProperties cannot be null for mutation.");
        if (genes == null || genes.length == 0) {
            return; // Cannot mutate empty or null genotype
        }

        int minMutations = simulationProperties.getMinimumNumberOfMutations();
        int maxMutations = simulationProperties.getMaximumNumberOfMutations();

        // Ensure min/max are valid relative to each other and genotype length
        minMutations = Math.max(0, minMutations);
        maxMutations = Math.max(minMutations, Math.min(genes.length, maxMutations)); // Ensure max >= min and max <= length

        if (minMutations > maxMutations) {
            // This case should ideally be caught during config validation, but handle defensively
            System.err.println("Warning: Min mutations (" + minMutations + ") > Max mutations (" + maxMutations + ") in RandomMutation. No mutations applied.");
            return;
        }

        // Determine the actual number of mutations to apply for this instance
        int numberOfMutations = (maxMutations == minMutations)
                ? minMutations
                : minMutations + random.nextInt(maxMutations - minMutations + 1);

        // Apply the mutations
        for (int i = 0; i < numberOfMutations; i++) {
            // Select a random index within the genotype array
            int geneIndexToMutate = random.nextInt(genes.length);
            // Assign a new random gene value (0-7)
            genes[geneIndexToMutate] = random.nextInt(8);
        }
        // Genes array is modified in place.
    }
}