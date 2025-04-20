package proj.app.services;

import java.io.File;

/**
 * Interface defining a contract for a service that allows the user to select a file path for saving data.
 * This abstraction decouples the core application logic from the specific UI framework's file chooser implementation.
 */
public interface IFileSaveService {

    /**
     * Displays a file save dialog to the user, allowing them to choose a location and filename.
     *
     * @param initialFileName A suggested default filename for the save dialog.
     * @param description     A description of the file type(s) being saved (e.g., "JSON files (*.json)").
     * @param extensions      Varargs array of allowed file extensions (e.g., "*.json", "*.txt").
     *                        Implementations should handle the formatting required by the underlying UI toolkit.
     * @return The selected {@link File} object representing the chosen path, or {@code null} if the user cancelled the dialog.
     */
    File selectSaveFile(String initialFileName, String description, String... extensions);
}