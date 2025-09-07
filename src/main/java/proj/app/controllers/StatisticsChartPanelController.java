// Plik: proj/app/controllers/StatisticsChartPanelController.java
package proj.app.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import proj.app.services.IMessageService;
import proj.app.viewmodels.ChartDataModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Kontroler dla panelu wykresu statystyk (StatisticsChartPanel.fxml).
 * Odpowiada za wyświetlanie danych historycznych z ChartDataModel
 * i zarządzanie wyglądem serii danych na wykresie.
 */
public class StatisticsChartPanelController {

    @FXML private LineChart<Number, Number> statisticsChart;
    @FXML private NumberAxis dayAxis;
    @FXML private NumberAxis valueAxis;

    // Klucze identyfikujące serie danych (muszą pasować do ChartDataModel i ChartControlsPanelController)
    private static final String SERIES_ANIMALS = "animals";
    private static final String SERIES_PLANTS = "plants";
    private static final String SERIES_AVG_ENERGY = "avg_energy";
    private static final String SERIES_AVG_LIFESPAN = "avg_lifespan";
    private static final String SERIES_AVG_CHILDREN = "avg_children";

    // Domyślne kolory dla serii (powinny pasować do CSS)
    private static final Map<String, String> seriesColors = Map.of(
            SERIES_ANIMALS, "#f3622d", // Kolor z CSS chart-series-animals
            SERIES_PLANTS, "#fba71b",  // Kolor z CSS chart-series-plants
            SERIES_AVG_ENERGY, "#57b757", // Kolor z CSS chart-series-avg_energy
            SERIES_AVG_LIFESPAN, "#41a9c9", // Kolor z CSS chart-series-avg_lifespan
            SERIES_AVG_CHILDREN, "#9268e3"  // Kolor z CSS chart-series-avg_children
    );

    private IMessageService messageService;
    private ChartDataModel chartDataModel;
    // Mapa przechowująca referencje do obiektów serii wykresu, kluczowane przez String
    private final Map<String, XYChart.Series<Number, Number>> seriesMap = new HashMap<>();

    /**
     * Inicjalizuje kontroler po załadowaniu FXML. Ustawia podstawowe właściwości wykresu.
     */
    @FXML
    public void initialize() {
        statisticsChart.setCreateSymbols(false); // Domyślnie nie pokazuj markerów
        dayAxis.setForceZeroInRange(false); // Oś dni nie musi zaczynać się od 0
        valueAxis.setForceZeroInRange(false); // Oś wartości nie musi zaczynać się od 0
        statisticsChart.setLegendVisible(false); // Ukryj domyślną legendę JavaFX
        statisticsChart.setData(FXCollections.observableArrayList()); // Zainicjalizuj pustą listą danych

        // Dodaj listener do danych wykresu, aby stylizować nowo dodane serie
        statisticsChart.getData().addListener((ListChangeListener<XYChart.Series<Number, Number>>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (XYChart.Series<Number, Number> addedSeries : c.getAddedSubList()) {
                        // Zastosuj style (kolor linii i markerów) do nowo dodanej serii
                        applyStylesToExistingDataPoints(addedSeries);
                    }
                }
            }
        });
    }

    /**
     * Inicjalizuje kontroler danymi i zależnościami po utworzeniu.
     * @param model          Model danych {@link ChartDataModel}.
     * @param messageService Serwis wiadomości {@link IMessageService}.
     */
    public void initializeController(ChartDataModel model, IMessageService messageService) {
        this.chartDataModel = Objects.requireNonNull(model, "ChartDataModel cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");
        setUIText(); // Ustaw teksty osi i tytułu
        bindChartData(); // Utwórz serie i powiąż je z danymi z modelu
    }

    /** Ustawia teksty tytułu i osi wykresu używając IMessageService. */
    private void setUIText() {
        statisticsChart.setTitle(messageService.getMessage("chart.title"));
        dayAxis.setLabel(messageService.getMessage("chart.axis.day"));
        valueAxis.setLabel(messageService.getMessage("chart.axis.value"));
    }

    /** Tworzy serie danych i wiąże je z ObservableList w ChartDataModel. */
    private void bindChartData() {
        seriesMap.clear(); // Wyczyść mapę serii przed ponownym powiązaniem
        if (chartDataModel == null) return;

        // Utwórz i stylizuj każdą serię danych
        createAndStyleSeries(SERIES_ANIMALS, chartDataModel.getAnimalData());
        createAndStyleSeries(SERIES_PLANTS, chartDataModel.getPlantData());
        createAndStyleSeries(SERIES_AVG_ENERGY, chartDataModel.getAvgEnergyData());
        createAndStyleSeries(SERIES_AVG_LIFESPAN, chartDataModel.getAvgLifespanData());
        createAndStyleSeries(SERIES_AVG_CHILDREN, chartDataModel.getAvgChildrenData());

        // Dodaj początkowo widoczne serie do wykresu
        // (Zakładając, że te są domyślnie widoczne)
        setSeriesVisibility(SERIES_ANIMALS, true);
        setSeriesVisibility(SERIES_PLANTS, true);
        setSeriesVisibility(SERIES_AVG_ENERGY, true);
        setSeriesVisibility(SERIES_AVG_LIFESPAN, false); // Domyślnie ukryta
        setSeriesVisibility(SERIES_AVG_CHILDREN, false); // Domyślnie ukryta
    }

    /**
     * Tworzy obiekt serii XYChart, ustawia jej nazwę (z IMessageService),
     * przypisuje dane ObservableList i stosuje początkowe style.
     * @param seriesKey Klucz identyfikujący serię (np. "avg_energy").
     * @param dataList  Lista ObservableList z danymi dla tej serii.
     */
    private void createAndStyleSeries(String seriesKey, ObservableList<XYChart.Data<Number, Number>> dataList) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>(dataList);

        // --- POPRAWIONE GENEROWANIE KLUCZA WIADOMOŚCI ---
        String seriesNameKey;
        if (seriesKey.contains("_")) {
            // Konwertuje np. "avg_energy" na "avgEnergy"
            String[] parts = seriesKey.split("_");
            StringBuilder camelCase = new StringBuilder(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                camelCase.append(parts[i].substring(0, 1).toUpperCase())
                        .append(parts[i].substring(1).toLowerCase());
            }
            seriesNameKey = "chart.series." + camelCase.toString(); // np. chart.series.avgEnergy
        } else {
            // Dla kluczy bez podkreślenia, np. "animals", "plants"
            seriesNameKey = "chart.series." + seriesKey; // np. chart.series.animals
        }
        // --- KONIEC POPRAWKI ---

        series.setName(messageService.getMessage(seriesNameKey)); // Użyj poprawionego klucza
        seriesMap.put(seriesKey, series); // Dodaj serię do mapy dla łatwego dostępu

        String colorHex = seriesColors.get(seriesKey); // Pobierz kolor dla serii
        if (colorHex == null) return; // Jeśli brak koloru, nie stylizuj

        // Listener do stylizacji linii serii, gdy zostanie dodana do wykresu
        series.nodeProperty().addListener((obs, oldN, newN) -> {
            if (newN != null) {
                newN.setStyle("-fx-stroke: " + colorHex + ";");
            }
        });

        // Listener do stylizacji punktów danych (markerów), gdy zostaną dodane do serii
        series.getData().addListener((ListChangeListener<XYChart.Data<Number, Number>>) c -> {
            while(c.next()) {
                if (c.wasAdded()) {
                    for(XYChart.Data<Number, Number> addedData : c.getAddedSubList()) {
                        applyStyleToDataPointNode(addedData.getNode(), colorHex);
                    }
                }
            }
        });

        // Zastosuj style do już istniejących punktów danych (jeśli model danych nie był pusty)
        applyStylesToExistingDataPoints(series);
    }

    /**
     * Stosuje styl CSS do węzła reprezentującego punkt danych (marker).
     * Używane, gdy markery są włączone (createSymbols = true).
     * @param dataNode Węzeł punktu danych.
     * @param colorHex Kolor do zastosowania (z serii).
     */
    private void applyStyleToDataPointNode(Node dataNode, String colorHex) {
        if (dataNode != null && colorHex != null) {
            // Prosty styl: ustawia kolor tła markera
            dataNode.setStyle("-fx-background-color: " + colorHex + ";");
            // Można dodać bardziej złożone style, np. z obramowaniem:
             /*
             dataNode.setStyle("-fx-background-color: " + colorHex + ", white; " +
                              "-fx-background-insets: 0, 2; " +
                              "-fx-background-radius: 5px; " +
                              "-fx-padding: 3px;");
             */
        }
    }

    /**
     * Przechodzi przez wszystkie punkty danych w serii i próbuje zastosować do nich styl.
     * Przydatne do stylizacji serii zaraz po jej utworzeniu lub dodaniu do wykresu.
     * @param series Seria, której punkty danych mają być stylizowane.
     */
    private void applyStylesToExistingDataPoints(XYChart.Series<Number, Number> series) {
        // Znajdź klucz (i kolor) dla danej serii
        String seriesKey = seriesMap.entrySet().stream()
                .filter(entry -> entry.getValue() == series)
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
        if (seriesKey == null) return;
        String colorHex = seriesColors.get(seriesKey);
        if (colorHex == null) return;

        // Zastosuj styl do każdego punktu danych w serii
        for (XYChart.Data<Number, Number> data : series.getData()) {
            Node node = data.getNode(); // Pobierz węzeł punktu
            if (node != null) {
                // Jeśli węzeł już istnieje, zastosuj styl od razu
                applyStyleToDataPointNode(node, colorHex);
            } else {
                // Jeśli węzeł jeszcze nie istnieje (JavaFX tworzy je leniwie),
                // dodaj listener, który zastosuje styl, gdy węzeł zostanie utworzony.
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        applyStyleToDataPointNode(newNode, colorHex);
                        // Idealnie, listener powinien być usunięty po pierwszym wywołaniu,
                        // ale zarządzanie tym może być skomplikowane. W praktyce rzadko
                        // powoduje to problemy wydajnościowe dla typowych wykresów.
                    }
                });
            }
        }
    }

    /**
     * Ustawia widoczność danej serii danych na wykresie (dodaje ją lub usuwa).
     * Wykonywane synchronicznie w wątku JavaFX Application Thread.
     *
     * @param seriesKey Klucz serii do pokazania/ukrycia.
     * @param visible   True, aby pokazać serię, false, aby ukryć.
     */
    public void setSeriesVisibility(String seriesKey, boolean visible) {
        // --- USUNIĘTO Platform.runLater ---
        // Platform.runLater(() -> { // Niepotrzebne w tym kontekście
        if (statisticsChart == null || statisticsChart.getData() == null || seriesMap == null) {
            System.err.println("setSeriesVisibility: Chart or its data/seriesMap is null. Cannot set visibility for key: " + seriesKey);
            return;
        }
        XYChart.Series<Number, Number> series = seriesMap.get(seriesKey);
        if (series == null) {
            System.err.println("setSeriesVisibility: Series not found for key: " + seriesKey);
            return; // Seria o podanym kluczu nie istnieje
        }

        ObservableList<XYChart.Series<Number, Number>> chartData = statisticsChart.getData();

        if (visible) {
            // Pokaż serię: dodaj ją do danych wykresu, jeśli jeszcze jej tam nie ma
            if (!chartData.contains(series)) {
                chartData.add(series);
                // Po dodaniu, upewnij się, że style są zastosowane
                applyStylesToExistingDataPoints(series);
            }
        } else {
            // Ukryj serię: usuń ją z danych wykresu
            chartData.remove(series);
        }
        // }); // Koniec usuniętego Platform.runLater
    }
    /**
     * Sprawdza, czy dana seria jest aktualnie widoczna na wykresie.
     * @param seriesKey Klucz serii do sprawdzenia.
     * @return True, jeśli seria jest dodana do danych wykresu, false w przeciwnym razie.
     */
    public boolean isSeriesVisible(String seriesKey) {
        if (statisticsChart == null || statisticsChart.getData() == null || seriesMap == null) {
            System.err.println("Warning: Checking visibility when chart/data/seriesMap is null.");
            return false;
        }
        XYChart.Series<Number, Number> series = seriesMap.get(seriesKey);
        return series != null && statisticsChart.getData().contains(series);
    }

    /**
     * Włącza lub wyłącza wyświetlanie markerów (symboli) dla punktów danych na wykresie.
     * Wykonywane w wątku JavaFX Application Thread.
     * @param show True, aby pokazać markery, false, aby je ukryć.
     */
    public void toggleMarkers(boolean show) {
        Platform.runLater(() -> {
            if (statisticsChart != null) {
                statisticsChart.setCreateSymbols(show);
                // Po zmianie createSymbols, może być konieczne ponowne zastosowanie stylów
                // do punktów, jeśli style obejmowały coś więcej niż tylko tło.
                if (show) {
                    for (XYChart.Series<Number, Number> series : statisticsChart.getData()) {
                        applyStylesToExistingDataPoints(series);
                    }
                }
            }
        });
    }

    /**
     * Sprawdza, czy markery (symbole) są aktualnie widoczne na wykresie.
     * @return True, jeśli markery są włączone, false w przeciwnym razie.
     */
    public boolean areMarkersVisible() {
        return statisticsChart != null && statisticsChart.getCreateSymbols();
    }

    /**
     * Resetuje stan wykresu: czyści dane w modelu, usuwa serie z wykresu,
     * przywraca domyślną widoczność serii i markerów.
     */
    public void reset() {
        if(chartDataModel != null) chartDataModel.clearData(); // Czyść dane w modelu

        Platform.runLater(() -> {
            // Usuń wszystkie serie z wykresu
            if (statisticsChart != null && statisticsChart.getData() != null) {
                statisticsChart.getData().clear();
            }
            // Przywróć domyślną widoczność serii (może wymagać ponownego dodania do wykresu)
            setSeriesVisibility(SERIES_ANIMALS, true);
            setSeriesVisibility(SERIES_PLANTS, true);
            setSeriesVisibility(SERIES_AVG_ENERGY, true);
            setSeriesVisibility(SERIES_AVG_LIFESPAN, false);
            setSeriesVisibility(SERIES_AVG_CHILDREN, false);
            // Ustaw domyślny stan tworzenia symboli (markerów)
            toggleMarkers(false);
        });
    }
}