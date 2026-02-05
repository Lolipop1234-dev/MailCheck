package ui;

import engine.AppConfig;
import i18n.I18n;

import javax.swing.*;
import java.awt.*;

public class ScannerSettingsDialog extends JDialog {

    public ScannerSettingsDialog(JFrame parent, AppConfig cfg) {
        super(parent, I18n.t("settings.scanner.title"), true);
        setSize(800, 750); // Etwas breiter und höher für die größeren Schriften
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // 🔍 Phishing
        content.add(scannerToggle(
                I18n.t("scanner.phishing.name"),
                I18n.t("scanner.phishing.desc"),
                cfg.isScannerEnabled("phishing"),
                v -> cfg.setScannerEnabled("phishing", v)
        ));

        // 🌐 URL
        content.add(scannerToggle(
                I18n.t("scanner.url.name"),
                I18n.t("scanner.url.desc"),
                cfg.isScannerEnabled("url"),
                v -> cfg.setScannerEnabled("url", v)
        ));

        // 🧠 Readability
        content.add(scannerToggle(
                I18n.t("scanner.readability.name"),
                I18n.t("scanner.readability.desc"),
                cfg.isScannerEnabled("readability"),
                v -> cfg.setScannerEnabled("readability", v)
        ));

        // 🔑 Keyword
        content.add(scannerToggle(
                I18n.t("scanner.keyword.name"),
                I18n.t("scanner.keyword.desc"),
                cfg.isScannerEnabled("keyword"),
                v -> cfg.setScannerEnabled("keyword", v)
        ));

        // 📎 Attachment
        content.add(scannerToggle(
                I18n.t("scanner.attachment.name"),
                I18n.t("scanner.attachment.desc"),
                cfg.isScannerEnabled("attachment"),
                v -> cfg.setScannerEnabled("attachment", v)
        ));

        // 💳 IBAN
        content.add(scannerToggle(
                I18n.t("scanner.iban.name"),
                I18n.t("scanner.iban.desc"),
                cfg.isScannerEnabled("iban"),
                v -> cfg.setScannerEnabled("iban", v)
        ));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);

        JButton save = new JButton(I18n.t("btn.save"));
        save.setFont(new Font("Segoe UI", Font.BOLD, 14));
        save.addActionListener(e -> dispose());

        add(scroll, BorderLayout.CENTER);
        add(save, BorderLayout.SOUTH);
    }

    private JPanel scannerToggle(
            String title,
            String desc,
            boolean initial,
            java.util.function.Consumer<Boolean> onChange
    ) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        // Schrift für Titel größer gemacht
        JLabel name = new JLabel(title);
        name.setFont(new Font("Segoe UI", Font.BOLD, 16)); 

        JLabel description = new JLabel("<html>" + desc + "</html>");
        description.setForeground(Color.GRAY);

        // Hier wird nun JSwitch statt JToggleButton genutzt
        JSwitch toggle = new JSwitch();
        toggle.setSelected(initial);
        toggle.addPropertyChangeListener("selected", e -> {
            onChange.accept((Boolean) e.getNewValue());
        });

        p.add(name, BorderLayout.NORTH);
        p.add(description, BorderLayout.CENTER);
        
        // Container für den Switch, damit er mittig rechts bleibt
        JPanel switchWrapper = new JPanel(new GridBagLayout());
        switchWrapper.setOpaque(false);
        switchWrapper.add(toggle);
        p.add(switchWrapper, BorderLayout.EAST);

        // Trennlinie für die Optik
        p.add(new JSeparator(), BorderLayout.SOUTH);

        return p;
    }
}