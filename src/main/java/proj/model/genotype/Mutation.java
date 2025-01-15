package proj.model.genotype;

import proj.simulation.SimulationProperties;

/**
 * Interface representing a mutation strategy that can be applied to an array of genes.
 * Implementations of this interface define specific ways to modify the genes during mutation.
 *
 * @author <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public interface Mutation {

    /**
     * Applies a mutation to the given array of genes.
     * The mutation alters the genes in the array based on the rules defined by the mutation strategy
     * and the properties of the simulation.
     *
     * @param genes                The array of genes to mutate.
     * @param simulationProperties The simulation properties containing mutation configuration, such as
     *                             mutation type, rate, or other relevant parameters ({@link SimulationProperties}).
     */
    void applyMutation(int[] genes, SimulationProperties simulationProperties);
}
