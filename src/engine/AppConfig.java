package engine;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class AppConfig {

    // =================================================
    // Konfig-Pfad (neben der EXE)
    // =================================================
    private static final File USER_CONFIG =
            new File("config/app.properties");

    private final Properties props = new Properties();

    private Integer yellowThreshold = null;
    private Integer redThreshold = null;

    // =================================================
    // Konstruktor
    // =================================================
    public AppConfig() {
        load();
    }

    // =================================================
    // Laden (extern → intern)
    // =================================================
    private void load() {
    try {
        // 🔑 sicherstellen, dass externe Dateien existieren
        ConfigFiles.ensureExists("keywords.txt");
        ConfigFiles.ensureExists("attachment.properties");
        ConfigFiles.ensureExists("whitelist.txt");


        if (USER_CONFIG.exists()) {
            try (InputStream in = new FileInputStream(USER_CONFIG)) {
                props.load(in);
            }
        } else {
            try (InputStream in = getClass()
                    .getClassLoader()
                    .getResourceAsStream("config/app.properties")) {

                if (in != null) {
                    props.load(in);
                    save();
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // =================================================
    // Speichern
    // =================================================
    private void save() {
        try {
            USER_CONFIG.getParentFile().mkdirs();
            try (OutputStream out = new FileOutputStream(USER_CONFIG)) {
                props.store(out, "MailCheck Configuration");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =================================================
    // Getter
    // =================================================
    public boolean getBool(String key, boolean def) {
        String v = props.getProperty(key);
        return v != null ? Boolean.parseBoolean(v) : def;
    }

    public int getInt(String key, int def) {
        try {
            return Integer.parseInt(props.getProperty(key));
        } catch (Exception e) {
            return def;
        }
    }

    public String getString(String key, String def) {
        return props.getProperty(key, def);
    }

    public Set<String> getSet(String key) {
        Set<String> result = new HashSet<>();
        String raw = props.getProperty(key);
        if (raw == null) return result;

        for (String p : raw.split(",")) {
            String v = p.trim().toLowerCase();
            if (!v.isEmpty()) result.add(v);
        }
        return result;
    }

    // =================================================
    // Schreiben (Persistenz)
    // =================================================
    public void setBool(String key, boolean value) {
        props.setProperty(key, String.valueOf(value));
        save();
    }

    // =================================================
    // Scanner
    // =================================================
    public boolean isScannerEnabled(String name) {
        return getBool("scanner." + name, true);
    }

    public void setScannerEnabled(String name, boolean enabled) {
        setBool("scanner." + name, enabled);
    }

    // =================================================
    // Schwellen
    // =================================================
    public int getYellowThreshold() {
        return yellowThreshold != null
                ? yellowThreshold
                : getInt("risk.yellow.min", 20);
    }

    public int getRedThreshold() {
        return redThreshold != null
                ? redThreshold
                : getInt("risk.red.min", 35);
    }

    public void setYellowThreshold(int v) {
        yellowThreshold = v;
    }

    public void setRedThreshold(int v) {
        redThreshold = v;
    }

    public boolean hasDynamicThresholds() {
        return yellowThreshold != null || redThreshold != null;
    }

    public void setString(String key, String value) {
    props.setProperty(key, value);
    save();
    }

}
