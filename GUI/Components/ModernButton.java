package GUI.Components;

import GUI.Utilities.ScalingUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/**
 * ModernButton is a stylized button component.
 *
 * It takes width, height, and text as parameters. The button:
 *   - Uses a rounded shape (via a custom clip in paintComponent)
 *   - Displays a subtle hover effect (a lightened background)
 *   - Has a clean, modern appearance with proper padding
 *   - Features a subtle drop shadow for depth
 */
public class ModernButton extends JButton {

    private Color normalBackground;
    private Color hoverBackground;
    private Color pressedBackground;
    private Color borderColor;
    private final int arcRadius = ScalingUtil.scalePadding(30);
    private final int shadowSize = ScalingUtil.scalePadding(10);
    private final Color shadowColor = new Color(0, 0, 0, 50); // Semi-transparent black for shadow
    
    // State tracking variables
    private boolean isHovered = false;
    private boolean isPressed = false;

    /**
     * Constructs a ModernButton with the specified dimensions and text.
     *
     * @param width  the preferred width of the button
     * @param height the preferred height of the button
     * @param text   the button text
     */
    public ModernButton(int width, int height, String text) {
        super(text);
        setPreferredSize(new Dimension(width + shadowSize, height + shadowSize));

        // Set up button appearance
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(false);
        
        // Add padding with an EmptyBorder, accounting for shadow
        int paddingTop = ScalingUtil.scalePadding(15);
        int paddingLeft = ScalingUtil.scalePadding(15);
        int paddingBottom = ScalingUtil.scalePadding(15) + shadowSize;
        int paddingRight = ScalingUtil.scalePadding(15);
        
        setBorder(new EmptyBorder(paddingTop, paddingLeft, paddingBottom, paddingRight));

        normalBackground = Color.decode("#587785");
        hoverBackground = lightenColor(normalBackground, 0.1f);
        pressedBackground = darkenColor(normalBackground, 0.1f);
        borderColor = Color.decode("#587785"); 
        setBackground(normalBackground);

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
     * Override paintComponent to create a custom rounded button appearance with shadow.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Enable anti-aliasing for smoother rounded corners.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Create the rounded rectangle shape, accounting for shadow
        Shape roundedRect = new RoundRectangle2D.Float(0, 0, getWidth() - shadowSize, getHeight() - shadowSize, arcRadius, arcRadius);
        
        // Draw the shadow first
        drawShadow(g2, roundedRect);
        
        // Set clip to the component shape for further painting
        g2.setClip(roundedRect);
        
        // Determine the current color based on button state
        Color currentColor;
        if (isPressed) {
            currentColor = pressedBackground;
        } else if (isHovered) {
            currentColor = hoverBackground;
        } else {
            currentColor = normalBackground;
        }
        
        // Fill the button background
        g2.setColor(currentColor);
        g2.fill(roundedRect);
        
        // Draw a subtle border
        g2.setColor(borderColor);
        g2.draw(roundedRect);
        
        // Reset clip before drawing text
        g2.setClip(null);
        
        // Draw the text
        FontMetrics fm = g2.getFontMetrics();
        Rectangle textRect = new Rectangle(0, 0, getWidth() - shadowSize, getHeight() - shadowSize);
        String text = getText();
        
        int x = (textRect.width - fm.stringWidth(text)) / 2;
        int y = (textRect.height - fm.getHeight()) / 2 + fm.getAscent();
        
        g2.setColor(getForeground());
        g2.drawString(text, x, y);
        
        g2.dispose();
    }
    
    /**
     * Draws a shadow effect behind the component shape.
     * 
     * @param g2 The Graphics2D context to draw on
     * @param shape The shape to draw the shadow for
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
}
