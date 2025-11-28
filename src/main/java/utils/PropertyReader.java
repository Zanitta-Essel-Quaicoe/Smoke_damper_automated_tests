package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    private static Properties properties = new Properties();
    private static boolean isLoaded = false;

    private static void loadProperties() {
        if (isLoaded) return;

        try (InputStream is = PropertyReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RuntimeException("config.properties not found in classpath!");
            }
            properties.load(is);
            isLoaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        loadProperties();
        return properties.getProperty(key);
    }
}
