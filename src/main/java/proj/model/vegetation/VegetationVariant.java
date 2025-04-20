package proj.model.vegetation;

/**
 * Enumeration defining the available types of vegetation growth patterns or variants
 * used within the simulation. Used to select the appropriate {@link AbstractVegetationVariant} implementation.
 */
public enum VegetationVariant {
    /**
     * Represents a vegetation pattern where growth is favored in a central
     * horizontal band (the "equator") across the map.
     * Implemented by {@link ForestedEquator}.
     */
    FORESTED_EQUATOR

    // Add other vegetation variants here if created (e.g., TOXIC_CORPSES, FERTILE_FIELDS)
}