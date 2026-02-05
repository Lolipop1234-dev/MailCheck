package engine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigLoader {

    private final Map<String, List<String>> lists = new HashMap<>();

    public ConfigLoader() {
        // 🔥 ALLE BENÖTIGTEN LISTEN REGISTRIEREN
        loadList("keywords");
        loadList("ti-blacklist");
        loadList("whitelist");
    }

    // ==============================
    // Textdateien aus resources/config
    // ==============================
    private void loadList(String name) {

        String path = "config/" + name + ".txt";
        List<String> list = new ArrayList<>();

        try (InputStream in = getClass()
                .getClassLoader()
                .getResourceAsStream(path)) {

            if (in == null) {
                System.err.println("⚠ Datei nicht gefunden (Classpath): " + path);
                lists.put(name, list);
                return;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    list.add(line.toLowerCase()); // 🔥 normalize
                }
            }

            System.out.println("✔ Geladen: " + name + " (" + list.size() + " Einträge)");

        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden von " + path, e);
        }

        lists.put(name, list);
    }

    public List<String> getList(String key) {
        return lists.getOrDefault(key, Collections.emptyList());
    }
}
