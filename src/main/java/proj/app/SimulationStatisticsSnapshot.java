package proj.app;

import java.util.List;
import java.util.Map;
import java.util.Collections; // Import dla pustych kolekcji
import java.util.Objects;   // Import dla Objects.requireNonNullElse i requireNonNull

/**
 * Niezmienny rekord (immutable record) przechowujący migawkę kluczowych statystyk symulacji
 * w określonym momencie (dniu). Używany do wyświetlania statystyk i potencjalnego zapisu
 * podsumowań dziennych lub ręcznych migawek.
 * <p>
 * Rekordy w Javie zapewniają automatycznie generowane:
 * <ul>
 *     <li>Prywatne, finalne pola dla każdego komponentu.</li>
 *     <li>Publiczne metody dostępowe (gettery) dla każdego komponentu (np. {@code day()}).</li>
 *     <li>Konstruktor kanoniczny przyjmujący wszystkie komponenty.</li>
 *     <li>Implementacje metod {@code equals()}, {@code hashCode()} i {@code toString()} oparte na komponentach.</li>
 * </ul>
 *
 * @param day              Numer dnia symulacji, w którym zrobiono migawkę (nieujemny).
 * @param animalCount      Całkowita liczba żywych zwierząt (nieujemna).
 * @param plantCount       Całkowita liczba roślin na mapie (nieujemna).
 * @param averageEnergy    Średni poziom energii wszystkich żywych zwierząt.
 * @param averageLifespan  Średnia długość życia (w dniach) zwierząt, które już umarły.
 * @param averageChildren  Średnia liczba dzieci wyprodukowanych przez żywe zwierzęta.
 * @param emptyFieldsCount Liczba pól mapy niezajętych przez zwierzęta, rośliny lub wodę (nieujemna).
 * @param genotypeCounts   Niemodyfikowalna mapa, gdzie klucze to sformatowane ciągi genotypów, a wartości to liczba zwierząt z danym genotypem.
 * @param configName       Nazwa konfiguracji symulacji, do której należy ta migawka (nie null).
 * @param topGenotypes     Niemodyfikowalna lista N najczęściej występujących ciągów genotypów w momencie tworzenia migawki.
 */
public record SimulationStatisticsSnapshot(
        int day,
        int animalCount,
        int plantCount,
        double averageEnergy,
        double averageLifespan,
        double averageChildren,
        int emptyFieldsCount,
        Map<String, Integer> genotypeCounts, // Klucz: sformatowany genotyp, Wartość: liczba zwierząt
        String configName,
        List<String> topGenotypes // Lista topowych genotypów (String)
) {
    /**
     * Konstruktor kanoniczny zapewniający, że przekazane kolekcje (mapa i lista)
     * są przechowywane jako niemodyfikowalne kopie oraz wykonuje podstawową walidację pól.
     * To chroni wewnętrzny stan rekordu przed modyfikacjami z zewnątrz po utworzeniu obiektu.
     *
     * @throws NullPointerException jeśli configName jest null.
     * @throws IllegalArgumentException jeśli wartości liczbowe (dzień, liczniki) są ujemne.
     */
    public SimulationStatisticsSnapshot {
        // Zapewnienie niezmienności dla mapy i listy przez utworzenie kopii.
        // Objects.requireNonNullElse zapewnia, że jeśli przekazano null, użyta zostanie pusta kolekcja.
        genotypeCounts = Map.copyOf(Objects.requireNonNullElse(genotypeCounts, Collections.emptyMap()));
        topGenotypes = List.copyOf(Objects.requireNonNullElse(topGenotypes, Collections.emptyList()));

        // Podstawowa walidacja pól
        if (day < 0) throw new IllegalArgumentException("Day cannot be negative");
        if (animalCount < 0) throw new IllegalArgumentException("Animal count cannot be negative");
        if (plantCount < 0) throw new IllegalArgumentException("Plant count cannot be negative");
        if (emptyFieldsCount < 0) throw new IllegalArgumentException("Empty fields count cannot be negative");
        Objects.requireNonNull(configName, "Configuration name cannot be null");

        // Opcjonalna walidacja dla średnich (np. czy nie są NaN)
        // if (Double.isNaN(averageEnergy)) throw new IllegalArgumentException("Average energy cannot be NaN");
        // if (Double.isNaN(averageLifespan)) throw new IllegalArgumentException("Average lifespan cannot be NaN");
        // if (Double.isNaN(averageChildren)) throw new IllegalArgumentException("Average children count cannot be NaN");
        // Ujemne średnie mogą być technicznie możliwe jeśli np. lifespan jest liczony z błędem,
        // ale zazwyczaj oczekujemy wartości >= 0.
    }

    // Rekord automatycznie generuje metody dostępowe (gettery) dla wszystkich pól, np.:
    // public int day() { return day; }
    // public int animalCount() { return animalCount; }
    // public Map<String, Integer> genotypeCounts() { return genotypeCounts; }
    // public List<String> topGenotypes() { return topGenotypes; }
    // ... itd.

    // Rekord automatycznie generuje metody equals(), hashCode() i toString().
    // Domyślna implementacja toString() będzie zawierać wszystkie pola.
}