package engine;

public class ScanUtils {

    private ScanUtils() {
        // Utility-Klasse
    }

    public static ScanResult safeScan(Scanner scanner, ParsedEmail email) {
        try {
            return scanner.scan(email);
        } catch (Exception e) {

            // ⛑ Fallback-Result (neutral, nicht eskalierend)
            ScanResult r = new ScanResult(
                    scanner.getClass().getSimpleName(),
                    ScanCategory.INDICATOR,
                    0 // kein Einfluss auf Score
            );

            r.addFinding(
                    "Scanner-Fehler – übersprungen: "
                            + e.getClass().getSimpleName(),
                    ScanCategory.INDICATOR,
                    0
            );

            return r;
        }
    }
}
