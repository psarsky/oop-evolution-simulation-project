// Plik: proj/app/services/JsonFileStatisticsPersistence.java
package proj.app.services;

import com.google.gson.Gson;
import proj.app.AppConstants; // Użyjemy tylko tych naprawdę globalnych
import proj.app.SimulationStatisticsSnapshot;
import proj.simulation.SimulationProperties; // Potrzebne do nazwy configu

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Implementacja {@link IStatisticsPersistenceService} zapisująca statystyki
 * w formacie JSON do plików w dedykowanym katalogu dla danego uruchomienia symulacji.
 */
public class JsonFileStatisticsPersistence implements IStatisticsPersistenceService {

    // Stałe specyficzne dla tej implementacji (przeniesione z AppConstants)
    private static final String STATS_DAILY_FILENAME_FORMAT = "day_%05d.json";
    private static final String STATS_SNAPSHOT_FILENAME_FORMAT = "snapshot_%s_day%d_%s.json";
    private static final DateTimeFormatter DIR_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter SNAPSHOT_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String JSON_FILE_EXTENSION = "*.json";
    private static final String JSON_FILE_DESCRIPTION = "JSON files (*.json)";

    private final Gson gson;
    private final IFileSaveService fileSaveService;
    private final IMessageService messageService; // Do opisów w FileChooser
    private final SimulationProperties simProps;
    private final Path statisticsDirectory; // Ścieżka do katalogu specyficznego dla uruchomienia

    /**
     * Konstruuje serwis persystencji. Tworzy niezbędne katalogi.
     *
     * @param gson            Instancja Gson do serializacji.
     * @param fileSaveService Serwis do wyboru pliku przez użytkownika.
     * @param messageService  Serwis do pobierania wiadomości (np. opisu pliku).
     * @param simProps        Właściwości symulacji (dla nazwy konfigu).
     * @param baseStatsDir    Główny katalog statystyk (np. "statistics").
     * @throws IOException Jeśli tworzenie katalogów zawiedzie.
     */
    public JsonFileStatisticsPersistence(Gson gson, IFileSaveService fileSaveService, IMessageService messageService,
                                         SimulationProperties simProps, String baseStatsDir) throws IOException {
        this.gson = Objects.requireNonNull(gson, "Gson cannot be null");
        this.fileSaveService = Objects.requireNonNull(fileSaveService, "FileSaveService cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "MessageService cannot be null");
        this.simProps = Objects.requireNonNull(simProps, "SimulationProperties cannot be null");

        this.statisticsDirectory = createStatisticsDirectory(baseStatsDir, simProps.getConfigName());
        System.out.println("Statistics Persistence initialized. Data will be saved to: " + statisticsDirectory.toAbsolutePath());
    }

    /**
     * Tworzy strukturę katalogów dla zapisu statystyk.
     * np. baseStatsDir/ConfigName_Timestamp/
     */
    private Path createStatisticsDirectory(String baseDirName, String configName) throws IOException {
        try {
            Path mainStatsDirPath = Paths.get(baseDirName);
            // Utwórz główny katalog statystyk, jeśli nie istnieje
            Files.createDirectories(mainStatsDirPath);

            String timestamp = LocalDateTime.now().format(DIR_TIMESTAMP_FORMATTER);
            // Utwórz bezpieczną nazwę katalogu z nazwy konfiguracji
            String safeConfigName = configName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String runDirName = safeConfigName + "_" + timestamp;
            Path runSpecificDirPath = mainStatsDirPath.resolve(runDirName);

            // Utwórz katalog specyficzny dla uruchomienia
            Files.createDirectories(runSpecificDirPath);
            return runSpecificDirPath;
        } catch (IOException e) {
            System.err.println("FATAL: Failed to create statistics directory structure in " + baseDirName);
            throw e; // Rzuć wyjątek dalej, aby manager mógł zareagować
        }
    }

    @Override
    public void saveDailySnapshot(SimulationStatisticsSnapshot snapshot) throws IOException, IllegalStateException {
        Objects.requireNonNull(snapshot, "Snapshot cannot be null for daily save");
        if (statisticsDirectory == null) {
            throw new IllegalStateException("Statistics persistence service not initialized properly (directory is null).");
        }

        String filename = String.format(STATS_DAILY_FILENAME_FORMAT, snapshot.day());
        File outputFile = statisticsDirectory.resolve(filename).toFile();

        try (Writer writer = new FileWriter(outputFile)) {
            gson.toJson(snapshot, writer);
            // Logowanie sukcesu można przenieść do StatisticsManager, jeśli chcemy
            // System.out.println("Daily statistics saved: " + outputFile.getName());
        } catch (IOException e) {
            System.err.println("Error saving daily statistics for day " + snapshot.day() + " to " + outputFile.getPath());
            throw e; // Rzuć wyjątek, aby manager mógł go obsłużyć (np. logując, pokazując alert)
        }
    }

    @Override
    public SaveResult saveManualSnapshot(SimulationStatisticsSnapshot snapshot) throws IllegalStateException {
        Objects.requireNonNull(snapshot, "Snapshot cannot be null for manual save");
        if (statisticsDirectory == null) {
            throw new IllegalStateException("Statistics persistence service not initialized properly (directory is null).");
        }

        String timestamp = LocalDateTime.now().format(SNAPSHOT_TIMESTAMP_FORMATTER);
        String defaultFilename = String.format(STATS_SNAPSHOT_FILENAME_FORMAT,
                simProps.getConfigName(), snapshot.day(), timestamp);

        // Użyj messageService do pobrania opisu i rozszerzenia
        String description = messageService.getFormattedMessage("filechooser.json.description", JSON_FILE_DESCRIPTION); // Przykładowy klucz
        String extension = messageService.getFormattedMessage("filechooser.json.extension", JSON_FILE_EXTENSION);     // Przykładowy klucz

        // Dodaj klucze do messages.properties:
        // filechooser.json.description=JSON Statistics Files (*.json)
        // filechooser.json.extension=*.json

        File file = fileSaveService.selectSaveFile(defaultFilename, description, extension);

        if (file != null) {
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(snapshot, writer);
                System.out.println("Manual snapshot saved to: " + file.getAbsolutePath());
                return SaveResult.SUCCESS;
            } catch (IOException e) {
                System.err.println("Error writing manual snapshot to file: " + file.getAbsolutePath());
                // Nie rzucamy wyjątku, ale zwracamy błąd, aby manager mógł poinformować użytkownika
                return SaveResult.FAILED_IO;
            } catch (Exception e) {
                System.err.println("Unexpected error during manual snapshot file write: " + e.getMessage());
                return SaveResult.FAILED_OTHER;
            }
        } else {
            System.out.println("Manual snapshot save cancelled by user.");
            return SaveResult.CANCELLED;
        }
    }

    @Override
    public String getStatisticsDirectoryPath() {
        return (statisticsDirectory != null) ? statisticsDirectory.toAbsolutePath().toString() : null;
    }
}