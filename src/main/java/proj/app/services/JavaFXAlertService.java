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
     * @param serviceType The abstract alert type defined in {@link IAlertService.AlertType}.
     * @return The equivalent JavaFX {@link Alert.AlertType}.
     */
    private Alert.AlertType convertType(AlertType serviceType) {
        return switch (serviceType) {
            case INFO -> Alert.AlertType.INFORMATION;
            case WARNING -> Alert.AlertType.WARNING;
            case ERROR -> Alert.AlertType.ERROR;
            case CONFIRMATION -> Alert.AlertType.CONFIRMATION;
        };
    }

    /**
     * Displays an alert using a JavaFX {@link Alert}. Ensures execution on the JavaFX Application Thread.
     * If called from a non-FX thread, the display is scheduled using {@code Platform.runLater}.
     *
     * @param type    The type of alert (e.g., INFO, WARNING, ERROR) defined in {@link IAlertService.AlertType}.
     * @param title   The text to be displayed in the title bar of the alert window.
     * @param header  The optional header text displayed above the main content. Can be null for no header.
     * @param content The main message text of the alert.
     */
    @Override
    public void showAlert(AlertType type, String title, String header, String content) {
        if (Platform.isFxApplicationThread()) {
            createAndShowAlert(type, title, header, content);
        } else {
            Platform.runLater(() -> createAndShowAlert(type, title, header, content));
        }
    }

    /**
     * Displays an alert using a JavaFX {@link Alert} with only title and content.
     * This is a convenience method that calls the full {@link #showAlert(AlertType, String, String, String)}
     * with a null header. Ensures execution on the JavaFX Application Thread.
     *
     * @param type    The type of alert (e.g., INFO, WARNING, ERROR) defined in {@link IAlertService.AlertType}.
     * @param title   The text to be displayed in the title bar of the alert window.
     * @param content The main message text of the alert.
     */
    @Override
    public void showAlert(AlertType type, String title, String content) {
        showAlert(type, title, null, content);
    }

    /**
     * Creates and displays the actual JavaFX {@link Alert} dialog.
     * This method must be called on the JavaFX Application Thread.
     *
     * @param serviceType The abstract alert type defined in {@link IAlertService.AlertType}.
     * @param title       The window title text.
     * @param header      The optional header text (can be null).
     * @param content     The main content message text.
     */
    private void createAndShowAlert(AlertType serviceType, String title, String header, String content) {
        Alert.AlertType fxType = convertType(serviceType);
        Alert alert = new Alert(fxType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}