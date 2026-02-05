package i18n;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class I18n {

    private static Locale locale = Locale.GERMAN;
    private static Properties props = new Properties();

    static {
        load();
    }

    public static void setLocale(Locale l) {
        locale = l;
        load();
    }

    public static String t(String key) {
        return props.getProperty(key, "??" + key + "??");
    }

    private static void load() {
        props.clear();

        String lang = locale.getLanguage(); // de / en
        String file = "i18n/messages_" + lang + ".properties";

        try (InputStream in = I18n.class
                .getClassLoader()
                .getResourceAsStream(file)) {

            if (in == null) {
                System.err.println("❌ i18n Datei nicht gefunden: " + file);
                return;
            }

            props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}