package proj.app.services;

/**
 * Interface defining a contract for a service that displays alerts or notifications to the user.
 * This abstraction decouples the core application logic from the specific UI framework used for alerts.
 */
public interface IAlertService {

    /**
     * Displays an alert message to the user.
     *
     * @param type    The type of alert (e.g., INFO, WARNING, ERROR).
     * @param title   The title for the alert window or notification.
     * @param header  A header text for the alert (can be null for none).
     * @param content The main content/message of the alert.
     */
    void showAlert(AlertType type, String title, String header, String content);

    /**
     * Displays an alert message to the user with only a title and content.
     * Convenience method that calls the full showAlert with a null header.
     *
     * @param type    The type of alert (e.g., INFO, WARNING, ERROR).
     * @param title   The title for the alert window or notification.
     * @param content The main content/message of the alert.
     */
    void showAlert(AlertType type, String title, String content);

    /**
     * Enumeration of standard alert types.
     * Implementations of {@link IAlertService} should map these types to their specific UI representations.
     */
    enum AlertType {
        /** Informational message. */
        INFO,
        /** Warning message indicating a potential issue. */
        WARNING,
        /** Error message indicating a failure or problem. */
        ERROR,
        /** Confirmation dialog requiring user affirmation (e.g., Yes/No, OK/Cancel). */
        CONFIRMATION // Note: Handling the *result* of confirmation may require a different method signature or callback.
    }
}