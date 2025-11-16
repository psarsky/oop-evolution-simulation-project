package proj.app;

/**
 * Utility class providing methods to format genotype gene sequences into human-readable representations.
 */
public class GenotypeFormatter {

    /**
     * Converts an array of integer genes into a space-separated string representation.
     * Each gene (0-7) corresponds to a cardinal or inter cardinal direction (N, NE, E, SE, S, SW, W, NW).
     * Invalid gene values are represented by "?".
     *
     * @param genes The integer array representing the gene sequence. Can be null or empty.
     * @return A formatted {@link String} representation of the genotype, or an empty string if the input is null or empty.
     */
    public static String formatGenotype(int[] genes) {
        if (genes == null || genes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genes.length; i++) {
            int gene = genes[i];
            switch (gene) {
                case 0 -> sb.append("N");
                case 1 -> sb.append("NE");
                case 2 -> sb.append("E");
                case 3 -> sb.append("SE");
                case 4 -> sb.append("S");
                case 5 -> sb.append("SW");
                case 6 -> sb.append("W");
                case 7 -> sb.append("NW");
                default -> sb.append("?"); // Handle unexpected gene values
            }
            if (i < genes.length - 1) {
                sb.append(" "); // Add space between genes
            }
        }
        return sb.toString();
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GenotypeFormatter() {
        // Utility class should not be instantiated.
    }
}