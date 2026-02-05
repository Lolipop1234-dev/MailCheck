package engine;

public class AwarenessText {

    private static AwarenessConfig awareness =
            new AwarenessConfig("de");

    // 🌍 vom UI aufrufbar
    public static void setLanguage(String lang) {
        awareness = new AwarenessConfig(lang);
    }

    public static String explain(Finding f) {

        if (f.getType() == FindingType.KEYWORD && f.hasKeyword()) {

            String kw = f.getKeyword().toLowerCase();

            String base = awareness.getOrDefault(
                    "keyword." + kw,
                    awareness.get("keyword.generic")
            );

            return "🔑 \"" + kw + "\"\n"
                 + base + "\n\n"
                 + "ℹ️ " + awareness.get("falsepositive");
        }

        String typeKey = "type." + f.getType().name().toLowerCase();
        String typeText = awareness.get(typeKey);

        if (typeText != null) {
            return typeText + "\n\nℹ️ " + awareness.get("falsepositive");
        }

        return awareness.get("falsepositive");
    }
}
