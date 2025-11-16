package proj.app.services;

import proj.app.SimulationStatisticsSnapshot;
import java.io.IOException;

/**
 * Interfejs definiujący kontrakt dla serwisu odpowiedzialnego za
 * trwałe zapisywanie migawek statystyk symulacji.
 */
public interface IStatisticsPersistenceService {

    /**
     * Enum określający wynik operacji zapisu manualnej migawki.
     */
    enum SaveResult {
        SUCCESS,
        CANCELLED, // Użytkownik anulował dialog zapisu
        FAILED_IO, // Błąd we/wy podczas zapisu pliku
        FAILED_OTHER // Inny, nieoczekiwany błąd
    }

    /**
     * Zapisuje dzienną migawkę statystyk do pliku w predefiniowanym
     * katalogu specyficznym dla danego uruchomienia symulacji.
     * Nazwa pliku jest generowana automatycznie na podstawie dnia symulacji.
     *
     * @param snapshot Migawka statystyk do zapisania.
     * @throws IOException Jeśli wystąpi błąd podczas zapisu pliku.
     * @throws IllegalStateException Jeśli serwis nie został poprawnie zainicjalizowany (np. problem z katalogiem).
     */
    void saveDailySnapshot(SimulationStatisticsSnapshot snapshot) throws IOException, IllegalStateException;

    /**
     * Inicjuje proces zapisu manualnej migawki. Używa {@link IFileSaveService}
     * do uzyskania od użytkownika ścieżki zapisu, a następnie zapisuje
     * migawkę do wybranego pliku.
     *
     * @param snapshot Migawka statystyk do zapisania.
     * @return Enum {@link SaveResult} wskazujący wynik operacji (sukces, anulowanie, błąd).
     * @throws IllegalStateException Jeśli serwis nie został poprawnie zainicjalizowany.
     */
    SaveResult saveManualSnapshot(SimulationStatisticsSnapshot snapshot) throws IllegalStateException;

    /**
     * Zwraca ścieżkę do katalogu, w którym zapisywane są statystyki
     * dla tego konkretnego uruchomienia symulacji.
     *
     * @return Ścieżka do katalogu statystyk lub null, jeśli inicjalizacja zawiodła.
     */
    String getStatisticsDirectoryPath();
}