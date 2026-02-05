package engine;

import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttachmentScanner implements Scanner {

    private static final Pattern ATTACHMENT_PATTERN =
            Pattern.compile("(?i)attachment:\\s*([\\w\\-.]+)");

    private final AppConfig cfg;

    private final Set<String> highRisk;
    private final Set<String> macroRisk;
    private final Set<String> archiveRisk;

    public AttachmentScanner(AppConfig cfg) {
        this.cfg = cfg;

        Properties props = new Properties();

        try {
            // 🔑 externe Datei sicherstellen & laden
            ConfigFiles.ensureExists("attachment.properties");
            try (InputStream in = ConfigFiles.open("attachment.properties")) {
                props.load(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ⚠️ Sets kommen bewusst aus AppConfig (app.properties)
        highRisk    = cfg.getSet("attachment.high");
        macroRisk   = cfg.getSet("attachment.macro");
        archiveRisk = cfg.getSet("attachment.archive");
    }

    @Override
    public ScanResult scan(ParsedEmail email) {

        ScanResult result = new ScanResult(
                "AttachmentScanner",
                ScanCategory.RISK,
                cfg.getInt("score.attachment.max", 15)
        );

        if (email == null) return result;

        String content =
                (email.rawHeaders != null ? email.rawHeaders : "") +
                "\n" +
                (email.body != null ? email.body : "");

        Matcher matcher = ATTACHMENT_PATTERN.matcher(content);

        while (matcher.find()) {
            analyzeAttachment(matcher.group(1), result);
        }

        return result;
    }

    // ------------------------------------
    // Einzelanalyse
    // ------------------------------------
    private void analyzeAttachment(String filename, ScanResult result) {

        String ext = getExtension(filename);

        // 🟡 Attachment vorhanden
        result.addFinding(new Finding(
                null,
                ScanCategory.INDICATOR,
                cfg.getInt("score.attachment.present", 2),
                FindingType.ATTACHMENT,
                null,
                "attachment.present",
                filename
        ));

        // 🔴 High risk
        if (highRisk.contains(ext)) {
            result.addFinding(new Finding(
                    null,
                    ScanCategory.RISK,
                    cfg.getInt("score.attachment.high", 8),
                    FindingType.ATTACHMENT,
                    null,
                    "attachment.highrisk",
                    filename
            ));
            return;
        }

        // 🔴 Macro risk
        if (macroRisk.contains(ext)) {
            result.addFinding(new Finding(
                    null,
                    ScanCategory.RISK,
                    cfg.getInt("score.attachment.macro", 6),
                    FindingType.ATTACHMENT,
                    null,
                    "attachment.macro",
                    filename
            ));
            return;
        }

        // 🟠 Archive
        if (archiveRisk.contains(ext)) {
            result.addFinding(new Finding(
                    null,
                    ScanCategory.RISK,
                    cfg.getInt("score.attachment.archive", 4),
                    FindingType.ATTACHMENT,
                    null,
                    "attachment.archive",
                    filename
            ));
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot == -1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }
}
