package proj.model.genotype;

import proj.simulation.SimulationProperties;

/**
 * Interface representing a mutation strategy that can be applied to an array of genes.
 */
public interface Mutation {
    /**
     * Applies a mutation to the given array of genes.
     * The mutation modifies some or all of the genes in the array.
     *
     * @param Genotype               the array of genes to mutate
     * @param simulationProperties the properties of the simulation containing mutation configuration
     */
    void applyMutation(int[] Genotype, SimulationProperties simulationProperties);
}
