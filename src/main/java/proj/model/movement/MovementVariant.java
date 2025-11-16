package proj.model.movement;

/**
 * Enumeration defining the available types of animal movement behaviors or variants
 * used within the simulation. Used to select the appropriate {@link Movement} implementation.
 */
public enum MovementVariant {
    /**
     * Represents the default, deterministic movement pattern where the animal's
     * next move is strictly determined by cycling through its genotype sequence.
     * Implemented by {@link PredestinedMovement}.
     */
    PREDESTINED,

    /**
     * Represents movement behavior affected by the animal's age. Older animals
     * have an increasing probability of skipping their movement action for the day.
     * Implemented by {@link OldAgeAintNoPicnic}.
     */
    OLD_AGE_AINT_NO_PICNIC

    // Add other movement variants here if created (e.g., RANDOM_WALK, TARGETED)
}