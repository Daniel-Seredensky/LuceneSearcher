package GUI.Components;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import GUI.Utilities.ScalingUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Custom text field with no border and matching background 
 */
public class CustomTextField extends JTextField {
    private String prompt = "I don't know what you're looking for, but have fun";
    private final Color BACKGROUND_COLOR = Color.decode("#DDE0D4"); // FETA color
    private final Color FOREGROUND_COLOR = Color.decode("#262626"); // Dark gray
    public CustomTextField() {
        setText("");
        int paddingTop = ScalingUtil.scalePadding(10);
        int paddingLeft = ScalingUtil.scalePadding(15);
        int paddingBottom = ScalingUtil.scalePadding(10);
        int paddingRight = ScalingUtil.scalePadding(15);
        
        setBorder(new EmptyBorder(paddingTop, paddingLeft, paddingBottom, paddingRight));
        setOpaque(false);
        setBackground(BACKGROUND_COLOR);
        setForeground(FOREGROUND_COLOR);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
        repaint();
    }
    public String getPrompt() {
        return prompt;
    } 

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty()) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.decode("#5C6872"));
            Font italic = getFont().deriveFont(Font.ITALIC);
            g2d.setFont(italic);
            Insets insets = getInsets();
            FontMetrics fm = g2d.getFontMetrics();
            int textY = insets.top + ((getHeight() - insets.top - insets.bottom - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(prompt, insets.left, textY);
            g2d.dispose();
        }
    }
}
