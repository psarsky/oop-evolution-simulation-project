package proj.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
//import proj.simulation.SimulationProperties;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main window
            FXMLLoader mainLoader = new FXMLLoader();
            mainLoader.setLocation(getClass().getClassLoader().getResource("fxml/MainWindow.fxml"));
            Parent mainRoot = mainLoader.load();

            // Create primary stage with main window
            primaryStage.setTitle("Evolution Simulator");
            primaryStage.setScene(new Scene(mainRoot));
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            primaryStage.show();

            // Main window is loaded and displayed
            // Note: The MainWindowController will handle opening simulation windows
            // when the user selects simulation configurations
        } catch (IOException e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    /**
//     * Opens a new simulation window with the given configuration.
//     * This method can be called from MainWindowController when a user starts a new simulation.
//     *
//     * @param simulationProperties The properties for the new simulation
//     * @throws IOException If loading the FXML file fails
//     */
//    public static void openSimulationWindow(SimulationProperties simulationProperties) throws IOException {
//        FXMLLoader simLoader = new FXMLLoader();
//        simLoader.setLocation(App.class.getClassLoader().getResource("fxml/SimulationWindow.fxml"));
//        Parent simRoot = simLoader.load();
//
//        // Get the controller and initialize the simulation
//        SimulationWindowController controller = simLoader.getController();
//        controller.initializeSimulation(simulationProperties);
//
//        // Create and show new stage for the simulation
//        Stage simStage = new Stage();
//        simStage.setTitle("Evolution Simulation - " + simulationProperties.getConfigName());
//        simStage.setScene(new Scene(simRoot));
//        simStage.setMinWidth(900);
//        simStage.setMinHeight(800);
//        simStage.show();
//    }

    public static void main(String[] args) {
        launch(args);
    }
}