package proj.model.genotype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import proj.simulation.SimulationProperties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RandomMutationTest {

    private SimulationProperties simulationPropertiesMock;

    @BeforeEach
    void setUp() {
        simulationPropertiesMock = mock(SimulationProperties.class);
        when(simulationPropertiesMock.getMinimumNumberOfMutations()).thenReturn(1);
        when(simulationPropertiesMock.getMaximumNumberOfMutations()).thenReturn(5);
    }

    @Test
    void testApplyMutation() {
        RandomMutation randomMutation = new RandomMutation();
        int[] genes = new int[8]; // 8 genes for testing

        // Initialize genes with some values
        for (int i = 0; i < genes.length; i++) {
            genes[i] = i; // genes are 0, 1, 2, ..., 7
        }

        randomMutation.applyMutation(genes, simulationPropertiesMock);

        // Test that at least one gene has changed
        boolean mutated = false;
        for (int i = 0; i < genes.length; i++) {
            if (genes[i] != i) {
                mutated = true;
                break;
            }
        }

        assertTrue(mutated, "At least one gene should have been mutated.");
    }

    @Test
    void testApplyMutationWithNoChange() {
        // Test for case when the number of mutations is 0
        when(simulationPropertiesMock.getMinimumNumberOfMutations()).thenReturn(0);
        when(simulationPropertiesMock.getMaximumNumberOfMutations()).thenReturn(0);

        RandomMutation randomMutation = new RandomMutation();
        int[] genes = new int[10];

        // Initialize genes with some values
        for (int i = 0; i < genes.length; i++) {
            genes[i] = i;
        }

        randomMutation.applyMutation(genes, simulationPropertiesMock);

        // If the number of mutations is 0, genes should stay the same
        for (int i = 0; i < genes.length; i++) {
            assertEquals(i, genes[i], "Gene should not have mutated.");
        }
    }
}