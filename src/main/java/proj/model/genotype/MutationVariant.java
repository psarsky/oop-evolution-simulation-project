package proj.model.genotype;

/**
 * Enumeration defining the available types of mutation strategies
 * that can be applied to an animal's genotype during reproduction.
 * Used to select the specific {@link Mutation} implementation.
 */
public enum MutationVariant {
    /**
     * A mutation strategy where a random number of genes (within configured bounds)
     * are selected and replaced with new, random gene values (0-7).
     * Implemented by {@link RandomMutation}.
     */
    RANDOM

    // Add other mutation variants here if created (e.g., SWAP, SLIGHT_CHANGE)
}