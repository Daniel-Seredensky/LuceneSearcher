package GUI.Components;

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
import java.awt.geom.AffineTransform;
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
        
        hoverColor = lightenColor(normalColor, 0.1f);
        pressedColor = darkenColor(normalColor, 0.1f);
        
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
        
        drawShadow(g2, circleShape);
        
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
    
    /**
     * Draws a shadow effect behind the component shape.
     */
    private void drawShadow(Graphics2D g2, Shape shape) {
        AffineTransform originalTransform = g2.getTransform();
        
        for (int i = 0; i < shadowSize; i++) {
            float alpha = 0.5f * (shadowSize - i) / shadowSize;
            Color currentShadowColor = new Color(
                shadowColor.getRed(), 
                shadowColor.getGreen(), 
                shadowColor.getBlue(), 
                (int)(alpha * shadowColor.getAlpha())
            );
            
            g2.setColor(currentShadowColor);
            
            g2.translate(i > 0 ? 1 : 0, i > 0 ? 1 : 0);
            g2.fill(shape);
        }
        
        g2.setTransform(originalTransform);
    }
    
    /**
     * Lightens the given color by a fraction.
     *
     * @param color    the original color
     * @param fraction fraction to lighten (e.g., 0.1 for 10%)
     * @return a new lightened Color
     */
    private Color lightenColor(Color color, float fraction) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        red = red + Math.round((255 - red) * fraction);
        green = green + Math.round((255 - green) * fraction);
        blue = blue + Math.round((255 - blue) * fraction);
        return new Color(red, green, blue, color.getAlpha());
    }
    
    /**
     * Darkens the given color by a fraction.
     *
     * @param color    the original color
     * @param fraction fraction to darken (e.g., 0.1 for 10%)
     * @return a new darkened Color
     */
    private Color darkenColor(Color color, float fraction) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        red = Math.max(0, red - Math.round(red * fraction));
        green = Math.max(0, green - Math.round(green * fraction));
        blue = Math.max(0, blue - Math.round(blue * fraction));
        return new Color(red, green, blue, color.getAlpha());
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        // Add shadow size to the preferred dimensions
        return new Dimension(size.width + shadowSize, size.height + shadowSize);
    }
}