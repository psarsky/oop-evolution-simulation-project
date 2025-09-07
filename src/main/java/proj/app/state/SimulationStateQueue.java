package proj.app.state;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.Objects; // Dodano dla Objects.requireNonNull

/**
 * Wątkowo bezpieczna, ograniczona, dwukierunkowa kolejka (deque)
 * przechowująca migawki stanu symulacji (dowolnego typu T, np.
 * {@link SimulationRenderSnapshot} lub {@link proj.app.SimulationStatisticsSnapshot}).
 * Działa jako bufor między wątkiem produkującym migawki a wątkiem
 * konsumującym je (np. wątkiem UI).
 * <p>
 * Gdy kolejka jest pełna, dodanie nowego elementu (enqueue) powoduje
 * usunięcie najstarszego elementu (z początku kolejki). Elementy są
 * pobierane (dequeue) z początku kolejki w porządku FIFO.
 * </p>
 * @param <T> Typ przechowywanych migawek stanu.
 */
public class SimulationStateQueue<T> {

    private final BlockingDeque<T> stateQueue; // Wewnętrzna implementacja kolejki
    private final int maxQueueSize; // Maksymalny rozmiar kolejki

    /**
     * Konstruuje kolejkę {@code SimulationStateQueue} o określonej maksymalnej pojemności.
     *
     * @param maxQueueSize Maksymalna liczba migawek, jaką kolejka może pomieścić,
     *                     zanim zacznie usuwać najstarsze. Musi być dodatnia.
     * @throws IllegalArgumentException jeśli maxQueueSize nie jest dodatnie.
     */
    public SimulationStateQueue(int maxQueueSize) {
        if (maxQueueSize <= 0) {
            throw new IllegalArgumentException("maxQueueSize must be positive");
        }
        this.maxQueueSize = maxQueueSize;
        // Użycie LinkedBlockingDeque jako implementacji BlockingDeque.
        this.stateQueue = new LinkedBlockingDeque<>(maxQueueSize);
    }

    /**
     * Dodaje nową migawkę stanu na koniec kolejki.
     * Jeśli kolejka jest pełna, najpierw usuwa element z początku (najstarszą migawkę),
     * a następnie dodaje nową migawkę na koniec. Operacja jest nieblokująca.
     *
     * @param snapshot Migawka stanu typu T do dodania. Nie może być null.
     * @throws NullPointerException jeśli snapshot jest null.
     */
    public void enqueue(T snapshot) {
        Objects.requireNonNull(snapshot, "Cannot enqueue a null snapshot");
        // Pętla zapewniająca zwolnienie miejsca, jeśli kolejka jest pełna.
        // pollFirst() usuwa element z początku (head).
        while (stateQueue.size() >= maxQueueSize) {
            stateQueue.pollFirst(); // Usuń najstarszy element
        }
        // offerLast dodaje na koniec (tail), nie blokuje, zwraca false, jeśli się nie uda (tu nie powinno)
        stateQueue.offerLast(snapshot);
    }

    /**
     * Pobiera i usuwa migawkę stanu z początku kolejki (porządek FIFO).
     * Jeśli kolejka jest pusta, metoda zwraca {@code null}.
     * Operacja jest nieblokująca.
     *
     * @return Migawka z początku kolejki, lub {@code null}, jeśli kolejka jest pusta.
     */
    public T dequeue() {
        // pollFirst pobiera i usuwa element z początku, zwraca null, jeśli pusta.
        return stateQueue.pollFirst();
    }

    /**
     * Pobiera, ale nie usuwa, ostatnio dodaną migawkę (element na końcu kolejki).
     * Jeśli kolejka jest pusta, metoda zwraca {@code null}.
     * Przydatne do podglądu najnowszego stanu bez jego konsumowania.
     *
     * @return Migawka z końca kolejki, lub {@code null}, jeśli kolejka jest pusta.
     */
    public T peekLast() {
        // peekLast pobiera, ale nie usuwa elementu z końca, zwraca null, jeśli pusta.
        return stateQueue.peekLast();
    }

    /**
     * Sprawdza, czy kolejka zawiera jakiekolwiek migawki.
     *
     * @return {@code true}, jeśli kolejka nie jest pusta, {@code false} w przeciwnym razie.
     */
    public boolean hasSnapshots() {
        return !stateQueue.isEmpty();
    }

    /**
     * Zwraca bieżącą liczbę migawek w kolejce.
     *
     * @return Liczba elementów w kolejce.
     */
    public int size() {
        return stateQueue.size();
    }
}