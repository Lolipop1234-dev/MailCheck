package ui;
import i18n.I18n;
import javax.swing.*;
import java.awt.*;

public class RiskPanel extends JPanel {

    private final JLabel iconLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel textLabel = new JLabel("", SwingConstants.CENTER);
    private final Color NEUTRAL_BLUE = new Color(30, 41, 59); // Passend zum Awareness-Look

    public RiskPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 70));
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        
        add(iconLabel, BorderLayout.CENTER);
        add(textLabel, BorderLayout.SOUTH);
        
        reset();
    }

    public void reset() {
        setBackground(NEUTRAL_BLUE);
        iconLabel.setText("🔍");
        textLabel.setForeground(Color.LIGHT_GRAY);
        textLabel.setText(I18n.t("btn.std.anz"));
    }

    public void setRisk(String risk) {
        String title;
        String message;

        switch (risk) {
            case "ROT" -> {
                setBackground(new Color(180, 40, 60)); // Dezenteres Rot
                iconLabel.setText("⛔");
                title = I18n.t("btn.dan");
                message = I18n.t("btn.dan.erk");
            }
            case "GELB" -> {
                setBackground(new Color(230, 160, 0)); // Warmes Gelb
                iconLabel.setText("⚠️");
                textLabel.setForeground(new Color(40, 30, 0)); 
                title = I18n.t("btn.ver");
                message = I18n.t("btn.ver.anz");
            }
            default -> {
                setBackground(new Color(40, 120, 80)); // Beruhigendes Grün
                iconLabel.setText("✅");
                title = I18n.t("btn.unv");
                message = I18n.t("btn.unv.anz");
            }
        }

        textLabel.setText("<html><center><b><font size='5'>" + title + "</font></b><br><br>" + message + "</center></html>");

        if (!risk.equals("GELB")) {
            textLabel.setForeground(Color.WHITE);
        }
        repaint();
    }
}