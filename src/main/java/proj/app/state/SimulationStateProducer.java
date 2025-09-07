package proj.app.state;

import proj.app.AppConstants;
import proj.app.StatisticsManager;
import proj.app.SimulationStatisticsSnapshot;
import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.WaterWorld;
import proj.simulation.Simulation;
import proj.app.viewmodels.SelectedAnimalViewModel;
import proj.util.Vector2d;

import java.util.*;

/**
 * Zadanie działające w tle (implementujące {@link Runnable}), odpowiedzialne za okresowe
 * tworzenie niezmiennych migawek stanu symulacji specjalnie dla celów **renderowania**
 * ({@link SimulationRenderSnapshot}).
 * Odpytuje bieżący stan {@link Simulation}, pobiera dane mapy, informacje o wybranym
 * zwierzęciu z {@link SelectedAnimalViewModel} oraz listę dominujących genotypów
 * z {@link StatisticsManager} (poprzez wygenerowanie pełnej migawki statystyk).
 * Tworzy kopie obronne danych tam, gdzie to konieczne, i umieszcza wynikową migawkę
 * renderowania we współdzielonej kolejce {@link SimulationStateQueue} do konsumpcji
 * przez wątek UI (np. {@link proj.app.render.SimulationRenderer}).
 */
public class SimulationStateProducer implements Runnable {

    private final Simulation simulation;
    private final SimulationStateQueue<SimulationRenderSnapshot> stateQueue; // Kolejka migawek do renderowania
    private final StatisticsManager statisticsManager; // Do pobierania danych statystycznych (np. top genotypów)
    private final SelectedAnimalViewModel selectedAnimalViewModel; // Do pobierania info o wybranym zwierzęciu
    private volatile boolean running = true; // Flaga kontrolująca pętlę producenta

    /**
     * Konstruuje {@code SimulationStateProducer}.
     *
     * @param simulation            Instancja {@link Simulation} dostarczająca bieżący stan symulacji. Nie może być null.
     * @param stateQueue            Wątkowo-bezpieczna {@link SimulationStateQueue} dla {@link SimulationRenderSnapshot},
     *                              do której będą dodawane utworzone migawki. Nie może być null.
     * @param statisticsManager     Instancja {@link StatisticsManager} używana do uzyskania aktualnych danych statystycznych,
     *                              z których pobierane są np. topowe genotypy. Nie może być null.
     * @param selectedAnimalViewModel Instancja {@link SelectedAnimalViewModel} używana do pobrania referencji
     *                                do aktualnie wybranego zwierzęcia. Nie może być null.
     * @throws NullPointerException jeśli którykolwiek parametr jest null.
     */
    public SimulationStateProducer(Simulation simulation,
                                   SimulationStateQueue<SimulationRenderSnapshot> stateQueue,
                                   StatisticsManager statisticsManager,
                                   SelectedAnimalViewModel selectedAnimalViewModel) {
        this.simulation = Objects.requireNonNull(simulation, "Simulation cannot be null");
        this.stateQueue = Objects.requireNonNull(stateQueue, "SimulationStateQueue<SimulationRenderSnapshot> cannot be null");
        this.statisticsManager = Objects.requireNonNull(statisticsManager, "StatisticsManager cannot be null");
        this.selectedAnimalViewModel = Objects.requireNonNull(selectedAnimalViewModel, "SelectedAnimalViewModel cannot be null");
    }

    /**
     * Sygnalizuje wątkowi producenta, aby zakończył pętlę wykonania w sposób kontrolowany.
     * Wątek zakończy bieżącą iterację (jeśli jest w trakcie) i następnie się zatrzyma.
     */
    public void stop() {
        this.running = false;
        System.out.println("Simulation State Producer stop requested.");
    }

    /**
     * Główna pętla wykonawcza wątku producenta.
     * Dopóki flaga {@code running} jest ustawiona na true:
     * 1. Wywołuje {@link #createCurrentRenderSnapshot()} w celu wygenerowania migawki stanu do renderowania.
     * 2. Umieszcza migawkę w kolejce {@link SimulationStateQueue}.
     * 3. Usypia na interwał określony przez {@link AppConstants#SIMULATION_STATE_PRODUCER_INTERVAL_MS}.
     * Obsługuje {@link InterruptedException} dla kontrolowanego zakończenia i łapie inne potencjalne wyjątki.
     */
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("Simulation State Producer thread [" + threadName + "] started.");
        while (running) {
            try {
                SimulationRenderSnapshot renderSnapshot = createCurrentRenderSnapshot();

                if (renderSnapshot != null) {
                    stateQueue.enqueue(renderSnapshot);
                } else {
                    System.err.println("State Producer: Failed to create render snapshot, retrying after delay.");
                    Thread.sleep(AppConstants.SIMULATION_STATE_PRODUCER_INTERVAL_MS * 2);
                    continue;
                }

                Thread.sleep(AppConstants.SIMULATION_STATE_PRODUCER_INTERVAL_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                System.out.println("Simulation State Producer thread [" + threadName + "] interrupted.");
            } catch (Exception e) {
                if (running) {
                    System.err.println("Error in Simulation State Producer loop ("+ threadName +"): " + e.getMessage());
                    e.printStackTrace();
                    try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); running = false; }
                }
            }
        }
        System.out.println("Simulation State Producer thread [" + threadName + "] finished.");
    }

    /**
     * Tworzy niezmienną migawkę bieżącego stanu symulacji przeznaczoną do RENDEROWANIA.
     * Pobiera dane mapy z {@link Simulation}, wybrane zwierzę z {@link SelectedAnimalViewModel},
     * a listę dominujących genotypów uzyskuje poprzez wygenerowanie pełnej migawki statystyk
     * za pomocą {@link StatisticsManager}. Metoda ta dba o bezpieczeństwo wątkowe poprzez
     * synchronizację dostępu do mapy i tworzenie kopii obronnych kolekcji.
     *
     * @return Obiekt {@link SimulationRenderSnapshot} reprezentujący stan do narysowania,
     *         lub {@code null} jeśli mapa symulacji jest niedostępna lub wystąpił inny błąd krytyczny.
     */
    private SimulationRenderSnapshot createCurrentRenderSnapshot() {
        AbstractWorldMap map = simulation.getMap();
        if (map == null) {
            // Logowanie ostrzeżenia, jeśli mapa jest null
            // System.err.println("State Producer Warning: Map is not available for creating render snapshot.");
            return null; // Mapa nie jest gotowa
        }

        // 1. Pobierz najnowszą pełną migawkę statystyk z Managera Statystyk.
        Optional<SimulationStatisticsSnapshot> statsSnapshotOpt = statisticsManager.generateCurrentSnapshot();

        // 2. Wyciągnij listę topowych genotypów z migawki statystyk.
        List<String> topGenotypes = statsSnapshotOpt
                .map(SimulationStatisticsSnapshot::topGenotypes)
                .orElse(Collections.emptyList());

        // 3. Pobierz referencję do aktualnie wybranego zwierzęcia z ViewModelu.
        Animal currentSelectedAnimal = selectedAnimalViewModel.getCurrentAnimalReference();

        // 4. Skopiuj dane mapy (zwierzęta, rośliny, woda) w sposób bezpieczny wątkowo.
        Map<Vector2d, ?> waterFieldsCopy = null;
        Map<Vector2d, List<Animal>> animalsCopy;
        Map<Vector2d, Plant> plantsCopy;

        // Synchronizacja na obiekcie mapy zapewnia spójny odczyt jej stanu.
        synchronized (map) {
            // Skopiuj pola wody, jeśli to WaterWorld
            if (map instanceof WaterWorld waterMap) {
                waterFieldsCopy = new HashMap<>(waterMap.getWaterFields());
            }

            // Stwórz głęboką kopię mapy zwierząt
            animalsCopy = new HashMap<>();
            map.getAnimals().forEach((key, valueList) -> {
                animalsCopy.put(key, valueList != null ? List.copyOf(valueList) : Collections.emptyList());
            });

            // Stwórz kopię mapy roślin
            plantsCopy = new HashMap<>(map.getPlants());
        } // Koniec bloku synchronized(map)

        // 5. Utwórz i zwróć niezmienną migawkę renderowania.
        return new SimulationRenderSnapshot(
                animalsCopy,
                plantsCopy,
                waterFieldsCopy,
                topGenotypes,
                currentSelectedAnimal
        );
    }

    /**
     * Tworzy początkową migawkę stanu symulacji do renderowania.
     *
     * @return Początkowa migawka {@link SimulationRenderSnapshot} lub {@code null}.
     */
    public SimulationRenderSnapshot createInitialSnapshot() {
        System.out.println("Creating initial simulation render snapshot...");
        SimulationRenderSnapshot snapshot = createCurrentRenderSnapshot();
        if (snapshot == null) {
            System.err.println("Failed to create initial render snapshot.");
        }
        return snapshot;
    }
}