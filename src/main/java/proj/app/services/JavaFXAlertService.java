package proj.app.services;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * An implementation of the {@link IAlertService} interface using JavaFX's {@link Alert} dialogs.
 * Ensures that alerts are displayed on the JavaFX Application Thread.
 */
public class JavaFXAlertService implements IAlertService {

    /**
     * Converts the abstract {@link AlertType} to the corresponding JavaFX {@link Alert.AlertType}.
     *
     * @param serviceType The abstract alert type.
     * @return The equivalent JavaFX alert type.
     */
    private Alert.AlertType convertType(AlertType serviceType) {
        return switch (serviceType) {
            case INFO -> Alert.AlertType.INFORMATION;
            case WARNING -> Alert.AlertType.WARNING;
            case ERROR -> Alert.AlertType.ERROR;
            case CONFIRMATION -> Alert.AlertType.CONFIRMATION;
            // default -> Alert.AlertType.NONE; // Or throw exception for unknown types
        };
    }

    /**
     * {@inheritDoc}
     * Displays the alert using a JavaFX {@link Alert}. Ensures execution on the JavaFX Application Thread.
     */
    @Override
    public void showAlert(AlertType type, String title, String header, String content) {
        // Ensure the alert is shown on the JavaFX Application Thread
        if (Platform.isFxApplicationThread()) {
            createAndShowAlert(type, title, header, content);
        } else {
            // If called from another thread, schedule it to run on the FX thread
            Platform.runLater(() -> createAndShowAlert(type, title, header, content));
        }
    }

    /**
     * {@inheritDoc}
     * Convenience method that calls the full showAlert with a null header.
     */
    @Override
    public void showAlert(AlertType type, String title, String content) {
        showAlert(type, title, null, content); // Delegate to the main method
    }

    /**
     * Creates and displays the JavaFX {@link Alert} dialog.
     * This method assumes it is already running on the JavaFX Application Thread.
     *
     * @param serviceType The abstract alert type.
     * @param title       The window title.
     * @param header      The header text (can be null).
     * @param content     The main content message.
     */
    private void createAndShowAlert(AlertType serviceType, String title, String header, String content) {
        Alert.AlertType fxType = convertType(serviceType);
        Alert alert = new Alert(fxType);
        alert.setTitle(title);
        alert.setHeaderText(header); // Setting null is acceptable for no header
        alert.setContentText(content);

        // For CONFIRMATION, showAndWait() returns an Optional<ButtonType>.
        // This basic implementation doesn't return the result, but it could be modified if needed.
        alert.showAndWait();
    }
}