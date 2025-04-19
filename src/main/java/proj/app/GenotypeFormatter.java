package proj.app;

/**
 * Utility class for formatting genotypes.
 * Provides methods for converting gene sequences to readable string representation.
 */
public class GenotypeFormatter {

    /**
     * Converts a gene sequence to a readable string representation.
     *
     * @param genes The gene sequence to format
     * @return A formatted string representation of the genes
     */
    public static String formatGenotype(int[] genes) {
        if (genes == null || genes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int gene : genes) {
            switch (gene) {
                case 0 -> sb.append("N");
                case 1 -> sb.append("NE");
                case 2 -> sb.append("E");
                case 3 -> sb.append("SE");
                case 4 -> sb.append("S");
                case 5 -> sb.append("SW");
                case 6 -> sb.append("W");
                case 7 -> sb.append("NW");
                default -> sb.append("?");
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private GenotypeFormatter() {
        // Prevent instantiation
    }
}