package engine;

import i18n.I18n;

public class Finding {

    private final String message;      // technischer / Fallback-Text
    private final ScanCategory category;
    private final int score;

    private final FindingType type;
    private final String keyword;      // optional (nur bei KEYWORD)

    private final String code;         // z.B. "phish.spoofing"
    private final String details;      // technische Details (optional)

    // ------------------------------------
    // Standard-Finding (ohne Keyword)
    // ------------------------------------
    public Finding(
            String message,
            ScanCategory category,
            int score,
            FindingType type,
            String code,
            String details
    ) {
        this.message = message;
        this.category = category;
        this.score = score;
        this.type = type;
        this.keyword = null;
        this.code = code;
        this.details = details;
    }

    // ------------------------------------
    // Keyword-Finding
    // ------------------------------------
    public Finding(
            String message,
            ScanCategory category,
            int score,
            FindingType type,
            String keyword,
            String code,
            String details
    ) {
        this.message = message;
        this.category = category;
        this.score = score;
        this.type = type;
        this.keyword = keyword;
        this.code = code;
        this.details = details;
    }

    // ------------------------------------
    // Getter
    // ------------------------------------
    public String getMessage() {
        return message;
    }

    public ScanCategory getCategory() {
        return category;
    }

    public int getScore() {
        return score;
    }

    public FindingType getType() {
        return type;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    // ------------------------------------
    // Convenience
    // ------------------------------------
    public boolean hasKeyword() {
        return type == FindingType.KEYWORD
                && keyword != null
                && !keyword.isBlank();
    }

    /**
     * Menschlich verständliche Meldung (I18n),
     * fällt sauber auf message zurück
     */
    public String getHumanMessage() {
        if (code != null && !code.isBlank()) {
            return I18n.t("finding." + code);
        }
        return message;
    }
}
