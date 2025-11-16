package proj.app.viewmodels; // Lub odpowiedni pakiet np. proj.app.charts

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import proj.app.SimulationStatisticsSnapshot;

import java.util.Objects;

/**
 * Prosty model danych do przechowywania historii statystyk dla wykresu.
 * Używa ObservableList, aby umożliwić kontrolerowi wykresu reagowanie na zmiany.
 * UWAGA: Ta implementacja przechowuje wszystkie punkty w pamięci. Dla bardzo
 * długich symulacji może wymagać optymalizacji (np. ograniczania liczby punktów).
 */
public class ChartDataModel {

    // Używamy ObservableList<XYChart.Data<Number, Number>> dla każdej serii
    // To pozwala wykresowi automatycznie aktualizować się po dodaniu danych.
    private final ObservableList<XYChart.Data<Number, Number>> animalData = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Data<Number, Number>> plantData = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Data<Number, Number>> avgEnergyData = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Data<Number, Number>> avgLifespanData = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Data<Number, Number>> avgChildrenData = FXCollections.observableArrayList();

    // Opcjonalnie: Limit przechowywanych punktów
    private int maxDataPoints = 1000; // Domyślny limit

    /**
     * Dodaje nowy punkt danych na podstawie migawki statystyk.
     * Wywoływane na koniec każdego dnia symulacji.
     *
     * @param snapshot Migawka statystyk z danymi dla danego dnia.
     */
    public void addDailyData(SimulationStatisticsSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "Snapshot cannot be null");
        int day = snapshot.day();

        // Dodaj dane do odpowiednich list ObservableList
        // Używamy add, co powiadomi obserwatorów (jak wykres)
        animalData.add(new XYChart.Data<>(day, snapshot.animalCount()));
        plantData.add(new XYChart.Data<>(day, snapshot.plantCount()));
        avgEnergyData.add(new XYChart.Data<>(day, snapshot.averageEnergy()));
        avgLifespanData.add(new XYChart.Data<>(day, snapshot.averageLifespan()));
        avgChildrenData.add(new XYChart.Data<>(day, snapshot.averageChildren()));

        // Zastosuj limit punktów, jeśli jest ustawiony
        applyDataLimit();
    }

    /** Usuwa najstarsze punkty danych, jeśli przekroczono limit. */
    private void applyDataLimit() {
        if (maxDataPoints > 0) {
            limitList(animalData);
            limitList(plantData);
            limitList(avgEnergyData);
            limitList(avgLifespanData);
            limitList(avgChildrenData);
        }
    }

    /** Pomocnicza metoda do ograniczania rozmiaru listy ObservableList. */
    private void limitList(ObservableList<XYChart.Data<Number, Number>> list) {
        while (list.size() > maxDataPoints) {
            list.removeFirst(); // Usuń najstarszy punkt
        }
    }

    /** Czyści wszystkie dane historyczne. */
    public void clearData() {
        animalData.clear();
        plantData.clear();
        avgEnergyData.clear();
        avgLifespanData.clear();
        avgChildrenData.clear();
    }

    // --- Gettery dla ObservableList ---
    // Kontroler wykresu będzie używał tych metod do powiązania danych z seriami wykresu.
    public ObservableList<XYChart.Data<Number, Number>> getAnimalData() { return animalData; }
    public ObservableList<XYChart.Data<Number, Number>> getPlantData() { return plantData; }
    public ObservableList<XYChart.Data<Number, Number>> getAvgEnergyData() { return avgEnergyData; }
    public ObservableList<XYChart.Data<Number, Number>> getAvgLifespanData() { return avgLifespanData; }
    public ObservableList<XYChart.Data<Number, Number>> getAvgChildrenData() { return avgChildrenData; }

    // --- Opcjonalny Setter dla limitu ---
    public void setMaxDataPoints(int maxPoints) {
        this.maxDataPoints = Math.max(0, maxPoints); // Ustaw limit (0 oznacza brak limitu)
        applyDataLimit(); // Zastosuj limit od razu
    }

    public int getMaxDataPoints() {
        return maxDataPoints;
    }
}