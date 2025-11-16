// Plik: proj/app/StatisticsManager.java
package proj.app;

import com.google.gson.Gson;
import proj.app.services.IAlertService;
import proj.app.services.IFileSaveService;
import proj.app.services.IMessageService;
import proj.app.services.IStatisticsPersistenceService;
import proj.app.services.JsonFileStatisticsPersistence;
import proj.app.statistics.StatisticsCalculator;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Zarządza procesem zbierania i inicjowania zapisu statystyk symulacji.
 * Używa {@link StatisticsCalculator} do generowania danych i deleguje trwałość
 * do {@link IStatisticsPersistenceService}. Zarządza stanem zbierania danych.
 * Używa {@link IAlertService} i {@link IMessageService} do informowania użytkownika.
 */
public class StatisticsManager {
    private final Simulation simulation;
    private final SimulationProperties simProps;
    private final StatisticsCalculator calculator;
    private final IAlertService alertService;
    private final IMessageService messageService;
    private final IStatisticsPersistenceService persistenceService; // Może być null, jeśli inicjalizacja zawiedzie

    // Stan
    private volatile boolean isCollectingData; // Bieżący stan włącznika logowania
    private boolean persistenceInitializedSuccessfully = false; // Czy komponent zapisu się zainicjował?

    private static final String MAIN_STATS_DIR_NAME = AppConstants.STATS_DIR_NAME;

    /**
     * Konstruuje StatisticsManager z wstrzykniętymi zależnościami.
     * ZAWSZE próbuje zainicjalizować serwis persystencji, jeśli zależności są dostępne.
     * Stan początkowy 'isCollectingData' jest ustawiany zgodnie z flagą w konfiguracji.
     *
     * @param simulation      Instancja {@link Simulation}.
     * @param simProps        Obiekt {@link SimulationProperties}.
     * @param fileSaveService Serwis do ręcznego zapisu plików (przekazywany do persistence).
     * @param gson            Instancja Gson do serializacji JSON (przekazywana do persistence).
     * @param calculator      Instancja {@link StatisticsCalculator}.
     * @param alertService    Serwis do wyświetlania powiadomień.
     * @param messageService  Serwis do pobierania zlokalizowanych wiadomości.
     */
    public StatisticsManager(Simulation simulation, SimulationProperties simProps,
                             IFileSaveService fileSaveService, Gson gson,
                             StatisticsCalculator calculator, IAlertService alertService,
                             IMessageService messageService) {
        this.simulation = Objects.requireNonNull(simulation, "Simulation cannot be null");
        this.simProps = Objects.requireNonNull(simProps, "SimulationProperties cannot be null");
        this.calculator = Objects.requireNonNull(calculator, "StatisticsCalculator cannot be null");
        this.alertService = Objects.requireNonNull(alertService, "AlertService cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");

        // 1. Ustaw początkowy stan włącznika zgodnie z konfiguracją
        this.isCollectingData = simProps.getSaveStatisticsFlag();

        // 2. ZAWSZE spróbuj zainicjalizować komponent zapisu
        IStatisticsPersistenceService tempPersistenceService = null;
        try {
            System.out.println("Initializing statistics persistence component..."); // Log
            tempPersistenceService = new JsonFileStatisticsPersistence(
                    Objects.requireNonNull(gson),
                    Objects.requireNonNull(fileSaveService),
                    this.messageService,
                    this.simProps,
                    MAIN_STATS_DIR_NAME
            );
            this.persistenceInitializedSuccessfully = true; // Oznacz sukces inicjalizacji
            System.out.println("Statistics persistence initialized successfully. Path: " + tempPersistenceService.getStatisticsDirectoryPath()); // Log

        } catch (IOException e) {
            this.persistenceInitializedSuccessfully = false; // Inicjalizacja nieudana
            String specificError = e.getMessage() != null ? e.getMessage() : "Unknown I/O Error";
            System.err.println("FATAL: Failed to initialize statistics directory: " + specificError);
            e.printStackTrace();
            // Pokaż alert, ale tylko jeśli użytkownik *chciał* zapisywać statystyki
            if (simProps.getSaveStatisticsFlag()) {
                alertService.showAlert(IAlertService.AlertType.WARNING,
                        messageService.getMessage("warning.title"),
                        messageService.getMessage("error.stats.init.io.header"),
                        messageService.getFormattedMessage("error.stats.init.io.content", specificError)
                );
            } else {
                System.out.println("Statistics saving was disabled in config, directory creation failed silently: " + specificError);
            }
            // Mimo błędu inicjalizacji, symulacja może kontynuować bez zapisu statystyk

        } catch (Exception e) {
            this.persistenceInitializedSuccessfully = false; // Inicjalizacja nieudana
            System.err.println("FATAL: Unexpected error initializing statistics persistence: " + e.getMessage());
            e.printStackTrace();
            // Pokaż alert, ale tylko jeśli użytkownik *chciał* zapisywać statystyki
            if (simProps.getSaveStatisticsFlag()) {
                alertService.showAlert(IAlertService.AlertType.ERROR,
                        messageService.getMessage("error.title"),
                        messageService.getMessage("error.stats.init.fail")); // Ogólny błąd inicjalizacji
            }
        }
        this.persistenceService = tempPersistenceService; // Przypisz zainicjalizowany serwis (lub null, jeśli błąd)

        // 3. Loguj ostateczny stan
        System.out.println("StatisticsManager initialized. Initial collectingData state: " + this.isCollectingData +
                ", Persistence component available: " + this.persistenceInitializedSuccessfully);
    }


    /**
     * Przełącza stan automatycznego zbierania i zapisywania statystyk dziennych.
     * Informuje użytkownika o zmianie statusu.
     * Nie pozwala włączyć, jeśli inicjalizacja komponentu zapisu zawiodła.
     */
    public void toggleDataCollection() {
        // Sprawdź, czy próba włączenia jest możliwa (inicjalizacja musiała się powieść)
        if (!persistenceInitializedSuccessfully && !isCollectingData) { // Chcemy włączyć (isCollectingData jest false), ale inicjalizacja zawiodła
            alertService.showAlert(IAlertService.AlertType.WARNING,
                    messageService.getMessage("warning.title"),
                    messageService.getMessage("warning.stats.init.failed.cant.enable"));
            return;
        }

        // Jeśli inicjalizacja się powiodła LUB chcemy wyłączyć (isCollectingData jest true), możemy przełączyć
        isCollectingData = !isCollectingData;
        System.out.println("Statistics data collection toggled: " + (isCollectingData ? "On" : "Off"));

        // Informuj użytkownika o nowym stanie
        String statusKey = isCollectingData ? "status.on" : "status.off";
        String status = messageService.getFormattedMessage(statusKey, statusKey.toUpperCase());
        alertService.showAlert(IAlertService.AlertType.INFO,
                messageService.getMessage("info.title"),
                messageService.getFormattedMessage("info.logging.status", status));
    }

    /**
     * Sprawdza, czy automatyczne zbieranie i zapisywanie statystyk jest aktualnie włączone
     * ORAZ czy inicjalizacja komponentu zapisu się powiodła.
     * @return {@code true} jeśli zbieranie danych jest włączone i możliwe, {@code false} w przeciwnym razie.
     */
    public boolean isCollectingData() {
        // Zwraca true tylko jeśli użytkownik chce (isCollectingData) ORAZ komponent jest dostępny
        return isCollectingData && persistenceInitializedSuccessfully;
    }

    /**
     * Generuje bieżącą migawkę statystyk za pomocą wstrzykniętego kalkulatora.
     * @return {@link Optional} zawierający {@link SimulationStatisticsSnapshot}, lub pusty {@code Optional}.
     */
    public Optional<SimulationStatisticsSnapshot> generateCurrentSnapshot() {
        if (simulation == null) {
            return Optional.empty();
        }
        // Nie sprawdzamy już tutaj simulation.getAnimals().isEmpty(),
        // ponieważ StatisticsCalculator może obliczyć migawkę nawet bez zwierząt (np. sam dzień).
        return Optional.ofNullable(calculator.calculateSnapshot(simulation));
    }

    /**
     * Generuje i deleguje zapis dziennej migawki statystyk, *jeśli* zbieranie danych jest włączone
     * i serwis persystencji jest dostępny i poprawnie zainicjalizowany.
     */
    public void generateAndSaveDailyStatisticsIfNeeded() {
        // Użyj isCollectingData(), które sprawdza OBA warunki (włącznik ORAZ sukces inicjalizacji)
        if (!isCollectingData()) {
            return;
        }
        // Dodatkowe sprawdzenie, czy persistenceService nie jest null (na wszelki wypadek)
        if (persistenceService == null){
            System.err.println("Error: generateAndSaveDailyStatisticsIfNeeded called but persistenceService is null.");
            return;
        }

        Optional<SimulationStatisticsSnapshot> snapshotOpt = generateCurrentSnapshot();

        snapshotOpt.ifPresent(snapshot -> {
            try {
                persistenceService.saveDailySnapshot(snapshot);
            } catch (IOException | IllegalStateException e) {
                System.err.println("Error saving daily statistics for day " + snapshot.day() + ": " + e.getMessage());
                // Można dodać alert, ale może być zbyt częsty
            } catch (Exception e) {
                System.err.println("Unexpected error during daily statistics save: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Generuje migawkę statystyk i inicjuje proces ręcznego zapisu przez użytkownika,
     * pod warunkiem, że komponent zapisu został poprawnie zainicjalizowany.
     * @throws IllegalStateException Jeśli komponent zapisu nie jest dostępny.
     */
    public void generateAndSaveSnapshotManually() throws IllegalStateException {
        // Kluczowy warunek: czy komponent zapisu jest dostępny?
        if (!persistenceInitializedSuccessfully) {
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getMessage("error.snapshot.persistence.unavailable"));
            return;
        }
        // Dodatkowe zabezpieczenie
        if (persistenceService == null){
            System.err.println("Error: generateAndSaveSnapshotManually called but persistenceService is null.");
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getMessage("error.stats.component.unavailable"));
            return;
        }

        Optional<SimulationStatisticsSnapshot> snapshotOpt = generateCurrentSnapshot();

        if (snapshotOpt.isEmpty()) {
            System.err.println("Failed to save snapshot manually: Could not generate statistics snapshot.");
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getMessage("error.snapshot.generate"));
            return;
        }

        SimulationStatisticsSnapshot currentSnapshot = snapshotOpt.get();

        try {
            // Wywołaj metodę serwisu persystencji
            IStatisticsPersistenceService.SaveResult result = persistenceService.saveManualSnapshot(currentSnapshot);

            // Poinformuj użytkownika na podstawie wyniku zwróconego przez serwis
            switch (result) {
                case SUCCESS:
                    alertService.showAlert(IAlertService.AlertType.INFO,
                            messageService.getMessage("info.title"),
                            messageService.getMessage("info.snapshot.manual.saved"));
                    break;
                case CANCELLED:
                    // Nie pokazuj alertu przy anulowaniu, to normalne zachowanie
                    // alertService.showAlert(IAlertService.AlertType.INFO,
                    //        messageService.getMessage("info.title"),
                    //        messageService.getMessage("info.snapshot.cancelled"));
                    System.out.println("Manual snapshot save cancelled by user.");
                    break;
                case FAILED_IO:
                    alertService.showAlert(IAlertService.AlertType.ERROR,
                            messageService.getMessage("error.title"),
                            messageService.getMessage("error.snapshot.save.io"));
                    break;
                case FAILED_OTHER:
                    alertService.showAlert(IAlertService.AlertType.ERROR,
                            messageService.getMessage("error.title"),
                            messageService.getMessage("error.snapshot.save.other"));
                    break;
            }
        } catch (IllegalStateException e) { // Błąd powinien być złapany wyżej, ale na wszelki wypadek
            System.err.println("IllegalStateException during manual snapshot save: " + e.getMessage());
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getMessage("error.snapshot.persistence.unavailable"));
            // Nie rzucaj dalej, aby nie zatrzymać aplikacji
        } catch (Exception e) { // Inne nieoczekiwane błędy
            System.err.println("Unexpected error during manual snapshot trigger: " + e.getMessage());
            alertService.showAlert(IAlertService.AlertType.ERROR,
                    messageService.getMessage("error.title"),
                    messageService.getFormattedMessage("error.snapshot.unexpected", e.getMessage()));
            e.printStackTrace();
        }
    }
}