package proj.app;

import com.google.gson.Gson;
import javafx.scene.canvas.Canvas;
import javafx.stage.Window;
import proj.app.render.MapRenderer;
import proj.app.services.IFileSaveService;
import proj.app.services.JavaFXFileSaveService;
import proj.app.state.SimulationStateProducer;
import proj.app.state.SimulationStateQueue;
import proj.app.viewmodels.SelectedAnimalViewModel;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

import java.util.Objects;

/**
 * Responsible for creating and initializing all the core components
 * required to run and manage a single simulation instance within the UI context.
 * It encapsulates the setup complexity and injects dependencies like Gson into the
 * components it creates.
 */
public class SimulationInitializer {

    private static final int MAX_STATE_QUEUE_SIZE = 5;
    private final Gson gson; // Injected dependency

    /**
     * Constructs a SimulationInitializer.
     *
     * @param gson The {@link Gson} instance to be injected into components created
     *             during initialization (e.g., {@link StatisticsManager}). Must not be null.
     * @throws NullPointerException if gson is null.
     */
    public SimulationInitializer(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "Gson instance cannot be null");
    }

    /**
     * Creates and configures all core simulation components based on the provided configuration
     * and UI context. This includes the simulation logic, controller, state management,
     * statistics, rendering, and necessary services.
     *
     * @param config                 The {@link SimulationProperties} object defining the simulation's parameters. Must not be null.
     * @param canvas                 The JavaFX {@link Canvas} element where the simulation will be rendered. Must not be null.
     * @param ownerWindow            The parent JavaFX {@link Window} (typically a Stage) used for context-dependent services
     *                               like file choosers ({@link JavaFXFileSaveService}). Must not be null.
     * @param selectedAnimalViewModel The {@link SelectedAnimalViewModel} instance that tracks the user's animal selection
     *                                and is used by the {@link SimulationStateProducer}. Must not be null.
     * @return A {@link SimulationComponents} record containing references to all the newly created and configured
     *         core simulation components (Simulation, SimulationController, StatisticsManager, etc.).
     * @throws NullPointerException if any of the non-nullable parameters (config, canvas, ownerWindow, selectedAnimalViewModel) are null.
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

        // Create context-specific service requiring the owner window
        IFileSaveService fileSaveService = new JavaFXFileSaveService(ownerWindow);

        // Create core simulation logic
        Simulation simulation = SimulationFactory.createSimulation(config);
        SimulationController simulationController = new SimulationController(simulation, config);

        // Create state and statistics components
        SimulationStateQueue stateQueue = new SimulationStateQueue(MAX_STATE_QUEUE_SIZE);
        // Inject the fileSaveService and the injected Gson instance into StatisticsManager
        StatisticsManager statisticsManager = new StatisticsManager(simulation, config, fileSaveService, this.gson);
        SimulationStateProducer stateProducer = new SimulationStateProducer(simulation, stateQueue, statisticsManager, selectedAnimalViewModel);

        // Create UI rendering component
        MapRenderer mapRenderer = new MapRenderer(canvas, config);

        // Package and return the components
        return new SimulationComponents(
                simulation,
                simulationController,
                statisticsManager,
                stateProducer,
                stateQueue,
                mapRenderer
        );
    }
}