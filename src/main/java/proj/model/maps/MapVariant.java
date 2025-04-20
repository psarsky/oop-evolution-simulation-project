package proj.model.maps;

/**
 * Enumeration defining the available types of world map topologies or variants
 * used within the simulation. Used to select the appropriate {@link AbstractWorldMap} implementation.
 */
public enum MapVariant {
    /**
     * Represents a map with globe-like wrapping behavior. Entities moving
     * horizontally off one edge reappear on the opposite edge. Vertical movement
     * is bounded. Implemented by {@link Globe}.
     */
    GLOBE,

    /**
     * Represents a map featuring significant water bodies that affect movement
     * and element interactions, potentially including dynamic tides.
     * Implemented by {@link WaterWorld}.
     */
    WATER_WORLD

    // Add other map variants here if created (e.g., WALLED_MAP, INFINITE_MAP)
}