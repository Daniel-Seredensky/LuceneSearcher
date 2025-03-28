package GUI.Components;

import GUI.Utilities.DrawingUtils;
import GUI.Utilities.ScalingUtil;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

/**
 * Custom circular search button with shadow effect and hover states
 */
public class SearchButton extends JButton {
    private final int shadowSize = ScalingUtil.scalePadding(10);
    private final Color shadowColor = new Color(0, 0, 0, 50); 
    
    private Color normalColor = Color.decode("#5E6C5E");
    private Color hoverColor;
    private Color pressedColor;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public SearchButton() {
        setText("^");
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);

        setFont(getFont().deriveFont((float)ScalingUtil.scalePadding(getFont().getSize() + 10)));
        
        hoverColor = DrawingUtils.lightenColor(normalColor, 0.1f);
        pressedColor = DrawingUtils.darkenColor(normalColor, 0.1f);
        
        // Add mouse listeners for hover and press effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Shape circleShape = new Ellipse2D.Float(0, 0, getWidth() - shadowSize, getHeight() - shadowSize);
        
        DrawingUtils.drawShadow(g2, circleShape, shadowSize, shadowColor);
        
        Color currentColor;
        if (isPressed) {
            currentColor = pressedColor;
        } else if (isHovered) {
            currentColor = hoverColor;
        } else {
            currentColor = normalColor;
        }
        
        g2.setColor(currentColor);
        g2.fill(circleShape);
        
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(getText());
        int textHeight = fm.getHeight();
        
        // center
        int x = (getWidth() - shadowSize - textWidth) / 2;
        int y = ((getHeight() - shadowSize - textHeight) / 2) + fm.getAscent();
        
        g2.setColor(Color.WHITE);
        g2.drawString(getText(), x, y);
        
        g2.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        // Add shadow size to the preferred dimensions
        return new Dimension(size.width + shadowSize, size.height + shadowSize);
    }
}
