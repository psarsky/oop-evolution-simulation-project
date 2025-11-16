package proj.app.render;

import javafx.animation.AnimationTimer;
import proj.app.AppConstants;
import proj.app.state.SimulationStateQueue;
// --- POPRAWNY IMPORT ---
import proj.app.state.SimulationRenderSnapshot;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Zarządza pętlą renderowania za pomocą JavaFX {@link AnimationTimer}.
 * Efektywnie pobiera migawki stanu symulacji ({@link SimulationRenderSnapshot})
 * z {@link SimulationStateQueue}, przekazuje je do callbacka (np. w kontrolerze)
 * i deleguje rysowanie do {@link MapRenderer}.
 */
public class SimulationRenderer {

    // Kolejka i procesor muszą używać SimulationRenderSnapshot
    private final SimulationStateQueue<SimulationRenderSnapshot> stateQueue;
    private final MapRenderer mapRenderer;
    private final Consumer<SimulationRenderSnapshot> snapshotProcessor; // Callback akceptuje RenderSnapshot
    private AnimationTimer animationTimer;
    private volatile boolean isRunning = false;

    /**
     * Konstruuje SimulationRenderer.
     *
     * @param stateQueue        Kolejka dostarczająca {@link SimulationRenderSnapshot}. Nie może być null.
     * @param mapRenderer       Instancja {@link MapRenderer}. Nie może być null.
     * @param snapshotProcessor Callback (np. metoda kontrolera) wywoływany przed renderowaniem migawki. Nie może być null.
     */
    public SimulationRenderer(SimulationStateQueue<SimulationRenderSnapshot> stateQueue, MapRenderer mapRenderer, Consumer<SimulationRenderSnapshot> snapshotProcessor) {
        this.stateQueue = Objects.requireNonNull(stateQueue, "StateQueue<SimulationRenderSnapshot> cannot be null");
        this.mapRenderer = Objects.requireNonNull(mapRenderer, "MapRenderer cannot be null");
        this.snapshotProcessor = Objects.requireNonNull(snapshotProcessor, "SnapshotProcessor callback cannot be null");
        createAnimationTimer();
    }

    /** Tworzy wewnętrzny AnimationTimer. */
    private void createAnimationTimer() {
        animationTimer = new AnimationTimer() {
            private long lastUiUpdateTimestamp = 0;

            /**
             * Wywoływana przez JavaFX dla każdej klatki. Pobiera najnowszą migawkę
             * renderowania, wywołuje procesor i rysuje.
             *
             * @param now Bieżący czas w nanosekundach.
             */
            @Override
            public void handle(long now) {
                if (!isRunning || (now - lastUiUpdateTimestamp < AppConstants.UI_RENDER_INTERVAL_NANOS)) {
                    return;
                }
                lastUiUpdateTimestamp = now;

                // --- POPRAWKA: Zadeklaruj zmienną z poprawnym typem ---
                SimulationRenderSnapshot snapshotToRender = stateQueue.dequeue();

                if (snapshotToRender != null) {
                    // Wywołaj callback z poprawnym typem
                    snapshotProcessor.accept(snapshotToRender);

                    // Przekaż poprawny typ do renderera mapy
                    mapRenderer.drawSimulation(snapshotToRender);
                }
            }
        };
    }

    /** Uruchamia pętlę renderowania. */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            animationTimer.start();
            System.out.println("SimulationRenderer started.");
        }
    }

    /** Zatrzymuje pętlę renderowania. */
    public void stop() {
        if (isRunning) {
            isRunning = false;
            animationTimer.stop();
            System.out.println("SimulationRenderer stopped.");
        }
    }

    /** Sprawdza, czy pętla renderowania jest aktywna. */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Wymusza natychmiastowe przerysowanie dostarczonej migawki renderowania.
     *
     * @param snapshotToDraw Migawka {@link SimulationRenderSnapshot} do narysowania.
     */
    public void redrawFrame(SimulationRenderSnapshot snapshotToDraw) { // Akceptuje RenderSnapshot
        if (mapRenderer != null) {
            // Przekaż poprawny typ do renderera mapy
            mapRenderer.drawSimulation(snapshotToDraw);
        }
    }
}