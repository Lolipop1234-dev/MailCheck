package engine;

import java.util.*;
import java.util.regex.*;

public class IBANScanner implements Scanner {

    private final AppConfig cfg;
    private final Map<String, Integer> ibanLengths;

    private static final Pattern IBAN_PATTERN =
            Pattern.compile("\\b[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}\\b");

    public IBANScanner(AppConfig cfg) {
        this.cfg = cfg;
        this.ibanLengths = loadIbanLengths();
    }

    @Override
    public ScanResult scan(ParsedEmail email) {

        ScanResult result = new ScanResult(
                "IBANScanner",
                ScanCategory.RISK,
                cfg.getInt("score.iban.max", 20)
        );

        if (email == null || email.body == null || email.body.isEmpty()) {
            return result;
        }

        Matcher matcher =
                IBAN_PATTERN.matcher(email.body.replace(" ", ""));

        while (matcher.find()) {
            analyzeIban(matcher.group(), result);
        }

        return result;
    }

    // ------------------------------------
    // Analyse einer IBAN
    // ------------------------------------
    private void analyzeIban(String iban, ScanResult result) {

        String country = iban.substring(0, 2);

        // 🟡 IBAN erkannt
        result.addFinding(new Finding(
                "iban.present",
                ScanCategory.INDICATOR,
                cfg.getInt("score.iban.present", 2),
                FindingType.IBAN,
                maskIban(iban),
                "iban.present",
                null
        ));

        // 🔴 Unbekanntes Land
        if (!ibanLengths.containsKey(country)) {
            result.addFinding(new Finding(
                    "iban.unknown_country",
                    ScanCategory.RISK,
                    cfg.getInt("score.iban.unknown_country", 4),
                    FindingType.IBAN,
                    country,
                    "iban.unknown_country",
                    null
            ));
            return;
        }

        // 🔴 Ungültige Länge
        int expectedLength = ibanLengths.get(country);
        if (iban.length() != expectedLength) {
            result.addFinding(new Finding(
                    "iban.length_invalid",
                    ScanCategory.RISK,
                    cfg.getInt("score.iban.length_invalid", 5),
                    FindingType.IBAN,
                    country,
                    "iban.length_invalid",
                    String.valueOf(expectedLength)
            ));
            return;
        }

        // 🔴 Ungültige Checksumme
        if (!isValidIbanChecksum(iban)) {
            result.addFinding(new Finding(
                    "iban.checksum_invalid",
                    ScanCategory.RISK,
                    cfg.getInt("score.iban.checksum_invalid", 8),
                    FindingType.IBAN,
                    null,
                    "iban.checksum_invalid",
                    null
            ));
        }
    }

    // ------------------------------------
    // IBAN-Längen aus app.properties
    // ------------------------------------
    private Map<String, Integer> loadIbanLengths() {

        Map<String, Integer> map = new HashMap<>();

        String raw = cfg.getString(
                "iban.lengths",
                "DE:22,AT:20,CH:21,NL:18,FR:27,BE:16,ES:24,IT:27,PL:28"
        );

        for (String entry : raw.split(",")) {
            String[] parts = entry.trim().split(":");
            if (parts.length == 2) {
                try {
                    map.put(parts[0].toUpperCase(), Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return map;
    }

    // ------------------------------------
    // IBAN Checksum (ISO 13616)
    // ------------------------------------
    private boolean isValidIbanChecksum(String iban) {
        try {
            String rearranged = iban.substring(4) + iban.substring(0, 4);
            StringBuilder numeric = new StringBuilder();

            for (char c : rearranged.toCharArray()) {
                if (Character.isDigit(c)) {
                    numeric.append(c);
                } else {
                    numeric.append((int) c - 55);
                }
            }

            return mod97(numeric.toString()) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private int mod97(String number) {
        int checksum = 0;
        for (int i = 0; i < number.length(); i++) {
            checksum = (checksum * 10 + (number.charAt(i) - '0')) % 97;
        }
        return checksum;
    }

    private String maskIban(String iban) {
        if (iban.length() < 8) return iban;
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 4);
    }
}
