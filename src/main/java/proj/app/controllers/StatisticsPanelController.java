// Plik: proj/app/controllers/StatisticsPanelController.java
package proj.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
// Importy dla TextArea i Tooltip zostały usunięte
import proj.app.services.IMessageService;
import proj.app.viewmodels.StatisticsViewModel;

import java.util.Objects;

/**
 * Kontroler dla panelu bieżących statystyk symulacji (StatisticsPanel.fxml).
 * Wyświetla podstawowe statystyki takie jak dzień, liczba zwierząt, roślin, itp.
 * Nie wyświetla już informacji o genotypach (przeniesione do GenotypesPanel).
 */
public class StatisticsPanelController {

    //<editor-fold desc="FXML Fields - Elementy UI w panelu">
    @FXML private Label panelTitleLabel;
    @FXML private Label dayLabel;
    @FXML private Label dayCount;
    @FXML private Label animalLabel;
    @FXML private Label animalCount;
    @FXML private Label plantLabel;
    @FXML private Label plantCount;
    @FXML private Label emptyFieldsLabel;
    @FXML private Label emptyFieldsCount;
    // --- Pola związane z genotypami zostały USUNIĘTE ---
    // @FXML private Label popularGenotypesLabel; // USUNIĘTO
    // @FXML private TextArea popularGenotypes;    // USUNIĘTO
    @FXML private Label avgEnergyLabel;
    @FXML private Label averageEnergy;
    @FXML private Label avgLifespanLabel;
    @FXML private Label averageLifespan;
    @FXML private Label avgChildrenLabel;
    @FXML private Label averageChildren;
    //</editor-fold>

    private StatisticsViewModel viewModel;
    private IMessageService messageService;
    // --- Pole genotypesTooltip zostało USUNIĘTE ---
    // private Tooltip genotypesTooltip;

    @FXML
    public void initialize() {
        // Inicjalizacja jest teraz pusta lub zawiera tylko ogólne ustawienia,
        // które nie zależą od wstrzykniętych zależności.
        // Usunięto inicjalizację Tooltipa.
    }

    public void initializeController(StatisticsViewModel viewModel, IMessageService messageService) {
        this.viewModel = Objects.requireNonNull(viewModel, "StatisticsViewModel cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");

        // Sprawdź kluczowe kontrolki (teraz bez genotypów)
        if (panelTitleLabel == null || dayCount == null /* dodaj inne ważne, jeśli są */) {
            System.err.println("CRITICAL ERROR: FXML fields not injected correctly in StatisticsPanelController!");
            return;
        }

        // Usunięto konfigurację popularGenotypes i Tooltip.install

        setUIText();
        bindViewModel();
    }

    private void setUIText() {
        // Sprawdzenia null dla bezpieczeństwa
        if (panelTitleLabel != null) panelTitleLabel.setText(messageService.getMessage("sim.titledPane.currentStats"));
        if (dayLabel != null) dayLabel.setText(messageService.getMessage("sim.label.day"));
        if (animalLabel != null) animalLabel.setText(messageService.getMessage("sim.label.animals"));
        if (plantLabel != null) plantLabel.setText(messageService.getMessage("sim.label.plants"));
        if (emptyFieldsLabel != null) emptyFieldsLabel.setText(messageService.getMessage("sim.label.emptyFields"));
        if (avgEnergyLabel != null) avgEnergyLabel.setText(messageService.getMessage("sim.label.avgEnergy"));
        if (avgLifespanLabel != null) avgLifespanLabel.setText(messageService.getMessage("sim.label.avgLifespan"));
        if (avgChildrenLabel != null) avgChildrenLabel.setText(messageService.getMessage("sim.label.avgChildren"));
        // --- Ustawienie tekstu dla popularGenotypesLabel zostało USUNIĘTE ---
    }

    private void bindViewModel() {
        // Sprawdzenia null dla bezpieczeństwa
        if (dayCount != null) dayCount.textProperty().bind(viewModel.dayCountProperty().asString("%d"));
        if (animalCount != null) animalCount.textProperty().bind(viewModel.animalCountProperty().asString("%d"));
        if (plantCount != null) plantCount.textProperty().bind(viewModel.plantCountProperty().asString("%d"));
        if (emptyFieldsCount != null) emptyFieldsCount.textProperty().bind(viewModel.emptyFieldsCountProperty().asString("%d"));
        if (averageEnergy != null) averageEnergy.textProperty().bind(viewModel.averageEnergyProperty().asString("%.1f"));
        if (averageLifespan != null) averageLifespan.textProperty().bind(viewModel.averageLifespanFormattedProperty());
        if (averageChildren != null) averageChildren.textProperty().bind(viewModel.averageChildrenProperty().asString("%.2f"));

        // --- Powiązania dla popularGenotypes i Tooltipa zostały USUNIĘTE ---
    }
}