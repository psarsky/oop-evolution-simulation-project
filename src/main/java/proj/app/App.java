package proj.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The main entry point for the JavaFX Evolution Simulator application.
 * Responsible for loading the initial main window.
 */
public class App extends Application {

    /**
     * Starts the JavaFX application by loading and displaying the main window.
     *
     * @param primaryStage The primary stage for this application, onto which
     *                     the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main window layout from the FXML file
            FXMLLoader mainLoader = new FXMLLoader();
            mainLoader.setLocation(getClass().getClassLoader().getResource("fxml/MainWindow.fxml"));
            Parent mainRoot = mainLoader.load();

            // Configure and display the primary stage
            primaryStage.setTitle("Evolution Simulator");
            primaryStage.setScene(new Scene(mainRoot));
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            primaryStage.show();

            // The MainWindowController handles further actions, like opening simulation windows.
        } catch (IOException e) {
            System.err.println("Failed to start the application: " + e.getMessage());
            e.printStackTrace();
            // Consider showing an error dialog to the user here
        }
    }

    /**
     * The main method to launch the JavaFX application.
     *
     * @param args Command line arguments passed to the application. Not used in this application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}