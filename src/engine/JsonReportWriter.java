package engine;

import java.util.List;

public class JsonReportWriter {

    public static String toJson(Result result, ParsedEmail email, AppConfig cfg) {
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"subject\": \"").append(escape(email.subject)).append("\",\n");
        json.append("  \"from\": \"").append(escape(email.from)).append("\",\n");
        json.append("  \"risk\": \"")
            .append(result.getRiskLevel(cfg))
            .append("\",\n");
        json.append("  \"totalScore\": ")
            .append(result.getTotalScore(cfg))
            .append(",\n");
        json.append("  \"scanners\": [\n");

        List<ScanResult> scanners = result.getScannerResults();

        for (int i = 0; i < scanners.size(); i++) {
            ScanResult r = scanners.get(i);

            json.append("    {\n");
            json.append("      \"name\": \"")
                .append(escape(r.getScannerName()))
                .append("\",\n");
            json.append("      \"category\": \"")
                .append(r.getCategory())
                .append("\",\n");
            json.append("      \"score\": ")
                .append(r.getScore())
                .append(",\n");
            json.append("      \"maxScore\": ")
                .append(r.getMaxScore())
                .append(",\n");
            json.append("      \"findings\": [\n");

            List<Finding> findings = r.getFindings();
            for (int j = 0; j < findings.size(); j++) {
                Finding f = findings.get(j);

                json.append("        {\n");
                json.append("          \"message\": \"")
                    .append(escape(f.getMessage()))
                    .append("\",\n");
                json.append("          \"category\": \"")
                    .append(f.getCategory())
                    .append("\",\n");
                json.append("          \"score\": ")
                    .append(f.getScore())
                    .append("\n");
                json.append("        }");

                if (j < findings.size() - 1) json.append(",");
                json.append("\n");
            }

            json.append("      ]\n");
            json.append("    }");

            if (i < scanners.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}");

        return json.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
