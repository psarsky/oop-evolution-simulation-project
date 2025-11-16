package proj.app.services;

/**
 * Interface for a service that provides localized or configured strings
 * based on a key. Decouples components from the underlying resource bundle
 * or other message source implementation.
 */
public interface IMessageService {

    /**
     * Retrieves the string associated with the given key from the underlying message source.
     * Implementations should define how to handle cases where the key is not found
     * (e.g., return a placeholder, throw an exception).
     *
     * @param key The non-null key identifying the desired string message.
     * @return The string value associated with the key. The return value in case of a missing key is implementation-dependent.
     * @throws NullPointerException if the provided key is null.
     */
    String getMessage(String key);

    /**
     * Retrieves the string associated with the given key and formats it
     * using the provided arguments. The formatting follows the rules of
     * {@link String#format(String, Object...)} or a similar mechanism defined by the implementation.
     * Implementations should define how to handle missing keys or formatting errors.
     *
     * @param key  The non-null key identifying the desired format string within the message source.
     * @param args The arguments to be inserted into the format string.
     * @return The formatted string value. The return value in case of missing keys or formatting errors is implementation-dependent.
     * @throws NullPointerException if the provided key is null.
     */
    String getFormattedMessage(String key, Object... args);
}