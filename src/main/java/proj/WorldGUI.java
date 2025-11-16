package proj;

import javafx.application.Application;
import proj.app.App;

/**
 * Entry point class for launching the JavaFX GUI version of the Evolution Simulator.
 * Delegates the launch process to the {@link proj.app.App} class.
 */
public class WorldGUI {

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args Command line arguments passed to the application (typically none used).
     */
    public static void main(String[] args) {
        // Launch the JavaFX application defined in the App class
        Application.launch(App.class, args);
    }
}