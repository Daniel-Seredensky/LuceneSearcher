package GUI.Components;

import GUI.Utilities.DrawingUtils;
import GUI.Utilities.ScalingUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * ModernButton is a stylized button component.
 *
 * It takes width, height, and text as parameters. The button:<p>
 *   - Uses a rounded shape (via a custom clip in paintComponent)<p>
 *   - Displays a subtle hover effect (a lightened background)<p>
 *   - Has a clean, modern appearance with proper padding<p>
 *   - Features a subtle drop shadow for depth<p>
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
        setMinimumSize(new Dimension(width + shadowSize, height + shadowSize));
        setMaximumSize(new Dimension(width + shadowSize, height + shadowSize));

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
        hoverBackground = DrawingUtils.lightenColor(normalBackground, 0.1f);
        pressedBackground = DrawingUtils.darkenColor(normalBackground, 0.1f);
        borderColor = Color.decode("#587785"); 
        setBackground(normalBackground);

        // Add mouse listener for hover color change
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
     * Override paintComponent to create a custom rounded button appearance with shadow.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Enable anti-aliasing for smoother rounded corners
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Create the rounded rectangle shape, accounting for shadow
        Shape roundedRect = new RoundRectangle2D.Float(0, 0, getWidth() - shadowSize, getHeight() - shadowSize, arcRadius, arcRadius);
        
        // Draw the shadow first
        DrawingUtils.drawShadow(g2, roundedRect, shadowSize, shadowColor);
        
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
        
        g2.setColor(borderColor);
        g2.draw(roundedRect);
        
        // Reset clip before drawing text
        g2.setClip(null);
        
        FontMetrics fm = g2.getFontMetrics();
        Rectangle textRect = new Rectangle(0, 0, getWidth() - shadowSize, getHeight() - shadowSize);
        String text = getText();
        
        int x = (textRect.width - fm.stringWidth(text)) / 2;
        int y = (textRect.height - fm.getHeight()) / 2 + fm.getAscent();
        
        g2.setColor(getForeground());
        g2.drawString(text, x, y);
        
        g2.dispose();
    }
}
