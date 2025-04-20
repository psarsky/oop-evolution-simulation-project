package proj.app.services;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

/**
 * An implementation of the {@link IFileSaveService} interface using JavaFX's {@link FileChooser}.
 */
public class JavaFXFileSaveService implements IFileSaveService {
    private final Window ownerWindow; // Optional owner window for the dialog

    /**
     * Constructs the service.
     *
     * @param ownerWindow The parent {@link Window} (e.g., Stage) for the file chooser dialog.
     *                    Providing an owner makes the dialog modal relative to that window. Can be null.
     */
    public JavaFXFileSaveService(Window ownerWindow) {
        this.ownerWindow = ownerWindow; // Can be null
    }

    /**
     * {@inheritDoc}
     * Displays a JavaFX {@link FileChooser} configured for saving a file.
     */
    @Override
    public File selectSaveFile(String initialFileName, String description, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File As...");
        fileChooser.setInitialFileName(initialFileName);

        // Add extension filter if provided
        if (extensions != null && extensions.length > 0 && description != null && !description.isBlank()) {
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter(description, extensions);
            fileChooser.getExtensionFilters().add(extFilter);
            // Optionally add an "All Files" filter
            // fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        }

        // Show the save dialog, using the owner window if available
        return fileChooser.showSaveDialog(ownerWindow);
        // Returns null if the user cancels
    }
}