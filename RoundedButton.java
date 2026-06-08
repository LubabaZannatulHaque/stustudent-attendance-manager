package GUI;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {

    public Color hoverBackgroundColor;
    public Color pressedBackgroundColor;
    private Color originalBackground;

    public RoundedButton(String text) {
        super(text);
        super.setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setBackground(new Color(70, 130, 180));

        hoverBackgroundColor   = new Color(100, 149, 237);
        pressedBackgroundColor = new Color(65, 105, 225);
        originalBackground     = getBackground();

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(hoverBackgroundColor);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // BUG FIX: restore the button's own original color, not a hardcoded one
                setBackground(originalBackground);
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                setBackground(pressedBackgroundColor);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                setBackground(hoverBackgroundColor);
            }
        });
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        // Keep originalBackground in sync whenever the background is set externally
        if (originalBackground == null) {
            originalBackground = bg;
        }
    }

    /**
     * Call this AFTER setting the button's final background color so that
     * mouseExited restores the correct color.
     */
    public void syncOriginalBackground() {
        originalBackground = getBackground();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        super.paintComponent(g);
    }
}
