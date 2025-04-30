package GUI.Components;

import GUI.Utilities.DrawingUtils;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ModernButtonBoolean extends ModernButton to track a boolean state.
 * When the boolean is true, the button appears in its normal state.
 * When the boolean is false, the button appears in a pastel red color.
 */
public class ModernButtonBoolean extends ModernButton {
    
    private boolean state;
    private Color falseStateColor;
    private Color falseStateHoverColor;
    private Color falseStatePressedColor;
    
    /**
     * Constructs a ModernButtonBoolean with the specified dimensions and text.
     * The boolean state is initialized to true.
     *
     * @param width  the preferred width of the button
     * @param height the preferred height of the button
     * @param text   the button text
     */
    public ModernButtonBoolean(int width, int height, String text) {
        super(width, height, text);
        this.state = true;
                
        // Initialize colors for the false state
        falseStateColor = Color.decode("#C62828"); // Pastel red
        falseStateHoverColor = DrawingUtils.lightenColor(falseStateColor, 0.1f);
        falseStatePressedColor = DrawingUtils.darkenColor(falseStateColor, 0.1f);
        
        // Add action listener to toggle state when button is clicked
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleState();
            }
        });
    }
    
    /**
     * Sets the boolean state of the button.
     * 
     * @param state true for normal state, false for pastel red state
     */
    public void setState(boolean state) {
        this.state = state;
        repaint();
    }
    
    /**
     * Gets the current boolean state of the button.
     * 
     * @return the current state
     */
    public boolean getState() {
        return state;
    }
    
    /**
     * Toggles the current state of the button.
     * 
     * @return the new state after toggling
     */
    public boolean toggleState() {
        state = !state;
        repaint();
        return state;
    }
    
    /**
     * Override paintComponent to change the button color based on the boolean state.
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (state) {
            // If state is true, use the normal painting from the parent class
            super.paintComponent(g);
        } else {
            // If state is false, use custom painting with pastel red color
            Graphics2D g2 = (Graphics2D) g.create();
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            Shape roundedRect = new RoundRectangle2D.Float(0, 0, getWidth() - getShadowSize(), getHeight() - getShadowSize(), getArcRadius(), getArcRadius());
            
            Color shadowColor = new Color(0, 0, 0, 50);
            DrawingUtils.drawShadow(g2, roundedRect, getShadowSize(), shadowColor);
            
            g2.setClip(roundedRect);
            
            // Determine the current color based on button state
            Color currentColor;
            if (isPressed()) {
                currentColor = falseStatePressedColor;
            } else if (isHovered()) {
                currentColor = falseStateHoverColor;
            } else {
                currentColor = falseStateColor;
            }
            
            g2.setColor(currentColor);
            g2.fill(roundedRect);
            
            g2.setColor(falseStateColor.darker());
            g2.draw(roundedRect);
            
            g2.setClip(null);
            
            g2.setFont(getFont());
            g2.setColor(getForeground());
            
            java.awt.FontMetrics fm = g2.getFontMetrics();
            java.awt.Rectangle textRect = new java.awt.Rectangle(0, 0, getWidth() - getShadowSize(), getHeight() - getShadowSize());
            String text = getText();
            
            int x = (textRect.width - fm.stringWidth(text)) / 2;
            int y = (textRect.height - fm.getHeight()) / 2 + fm.getAscent();
            
            g2.drawString(text, x, y);
            
            g2.dispose();
        }
    }
    
    /**
     * Sets the color used when the button state is false.
     * 
     * @param color the color to use for the false state
     */
    public void setFalseStateColor(Color color) {
        this.falseStateColor = color;
        this.falseStateHoverColor = DrawingUtils.lightenColor(color, 0.1f);
        this.falseStatePressedColor = DrawingUtils.darkenColor(color, 0.1f);
        if (!state) {
            repaint();
        }
    }
}
