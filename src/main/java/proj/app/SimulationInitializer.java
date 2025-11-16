package proj.app;

import com.google.gson.Gson;
import javafx.scene.canvas.Canvas;
import javafx.stage.Window;
import proj.app.render.MapRenderer;
import proj.app.services.IAlertService;
import proj.app.services.IFileSaveService;
import proj.app.services.IMessageService; // Dodano import messageService
import proj.app.services.JavaFXFileSaveService;
import proj.app.state.SimulationRenderSnapshot; // Import nowego typu migawki
import proj.app.state.SimulationStateProducer;
import proj.app.state.SimulationStateQueue;
import proj.app.statistics.StatisticsCalculator; // Import kalkulatora
import proj.app.viewmodels.SelectedAnimalViewModel;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

import java.util.Objects;

/**
 * Odpowiada za tworzenie i inicjalizację wszystkich kluczowych komponentów
 * wymaganych do uruchomienia i zarządzania pojedynczą instancją symulacji
 * w kontekście interfejsu użytkownika. Hermetyzuje złożoność konfiguracji
 * i wstrzykuje zależności (jak Gson, IAlertService) do tworzonych komponentów.
 */
public class SimulationInitializer {

    private final Gson gson;
    private final IAlertService alertService;
    private final IMessageService messageService; // Dodano messageService

    /**
     * Konstruuje SimulationInitializer.
     *
     * @param gson         Instancja {@link Gson}. Nie może być null.
     * @param alertService Instancja {@link IAlertService}. Nie może być null.
     * @param messageService Instancja {@link IMessageService}. Nie może być null.
     * @throws NullPointerException jeśli którykolwiek argument jest null.
     */
    public SimulationInitializer(Gson gson, IAlertService alertService, IMessageService messageService) { // Dodano messageService
        this.gson = Objects.requireNonNull(gson, "Gson instance cannot be null");
        this.alertService = Objects.requireNonNull(alertService, "AlertService cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null"); // Zapisz messageService
    }

    /**
     * Tworzy i konfiguruje wszystkie podstawowe komponenty symulacji.
     *
     * @param config                 {@link SimulationProperties}. Nie może być null.
     * @param canvas                 {@link Canvas} do renderowania. Nie może być null.
     * @param ownerWindow            Nadrzędne {@link Window}. Nie może być null.
     * @param selectedAnimalViewModel {@link SelectedAnimalViewModel}. Nie może być null.
     * @return Rekord {@link SimulationComponents} z utworzonymi komponentami.
     * @throws NullPointerException jeśli którykolwiek argument non-null jest null.
     */
    public SimulationComponents initializeSimulationComponents(
            SimulationProperties config,
            Canvas canvas,
            Window ownerWindow,
            SelectedAnimalViewModel selectedAnimalViewModel)
    {
        Objects.requireNonNull(config, "SimulationProperties cannot be null");
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        Objects.requireNonNull(ownerWindow, "Owner Window cannot be null");
        Objects.requireNonNull(selectedAnimalViewModel, "SelectedAnimalViewModel cannot be null");

        IFileSaveService fileSaveService = new JavaFXFileSaveService(ownerWindow);
        Simulation simulation = SimulationFactory.createSimulation(config);
        SimulationEngine simulationEngine = new SimulationEngine(simulation, config);

        // --- Inicjalizacja zrefaktoryzowanych komponentów statystyk i stanu ---
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
        // Utwórz kolejkę dla migawek renderowania
        SimulationStateQueue<SimulationRenderSnapshot> stateQueue =
                new SimulationStateQueue<>(AppConstants.MAX_STATE_QUEUE_SIZE);
        // Utwórz manager statystyk, wstrzykując kalkulator, alertService i messageService
        StatisticsManager statisticsManager = new StatisticsManager(
                simulation, config, fileSaveService, this.gson,
                statisticsCalculator, this.alertService, this.messageService // Przekaż messageService
        );
        // Utwórz producenta stanu renderowania
        SimulationStateProducer stateProducer = new SimulationStateProducer(
                simulation, stateQueue, statisticsManager, selectedAnimalViewModel
        );

        // Utwórz renderer mapy
        MapRenderer mapRenderer = new MapRenderer(canvas, config);

        // Zwróć wszystkie komponenty
        return new SimulationComponents(
                simulation,
                simulationEngine,
                statisticsManager, // Zrefaktoryzowany manager
                stateProducer,     // Zaktualizowany producent
                stateQueue,        // Kolejka typu SimulationRenderSnapshot
                mapRenderer
        );
    }
}