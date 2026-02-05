import engine.*;
import i18n.I18n;
import ui.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
//import org.simplejavamail.converter.EmailConverter; // Aus der externen Library
//import java.nio.file.Files; // Zum Schreiben der temp Datei

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class AwarenessApp {

    private static boolean darkMode = false;
    private static Locale currentLocale = Locale.GERMAN;
    private static final Map<String, Locale> SUPPORTED_LANGUAGES = Map.of(
    "de", Locale.GERMAN,
    "en", Locale.ENGLISH,
    "es", new Locale("es"),
    "fr", new Locale("fr")
);

    

    // Speichert das Risiko-Level zwischen, bis der Nutzer "Enthüllen" klickt
    private static String lastPendingRisk = "";

    // ==================================================
    // MAIN
    // ==================================================
    public static void main(String[] args) {

        FlatLightLaf.setup();

       AppConfig cfg = new AppConfig();

    String lang = cfg.getString("app.language", null);
    String tutMade = cfg.getString("tut.made", "false");

    if (lang != null && SUPPORTED_LANGUAGES.containsKey(lang)) {
     // Sprache wurde schon gespeichert
    currentLocale = SUPPORTED_LANGUAGES.get(lang);
    } else {
    // Erster Start → System-Sprache prüfen
    Locale systemLocale = Locale.getDefault();
    String sysLang = systemLocale.getLanguage();

    if ("false".equalsIgnoreCase(tutMade) && SUPPORTED_LANGUAGES.containsKey(sysLang)) {
        currentLocale = SUPPORTED_LANGUAGES.get(sysLang);
    } else {
        currentLocale = Locale.ENGLISH; // Fallback
    }

    // Sprache speichern
    saveLanguage(currentLocale);
    }

    I18n.setLocale(currentLocale);
    SwingUtilities.invokeLater(AwarenessApp::createUI);
        // --- HIER IST DIE LOGIK FÜR DEN ERSTEN START ---
        // Wenn tut.made "false" ist (oder nicht existiert), Tutorial öffnen
    if (!new AppConfig().getBool("tut.made", false)) {
    openTutorial();
    new AppConfig().setBool("tut.made", true);
    }
    darkMode = cfg.getBool("app.mode", false);
    if (darkMode) {
        FlatDarkLaf.setup();
    } else {
        FlatLightLaf.setup();
    }

 }

    // ==================================================
    // UI
    // ==================================================
    private static void createUI() {

        JFrame frame = new JFrame(I18n.t("app.title"));
        frame.setSize(1000, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        frame.setJMenuBar(createMenuBar(frame));

        RiskPanel riskPanel = new RiskPanel();

        // 1. Border erstellen
        javax.swing.border.TitledBorder riskBorder = BorderFactory.createTitledBorder(I18n.t("risk.title"));
        // 2. Farbe auf Weiß ändern
        riskBorder.setTitleColor(Color.WHITE);
        // 3. Border setzen
        riskPanel.setBorder(riskBorder);
        riskPanel.setPreferredSize(new Dimension(300, 0));

        JTextPane output = new JTextPane();
        output.setEditable(false);
        output.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        output.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane outputScroll = new JScrollPane(output);

        MailListPanel mailListPanel = new MailListPanel();
        mailListPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("btn.email")));

        // --- BUTTONS ---
        JButton analyzeBtn = new JButton(I18n.t("btn.analyze"));
        JButton revealBtn = new JButton(I18n.t("btn.reveal"));

        revealBtn.setEnabled(false);
        revealBtn.setBackground(new Color(59, 130, 246));
        revealBtn.setForeground(Color.WHITE);

        JButton chooseDirBtn = new JButton(I18n.t("btn.choose"));

        // Action Listener für Analyse
        analyzeBtn.addActionListener(e -> {
            File selected = mailListPanel.getSelectedMail();
            if (selected == null) {
                JOptionPane.showMessageDialog(
                        frame,
                        I18n.t("msg.selectMail"),
                        I18n.t("msg.hint"),
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            riskPanel.reset();
            lastPendingRisk = analyzeMail(selected, output);
            revealBtn.setEnabled(true);
            output.setCaretPosition(0);
        });

        // Action Listener für Enthüllen
        revealBtn.addActionListener(e -> {
            if (!lastPendingRisk.isEmpty()) {
                riskPanel.setRisk(lastPendingRisk);
                revealBtn.setEnabled(false);
            }
        });

        chooseDirBtn.addActionListener(e -> mailListPanel.chooseDirectory(frame));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(chooseDirBtn);
        top.add(analyzeBtn);
        top.add(revealBtn);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(mailListPanel, BorderLayout.WEST);
        center.add(outputScroll, BorderLayout.CENTER);
        center.add(riskPanel, BorderLayout.EAST);

        frame.add(top, BorderLayout.NORTH);
        frame.add(center, BorderLayout.CENTER);
        frame.setVisible(true);

        setAppIcon(frame);
    }

    private static void setAppIcon(JFrame frame) {
        try {
            var url = AwarenessApp.class.getResource("/mailcheck.png");
            if (url == null) {
                System.err.println("❌ App-Icon NICHT gefunden: /mailcheck.png");
                return;
            }
            BufferedImage img = ImageIO.read(url);
            frame.setIconImage(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================
    // MENU BAR
    // ==================================================
    private static JMenuBar createMenuBar(JFrame frame) {
        JMenuBar bar = new JMenuBar();
        JMenu settings = new JMenu(I18n.t("btn.setting"));

        // --- NEU: TUTORIAL MENU ITEM ---
        // Fügt einen Button hinzu, um das Tutorial jederzeit erneut zu öffnen
        JMenuItem openTutorialItem = new JMenuItem("Tutorial"); // Du kannst hier auch I18n.t("btn.tutorial") nutzen
        openTutorialItem.addActionListener(e -> openTutorial());
        settings.add(openTutorialItem);
        settings.addSeparator(); 
        // -------------------------------

        JMenuItem scannerSettings = new JMenuItem(I18n.t("btn.scanner"));
        scannerSettings.addActionListener(e ->
                new ScannerSettingsDialog(frame, new AppConfig()).setVisible(true)
        );
        settings.add(scannerSettings);
        
        Locale spanishLocale = new Locale("es");
        Locale frenchLocale = new Locale("fr");
        
        // Scanner-Dateien
        JMenu scannerFiles = new JMenu(I18n.t("btn.config"));

        JMenuItem exportKeywords = new JMenuItem(I18n.t("btn.key.exp"));
        exportKeywords.addActionListener(e -> exportConfig(frame, "keywords.txt"));

        JMenuItem importKeywords = new JMenuItem(I18n.t("btn.key.imp"));
        importKeywords.addActionListener(e -> importConfig(frame, "keywords.txt"));

        JMenuItem exportApp = new JMenuItem(I18n.t("btn.app.exp"));
        exportApp.addActionListener(e -> exportConfig(frame, "app.properties"));

        JMenuItem importApp = new JMenuItem(I18n.t("btn.app.imp"));
        importApp.addActionListener(e -> importConfig(frame, "app.properties"));

        scannerFiles.add(exportKeywords);
        scannerFiles.add(importKeywords);
        scannerFiles.addSeparator();
        scannerFiles.add(exportApp);
        scannerFiles.add(importApp);

        JMenuItem exportAttachment = new JMenuItem(I18n.t("btn.att.exp"));
        exportAttachment.addActionListener(e -> exportConfig(frame, "attachment.properties"));

        JMenuItem importAttachment = new JMenuItem(I18n.t("btn.att.imp"));
        importAttachment.addActionListener(e -> importConfig(frame, "attachment.properties"));

        scannerFiles.addSeparator();
        scannerFiles.add(exportAttachment);
        scannerFiles.add(importAttachment);

        JMenuItem exportWhitelist = new JMenuItem(I18n.t("btn.wlt.exp"));
        exportWhitelist.addActionListener(e -> exportConfig(frame, "whitelist.txt"));

        JMenuItem importWhitelist = new JMenuItem(I18n.t("btn.wlt.imp"));
        importWhitelist.addActionListener(e -> importConfig(frame, "whitelist.txt"));

        scannerFiles.addSeparator();
        scannerFiles.add(exportWhitelist);
        scannerFiles.add(importWhitelist);

        JMenuItem exportBlacklist = new JMenuItem(I18n.t("btn.blt.exp"));
        // Achtung: Hier war im Originalcode ein kleiner Tippfehler beim Listener (exportWhitelist statt exportBlacklist)
        // Habe ich korrigiert:
        exportBlacklist.addActionListener(e -> exportConfig(frame, "ti-blacklist.txt"));

        JMenuItem importBlacklist = new JMenuItem(I18n.t("btn.blt.imp"));
        // Hier auch korrigiert:
        importBlacklist.addActionListener(e -> importConfig(frame, "ti-blacklist.txt"));

        scannerFiles.addSeparator();
        scannerFiles.add(exportBlacklist);
        scannerFiles.add(importBlacklist);

        settings.add(scannerFiles);

        // Design
        JMenu themeMenu = new JMenu(I18n.t("btn.design"));
        JRadioButtonMenuItem light = new JRadioButtonMenuItem(I18n.t("btn.light"), !darkMode);
        JRadioButtonMenuItem dark = new JRadioButtonMenuItem(I18n.t("btn.dark"), darkMode);

        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(light);
        themeGroup.add(dark);

        light.addActionListener(e -> setTheme(false, frame));
        dark.addActionListener(e -> setTheme(true, frame));

        themeMenu.add(light);
        themeMenu.add(dark);

        // Sprache
        JMenu langMenu = new JMenu(I18n.t("btn.language"));
        JRadioButtonMenuItem de = new JRadioButtonMenuItem("Deutsch", currentLocale == Locale.GERMAN);
        JRadioButtonMenuItem en = new JRadioButtonMenuItem("English", currentLocale == Locale.ENGLISH);
        JRadioButtonMenuItem sp = new JRadioButtonMenuItem("Español", currentLocale.getLanguage().equals("es"));
        JRadioButtonMenuItem fr = new JRadioButtonMenuItem("Français", currentLocale.getLanguage().equals("fr"));

        ButtonGroup langGroup = new ButtonGroup();
        langGroup.add(de);
        langGroup.add(en);
        langGroup.add(sp);
        langGroup.add(fr);

        de.addActionListener(e -> setLanguage(Locale.GERMAN, frame));
        en.addActionListener(e -> setLanguage(Locale.ENGLISH, frame));
        sp.addActionListener(e -> setLanguage(spanishLocale, frame));
        fr.addActionListener(e -> setLanguage(frenchLocale, frame));

        langMenu.add(de);
        langMenu.add(en);
        langMenu.add(sp);
        langMenu.add(fr);

        JMenuItem licenses = new JMenuItem(I18n.t("btn.liz"));
        licenses.addActionListener(e -> showLicenses(frame));

        settings.add(themeMenu);
        settings.add(langMenu);
        settings.addSeparator();
        settings.add(licenses);

        bar.add(settings);
        return bar;
    }

    private static void setTheme(boolean dark, JFrame frame) {
    try {
        if (dark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }

        darkMode = dark;

        // 💾 speichern
        new AppConfig().setBool("app.mode", dark);

        SwingUtilities.updateComponentTreeUI(frame);
    } catch (Exception ignored) {}
}


    private static void setLanguage(Locale locale, JFrame frame) {
    currentLocale = locale;
    I18n.setLocale(locale);
    saveLanguage(locale);   // 👈 wichtig!
    frame.dispose();
    createUI();
}


    // ==================================================
    // ANALYSE
    // ==================================================
    private static String analyzeMail(File file, JTextPane output) {
        try {
            ParsedEmail email = new EmailParser().parseFile(file.getAbsolutePath());
            AppConfig cfg = new AppConfig();
            Result total = new Result();

            if (cfg.isScannerEnabled("phishing")) {
                total.addScanResult(ScanUtils.safeScan(new PhishingScanner(cfg), email));
            }
            if (cfg.isScannerEnabled("url")) {
                total.addScanResult(ScanUtils.safeScan(new UrlScanner(cfg), email));
            }
            if (cfg.isScannerEnabled("readability")) {
                total.addScanResult(ScanUtils.safeScan(new ReadabilityScanner(cfg), email));
            }
            if (cfg.isScannerEnabled("attachment")) {
                total.addScanResult(ScanUtils.safeScan(new AttachmentScanner(cfg), email));
            }
            if (cfg.isScannerEnabled("iban")) {
                total.addScanResult(ScanUtils.safeScan(new IBANScanner(cfg), email));
            }
            if (cfg.isScannerEnabled("keyword")) {
                total.addScanResult(ScanUtils.safeScan(new KeywordScanner(cfg), email));
            }

            renderAwarenessResult(total, email, output);

            return total.getRiskLevel(cfg);

        } catch (Exception ex) {
            output.setText("❌ Fehler:\n" + ex.getMessage());
            return "";
        }
    }

    // ==================================================
    // RENDER
    // ==================================================
    private static void renderAwarenessResult(Result result, ParsedEmail email, JTextPane pane) {
        pane.setText("");
        StyledDocument doc = pane.getStyledDocument();

        Style normal = pane.addStyle("normal", null);
        Style awareness = pane.addStyle("awareness", null);
        Style header = pane.addStyle("header", null);

        StyleConstants.setItalic(awareness, true);
        StyleConstants.setForeground(awareness, new Color(100, 150, 200));
        StyleConstants.setFontFamily(header, "Monospaced");
        StyleConstants.setFontSize(header, 12);

        boolean dark = UIManager.getLookAndFeel().getName().toLowerCase().contains("dark");

        Map<String, Style> scannerStyles = new HashMap<>();
        scannerStyles.put("PhishingScanner", createStyle(pane, "phish",
                dark ? new Color(130, 50, 50) : new Color(255, 170, 170)));
        scannerStyles.put("UrlScanner", createStyle(pane, "url",
                dark ? new Color(120, 80, 30) : new Color(255, 210, 150)));
        scannerStyles.put("KeywordScanner", createStyle(pane, "key",
                dark ? new Color(90, 90, 40) : new Color(255, 240, 150)));
        scannerStyles.put("ReadabilityScanner", createStyle(pane, "read",
                dark ? new Color(50, 80, 120) : new Color(190, 220, 255)));
        scannerStyles.put("AttachmentScanner", createStyle(pane, "att",
                dark ? new Color(90, 50, 120) : new Color(220, 190, 255)));
        scannerStyles.put("IBANScanner", createStyle(pane, "iban",
                dark ? new Color(70, 70, 70) : new Color(220, 220, 220)));

        try {
            // STEP 1: ANALYSE & SUMMARY
            doc.insertString(doc.getLength(), buildSummary(result), normal);

            // STEP 2: SELBST-CHECK
            doc.insertString(doc.getLength(), buildAwarenessQuestions(), awareness);

            // E-MAIL HEADER
            if (email.rawHeaders != null && !email.rawHeaders.isBlank()) {
                doc.insertString(doc.getLength(),
                        "📨 E-Mail-Header\n────────────────────────────\n",
                        normal);
                doc.insertString(doc.getLength(),
                        email.rawHeaders + "\n\n",
                        header);
            }

            // E-MAIL BODY
            int bodyStart = doc.getLength();
            if (email.body != null) {
                doc.insertString(doc.getLength(), "E-MAIL INHALT:\n", normal);
                doc.insertString(doc.getLength(), "────────────────────────────\n", normal);
                String body = email.body;
                doc.insertString(doc.getLength(), body, normal);

                // Highlighting
                String lower = body.toLowerCase();
                for (ScanResult sr : result.getScannerResults()) {
                    Style style = scannerStyles.getOrDefault(sr.getScannerName(), normal);

                    for (Finding f : sr.getFindings()) {
                        if (!f.hasKeyword()) continue;

                        String kw = f.getKeyword().toLowerCase();
                        int idx = lower.indexOf(kw);

                        while (idx >= 0) {
                            doc.setCharacterAttributes(
                                    bodyStart + "E-MAIL INHALT:\n────────────────────────────\n".length() + idx,
                                    kw.length(),
                                    style,
                                    false
                            );
                            idx = lower.indexOf(kw, idx + kw.length());
                        }
                    }
                }
            }

            doc.insertString(doc.getLength(), "\n\n", normal);

            // HANDLUNGSEMPFEHLUNG
            doc.insertString(doc.getLength(), buildEmpfehlungen(), awareness);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private static Style createStyle(JTextPane pane, String name, Color bg) {
        Style s = pane.addStyle(name, null);
        StyleConstants.setBackground(s, bg);
        StyleConstants.setUnderline(s, true);
        return s;
    }

    // ==================================================
    // SUMMARY
    // ==================================================
    private static String buildSummary(Result result) {
        StringBuilder sb = new StringBuilder();

        sb.append(I18n.t("section.summary")).append("\n");
        sb.append("────────────────────────────\n\n");

        for (ScanResult sr : result.getScannerResults()) {
            sb.append("• ").append(sr.getScannerName());

            if (!sr.isTriggered()) {
                sb.append(": ").append(I18n.t("summary.none")).append("\n");
                continue;
            }

            double ratio = sr.getMaxScore() == 0 ? 0 : (double) sr.getScore() / sr.getMaxScore();
            sb.append(": ");
            sb.append(ratio >= 0.7 ? I18n.t("risk.high")
                    : ratio >= 0.4 ? I18n.t("risk.medium") : I18n.t("risk.low"));

            sb.append(", ");

            int c = 0;
            for (Finding f : sr.getFindings()) {
                if (c++ > 0) sb.append(", ");
                sb.append(f.getHumanMessage());
                if (c == 3) break;
            }

            if (sr.getFindings().size() > 3) sb.append(" …");
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    // ==================================================
    // AWARENESS & EMPFEHLUNGEN
    // ==================================================
    private static String buildAwarenessQuestions() {
        return I18n.t("section.awareness") + "\n" +
                "──────────────────────────────────\n\n" +
                "1) " + I18n.t("awareness.q1") + "\n" +
                "2) " + I18n.t("awareness.q2") + "\n" +
                "3) " + I18n.t("awareness.q3") + "\n" +
                "4) " + I18n.t("awareness.q4") + "\n" +
                "5) " + I18n.t("awareness.q5") + "\n" +
                "6) " + I18n.t("awareness.q6") + "\n" +
                "7) " + I18n.t("awareness.q7") + "\n\n";
    }

    private static String buildEmpfehlungen() {
        return I18n.t("section.Empfehlung") + "\n" +
                "──────────────────────────────────\n\n" +
                I18n.t("Empfehlung.q7") + "\n" +
                I18n.t("Empfehlung.q8") + " " +
                I18n.t("Empfehlung.q9") + "\n" +
                I18n.t("Empfehlung.q10") + "\n" +
                I18n.t("Empfehlung.q11") + "\n" +
                I18n.t("Empfehlung.q12") + "\n\n";
    }

    // ==================================================
    // LICENSES & CONFIG & TUTORIAL
    // ==================================================
    private static void showLicenses(JFrame frame) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        try (InputStream in = AwarenessApp.class.getClassLoader().getResourceAsStream("licenses/apache-2.0.txt")) {
            area.setText(in != null
                    ? new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)
                    : "Apache License 2.0\nhttps://www.apache.org/licenses/LICENSE-2.0");
        } catch (Exception e) {
            area.setText("Lizenz konnte nicht geladen werden.");
        }

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(700, 500));

        JOptionPane.showMessageDialog(frame, scroll, "Open-Source Lizenzen", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void exportConfig(JFrame parent, String fileName) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(fileName));

        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try (InputStream in = ConfigFiles.open(fileName);
                 OutputStream out = new FileOutputStream(fc.getSelectedFile())) {
                in.transferTo(out);
                JOptionPane.showMessageDialog(parent, "Datei exportiert");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage());
            }
        }
    }

    private static void importConfig(JFrame parent, String fileName) {
        JFileChooser fc = new JFileChooser();

        if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try (InputStream in = new FileInputStream(fc.getSelectedFile());
                 OutputStream out = new FileOutputStream(ConfigFiles.get(fileName))) {
                in.transferTo(out);
                JOptionPane.showMessageDialog(parent, "Datei übernommen");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage());
            }
        }
    }

    // --- TUTORIAL METHODEN ---

    private static void openTutorial() {
    try {
        // Holt "de", "en", "es" oder "fr"
        String lang = currentLocale.getLanguage().toLowerCase(); 
        
        // Java sucht direkt nach tutorial_de.html, tutorial_en.html, etc.
        File htmlFile = new File("tutorial_" + lang + ".html");

        // Sicherheits-Check: Falls die Datei fehlt, nimm Deutsch als Standard
        if (!htmlFile.exists()) {
            htmlFile = new File("tutorial_de.html");
        }

        if (htmlFile.exists()) {
            Desktop.getDesktop().browse(htmlFile.toURI());
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    
    
    private static void saveLanguage(Locale locale) {
    new AppConfig().setString("app.language", locale.getLanguage());
}


}