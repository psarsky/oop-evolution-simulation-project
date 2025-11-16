package proj.app.viewmodels;

import javafx.beans.property.*;
import proj.app.GenotypeFormatter;
import proj.app.services.IMessageService; // Import serwisu wiadomości
import proj.model.elements.Animal;
import java.util.Objects;

/**
 * ViewModel odpowiedzialny za przechowywanie i udostępnianie szczegółów
 * aktualnie wybranego {@link Animal} w interfejsie użytkownika symulacji.
 * Używa właściwości JavaFX do łatwego powiązania z etykietami i innymi kontrolkami
 * w panelu "Selected Animal". Używa {@link IMessageService} do pobierania placeholderów.
 */
public class SelectedAnimalViewModel {

    private final IMessageService messageService; // Serwis wiadomości
    private final String placeholder; // Przechowywany placeholder

    // --- Właściwości do powiązania z UI ---
    private final ReadOnlyIntegerWrapper energy = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper age = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper childrenMade = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper plantsEaten = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyStringWrapper genotype; // Inicjalizowany w konstruktorze
    private final ReadOnlyIntegerWrapper activeGeneIndex = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper descendantsCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyStringWrapper deathDate; // Inicjalizowany w konstruktorze
    private final ReadOnlyBooleanWrapper isSelected = new ReadOnlyBooleanWrapper(false);

    // Referencja do aktualnie wyświetlanego obiektu Animal
    private Animal currentAnimalReference = null;

    /**
     * Konstruuje SelectedAnimalViewModel.
     * @param messageService Serwis do pobierania zlokalizowanych wiadomości (np. placeholderów). Nie może być null.
     */
    public SelectedAnimalViewModel(IMessageService messageService) {
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");
        this.placeholder = messageService.getMessage("placeholder.selected.none"); // Pobierz placeholder raz

        // Inicjalizuj właściwości StringWrapper z placeholderem
        this.genotype = new ReadOnlyStringWrapper(placeholder);
        this.deathDate = new ReadOnlyStringWrapper(placeholder);

        clear(); // Ustaw początkowe wartości liczbowe na 0, a tekstowe na placeholder
    }

    /**
     * Aktualizuje ViewModel danymi z dostarczonego {@link Animal}.
     * Jeśli przekazane zwierzę jest null, czyści ViewModel.
     *
     * @param animal Zwierzę {@link Animal}, którego dane mają być wyświetlone, lub {@code null} do wyczyszczenia wyboru.
     */
    public void update(Animal animal) {
        if (animal != null) {
            energy.set(animal.getEnergy());
            age.set(animal.getAge());
            childrenMade.set(animal.getChildrenMade());
            plantsEaten.set(animal.getPlantsEaten());
            genotype.set(GenotypeFormatter.formatGenotype(animal.getGenes()));
            activeGeneIndex.set(animal.getActiveGeneIndex());
            descendantsCount.set(animal.getDescendantsCount());
            // Ustaw datę śmierci lub placeholder, jeśli zwierzę żyje
            deathDate.set(animal.isAlive() ? placeholder : String.valueOf(animal.getDeathDate()));
            isSelected.set(true); // Oznacz jako wybrane
            currentAnimalReference = animal; // Zapisz referencję
        } else {
            clear(); // Wyczyść, jeśli przekazano null
        }
    }

    /**
     * Czyści ViewModel, resetując wszystkie właściwości do wartości domyślnych/pustych (używając placeholdera).
     */
    public void clear() {
        energy.set(0);
        age.set(0);
        childrenMade.set(0);
        plantsEaten.set(0);
        genotype.set(placeholder); // Użyj zapamiętanego placeholdera
        activeGeneIndex.set(0); // 0 jest zazwyczaj bezpieczną wartością domyślną
        descendantsCount.set(0);
        deathDate.set(placeholder); // Użyj zapamiętanego placeholdera
        isSelected.set(false); // Oznacz jako niewybrane
        currentAnimalReference = null; // Wyczyść referencję
    }

    /**
     * Pobiera referencję do aktualnego obiektu {@link Animal} reprezentowanego przez ten ViewModel.
     * Zwraca {@code null}, jeśli żadne zwierzę nie jest aktualnie wybrane.
     * @return Referencja do wybranego {@link Animal} lub {@code null}.
     */
    public Animal getCurrentAnimalReference() {
        return currentAnimalReference;
    }

    // --- Gettery ReadOnly Property (do powiązania z JavaFX UI) ---
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