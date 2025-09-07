// Plik: proj/app/controllers/SelectedAnimalPanelController.java
package proj.app.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
// Import usunięty: import javafx.scene.control.TitledPane;
import proj.app.services.IMessageService;
import proj.app.viewmodels.SelectedAnimalViewModel;

import java.util.Objects;

/**
 * Kontroler dla panelu szczegółów wybranego zwierzęcia (SelectedAnimalPanel.fxml).
 * Odpowiada za powiązanie elementów UI (etykiet) z właściwościami
 * dostarczonymi przez {@link SelectedAnimalViewModel}.
 */
public class SelectedAnimalPanelController {

    //<editor-fold desc="FXML Fields - Elementy UI w panelu">
    // Zamiast TitledPane, teraz jest Label dla tytułu
    @FXML private Label panelTitleLabel;
    @FXML private Label selectedAnimalEnergyLabel;
    @FXML private Label selectedAnimalEnergy;
    @FXML private Label selectedAnimalAgeLabel;
    @FXML private Label selectedAnimalAge;
    @FXML private Label selectedAnimalChildrenLabel;
    @FXML private Label selectedAnimalChildren;
    @FXML private Label selectedAnimalPlantsEatenLabel;
    @FXML private Label selectedAnimalPlantsEaten;
    @FXML private Label selectedAnimalGenotypeLabel;
    @FXML private Label selectedAnimalGenotype;
    @FXML private Label selectedAnimalActiveGeneLabel;
    @FXML private Label selectedAnimalActiveGene;
    @FXML private Label selectedAnimalDescendantsLabel;
    @FXML private Label selectedAnimalDescendants;
    @FXML private Label selectedAnimalDeathDateLabel;
    @FXML private Label selectedAnimalDeathDate;
    //</editor-fold>

    // Wstrzyknięte zależności (ustawiane przez SimulationWindowController)
    private SelectedAnimalViewModel viewModel;
    private IMessageService messageService;
    private String placeholder; // Lokalna kopia placeholdera dla wydajności

    /**
     * Inicjalizuje kontroler (wywoływane automatycznie przez FXML loader).
     * Na tym etapie zależności (ViewModel, MessageService) nie są jeszcze dostępne.
     */
    @FXML
    public void initialize() {
        // Minimalna konfiguracja, jeśli potrzebna, np. style, które nie zależą od danych.
        // Ustawienie tekstów i powiązań nastąpi w initializeController.
    }

    /**
     * Metoda do wywołania przez nadrzędny kontroler (`SimulationWindowController`)
     * w celu przekazania niezbędnych zależności (ViewModel, MessageService)
     * i dokończenia inicjalizacji (ustawienie tekstów i powiązań).
     *
     * @param viewModel      Instancja {@link SelectedAnimalViewModel}. Nie może być null.
     * @param messageService Instancja {@link IMessageService}. Nie może być null.
     */
    public void initializeController(SelectedAnimalViewModel viewModel, IMessageService messageService) {
        this.viewModel = Objects.requireNonNull(viewModel, "SelectedAnimalViewModel cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");
        // Pobierz i zapisz placeholder raz, aby uniknąć wielokrotnego odczytu z resource bundle
        this.placeholder = messageService.getMessage("placeholder.selected.none");

        // Teraz można ustawić teksty i powiązać dane
        setUIText();
        bindViewModel();
    }

    /**
     * Ustawia statyczne teksty etykiet w panelu, używając {@link IMessageService}.
     * Wywoływana po otrzymaniu messageService w `initializeController`.
     */
    private void setUIText() {
        // Ustaw tytuł panelu (teraz jako Label)
        panelTitleLabel.setText(messageService.getMessage("sim.titledPane.selectedAnimal")); // Użyj starego klucza lub dodaj nowy

        // Ustaw teksty pozostałych etykiet opisowych
        selectedAnimalEnergyLabel.setText(messageService.getMessage("sim.label.selected.energy"));
        selectedAnimalAgeLabel.setText(messageService.getMessage("sim.label.selected.age"));
        selectedAnimalChildrenLabel.setText(messageService.getMessage("sim.label.selected.children"));
        selectedAnimalPlantsEatenLabel.setText(messageService.getMessage("sim.label.selected.plantsEaten"));
        selectedAnimalGenotypeLabel.setText(messageService.getMessage("sim.label.selected.genotype"));
        selectedAnimalActiveGeneLabel.setText(messageService.getMessage("sim.label.selected.activeGene"));
        selectedAnimalDescendantsLabel.setText(messageService.getMessage("sim.label.selected.descendants"));
        selectedAnimalDeathDateLabel.setText(messageService.getMessage("sim.label.selected.deathDate"));
    }

    /**
     * Wiąże dynamiczne elementy UI (etykiety pokazujące wartości)
     * z odpowiednimi właściwościami w {@link SelectedAnimalViewModel}.
     * Wywoływana po otrzymaniu viewModel w `initializeController`.
     */
    private void bindViewModel() {
        // Powiąż etykiety z wartościami, używając pomocniczej metody do obsługi placeholdera
        selectedAnimalEnergy.textProperty().bind(createSelectedAnimalBinding(viewModel.energyProperty()));
        selectedAnimalAge.textProperty().bind(createSelectedAnimalBinding(viewModel.ageProperty(), "unit.days")); // Dodaj jednostkę
        selectedAnimalChildren.textProperty().bind(createSelectedAnimalBinding(viewModel.childrenMadeProperty()));
        selectedAnimalPlantsEaten.textProperty().bind(createSelectedAnimalBinding(viewModel.plantsEatenProperty()));
        selectedAnimalGenotype.textProperty().bind(viewModel.genotypeProperty()); // Genotyp jest już stringiem z obsługą placeholdera w ViewModel
        selectedAnimalActiveGene.textProperty().bind(createSelectedAnimalBinding(viewModel.activeGeneIndexProperty()));
        selectedAnimalDescendants.textProperty().bind(createSelectedAnimalBinding(viewModel.descendantsCountProperty()));
        selectedAnimalDeathDate.textProperty().bind(viewModel.deathDateProperty()); // Data śmierci jest już stringiem z obsługą placeholdera w ViewModel
    }

    /**
     * Tworzy powiązanie `StringBinding` dla właściwości `ReadOnlyIntegerProperty`.
     * Automatycznie pokazuje zapisany placeholder (`this.placeholder`), gdy
     * `viewModel.isSelectedProperty()` jest `false`, w przeciwnym razie pokazuje
     * wartość liczbową z opcjonalnym sufiksem (pobranym z `IMessageService`).
     *
     * @param property  Właściwość typu Integer do powiązania.
     * @param suffixKey Klucz w `messages.properties` dla opcjonalnego sufiksu (np. "unit.days"), lub null/pusty.
     * @return Obiekt `StringBinding`.
     */
    private StringBinding createSelectedAnimalBinding(ReadOnlyIntegerProperty property, String suffixKey) {
        // Pobierz sufiks tylko raz, jeśli klucz jest podany
        String suffix = (suffixKey != null && !suffixKey.isEmpty())
                ? " " + messageService.getMessage(suffixKey)
                : ""; // Pusty sufiks, jeśli brak klucza

        // Utwórz powiązanie, które zależy od stanu 'isSelected' w ViewModelu
        return Bindings.createStringBinding(
                () -> viewModel.isSelectedProperty().get()
                        ? String.valueOf(property.get()) + suffix // Pokaż wartość + sufiks, gdy wybrane
                        : placeholder, // Pokaż placeholder, gdy nic nie jest wybrane
                viewModel.isSelectedProperty(), property // Zależności powiązania
        );
    }

    /**
     * Przeciążona wersja `createSelectedAnimalBinding` bez sufiksu.
     *
     * @param property Właściwość typu Integer do powiązania.
     * @return Obiekt `StringBinding`.
     */
    private StringBinding createSelectedAnimalBinding(ReadOnlyIntegerProperty property) {
        return createSelectedAnimalBinding(property, null); // Wywołaj główną metodę bez sufiksu
    }
}