package proj.app.services;

import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * An implementation of {@link IMessageService} that retrieves strings
 * from a {@link ResourceBundle}. Handles missing keys gracefully by returning
 * a placeholder string.
 */
public class ResourceBundleMessageService implements IMessageService {

    private final ResourceBundle resourceBundle;

    /**
     * Constructs the service with a specific ResourceBundle instance.
     * This bundle will be used as the source for all message lookups.
     *
     * @param resourceBundle The {@link ResourceBundle} containing the key-value pairs for messages. Must not be null.
     * @throws NullPointerException if resourceBundle is null.
     */
    public ResourceBundleMessageService(ResourceBundle resourceBundle) {
        this.resourceBundle = Objects.requireNonNull(resourceBundle, "ResourceBundle cannot be null");
    }

    /**
     * Retrieves the string associated with the given key from the configured {@link ResourceBundle}.
     * If the key is not found in the bundle, a warning is logged to standard error, and a
     * placeholder string in the format "?key?" is returned.
     *
     * @param key The non-null key identifying the desired string message.
     * @return The string value associated with the key, or a placeholder string "?key?" if the key is not found.
     * @throws NullPointerException if key is null.
     */
    @Override
    public String getMessage(String key) {
        Objects.requireNonNull(key, "Message key cannot be null");
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("Warning: Missing resource key: " + key);
            return "?" + key + "?"; // Return placeholder for missing keys
        }
    }

    /**
     * Retrieves the string associated with the given key from the configured {@link ResourceBundle}
     * and formats it using {@link String#format(String, Object...)} with the provided arguments.
     * <p>
     * If the key is not found, returns the placeholder "?key?".
     * If the key is found but a formatting error occurs (e.g., incompatible arguments),
     * a warning is logged, and a placeholder "!key!" is returned.
     *
     * @param key  The non-null key identifying the desired format string within the message source.
     * @param args The arguments to be inserted into the format string.
     * @return The formatted string value, or a placeholder "?key?"/"!key!" on failure.
     * @throws NullPointerException if key is null.
     */
    @Override
    public String getFormattedMessage(String key, Object... args) {
        String formatString = getMessage(key); // Handles missing key check and returns placeholder if needed
        if (formatString.startsWith("?")) {
            return formatString; // Return "?key?" placeholder if key was missing
        }
        try {
            // Use String.format with the retrieved format string
            return String.format(formatString, args);
        } catch (Exception e) { // Catch potential IllegalFormatException etc.
            System.err.println("Warning: Failed to format message for key '" + key + "'. Error: " + e.getMessage());
            return "!" + key + "!"; // Return different placeholder "!" for formatting errors
        }
    }
}