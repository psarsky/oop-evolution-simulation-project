// Plik: proj/app/App.java
package proj.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import proj.app.controllers.MainWindowController;
import proj.app.services.IAlertService;
import proj.app.services.IMessageService;
import proj.app.services.JavaFXAlertService;
import proj.app.services.ResourceBundleMessageService;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class App extends Application {

    private static final String BASE_BUNDLE_NAME = "messages";
    private static final String MAIN_FXML_PATH = "/fxml/MainWindow.fxml";

    @Override
    public void start(Stage primaryStage) {
        // --- 1. Utwórz rdzeń usług / singletony ---
        final IAlertService alertService = new JavaFXAlertService();
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final ActiveSimulationRegistry activeSimulationRegistry = ActiveSimulationRegistry.getInstance();
        // Załaduj ResourceBundle i utwórz MessageService
        ResourceBundle resourceBundle = loadResourceBundle(alertService);
        final IMessageService messageService = new ResourceBundleMessageService(resourceBundle);
        // --- Poprawka: Zaktualizuj wywołanie konstruktora SimulationInitializer ---
        final SimulationInitializer simulationInitializer = new SimulationInitializer(gson, alertService, messageService); // Przekaż wszystkie zależności

        // --- 2. Załaduj główny FXML ---
        try {
            FXMLLoader mainLoader = new FXMLLoader();
            URL fxmlUrl = getClass().getResource(MAIN_FXML_PATH);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file resource at classpath path: " + MAIN_FXML_PATH);
            }
            mainLoader.setLocation(fxmlUrl);

            // --- 3. Skonfiguruj ControllerFactory dla DI ---
            mainLoader.setControllerFactory(controllerClass -> {
                if (controllerClass == MainWindowController.class) {
                    // Przekaż wszystkie wymagane zależności do MainWindowController
                    return new MainWindowController(
                            alertService, activeSimulationRegistry, simulationInitializer, gson, messageService
                    );
                } else {
                    try { return controllerClass.getDeclaredConstructor().newInstance(); }
                    catch (Exception e) { throw new RuntimeException("Controller instantiation failed for " + controllerClass.getName(), e); }
                }
            });

            // --- 4. Załaduj korzeń i ustaw scenę ---
            Parent mainRoot = mainLoader.load();

            // --- 5. Skonfiguruj i wyświetl Stage ---
            primaryStage.setTitle(messageService.getMessage("app.title"));
            primaryStage.setScene(new Scene(mainRoot));
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            primaryStage.show();

        } catch (IOException e) {
            handleStartupError(alertService, messageService, "error.title", "error.app.startup.load", e, MAIN_FXML_PATH);
        } catch (RuntimeException e) {
            handleStartupError(alertService, messageService, "error.title", "error.app.startup.unexpected", e);
        } catch (Exception e) {
            handleStartupError(alertService, messageService, "error.title", "error.app.startup.critical", e);
        }
    }

    /** Ładuje ResourceBundle. */
    private ResourceBundle loadResourceBundle(IAlertService alertService) {
        try {
            return ResourceBundle.getBundle(BASE_BUNDLE_NAME, Locale.getDefault());
        } catch (MissingResourceException e) {
            System.err.println("FATAL: Could not load resource bundle '" + BASE_BUNDLE_NAME + "'. UI text will be missing.");
            // alertService.showAlert(IAlertService.AlertType.ERROR, "Resource Error", "Failed to load UI text resources."); // Może nie działać
            return new ResourceBundle() {
                @Override protected Object handleGetObject(String key) { return "???" + key + "???"; } // Zwracaj widoczny placeholder
                @Override public java.util.Enumeration<String> getKeys() { return java.util.Collections.emptyEnumeration(); }
            };
        }
    }

    /** Obsługuje krytyczne błędy startowe. */
    private void handleStartupError(IAlertService alertService, IMessageService messageService,
                                    String titleKey, String headerKey, Exception e, Object... args) {
        String header = messageService.getFormattedMessage(headerKey, args);
        String title = messageService.getMessage(titleKey);

        System.err.println(header + " Error: " + e.getMessage());
        e.printStackTrace();
        try {
            String content = "Error details: " + e.getMessage() +
                    (e.getCause() != null ? "\nCause: " + e.getCause().getMessage() : "");
            alertService.showAlert(IAlertService.AlertType.ERROR, title, header, content);
        } catch (Exception alertEx) {
            System.err.println("Additionally, failed to show the error alert dialog: " + alertEx.getMessage());
        }
    }

    /** Główny punkt wejścia. */
    public static void main(String[] args) {
        launch(args);
    }
}