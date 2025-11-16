package proj.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import proj.simulation.SimulationProperties;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the saving, loading, deleting, and listing of simulation configuration files.
 * Configurations are stored as JSON files in a dedicated directory.
 */
public class ConfigManager {
    private static final String CONFIG_DIRECTORY = "configs";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Saves the given simulation configuration to a JSON file named after the configuration name.
     * Creates the configuration directory if it doesn't exist.
     *
     * @param config The {@link SimulationProperties} object to save.
     * @throws IOException If an I/O error occurs during file writing or directory creation.
     */
    public static void saveConfig(SimulationProperties config) throws IOException {
        Path configDirPath = Paths.get(CONFIG_DIRECTORY);
        Files.createDirectories(configDirPath); // Ensure the directory exists

        Path filePath = configDirPath.resolve(config.getConfigName() + ".json");

        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(config, writer);
        }
    }

    /**
     * Loads a simulation configuration from a JSON file based on its name.
     *
     * @param configName The name of the configuration to load (without the .json extension).
     * @return The loaded {@link SimulationProperties} object.
     * @throws IOException If an I/O error occurs during file reading or if the file is not found.
     */
    public static SimulationProperties loadConfig(String configName) throws IOException {
        Path filePath = Paths.get(CONFIG_DIRECTORY, configName + ".json");
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Configuration file not found: " + filePath);
        }
        try (Reader reader = Files.newBufferedReader(filePath)) {
            return gson.fromJson(reader, SimulationProperties.class);
        }
    }

    /**
     * Deletes the configuration file associated with the given name.
     *
     * @param configName The name of the configuration to delete (without the .json extension).
     * @throws IOException           If an I/O error occurs during file deletion.
     * @throws FileNotFoundException If the configuration file does not exist.
     */
    public static void deleteConfig(String configName) throws IOException {
        Path filePath = Paths.get(CONFIG_DIRECTORY, configName + ".json");
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {
            throw new FileNotFoundException("Configuration file not found for deletion: " + filePath);
        }
    }

    /**
     * Retrieves a list of names for all available configuration files (without the .json extension).
     *
     * @return A {@link List} of available configuration names.
     * @throws IOException If an I/O error occurs while reading the configuration directory.
     */
    public static List<String> getAvailableConfigs() throws IOException {
        Path configDir = Paths.get(CONFIG_DIRECTORY);

        if (!Files.isDirectory(configDir)) {
            return new ArrayList<>(); // Return empty list if directory doesn't exist
        }

        List<String> configs = new ArrayList<>();
        try (Stream<Path> stream = Files.list(configDir)) {
            configs = stream
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.toLowerCase().endsWith(".json"))
                    .map(name -> name.substring(0, name.length() - 5)) // Remove .json extension
                    .collect(Collectors.toList());
        }
        return configs;
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ConfigManager() {
        // Utility class should not be instantiated.
    }
}