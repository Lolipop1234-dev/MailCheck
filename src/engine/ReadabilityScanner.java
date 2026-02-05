package engine;

public class ReadabilityScanner implements Scanner {

    private final AppConfig cfg;

    public ReadabilityScanner(AppConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public ScanResult scan(ParsedEmail email) {

        ScanResult result = new ScanResult(
                "ReadabilityScanner",
                ScanCategory.INDICATOR,
                cfg.getInt("score.readability.max", 10)
        );

        if (email == null || email.body == null || email.body.trim().isEmpty()) {
            return result;
        }

        String text = email.body.trim();

        int wordCount = countWords(text);
        int sentenceCount = countSentences(text);
        double index = colemanLiauIndex(text);

        // ------------------------------------
        // 1️⃣ Sprach-Komplexität (INDICATOR)
        // ------------------------------------
        if (index < 4.0) {
            result.addFinding(new Finding(
                    "Extrem einfache Sprache erkannt (Lesbarkeitsindex: " + fmt(index) + ")",
                    ScanCategory.INDICATOR,
                    cfg.getInt("score.readability.very_simple", 5),
                    FindingType.OTHER,
                    null,
                    "readability.extreme_simple",
                    "Sehr einfache Sprache kann auf Massen- oder Betrugsmails hindeuten"
            ));
        }
        else if (index < 6.0) {
            result.addFinding(new Finding(
                    "Sehr einfache Sprache erkannt (Lesbarkeitsindex: " + fmt(index) + ")",
                    ScanCategory.INDICATOR,
                    cfg.getInt("score.readability.simple", 4),
                    FindingType.OTHER,
                    null,
                    "readability.very_simple",
                    "Ungewöhnlich einfache Sprache für geschäftliche Kommunikation"
            ));
        }
        else if (index < 8.0) {
            result.addFinding(new Finding(
                    "Auffällig einfache Sprache (Lesbarkeitsindex: " + fmt(index) + ")",
                    ScanCategory.INDICATOR,
                    cfg.getInt("score.readability.suspicious", 2),
                    FindingType.OTHER,
                    null,
                    "readability.simple",
                    "Einfache Sprache kann auf automatisierte Inhalte hindeuten"
            ));
        }

        // ------------------------------------
        // 2️⃣ Sehr kurze + einfache Mails
        // ------------------------------------
        if (wordCount <= 40 && sentenceCount <= 3) {

            result.addFinding(new Finding(
                    "Sehr kurze und einfache E-Mail (" +
                    wordCount + " Wörter, " +
                    sentenceCount + " Sätze)",
                    ScanCategory.INFO,
                    cfg.getInt("score.readability.short_simple", 2),
                    FindingType.OTHER,
                    null,
                    "readability.short_mail",
                    "Kurze, einfache Mails werden häufig für Phishing genutzt"
            ));
        }

        return result;
    }

    // -------------------------------------------------
    // Coleman-Liau Index
    // -------------------------------------------------
    private double colemanLiauIndex(String text) {

        int letters = 0;
        int words = 0;
        int sentences = 0;
        boolean inWord = false;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (!inWord) {
                    words++;
                    inWord = true;
                }
            } else {
                inWord = false;
            }

            if (c == '.' || c == '!' || c == '?') {
                sentences++;
            }
        }

        if (words == 0) return 0;

        double L = (letters * 100.0) / words;
        double S = (sentences * 100.0) / words;

        return 0.0588 * L - 0.296 * S - 15.8;
    }

    private int countWords(String text) {
        return text.trim().split("\\s+").length;
    }

    private int countSentences(String text) {
        return text.split("[.!?]+").length;
    }

    private String fmt(double d) {
        return String.format("%.2f", d);
    }
}
