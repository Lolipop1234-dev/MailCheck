package engine;

import java.io.InputStream;
import java.util.Properties;

public class AwarenessConfig {

    private final Properties props = new Properties();

    public AwarenessConfig(String lang) {
        String file = "awareness_" + lang + ".properties";

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(file)) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load awareness config: " + file, e);
        }
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    public String getOrDefault(String key, String def) {
        return props.getProperty(key, def);
    }
}
