package proj.model.genotype;

import proj.simulation.SimulationProperties;

/**
 * Interface defining a strategy for applying mutations to an animal's genotype (gene array).
 * Implementations determine how genes are altered based on simulation rules.
 *
 * @author <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public interface Mutation {

    /**
     * Applies the specific mutation logic defined by this strategy to the given gene array.
     * Modifications occur in place within the `genes` array.
     *
     * @param genes                The integer array representing the genotype's gene sequence to be mutated.
     * @param simulationProperties The {@link SimulationProperties} containing configuration relevant to mutation
     *                             (e.g., min/max mutation counts).
     */
    void applyMutation(int[] genes, SimulationProperties simulationProperties);
}