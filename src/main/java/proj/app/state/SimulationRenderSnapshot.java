package proj.app.state;

import proj.model.elements.Animal;
import proj.model.elements.Plant;
import proj.util.Vector2d;

import java.util.*;

/**
 * Reprezentuje niezmienną migawkę stanu świata symulacji przeznaczoną
 * specjalnie do celów **renderowania** interfejsu użytkownika.
 * Zawiera tylko te dane, które są bezpośrednio potrzebne przez {@link proj.app.render.MapRenderer}
 * do narysowania aktualnego stanu mapy, w tym pozycji elementów i informacji
 * potrzebnych do ich wyróżnienia (wybrane zwierzę, dominujące genotypy).
 *
 * @param animals        Niemodyfikowalna mapa pozycji {@link Vector2d} do (niemodyfikowalnej listy) {@link Animal}i.
 * @param plants         Niemodyfikowalna mapa pozycji {@link Vector2d} do {@link Plant}.
 * @param waterFields    Niemodyfikowalna mapa pozycji {@link Vector2d} do elementów wody (może być null).
 * @param topGenotypes   Niemodyfikowalna lista N najpopularniejszych genotypów (String) do podświetlenia.
 * @param selectedAnimal Referencja do aktualnie wybranego {@link Animal} (do podświetlenia), może być null.
 */
public record SimulationRenderSnapshot(
        Map<Vector2d, List<Animal>> animals,
        Map<Vector2d, Plant> plants,
        Map<Vector2d, ?> waterFields,
        List<String> topGenotypes,
        Animal selectedAnimal
) {
    /**
     * Konstruktor kanoniczny zapewniający, że przekazane kolekcje
     * są przechowywane jako niemodyfikowalne kopie, co gwarantuje
     * bezpieczeństwo wątkowe i niezmienność migawki.
     */
    public SimulationRenderSnapshot {
        // Tworzenie niemodyfikowalnych kopii map i list
        Map<Vector2d, List<Animal>> immutableAnimals = new HashMap<>();
        if (animals != null) {
            // Kopiujemy zewnętrzną mapę i wewnętrzne listy
            animals.forEach((pos, list) -> immutableAnimals.put(pos, list != null ? List.copyOf(list) : Collections.emptyList()));
        }
        // Przypisz niemodyfikowalną mapę zwierząt
        animals = Map.copyOf(immutableAnimals);

        // Utwórz niemodyfikowalną mapę roślin lub pustą mapę
        plants = Map.copyOf(Objects.requireNonNullElse(plants, Collections.emptyMap()));

        // Utwórz niemodyfikowalną mapę wody lub zachowaj null
        waterFields = (waterFields != null) ? Map.copyOf(waterFields) : null;

        // Utwórz niemodyfikowalną listę genotypów lub pustą listę
        topGenotypes = List.copyOf(Objects.requireNonNullElse(topGenotypes, Collections.emptyList()));

        // selectedAnimal jest referencją, która jest już finalna w rekordzie.
    }
}