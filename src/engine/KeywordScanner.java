package engine;

import java.io.*;
import java.util.*;

public class KeywordScanner implements Scanner {

    private final Set<String> keywords;
    private final Set<String> riskKeywords;
    private final AppConfig cfg;

    public KeywordScanner(AppConfig cfg) {
        this.cfg = cfg;

        this.keywords = loadKeywords("keywords.txt");
        this.riskKeywords = loadKeywords("keywords-risk.txt");
    }

    private Set<String> loadKeywords(String file) {
        Set<String> result = new HashSet<>();

        try {
            ConfigFiles.ensureExists(file);

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(ConfigFiles.open(file)))) {

                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    result.add(line);
                }
            }

            System.out.println("✅ Keywords geladen (" + file + "): " + result.size());

        } catch (Exception e) {
            System.err.println("❌ Fehler beim Laden von " + file);
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public ScanResult scan(ParsedEmail email) {

        ScanResult result = new ScanResult(
                "KeywordScanner",
                ScanCategory.INDICATOR,
                cfg.getInt("score.keyword.max", 8)
        );

        if (email == null || keywords.isEmpty()) {
            return result;
        }

        String subject = email.subject != null ? email.subject.toLowerCase() : "";
        String body    = email.body    != null ? email.body.toLowerCase()    : "";

        Set<String> indicatorHits = new HashSet<>();
        Set<String> riskHits = new HashSet<>();

        for (String keyword : keywords) {

            boolean inSubject = subject.contains(keyword);
            boolean inBody = body.contains(keyword);

            if (inSubject || inBody) {
                String where = inSubject ? "subject" : "body";

                if (riskKeywords.contains(keyword)) {
                    riskHits.add(keyword + "|" + where);
                } else {
                    indicatorHits.add(keyword + "|" + where);
                }
            }
        }

        int base = cfg.getInt("score.keyword.base", 1);

        for (String hit : indicatorHits) {
            String[] p = hit.split("\\|");
            result.addFinding(new Finding(
                    null,
                    ScanCategory.INDICATOR,
                    base,
                    FindingType.KEYWORD,
                    p[0],
                    "keyword.indicator",
                    p[1]
            ));
        }

        for (String hit : riskHits) {
            String[] p = hit.split("\\|");
            result.addFinding(new Finding(
                    null,
                    ScanCategory.RISK,
                    base + 1,
                    FindingType.KEYWORD,
                    p[0],
                    "keyword.risk",
                    p[1]
            ));
        }

        int totalHits = indicatorHits.size() + riskHits.size();

        if (totalHits >= 2) {
            result.addFinding(new Finding(
                    null,
                    ScanCategory.INDICATOR,
                    cfg.getInt("score.keyword.multi2", 2),
                    FindingType.KEYWORD,
                    null,
                    "keyword.multiple",
                    String.valueOf(totalHits)
            ));
        }

        if (totalHits >= 3) {
            result.addFinding(new Finding(
                    null,
                    ScanCategory.RISK,
                    cfg.getInt("score.keyword.multi3", 4),
                    FindingType.KEYWORD,
                    null,
                    "keyword.many",
                    String.valueOf(totalHits)
            ));
        }

        if (!indicatorHits.isEmpty() && !riskHits.isEmpty()) {
            result.addFinding(new Finding(
                    null,
                    ScanCategory.RISK,
                    cfg.getInt("score.keyword.comboRisk", 3),
                    FindingType.KEYWORD,
                    null,
                    "keyword.combo",
                    null
            ));
        }

        return result;
    }
}
