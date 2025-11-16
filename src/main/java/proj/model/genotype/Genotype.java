package proj.model.genotype;

import proj.model.elements.Animal;
import proj.simulation.SimulationProperties;

import java.util.*;

/**
 * Represents the genetic makeup (genotype) of an {@link Animal}.
 * Contains an array of genes (integers 0-7 representing movement directions/rotations)
 * that determine the animal's behavior. Handles gene initialization, inheritance from parents,
 * and mutation application based on simulation properties and a specified {@link Mutation} strategy.
 *
 * @author <a href="https://github.com/jakubkalinski0">jakubkalinski0</a>
 */
public class Genotype {

    private final int[] genes;                          // Array representing the gene sequence.
    private final SimulationProperties simulationProperties; // Simulation configuration reference.
    private final Mutation mutation;                    // Mutation strategy applied to this genotype.
    private static final Random random = new Random();  // Static random instance for internal use.

    /**
     * Constructs a {@code Genotype} with random genes of the size specified in simulation properties.
     * Used typically for the initial animal population.
     *
     * @param simulationProperties The simulation configuration ({@link SimulationProperties}), providing genotype size, etc. Must not be null.
     * @param mutation             The {@link Mutation} strategy to be potentially applied later (e.g., during reproduction). Must not be null.
     */
    public Genotype(SimulationProperties simulationProperties, Mutation mutation) {
        this.simulationProperties = Objects.requireNonNull(simulationProperties, "SimulationProperties cannot be null");
        this.mutation = Objects.requireNonNull(mutation, "Mutation strategy cannot be null");
        int genotypeSize = simulationProperties.getGenotypeSize();
        if (genotypeSize <= 0) {
            // Handle invalid genotype size, maybe default to a small size or throw exception
            System.err.println("Warning: Genotype size is non-positive (" + genotypeSize + "). Defaulting to size 1.");
            genotypeSize = 1; // Example fallback
        }
        this.genes = new int[genotypeSize];
        initializeRandomGenes();
    }

    /**
     * Constructs a {@code Genotype} for a child animal by combining genes from two parents.
     * The proportion of genes inherited from each parent is determined by their relative energy levels.
     * Mutation is applied after gene combination according to the provided strategy.
     *
     * @param parent1              The first parent {@link Animal}. Must not be null.
     * @param parent2              The second parent {@link Animal}. Must not be null.
     * @param simulationProperties The simulation configuration ({@link SimulationProperties}). Must not be null.
     * @param mutation             The {@link Mutation} strategy to apply to the child's genes. Must not be null.
     */
    public Genotype(Animal parent1, Animal parent2, SimulationProperties simulationProperties, Mutation mutation) {
        Objects.requireNonNull(parent1, "Parent1 cannot be null");
        Objects.requireNonNull(parent2, "Parent2 cannot be null");
        this.simulationProperties = Objects.requireNonNull(simulationProperties, "SimulationProperties cannot be null");
        this.mutation = Objects.requireNonNull(mutation, "Mutation strategy cannot be null");
        int genotypeSize = simulationProperties.getGenotypeSize();
        if (genotypeSize <= 0) {
            System.err.println("Warning: Genotype size is non-positive (" + genotypeSize + "). Defaulting to size 1.");
            genotypeSize = 1;
        }
        this.genes = new int[genotypeSize];
        generateChildGenes(parent1, parent2);
        // Mutation is applied within generateChildGenes
    }

    /**
     * Initializes the genotype's gene array with random values, each representing a direction (0-7).
     */
    private void initializeRandomGenes() {
        if (genes.length == 0) return; // Cannot initialize if size is zero
        for (int i = 0; i < genes.length; i++) {
            genes[i] = random.nextInt(8); // Random direction/rotation gene (0-7)
        }
        ensureGeneDiversity(); // Ensure some basic gene diversity after random init
    }


    /**
     * Generates the child's gene sequence by combining segments from both parents' genotypes.
     * The split point is determined by the parents' relative energy levels. A random decision
     * determines which parent contributes the "left" part and which contributes the "right" part.
     * Finally, applies mutation to the resulting child genes.
     *
     * @param parent1 The first parent {@link Animal}.
     * @param parent2 The second parent {@link Animal}.
     */
    private void generateChildGenes(Animal parent1, Animal parent2) {
        int[] genes1 = parent1.getGenes();
        int[] genes2 = parent2.getGenes();

        // Basic check for compatibility (ideally parents should always have same genotype size)
        if (genes.length == 0 || genes1.length != genes.length || genes2.length != genes.length) {
            System.err.println("Error: Parent genotype sizes mismatch or child genotype size is zero during reproduction. Cannot generate child genes.");
            initializeRandomGenes(); // Fallback to random genes? Or handle error differently.
            return;
        }


        // Ensure total energy is positive to avoid division by zero or negative split index
        long totalEnergy = (long) Math.max(1, parent1.getEnergy()) + Math.max(1, parent2.getEnergy()); // Use long to prevent overflow, ensure positive
        long energy1 = Math.max(1, parent1.getEnergy());

        // Calculate split point based on energy ratio
        // Ensure split index is within valid bounds [0, genes.length]
        int splitIndex = (int) Math.max(0, Math.min(genes.length, Math.round(genes.length * (double) energy1 / totalEnergy)));

        // Randomly decide which parent provides which segment (left/right)
        boolean parent1Left = random.nextBoolean();

        if (parent1Left) {
            // Copy left part from parent1, right part from parent2
            System.arraycopy(genes1, 0, this.genes, 0, splitIndex);
            System.arraycopy(genes2, splitIndex, this.genes, splitIndex, genes.length - splitIndex);
        } else {
            // Copy left part from parent2, right part from parent1
            System.arraycopy(genes2, 0, this.genes, 0, splitIndex);
            System.arraycopy(genes1, splitIndex, this.genes, splitIndex, genes.length - splitIndex);
        }

        // Apply mutation to the combined genes
        this.mutation.applyMutation(this.genes, this.simulationProperties);
        ensureGeneDiversity(); // Ensure diversity after mutation as well
    }

    /**
     * Ensures that the genotype contains at least one of each possible gene value (0-7)
     * if the genotype length allows. If not, it attempts to correct the genes by
     * randomly replacing duplicates until all values are present or the array is filled.
     * This prevents genotypes where an animal can only turn in a limited set of directions.
     */
    private void ensureGeneDiversity() {
        if (genes == null || genes.length < 8) {
            return; // Cannot ensure full diversity if length is less than 8
        }

        boolean[] present = new boolean[8];
        int distinctCount = 0;
        for (int gene : genes) {
            if (gene >= 0 && gene < 8 && !present[gene]) {
                present[gene] = true;
                distinctCount++;
            }
        }

        if (distinctCount == 8) {
            return; // All genes are already present
        }

        // Find missing genes
        List<Integer> missing = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            if (!present[i]) {
                missing.add(i);
            }
        }

        // Replace random genes until all missing ones are added
        // This is a simple approach; more sophisticated ones exist.
        int replaceIndex = 0;
        while (!missing.isEmpty() && replaceIndex < genes.length) {
            int currentGene = genes[replaceIndex];
            // If this gene value is already present multiple times (or is one we don't need)
            // and we still have missing genes, replace it with a missing one.
            // A simpler check: just replace if the gene at replaceIndex is NOT the *only* instance of its value
            int geneToReplaceWith = missing.removeFirst(); // Get and remove a missing gene
            genes[replaceIndex] = geneToReplaceWith; // Replace the gene at current index

            replaceIndex++; // Move to the next index to consider for replacement
        }

        // Optional: Final shuffle might improve distribution after correction, but adds cost.
        // shuffleGenes();
    }


    // --- Getters ---

    /**
     * Gets the gene sequence array of this genotype.
     * Note: Returns the direct internal array reference. Modifications to the returned array
     * will affect the genotype's state. Consider returning a copy if immutability is required externally.
     *
     * @return The integer array representing the genes.
     */
    public int[] getGenes() {
        return genes;
        // return Arrays.copyOf(genes, genes.length); // Return copy if external modification is a risk
    }

    /**
     * Gets the {@link Mutation} strategy associated with this genotype.
     *
     * @return The {@link Mutation} object.
     */
    public Mutation getMutation() {
        return this.mutation;
    }

    /**
     * Gets the {@link SimulationProperties} associated with this genotype.
     *
     * @return The {@link SimulationProperties} object.
     */
    public SimulationProperties getSimulationProperties() {
        return simulationProperties;
    }

    // --- Object Overrides ---

    /**
     * Returns a string representation of the genotype, showing the sequence of genes.
     *
     * @return A {@link String} representation of the gene array (e.g., "[1, 5, 0, ...]").
     */
    @Override
    public String toString() {
        return Arrays.toString(genes);
    }

    /**
     * Returns a hash code value for the genotype, based on its gene sequence.
     *
     * @return The hash code value for this genotype.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(genes);
    }

    /**
     * Checks if this genotype is equal to another object.
     * Equality is determined by comparing the gene sequences.
     *
     * @param obj The object to compare with.
     * @return {@code true} if the other object is a Genotype with the same gene sequence, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Genotype other = (Genotype) obj;
        return Arrays.equals(genes, other.genes);
    }
}