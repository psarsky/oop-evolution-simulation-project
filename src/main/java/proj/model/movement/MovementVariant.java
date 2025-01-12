package proj.model.movement;

/**
 * Enum representing different movement variants in the simulation.
 * Each variant defines a specific movement behavior or strategy for entities in the simulation.
 */
public enum MovementVariant {
    /**
     * The "Predestined" movement variant:
     *
     * This variant represents a deterministic movement pattern,
     * where the path or behavior is predefined and does not change.
     */
    PREDESTINED,

    /**
     * The "Old Age Ain't No Picnic" movement variant:
     *
     * This variant introduces age-dependent movement behavior.
     * As the entity ages, the probability of skipping a movement on a given day increases,
     * reaching a maximum skip chance of 80%.
     */
    OLD_AGE_AINT_NO_PICNIC
}
