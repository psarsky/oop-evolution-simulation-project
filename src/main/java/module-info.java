module proj.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens proj.app to javafx.fxml;
    exports proj.app;
}