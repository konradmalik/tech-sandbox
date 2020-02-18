package io.github.konradmalik.soap.RomanConverter;

import javax.swing.*;
import java.awt.*;


public class GPanel extends JPanel {
    private GradientPaint gp;

    @Override
    public void paintComponent(Graphics g) {
        if (gp == null)
            gp = new GradientPaint(0, 0, Color.pink, 0, getHeight(), Color.orange);
        // Paint a nice gradient background with pink at the top and orange at
        // the bottom.
        ((Graphics2D) g).setPaint(gp);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

