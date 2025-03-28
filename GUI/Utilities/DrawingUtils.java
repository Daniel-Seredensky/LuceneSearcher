package GUI.Utilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * Utility class for common drawing operations used across UI components.
 * Centralizes duplicate code for shadow effects, color manipulation, etc.
 */
public class DrawingUtils {
    
    /**
     * Draws a shadow effect behind the component shape.
     * 
     * @param g2 The Graphics2D context to draw on
     * @param shape The shape to draw the shadow for
     * @param shadowSize The size/depth of the shadow in pixels
     * @param shadowColor The base color of the shadow
     */
    public static void drawShadow(Graphics2D g2, Shape shape, int shadowSize, Color shadowColor) {
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
     * Draws text with a shadow effect.
     * 
     * @param g2d The Graphics2D context to draw on
     * @param text The text to draw
     * @param x The x-coordinate for the text
     * @param y The y-coordinate for the text
     * @param font The font to use for the text
     * @param textColor The color of the text
     * @param shadowColor The color of the shadow
     * @param shadowSize The size/depth of the shadow in pixels
     */
    public static void drawTextWithShadow(Graphics2D g2d, String text, int x, int y, 
                                         Font font, Color textColor, Color shadowColor, int shadowSize) {
        g2d.setFont(font);
        
        // Draw shadow
        TextLayout textLayout = new TextLayout(text, font, g2d.getFontRenderContext());
        AffineTransform originalTransform = g2d.getTransform();
        
        for (int i = 0; i < shadowSize; i++) {
            float alpha = 0.5f * (shadowSize - i) / shadowSize;
            Color currentShadowColor = new Color(
                shadowColor.getRed(), 
                shadowColor.getGreen(), 
                shadowColor.getBlue(), 
                (int)(alpha * shadowColor.getAlpha())
            );
            
            g2d.setColor(currentShadowColor);
            g2d.translate(i > 0 ? 1 : 0, i > 0 ? 1 : 0);
            textLayout.draw(g2d, x, y);
        }
        
        g2d.setTransform(originalTransform);
        
        // Draw actual text
        g2d.setColor(textColor);
        g2d.drawString(text, x, y);
    }
    
    /**
     * Lightens the given color by a fraction.
     *
     * @param color    the original color
     * @param fraction fraction to lighten (e.g., 0.1 for 10%)
     * @return a new lightened Color
     */
    public static Color lightenColor(Color color, float fraction) {
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
    public static Color darkenColor(Color color, float fraction) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        red = Math.max(0, red - Math.round(red * fraction));
        green = Math.max(0, green - Math.round(green * fraction));
        blue = Math.max(0, blue - Math.round(blue * fraction));
        return new Color(red, green, blue, color.getAlpha());
    }
    
    /**
     * Creates a semi-transparent shadow color.
     * 
     * @param alpha The alpha value (0-255) for the shadow
     * @return A semi-transparent black color for shadows
     */
    public static Color createShadowColor(int alpha) {
        return new Color(0, 0, 0, alpha);
    }
}
