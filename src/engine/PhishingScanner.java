package engine;

public class PhishingScanner implements Scanner {

    private final AppConfig cfg;

    public PhishingScanner(AppConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public ScanResult scan(ParsedEmail email) {

        ScanResult result = new ScanResult(
                "PhishingScanner",
                ScanCategory.RISK,
                cfg.getInt("score.phish.max", 15)
        );

        if (email == null) {
            return result;
        }

        String fromDomain   = getDomain(email.from);
        String replyDomain  = getDomain(email.replyTo);
        String returnDomain = getDomain(email.returnPath);

        boolean fromReplyMismatch  = false;
        boolean fromReturnMismatch = false;

        // -------------------------------------------------
        // FROM ≠ REPLY-TO → INDICATOR
        // -------------------------------------------------
        if (!fromDomain.isEmpty()
                && !replyDomain.isEmpty()
                && !fromDomain.equalsIgnoreCase(replyDomain)) {

            fromReplyMismatch = true;

            result.addFinding(new Finding(
                    "Absender-Domain unterscheidet sich von Reply-To (" +
                    fromDomain + " ≠ " + replyDomain + ")",
                    ScanCategory.INDICATOR,
                    cfg.getInt("score.phish.replyto", 2),
                    FindingType.OTHER,
                    null,
                    "phish.replyto_mismatch",
                    "FROM und REPLY-TO zeigen auf unterschiedliche Domains"
            ));
        }

        // -------------------------------------------------
        // FROM ≠ RETURN-PATH → INDICATOR
        // -------------------------------------------------
        if (!fromDomain.isEmpty()
                && !returnDomain.isEmpty()
                && !fromDomain.equalsIgnoreCase(returnDomain)) {

            fromReturnMismatch = true;

            result.addFinding(new Finding(
                    "Return-Path Domain weicht vom Absender ab (" +
                    fromDomain + " ≠ " + returnDomain + ")",
                    ScanCategory.INDICATOR,
                    cfg.getInt("score.phish.returnpath", 2),
                    FindingType.OTHER,
                    null,
                    "phish.returnpath_mismatch",
                    "MAIL FROM / Return-Path unterscheidet sich vom sichtbaren Absender"
            ));
        }

        // -------------------------------------------------
        // Kritische Kombination → RISK
        // -------------------------------------------------
        if (fromReplyMismatch && fromReturnMismatch) {

            result.addFinding(new Finding(
                    "Kritisches Spoofing-Muster erkannt",
                    ScanCategory.RISK,
                    cfg.getInt("score.phish.combo", 8),
                    FindingType.OTHER,
                    null,
                    "phish.spoofing_combo",
                    "FROM, REPLY-TO und RETURN-PATH zeigen auf unterschiedliche Domains"
            ));
        }

        return result;
    }

    // -------------------------------------------------
    // Domain-Extraktion
    // -------------------------------------------------
    private String getDomain(String value) {
        if (value == null) return "";

        value = value.toLowerCase();

        int lt = value.indexOf('<');
        int gt = value.indexOf('>');

        if (lt != -1 && gt != -1 && gt > lt) {
            value = value.substring(lt + 1, gt);
        }

        int at = value.lastIndexOf('@');
        if (at == -1) return "";

        return value.substring(at + 1).trim();
    }
}
