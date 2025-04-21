package proj.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import proj.app.controllers.MainWindowController; // Import relevant controller
import proj.app.services.IAlertService;
import proj.app.services.IMessageService; // Import interface
import proj.app.services.JavaFXAlertService;
import proj.app.services.ResourceBundleMessageService; // Import implementation

import java.io.IOException;
import java.net.URL; // Import URL
import java.util.Locale; // For resource bundle locale
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The main entry point for the JavaFX Evolution Simulator application.
 * Acts as the Composition Root: Initializes shared services, retrieves singletons,
 * creates components, sets up Dependency Injection, loads and displays the main window.
 */
public class App extends Application {

    /** The base name of the resource bundle file (e.g., "messages.properties"). */
    private static final String BASE_BUNDLE_NAME = "messages";
    /** The path to the main FXML file, relative to the classpath root. */
    private static final String MAIN_FXML_PATH = "/fxml/MainWindow.fxml"; // Use leading '/' for classpath root

    /**
     * Starts the JavaFX application lifecycle. Initializes shared services, loads the resource bundle,
     * configures DI for the main controller, loads MainWindow.fxml, and displays the stage.
     * Handles critical startup errors.
     *
     * @param primaryStage The primary {@link Stage} provided by the JavaFX runtime. Must not be null.
     */
    @Override
    public void start(Stage primaryStage) {

        // --- 1. Create Core Shared Services / Singletons ---
        final IAlertService alertService = new JavaFXAlertService();
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final ActiveSimulationRegistry activeSimulationRegistry = ActiveSimulationRegistry.getInstance();
        final SimulationInitializer simulationInitializer = new SimulationInitializer(gson);

        // --- Load Resource Bundle and Create Message Service ---
        ResourceBundle resourceBundle = loadResourceBundle(alertService); // Use helper method
        final IMessageService messageService = new ResourceBundleMessageService(resourceBundle);

        // --- 2. Load Main Window FXML ---
        try {
            FXMLLoader mainLoader = new FXMLLoader();

            // --- Corrected FXML Location Loading ---
            // Use getClass().getResource() with a path starting from the classpath root ('/')
            URL fxmlUrl = getClass().getResource(MAIN_FXML_PATH);
            if (fxmlUrl == null) {
                // Throw a more specific exception if the resource URL itself is null
                throw new IOException("Cannot find FXML file resource at classpath path: " + MAIN_FXML_PATH);
            }
            mainLoader.setLocation(fxmlUrl);
            // --- End Corrected FXML Location Loading ---


            // --- 3. Configure ControllerFactory for Dependency Injection ---
            mainLoader.setControllerFactory(controllerClass -> {
                if (controllerClass == MainWindowController.class) {
                    return new MainWindowController(
                            alertService, activeSimulationRegistry, simulationInitializer, gson, messageService
                    );
                } else {
                    try { return controllerClass.getDeclaredConstructor().newInstance(); }
                    catch (Exception e) { throw new RuntimeException("Controller instantiation failed for " + controllerClass.getName(), e); }
                }
            });
            // --- End ControllerFactory Setup ---

            // --- 4. Load Root Node and Set Scene ---
            Parent mainRoot = mainLoader.load(); // This line triggers the actual loading and parsing

            // --- 5. Configure and Display Stage ---
            primaryStage.setTitle(messageService.getMessage("app.title")); // Set window title
            primaryStage.setScene(new Scene(mainRoot));
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            primaryStage.show();

        } catch (IOException e) { // Catch FXML loading/parsing errors
            handleStartupError(alertService, messageService, "error.title",
                    "error.app.startup.load", e, MAIN_FXML_PATH); // Pass FXML path to error message
        } catch (RuntimeException e) { // Catch other runtime errors (e.g., controller factory)
            handleStartupError(alertService, messageService, "error.title",
                    "error.app.startup.unexpected", e);
        } catch (Exception e) { // Catch any other unexpected exception
            handleStartupError(alertService, messageService, "error.title",
                    "error.app.startup.critical", e);
        }
    }

    /**
     * Loads the resource bundle. Includes fallback for missing bundle.
     * @param alertService Service to potentially show alerts (though may not work if bundle load fails early).
     * @return The loaded ResourceBundle or a fallback empty bundle.
     */
    private ResourceBundle loadResourceBundle(IAlertService alertService) {
        try {
            return ResourceBundle.getBundle(BASE_BUNDLE_NAME, Locale.getDefault());
        } catch (MissingResourceException e) {
            System.err.println("FATAL: Could not load resource bundle '" + BASE_BUNDLE_NAME + "'. UI text will be missing.");
            // Consider showing an alert if alertService is functional without the bundle
            // alertService.showAlert(IAlertService.AlertType.ERROR, "Resource Error", "Failed to load UI text resources.");
            // Return an empty bundle to prevent NullPointerExceptions later, UI text will be broken.
            return new ResourceBundle() {
                @Override protected Object handleGetObject(String key) { return null; }
                @Override public java.util.Enumeration<String> getKeys() { return java.util.Collections.emptyEnumeration(); }
            };
        }
    }


    /**
     * Handles critical errors encountered during application startup. Logs the error
     * and attempts to display an alert dialog using localized messages.
     *
     * @param alertService    The {@link IAlertService} instance to use for displaying the alert.
     * @param messageService  The {@link IMessageService} instance to get localized error messages.
     * @param titleKey        The resource key for the error alert window title.
     * @param headerKey       The resource key for the header text summarizing the error context.
     * @param e               The {@link Exception} that occurred during startup.
     * @param args            Optional arguments for formatting the header message (e.g., filename).
     */
    private void handleStartupError(IAlertService alertService, IMessageService messageService,
                                    String titleKey, String headerKey, Exception e, Object... args) {
        String header = messageService.getFormattedMessage(headerKey, args);
        String title = messageService.getMessage(titleKey);

        System.err.println(header + " Error: " + e.getMessage());
        // Print stack trace for detailed debugging information in the console
        e.printStackTrace();
        try {
            // Construct detailed content for the alert, including the cause if available
            String content = "Error details: " + e.getMessage() +
                    (e.getCause() != null ? "\nCause: " + e.getCause().getMessage() : "");
            alertService.showAlert(IAlertService.AlertType.ERROR, title, header, content);
        } catch (Exception alertEx) {
            // Log if showing the alert itself fails
            System.err.println("Additionally, failed to show the error alert dialog: " + alertEx.getMessage());
        }
    }

    /**
     * The main entry point method that launches the JavaFX application.
     * Delegates to {@link Application#launch(Class, String...)}.
     *
     * @param args Command line arguments passed to the application (not used).
     */
    public static void main(String[] args) {
        launch(args);
    }
}