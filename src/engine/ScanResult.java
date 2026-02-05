package engine;

import java.util.ArrayList;
import java.util.List;

public class ScanResult {

    private final String scannerName;
    private final ScanCategory category;
    private final int maxScore;

    private final List<Finding> findings = new ArrayList<>();

    public ScanResult(String scannerName, ScanCategory category, int maxScore) {
        this.scannerName = scannerName;
        this.category = category;
        this.maxScore = maxScore;
    }

    // ---------------------------------
    // ALT (bleibt für bestehende Scanner)
    // ---------------------------------
    public void addFinding(String message, ScanCategory cat, int score) {
        findings.add(new Finding(
                message,
                cat,
                score,
                FindingType.OTHER,
                null,   // keyword
                null,   // code
                null    // details
        ));
    }

    // ---------------------------------
    // 🆕 NEU (empfohlen)
    // ---------------------------------
    public void addFinding(Finding finding) {
        findings.add(finding);
    }

    // ---------------------------------
    // Ableitungen
    // ---------------------------------
    public boolean isTriggered() {
        return !findings.isEmpty();
    }

    public int getScore() {
        int sum = findings.stream().mapToInt(Finding::getScore).sum();
        return Math.min(sum, maxScore);
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getScannerName() {
        return scannerName;
    }

    public ScanCategory getCategory() {
        return category;
    }

    public List<Finding> getFindings() {
        return findings;
    }
}
