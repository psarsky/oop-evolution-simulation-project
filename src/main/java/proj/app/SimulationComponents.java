// Plik: proj/app/SimulationComponents.java
package proj.app;

import proj.app.render.MapRenderer;
import proj.app.state.SimulationRenderSnapshot; // Import dla typu generycznego
import proj.app.state.SimulationStateProducer;
import proj.app.state.SimulationStateQueue;
import proj.simulation.Simulation;

/**
 * Prosty rekord przechowujący główne komponenty instancji symulacji
 * wymagane przez warstwę UI. Działa jako kontener do wspólnego przekazywania
 * tych komponentów.
 *
 * @param simulation           Instancja rdzenia logiki symulacji.
 * @param simulationEngine Kontroler zarządzający cyklem życia wątku symulacji.
 * @param statisticsManager    Manager do obliczania i zapisywania statystyk.
 * @param stateProducer        Zadanie tła produkujące migawki stanu symulacji.
 * @param stateQueue           Kolejka buforująca migawki stanu dla wątku UI (typu RenderSnapshot).
 * @param mapRenderer          Komponent odpowiedzialny za rysowanie stanu symulacji na płótnie.
 */
public record SimulationComponents(
        Simulation simulation,
        SimulationEngine simulationEngine,
        StatisticsManager statisticsManager,
        SimulationStateProducer stateProducer,
        SimulationStateQueue<SimulationRenderSnapshot> stateQueue, // Użyj typu generycznego
        MapRenderer mapRenderer
) {
    // Rekordy automatycznie zapewniają konstruktor, gettery, equals, hashCode, toString,
    // i niezmienność pól (referencje się nie zmienią).
}