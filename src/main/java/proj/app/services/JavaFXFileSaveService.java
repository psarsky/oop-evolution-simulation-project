package proj.app.services;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Objects;

/**
 * An implementation of the {@link IFileSaveService} interface using JavaFX's {@link FileChooser}.
 * Requires the owning Window context to display the dialog correctly.
 */
public class JavaFXFileSaveService implements IFileSaveService {
    private final Window ownerWindow; // The owner window for the dialog

    /**
     * Constructs the JavaFXFileSaveService.
     *
     * @param ownerWindow The parent {@link Window} (e.g., Stage) for the file chooser dialog.
     *                    Providing an owner makes the dialog modal relative to that window.
     *                    Must not be null.
     * @throws NullPointerException if ownerWindow is null.
     */
    public JavaFXFileSaveService(Window ownerWindow) {
        this.ownerWindow = Objects.requireNonNull(ownerWindow, "Owner Window cannot be null for JavaFXFileSaveService");
    }

    /**
     * Displays a JavaFX {@link FileChooser} configured for saving a file, modal to the
     * owner window provided during construction. Allows the user to select a file path
     * and name for saving.
     *
     * @param initialFileName A suggested default filename to pre-populate the save dialog. Can be null or empty.
     * @param description     A user-friendly description of the file type(s) being saved
     *                        (e.g., "JSON files (*.json)"). Used for the extension filter label.
     *                        Should not be null or blank if extensions are provided.
     * @param extensions      Varargs array of allowed file extensions, including the wildcard pattern
     *                        (e.g., "*.json", "*.txt", "*.*"). Used to create an extension filter.
     *                        Can be null or empty if no filter is desired.
     * @return The selected {@link File} object representing the chosen file path if the user confirms the save operation.
     *         Returns {@code null} if the user cancels the dialog.
     */
    @Override
    public File selectSaveFile(String initialFileName, String description, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File As...");
        fileChooser.setInitialFileName(initialFileName);
        if (extensions != null && extensions.length > 0 && description != null && !description.isBlank()) {
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter(description, extensions);
            fileChooser.getExtensionFilters().add(extFilter);
        }
        return fileChooser.showSaveDialog(ownerWindow);
    }
}