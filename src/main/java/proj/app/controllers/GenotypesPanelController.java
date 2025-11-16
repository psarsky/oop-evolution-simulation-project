package proj.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import proj.app.services.IMessageService;
import proj.app.viewmodels.StatisticsViewModel; // Potrzebuje dostępu do danych genotypów

import java.util.Objects;

public class GenotypesPanelController {

    @FXML private Label popularGenotypesLabel;
    @FXML private TextArea popularGenotypes;

    private StatisticsViewModel statisticsViewModel; // ViewModel z danymi
    private IMessageService messageService;

    @FXML
    public void initialize() {
        popularGenotypes.setEditable(false);
        popularGenotypes.setWrapText(true);
        Tooltip tt = new Tooltip();
        Tooltip.install(popularGenotypes, tt);
    }

    public void initializeController(StatisticsViewModel viewModel, IMessageService messageService) {
        this.statisticsViewModel = Objects.requireNonNull(viewModel, "StatisticsViewModel cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");
        setUIText();
        bindViewModel();
    }

    private void setUIText() {
        popularGenotypesLabel.setText(messageService.getMessage("sim.label.popularGenotypes"));
    }

    private void bindViewModel() {
        popularGenotypes.textProperty().bind(statisticsViewModel.popularGenotypesTextProperty());
        Tooltip tt = popularGenotypes.getTooltip();
        if (tt != null) {
            tt.textProperty().bind(statisticsViewModel.popularGenotypesTextProperty());
        }
    }
}