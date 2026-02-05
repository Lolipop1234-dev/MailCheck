package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JSwitch extends JComponent {
    private boolean selected = false;
    private final Color colorOn = new Color(40, 167, 69);
    private final Color colorOff = new Color(200, 200, 200);

    public JSwitch() {
        setPreferredSize(new Dimension(50, 25));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                setSelected(!selected);
            }
        });
    }

    public boolean isSelected() { return selected; }

    public void setSelected(boolean b) {
        this.selected = b;
        repaint();
        firePropertyChange("selected", !b, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hintergrund (Abgerundetes Rechteck)
        g2.setColor(selected ? colorOn : colorOff);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

        // Der weiße Kreis (Knopf)
        g2.setColor(Color.WHITE);
        int gap = 3;
        int size = getHeight() - (gap * 2);
        int x = selected ? (getWidth() - size - gap) : gap;
        g2.fillOval(x, gap, size, size);
    }
}