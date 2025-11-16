/**
 * Defines the module for the Evolution Simulator application (proj.app).
 * Specifies required modules (javafx.controls, javafx.fxml, com.google.gson)
 * and opens packages to allow JavaFX FXML loading and reflection (e.g., for Gson).
 */
module proj.app {
    // --- Required Modules ---
    requires javafx.controls; // For UI controls (buttons, labels, canvas, etc.)
    requires javafx.fxml;     // For loading FXML layout files
    requires com.google.gson; // For JSON serialization/deserialization (config, stats)

    // --- Package Opening ---
    // Opens packages to javafx.fxml to allow FXML loader access to controllers, viewmodels, etc.
    opens proj.app to javafx.fxml;
    opens proj.app.controllers to javafx.fxml;
    opens proj.app.viewmodels to javafx.fxml;
    // opens proj.app.render to javafx.fxml; // Likely not needed unless custom components are in FXML
    // opens proj.app.state to javafx.fxml; // Likely not needed unless state objects are directly used in FXML

    // --- Package Exporting ---
    // Exports packages to make their public types accessible outside this module (if needed by other modules)
    exports proj.app; // Export the main App class
    exports proj.app.controllers;
    exports proj.app.viewmodels;
    exports proj.app.render; // Export renderer if used externally
    exports proj.app.state;  // Export state classes (e.g., snapshots) if used externally
    exports proj.app.services; // Export service interfaces

    // Also need to open SimulationProperties to GSON for reflection
    opens proj.simulation to com.google.gson;
}