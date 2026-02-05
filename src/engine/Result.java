package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



public class Result {

    private final List<ScanResult> scannerResults = new ArrayList<>();

    public void addScanResult(ScanResult scanResult) {
        if (scanResult != null) {
            scannerResults.add(scanResult);
        }
    }

    public List<ScanResult> getScannerResults() {
        return scannerResults;
    }

    // ---------------------------------
    // Score (nur erklärend)
    // ---------------------------------
    public int getTotalScore(AppConfig cfg) {
        int sum = 0;
        int max = 0;

        for (ScanResult r : scannerResults) {
            sum += r.getScore();
            max += r.getMaxScore();
        }

        if (max == 0) return 0;
        return Math.min((int) Math.round(sum * 100.0 / max), 100);
    }

    // ---------------------------------
    // 🔥 ENTSCHEIDUNGSLOGIK (korrekt!)
    // ---------------------------------
    public String getRiskLevel(AppConfig cfg) {

    long risks = countRisks();
    long indicators = countIndicators();

    int hardRiskScore =
        cfg.getInt("risk.hard.min_score", 8);

    if (hasHardRisk(hardRiskScore) || risks >= 2) {
        return "ROT";
    }

    if (indicators >= cfg.getInt("risk.yellow.min_indicators", 2)) {
        return "GELB";
    }

    return "GRÜN";
}
public List<Finding> getAllFindings() {
    return scannerResults.stream()
            .flatMap(r -> r.getFindings().stream())
            .collect(Collectors.toList());
}


    public long countIndicators() {
    return getAllFindings().stream()
            .filter(f -> f.getCategory() == ScanCategory.INDICATOR)
            .count();
}


public long countRisks() {
    return getAllFindings().stream()
            .filter(f -> f.getCategory() == ScanCategory.RISK)
            .count();
}


public boolean hasHardRisk(int minScore) {
    return getAllFindings().stream()
            .anyMatch(f ->
                    f.getCategory() == ScanCategory.RISK &&
                    f.getScore() >= minScore
            );
}



    // ---------------------------------
    // Erklärung
    // ---------------------------------
    public String getTriggeredReasons() {
        return scannerResults.stream()
                .flatMap(r -> r.getFindings().stream())
                .map(f -> f.getCategory() + ":" + f.getMessage())
                .collect(Collectors.joining("|"));
    }
}
