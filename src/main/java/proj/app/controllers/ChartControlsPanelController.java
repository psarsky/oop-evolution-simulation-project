package proj.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region; // Używamy Region
import javafx.scene.layout.VBox;
// Importy HBox i Rectangle nie są już bezpośrednio potrzebne w tym kontrolerze
import proj.app.services.IMessageService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ChartControlsPanelController {

    //<editor-fold desc="FXML Fields - Dodano pola dla swatchy">
    @FXML private Label controlsTitleLabel;
    @FXML private CheckBox showAnimalCountCheck;
    @FXML private CheckBox showPlantCountCheck;
    @FXML private CheckBox showAvgEnergyCheck;
    @FXML private CheckBox showAvgLifespanCheck;
    @FXML private CheckBox showAvgChildrenCheck;
    @FXML private Label optionsLabel;
    @FXML private CheckBox showMarkersCheck;

    // Pola dla wskaźników koloru (swatchy) - fx:id muszą pasować do FXML
    @FXML private Region swatchAnimalCount;
    @FXML private Region swatchPlantCount;
    @FXML private Region swatchAvgEnergy;
    @FXML private Region swatchAvgLifespan;
    @FXML private Region swatchAvgChildren;

    // Usunięto pola związane z oddzielną legendą:
    // @FXML private Label legendTitleLabel;
    // @FXML private VBox legendContainer;
    //</editor-fold>

    private StatisticsChartPanelController chartController;
    private IMessageService messageService;
    private final Map<CheckBox, String> checkBoxSeriesMap = new LinkedHashMap<>();
    // Mapa nazw serii jest nadal potrzebna do ustawienia tekstów checkboxów
    private final Map<String, String> seriesNamesMap = new LinkedHashMap<>();


    @FXML
    public void initialize() {
        // Klucze muszą być zgodne z StatisticsChartPanelController
        checkBoxSeriesMap.put(showAnimalCountCheck, "animals");
        checkBoxSeriesMap.put(showPlantCountCheck, "plants");
        checkBoxSeriesMap.put(showAvgEnergyCheck, "avg_energy");
        checkBoxSeriesMap.put(showAvgLifespanCheck, "avg_lifespan");
        checkBoxSeriesMap.put(showAvgChildrenCheck, "avg_children");

        // Ustawienie listenerów na CheckBoxach - bez zmian
        checkBoxSeriesMap.forEach((cb, key) ->
                cb.selectedProperty().addListener((obs, ov, nv) -> {
                    if (chartController != null) {
                        chartController.setSeriesVisibility(key, nv);
                    }
                })
        );
        showMarkersCheck.selectedProperty().addListener((obs, ov, nv) -> {
            if (chartController != null) {
                chartController.toggleMarkers(nv);
            }
        });

        // Ustawienie rozmiaru dla swatchy (można też w CSS)
        applySwatchSize(swatchAnimalCount);
        applySwatchSize(swatchPlantCount);
        applySwatchSize(swatchAvgEnergy);
        applySwatchSize(swatchAvgLifespan);
        applySwatchSize(swatchAvgChildren);
    }

    /** Pomocnicza metoda do ustawiania rozmiaru swatcha */
    private void applySwatchSize(Region swatch) {
        if (swatch != null) {
            swatch.setPrefSize(10, 10);
            swatch.setMinSize(10, 10);
            swatch.setMaxSize(10, 10);
        }
    }

    public void initializeController(StatisticsChartPanelController chartController, IMessageService messageService) {
        this.chartController = Objects.requireNonNull(chartController, "ChartController cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");

        loadSeriesNames(); // Pobierz nazwy serii

        setUIText(); // Ustaw teksty kontrolek (w tym CheckBoxów)
        // Usunięto createLegendItems();
        syncCheckboxesWithChartState(); // Zsynchronizuj stan początkowy
    }

    /** Pobiera i przechowuje zlokalizowane nazwy serii. */
    private void loadSeriesNames() {
        seriesNamesMap.put("animals", messageService.getMessage("chart.series.animals"));
        seriesNamesMap.put("plants", messageService.getMessage("chart.series.plants"));
        seriesNamesMap.put("avg_energy", messageService.getMessage("chart.series.avgEnergy"));
        seriesNamesMap.put("avg_lifespan", messageService.getMessage("chart.series.avgLifespan"));
        seriesNamesMap.put("avg_children", messageService.getMessage("chart.series.avgChildren"));
    }

    private void setUIText() {
        controlsTitleLabel.setText(messageService.getMessage("chart.label.controlsTitle"));
        optionsLabel.setText(messageService.getMessage("chart.label.options"));
        // Usunięto ustawianie legendTitleLabel

        // Ustaw teksty CheckBoxów używając pobranych nazw serii
        showAnimalCountCheck.setText(seriesNamesMap.getOrDefault("animals", "Animals"));
        showPlantCountCheck.setText(seriesNamesMap.getOrDefault("plants", "Plants"));
        showAvgEnergyCheck.setText(seriesNamesMap.getOrDefault("avg_energy", "Avg Energy"));
        showAvgLifespanCheck.setText(seriesNamesMap.getOrDefault("avg_lifespan", "Avg Lifespan"));
        showAvgChildrenCheck.setText(seriesNamesMap.getOrDefault("avg_children", "Avg Children"));
        showMarkersCheck.setText(messageService.getMessage("chart.checkbox.showMarkers"));
    }

    // Usunięto metody createLegendItems() i createLegendItem()

    private void syncCheckboxesWithChartState() {
        if (chartController == null) return;
        checkBoxSeriesMap.forEach((cb, key) ->
                cb.setSelected(chartController.isSeriesVisible(key))
        );
        showMarkersCheck.setSelected(chartController.areMarkersVisible());
    }
}