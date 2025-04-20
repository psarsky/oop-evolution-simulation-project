package proj.app.viewmodels;

import javafx.beans.property.*;
import proj.app.GenotypeFormatter;
import proj.model.elements.Animal;

/**
 * ViewModel responsible for holding and exposing the details of the currently selected {@link Animal}
 * in the simulation UI. It uses JavaFX properties for easy binding with UI labels and other controls
 * in the "Selected Animal" panel.
 */
public class SelectedAnimalViewModel {

    // --- Properties for Selected Animal Details ---
    // Use ReadOnly*Wrapper for internal modification, expose ReadOnly*Property for external binding.
    private final ReadOnlyIntegerWrapper energy = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper age = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper childrenMade = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper plantsEaten = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyStringWrapper genotype = new ReadOnlyStringWrapper("-");
    private final ReadOnlyIntegerWrapper activeGeneIndex = new ReadOnlyIntegerWrapper(0); // Assuming 0 is a valid default index
    private final ReadOnlyIntegerWrapper descendantsCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyStringWrapper deathDate = new ReadOnlyStringWrapper("-"); // "-" indicates alive or not set
    private final ReadOnlyBooleanWrapper isSelected = new ReadOnlyBooleanWrapper(false); // Tracks if an animal is currently selected

    // Reference to the actual Animal object currently displayed. Null if none selected.
    private Animal currentAnimalReference = null;

    // --- Public Methods ---

    /**
     * Updates the ViewModel with the data from the provided {@link Animal}.
     * If the provided animal is null, it clears the ViewModel using {@link #clear()}.
     *
     * @param animal The {@link Animal} whose data should be displayed, or {@code null} to clear the selection.
     */
    public void update(Animal animal) {
        if (animal != null) {
            // Update properties with data from the animal
            energy.set(animal.getEnergy());
            age.set(animal.getAge());
            childrenMade.set(animal.getChildrenMade());
            plantsEaten.set(animal.getPlantsEaten());
            genotype.set(GenotypeFormatter.formatGenotype(animal.getGenes()));
            activeGeneIndex.set(animal.getActiveGeneIndex());
            descendantsCount.set(animal.getDescendantsCount()); // Ensure Animal calculates this efficiently if needed often
            deathDate.set(animal.isAlive() ? "-" : String.valueOf(animal.getDeathDate())); // Show death date if applicable
            isSelected.set(true); // Mark as selected
            currentAnimalReference = animal; // Store the reference
        } else {
            // If null animal is passed, clear the selection
            clear();
        }
    }

    /**
     * Clears the ViewModel, resetting all properties to their default/empty state.
     * Typically called when no animal is selected or the previously selected animal is deselected or removed.
     */
    public void clear() {
        energy.set(0);
        age.set(0);
        childrenMade.set(0);
        plantsEaten.set(0);
        genotype.set("-");
        activeGeneIndex.set(0);
        descendantsCount.set(0);
        deathDate.set("-");
        isSelected.set(false); // Mark as not selected
        currentAnimalReference = null; // Clear the reference
    }

    /**
     * Gets the reference to the actual {@link Animal} object currently represented by this ViewModel.
     * Returns {@code null} if no animal is currently selected. This reference can be used by other
     * components (like {@link proj.app.state.SimulationStateProducer} or
     * {@link proj.app.controllers.SimulationWindowController}) to identify the selected animal.
     *
     * @return The reference to the currently selected {@link Animal}, or {@code null}.
     */
    public Animal getCurrentAnimalReference() {
        return currentAnimalReference;
    }

    // --- ReadOnly Property Getters (for JavaFX binding) ---
    // Expose only the ReadOnly properties to prevent external modification of the ViewModel's state directly.
    public ReadOnlyIntegerProperty energyProperty() { return energy.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty ageProperty() { return age.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty childrenMadeProperty() { return childrenMade.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty plantsEatenProperty() { return plantsEaten.getReadOnlyProperty(); }
    public ReadOnlyStringProperty genotypeProperty() { return genotype.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty activeGeneIndexProperty() { return activeGeneIndex.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty descendantsCountProperty() { return descendantsCount.getReadOnlyProperty(); }
    public ReadOnlyStringProperty deathDateProperty() { return deathDate.getReadOnlyProperty(); }
    public ReadOnlyBooleanProperty isSelectedProperty() { return isSelected.getReadOnlyProperty(); }
}