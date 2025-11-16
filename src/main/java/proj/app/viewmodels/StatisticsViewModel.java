// Plik: proj/app/viewmodels/StatisticsViewModel.java
package proj.app.viewmodels;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import proj.app.SimulationStatisticsSnapshot;
import proj.app.services.IMessageService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ViewModel odpowiedzialny za agregowanie i udostępnianie ogólnych statystyk symulacji
 * w formacie odpowiednim do powiązania z kontrolkami JavaFX UI (np. Label, TextArea).
 * Przechowuje właściwości JavaFX dla kluczowych metryk i jest aktualizowany na podstawie
 * danych otrzymanych w obiektach {@link SimulationStatisticsSnapshot}.
 * Używa {@link IMessageService} do formatowania niektórych tekstów.
 */
public class StatisticsViewModel {

    private final IMessageService messageService; // Serwis do pobierania wiadomości

    //<editor-fold desc="Properties - Pola do powiązania z UI">
    private final ReadOnlyIntegerWrapper dayCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper animalCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper plantCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper emptyFieldsCount = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyDoubleWrapper averageEnergy = new ReadOnlyDoubleWrapper(0.0);
    private final ReadOnlyDoubleWrapper averageLifespan = new ReadOnlyDoubleWrapper(0.0);
    private final ReadOnlyDoubleWrapper averageChildren = new ReadOnlyDoubleWrapper(0.0);
    // Sformatowany tekst dla TextArea
    private final ReadOnlyStringWrapper popularGenotypesText;
    // Sformatowana średnia długość życia z jednostką
    private final ReadOnlyStringWrapper averageLifespanFormatted;
    // Surowa lista topowych genotypów (potencjalnie do innych zastosowań)
    private final ReadOnlyListWrapper<String> topGenotypes = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
    //</editor-fold>

    /**
     * Konstruuje StatisticsViewModel.
     * @param messageService Serwis do pobierania zlokalizowanych wiadomości. Nie może być null.
     */
    public StatisticsViewModel(IMessageService messageService) {
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");
        this.popularGenotypesText = new ReadOnlyStringWrapper(messageService.getMessage("placeholder.genotype.none"));
        this.averageLifespanFormatted = new ReadOnlyStringWrapper();
        this.averageLifespanFormatted.bind(Bindings.createStringBinding(
                () -> String.format("%.1f %s", averageLifespan.get(), messageService.getMessage("unit.days")),
                averageLifespan
        ));
        clearStatistics();
    }

    /**
     * Aktualizuje właściwości ViewModelu na podstawie danych z dostarczonej migawki.
     * Zapewnia wykonanie aktualizacji w wątku aplikacji JavaFX.
     *
     * @param statsSnapshot {@link SimulationStatisticsSnapshot} z najnowszymi statystykami.
     *                      Jeśli {@code null}, statystyki zostaną wyczyszczone.
     */
    public void updateStatistics(SimulationStatisticsSnapshot statsSnapshot) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateStatisticsInternal(statsSnapshot));
        } else {
            updateStatisticsInternal(statsSnapshot);
        }
    }

    /**
     * Wewnętrzna logika aktualizacji właściwości ViewModelu. Musi być wykonywana w wątku FX.
     * @param statsSnapshot Migawka statystyk lub null.
     */
    private void updateStatisticsInternal(SimulationStatisticsSnapshot statsSnapshot) {
        if (statsSnapshot != null) {
            dayCount.set(statsSnapshot.day());
            animalCount.set(statsSnapshot.animalCount());
            plantCount.set(statsSnapshot.plantCount());
            emptyFieldsCount.set(statsSnapshot.emptyFieldsCount());
            averageEnergy.set(statsSnapshot.averageEnergy());
            averageLifespan.set(statsSnapshot.averageLifespan());
            averageChildren.set(statsSnapshot.averageChildren());
            updateGenotypeStatistics(statsSnapshot.genotypeCounts(), statsSnapshot.animalCount(), statsSnapshot.topGenotypes());
        } else {
            System.err.println("Warning: StatisticsViewModel received null statsSnapshot. Clearing display.");
            clearStatistics(); // Wywołaj publiczną metodę
        }
    }

    /**
     * Aktualizuje właściwości związane z wyświetlaniem genotypów.
     *
     * @param genotypeCounts Mapa: genotyp (String) -> liczba wystąpień.
     * @param totalAnimals   Całkowita liczba żywych zwierząt.
     * @param currentTopGenotypes Lista topowych genotypów z migawki statystyk.
     */
    private void updateGenotypeStatistics(Map<String, Integer> genotypeCounts, int totalAnimals, List<String> currentTopGenotypes) {
        if (currentTopGenotypes == null || currentTopGenotypes.isEmpty() || totalAnimals <= 0 || genotypeCounts == null) {
            popularGenotypesText.set(messageService.getMessage("placeholder.genotype.none"));
            topGenotypes.set(FXCollections.observableArrayList());
            return;
        }

        StringBuilder genotypeTextBuilder = new StringBuilder(messageService.getMessage("sim.label.popularGenotypes"));
        genotypeTextBuilder.append("\n");

        for (int i = 0; i < currentTopGenotypes.size(); i++) {
            String genotypeStr = currentTopGenotypes.get(i);
            long count = genotypeCounts.getOrDefault(genotypeStr, 0);
            double percentage = (count * 100.0) / totalAnimals;

            String colorIndicator = switch (i) {
                case 0 -> "(Magenta)"; // TODO: Przenieść do IMessageService
                case 1 -> "(Black)";
                case 2 -> "(Blue)";
                default -> "";
            };

            genotypeTextBuilder.append(String.format("\n%d. %s %s\n   (%d, %.1f%%)",
                    i + 1, genotypeStr, colorIndicator, count, percentage));
        }

        popularGenotypesText.set(genotypeTextBuilder.toString().trim());
        topGenotypes.set(FXCollections.observableArrayList(currentTopGenotypes));
    }


    /** Resetuje wszystkie właściwości statystyk do wartości domyślnych. */
    public void clearStatistics() { // Zmieniono na public
        dayCount.set(0);
        animalCount.set(0);
        plantCount.set(0);
        emptyFieldsCount.set(0);
        averageEnergy.set(0.0);
        averageLifespan.set(0.0);
        averageChildren.set(0.0);
        popularGenotypesText.set(messageService.getMessage("placeholder.genotype.none"));
        topGenotypes.set(FXCollections.observableArrayList());
    }

    //<editor-fold desc="ReadOnly Property Getters - Do powiązania z UI">
    public ReadOnlyIntegerProperty dayCountProperty() { return dayCount.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty animalCountProperty() { return animalCount.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty plantCountProperty() { return plantCount.getReadOnlyProperty(); }
    public ReadOnlyIntegerProperty emptyFieldsCountProperty() { return emptyFieldsCount.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty averageEnergyProperty() { return averageEnergy.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty averageLifespanProperty() { return averageLifespan.getReadOnlyProperty(); }
    public ReadOnlyStringProperty averageLifespanFormattedProperty() { return averageLifespanFormatted.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty averageChildrenProperty() { return averageChildren.getReadOnlyProperty(); }
    public ReadOnlyStringProperty popularGenotypesTextProperty() { return popularGenotypesText.getReadOnlyProperty(); }
    public ReadOnlyListProperty<String> topGenotypesProperty() { return topGenotypes.getReadOnlyProperty(); }
    //</editor-fold>

    /** Pobiera bieżącą (niemodyfikowalną) listę topowych genotypów. */
    public List<String> getTopGenotypes() {
        return topGenotypes.get() == null ? List.of() : Collections.unmodifiableList(topGenotypes.get());
    }
}