// ConfigManager.java
package proj.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import proj.app.SimulationConfig;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final String CONFIG_DIRECTORY = "configs";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveConfig(SimulationConfig config) throws IOException {
        Files.createDirectories(Paths.get(CONFIG_DIRECTORY));
        String fileName = CONFIG_DIRECTORY + "/" + config.getConfigName() + ".json";

        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(config, writer);
        }
    }

    public static SimulationConfig loadConfig(String configName) throws IOException {
        String fileName = CONFIG_DIRECTORY + "/" + configName + ".json";
        try (Reader reader = new FileReader(fileName)) {
            return gson.fromJson(reader, SimulationConfig.class);
        }
    }

    public static List<String> getAvailableConfigs() throws IOException {
        List<String> configs = new ArrayList<>();
        Path configDir = Paths.get(CONFIG_DIRECTORY);

        if (Files.exists(configDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDir, "*.json")) {
                for (Path path : stream) {
                    String fileName = path.getFileName().toString();
                    configs.add(fileName.substring(0, fileName.length() - 5)); // Remove .json extension
                }
            }
        }

        return configs;
    }
}