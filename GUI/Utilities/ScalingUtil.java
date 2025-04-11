package GUI.Utilities;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * Utility class for scaling UI components based on screen resolution.
 * This ensures consistent appearance across different display sizes.
 */
public class ScalingUtil {
    // Reference resolution (MacBook Pro 13.3-inch)
    private static final int REFERENCE_WIDTH = 1680;
    private static final int REFERENCE_HEIGHT = 1050;
    
    // Current screen resolution
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    // Calculate scaling factors
    private static final double HORIZONTAL_SCALE = screenSize.getWidth() / REFERENCE_WIDTH;
    private static final double VERTICAL_SCALE = screenSize.getHeight() / REFERENCE_HEIGHT;
    
    // Reference frame sizes for dynamic component scaling
    private static final int MIN_FRAME_WIDTH = 800;
    private static final int MIN_FRAME_HEIGHT = 600;
    static {
        System.out.println(HORIZONTAL_SCALE + " " + VERTICAL_SCALE);
    }
    
    /**
     * Scales a width value based on the current screen resolution.
     * 
     * @param width The width value at reference resolution
     * @return The scaled width value
     */
    public static int scaleWidth(int width) {
        return (int) Math.round(width * HORIZONTAL_SCALE);
    }
    
    /**
     * Scales a height value based on the current screen resolution.
     * 
     * @param height The height value at reference resolution
     * @return The scaled height value
     */
    public static int scaleHeight(int height) {
        return (int) Math.round(height * VERTICAL_SCALE);
    }
    
    /**
     * Scales a dimension based on the current screen resolution.
     * 
     * @param width The width at reference resolution
     * @param height The height at reference resolution
     * @return A scaled Dimension object
     */
    public static Dimension scaleDimension(int width, int height) {
        return new Dimension(scaleWidth(width), scaleHeight(height));
    }
    
    /**
     * Scales a padding or margin value.
     * Uses the minimum of horizontal and vertical scale to ensure
     * consistent spacing.
     * 
     * @param value The padding value at reference resolution
     * @return The scaled padding value
     */
    public static int scalePadding(int value) {
        double scale = Math.min(HORIZONTAL_SCALE, VERTICAL_SCALE);
        return (int) Math.max(1, Math.round(value * scale));
    }
    
    /**
     * Calculates a ratio between 0.0 and 1.0 based on where the value falls between min and max.
     * 
     * @param value The current value
     * @param min The minimum value (corresponds to ratio 0.0)
     * @param max The maximum value (corresponds to ratio 1.0)
     * @return A ratio between 0.0 and 1.0
     */
    public static double calculateRatio(int value, int min, int max) {
        if (value <= min) return 0.0;
        if (value >= max) return 1.0;
        return (double)(value - min) / (max - min);
    }
    
    /**
     * Calculates a scaled value based on min, max and a ratio between 0.0 and 1.0.
     * 
     * @param min The minimum value
     * @param max The maximum value
     * @param ratio A ratio between 0.0 and 1.0
     * @return A value between min and max
     */
    public static int calculateScaledValue(int min, int max, double ratio) {
        return min + (int)(ratio * (max - min));
    }
    
    /**
     * Calculates a scaling ratio based on window size relative to min frame size and screen size.
     * 
     * @param window The window to calculate the ratio for
     * @return A ratio between 0.0 and 1.0
     */
    public static double calculateWindowSizeRatio(Window window) {
        if (window == null) return 1.0;
        Rectangle bounds = window.getBounds();
        Dimension screenSize = window.getToolkit().getScreenSize();
        double widthRatio = calculateRatio(bounds.width, MIN_FRAME_WIDTH, screenSize.width);
        double heightRatio = calculateRatio(bounds.height, MIN_FRAME_HEIGHT, screenSize.height);
        return Math.min(widthRatio, heightRatio);
    }
}
