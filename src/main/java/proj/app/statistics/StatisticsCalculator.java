package proj.app.statistics;

import proj.app.AppConstants;
import proj.app.GenotypeFormatter;
import proj.app.SimulationStatisticsSnapshot;
import proj.model.elements.Animal;
import proj.model.maps.AbstractWorldMap;
import proj.model.maps.WaterWorld;
import proj.simulation.Simulation;
import proj.simulation.SimulationProperties;
import proj.util.Vector2d;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Odpowiada wyłącznie za obliczanie różnych statystyk symulacji
 * na podstawie jej bieżącego stanu dostarczonego przez obiekt {@link Simulation}.
 * Ta klasa jest zasadniczo bezstanowa i jej metody powinny być wywoływane
 * w sposób bezpieczny wątkowo względem dostępu do obiektu Simulation.
 */
public class StatisticsCalculator {

    // Stała definiująca, ile najpopularniejszych genotypów śledzić.
    private static final int TOP_GENOTYPE_COUNT = AppConstants.TOP_GENOTYPE_COUNT;

    /**
     * Oblicza i zwraca kompletną migawkę statystyk dla bieżącego stanu symulacji.
     * <p>
     * <b>Uwaga dotycząca bezpieczeństwa wątkowego:</b> Ta metoda odczytuje stan
     * obiektu {@code simulation} (listy zwierząt, stan mapy). Wywołujący jest
     * odpowiedzialny za zapewnienie, że dostęp do obiektu {@code simulation}
     * jest bezpieczny w momencie wywołania tej metody (np. poprzez synchronizację
     * lub wywołanie z wątku, który ma wyłączny dostęp). Metody pomocnicze
     * w tej klasie używają synchronizacji na listach zwierząt dla bezpieczeństwa
     * iteracji.
     * </p>
     *
     * @param simulation Aktualny stan symulacji. Musi być nie null.
     * @return Obiekt {@link SimulationStatisticsSnapshot} zawierający obliczone statystyki,
     *         lub {@code null} jeśli niezbędne dane (np. mapa symulacji) są niedostępne.
     * @throws NullPointerException jeśli {@code simulation} jest null.
     */
    public SimulationStatisticsSnapshot calculateSnapshot(Simulation simulation) {
        Objects.requireNonNull(simulation, "Simulation cannot be null for calculation");
        AbstractWorldMap currentMap = simulation.getMap();
        int currentDay = simulation.getCurrentDay();
        SimulationProperties simProps = simulation.getSimulationProperties();

        // Sprawdzenie, czy mapa jest dostępna
        if (currentMap == null) {
            System.err.println("StatisticsCalculator: Cannot calculate snapshot, Simulation Map is null.");
            return null;
        }

        // Pobranie list zwierząt (metody getAnimals/getDeadAnimals zwracają listy synchronizowane)
        List<Animal> currentAnimals = simulation.getAnimals();
        List<Animal> currentDeadAnimals = simulation.getDeadAnimals();

        // --- Wykonanie obliczeń ---
        double avgEnergy = calculateAverageEnergy(currentAnimals);
        double avgLifespan = calculateAverageLifespan(currentDeadAnimals);
        double avgChildren = calculateAverageChildren(currentAnimals);
        int emptyFieldsCount = calculateEmptyFieldsCount(currentMap, simProps); // Wymaga simProps i mapy
        Map<String, Integer> genotypeCounts = calculateGenotypeCounts(currentAnimals);
        List<String> topGenotypes = calculateTopGenotypes(genotypeCounts); // Obliczenie topowych genotypów

        // Utworzenie i zwrot migawki
        return new SimulationStatisticsSnapshot(
                currentDay,
                currentAnimals.size(),
                currentMap.getPlants().size(), // Zakłada, że getPlants() jest bezpieczne (zwraca widok/kopię)
                avgEnergy,
                avgLifespan,
                avgChildren,
                emptyFieldsCount,
                genotypeCounts,
                simProps.getConfigName(),
                topGenotypes // Dodanie obliczonych topowych genotypów
        );
    }

    // --- Prywatne metody pomocnicze do obliczeń ---

    /** Oblicza średnią energię żywych zwierząt. Wymaga synchronizacji na liście. */
    private double calculateAverageEnergy(List<Animal> animals) {
        synchronized (animals) { // Synchronizacja na czas iteracji/strumieniowania
            if (animals.isEmpty()) return 0.0;
            return animals.stream()
                    .mapToInt(Animal::getEnergy)
                    .average()
                    .orElse(0.0);
        }
    }

    /** Oblicza średnią długość życia martwych zwierząt. Wymaga synchronizacji na liście. */
    private double calculateAverageLifespan(List<Animal> deadAnimals) {
        synchronized (deadAnimals) { // Synchronizacja na czas iteracji/strumieniowania
            if (deadAnimals.isEmpty()) return 0.0;
            return deadAnimals.stream()
                    .mapToInt(animal -> animal.getDeathDate() - animal.getBirthDate()) // Oblicz długość życia
                    .filter(lifespan -> lifespan >= 0) // Uwzględnij zwierzęta, które umarły w dniu narodzin
                    .average()
                    .orElse(0.0);
        }
    }

    /** Oblicza średnią liczbę dzieci posiadanych przez żywe zwierzęta. Wymaga synchronizacji na liście. */
    private double calculateAverageChildren(List<Animal> animals) {
        synchronized (animals) { // Synchronizacja na czas iteracji/strumieniowania
            if (animals.isEmpty()) return 0.0;
            return animals.stream()
                    .mapToInt(Animal::getChildrenMade)
                    .average()
                    .orElse(0.0);
        }
    }

    /**
     * Oblicza liczbę pustych pól na mapie. Wymaga synchronizacji na obiekcie mapy
     * dla spójnego odczytu stanu zwierząt, roślin i wody.
     */
    private int calculateEmptyFieldsCount(AbstractWorldMap map, SimulationProperties simProps) {
        int totalFields = simProps.getWidth() * simProps.getHeight();
        Set<Vector2d> occupiedPositions = new HashSet<>();

        // Synchronizacja na mapie jest kluczowa dla spójnego odczytu jej stanu
        synchronized (map) {
            // Metody getAnimals() i getPlants() zwracają niemodyfikowalne widoki,
            // więc samo ich wywołanie jest bezpieczne, ale odczytujemy je w jednym
            // bloku synchronizowanym, aby uzyskać spójny obraz stanu mapy.
            occupiedPositions.addAll(map.getAnimals().keySet());
            occupiedPositions.addAll(map.getPlants().keySet());

            // Sprawdzenie specyficzne dla WaterWorld
            if (map instanceof WaterWorld waterMap) {
                // Zakładamy, że getWaterFields() zwraca widok lub kopię,
                // ale odczytujemy w bloku synchronized(map) dla spójności.
                occupiedPositions.addAll(waterMap.getWaterFields().keySet());
            }
        } // Koniec bloku synchronized(map)

        // Zwróć liczbę pól całkowitych minus zajęte, nie mniej niż 0.
        return Math.max(0, totalFields - occupiedPositions.size());
    }

    /** Oblicza liczbę zwierząt dla każdego unikalnego genotypu. Wymaga synchronizacji na liście zwierząt. */
    private Map<String, Integer> calculateGenotypeCounts(List<Animal> animals) {
        synchronized (animals) { // Synchronizacja na czas iteracji/strumieniowania
            if (animals.isEmpty()) return Collections.emptyMap(); // Zwróć pustą mapę, jeśli nie ma zwierząt
            // Grupuj po sformatowanym genotypie i zliczaj wystąpienia
            return animals.stream()
                    .collect(Collectors.groupingBy(
                            animal -> GenotypeFormatter.formatGenotype(animal.getGenes()), // Klucz to sformatowany genotyp
                            Collectors.summingInt(animal -> 1) // Wartość to liczba zwierząt
                    ));
        }
    }

    /**
     * Wyznacza listę N najczęściej występujących genotypów na podstawie mapy zliczeń.
     * @param genotypeCounts Mapa: sformatowany genotyp -> liczba wystąpień.
     * @return Lista N (lub mniej, jeśli jest mniej unikalnych) najpopularniejszych genotypów (String).
     */
    private List<String> calculateTopGenotypes(Map<String, Integer> genotypeCounts) {
        if (genotypeCounts == null || genotypeCounts.isEmpty()) {
            return Collections.emptyList(); // Zwróć pustą listę, jeśli brak danych
        }
        // Sortuj wpisy mapy malejąco według wartości (liczby wystąpień)
        return genotypeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(TOP_GENOTYPE_COUNT) // Ogranicz do N najpopularniejszych
                .map(Map.Entry::getKey) // Wybierz tylko klucze (sformatowane genotypy)
                .collect(Collectors.toList()); // Zbierz do listy
    }
}