package GUI.GUIProgression;

import GUI.Utilities.ScalingUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Abstract base class for the main GUI of the Lucene Search Engine.
 * <p>
 * This class provides the overall structure of the application's main window.
 * It sets up the look and feel and defines abstract methods for initializing
 * and laying out the UI components.
 * </p>
 */
public abstract class BaseGUI extends JFrame {
    
    // Background colors for gradient
    private Color centerColor;
    private Color edgeColor;

    /**
     * Constructs the main application frame.
     */
    public BaseGUI(String title) {
        super();
        // Set up the look and feel for a modern appearance.
        initializeLookAndFeel();
        
        // Set up gradient background colors
        centerColor = Color.decode("#4D483D");
        edgeColor = darkenColor(centerColor, 0.4f);
        
        // Create and set a custom content pane with gradient background
        setContentPane(new GradientPanel());

        // Basic JFrame settings
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(ScalingUtil.scaleWidth(800), ScalingUtil.scaleHeight(600));
        setLocationRelativeTo(null); // Center the window on the screen

        // Call abstract methods to allow subclass customization of UI components.
        initializeComponents();
        layoutComponents();
    }

    /**
     * Initializes the FlatLaf look and feel with a custom color scheme.
     * Color scheme:
     *   PUNGA       -> #4D483D  (used as a primary background)
     *   FETA        -> #DDE0D4  (used as a primary foreground/text color)
     *   SAN_MARINO  -> #5C6872  (used for focus/selection accents)
     *   FIN_LANDIA -> #5E6C5E  (used for button backgrounds and secondary elements)
     *   BLUE_BAYOUX  -> #587785  (used for borders or additional accent, also title bar)
     */
    protected void initializeLookAndFeel() {
        try {
            // Set up the FlatLightLaf Look and Feel.
            FlatLightLaf.setup();

            // Define custom colors based on your scheme.
            Color punga       = Color.decode("#4D483D");   // Primary background color.
            Color feta        = Color.decode("#DDE0D4");   // Primary foreground (text) color.
            Color sanMarino   = Color.decode("#5C6872");   // Accent color for focus/selection.
            Color blueBayoux  = Color.decode("#5E6C5E");   // Secondary background (e.g., buttons).
            Color finLandia   = Color.decode("#587785");   // Accent for borders/details, also title bar.

            // Customize UI defaults for a clean and minimalist feel
            UIManager.put("Panel.background", punga);
            UIManager.put("OptionPane.background", punga);
            UIManager.put("TextComponent.background", feta);
            UIManager.put("TextComponent.foreground", punga);
            UIManager.put("Label.foreground", feta);
            UIManager.put("Button.background", blueBayoux);
            UIManager.put("Button.foreground", feta);
            UIManager.put("Button.focusColor", sanMarino);
            UIManager.put("Component.focusColor", sanMarino);
            UIManager.put("Component.accentColor", finLandia);
            UIManager.put("TabbedPane.selectedBackground", blueBayoux);
            UIManager.put("ScrollBar.track", punga);
            UIManager.put("ScrollBar.thumb", blueBayoux);
            UIManager.put("RootPane.background", punga);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Initializes the UI components.
     */
    protected abstract void initializeComponents();

    /**
     * Arranges the UI components in the frame.
     */
    protected abstract void layoutComponents();

    /**
     * Refreshes the UI by updating the component tree.
     */
    public void refreshUI() {
        SwingUtilities.updateComponentTreeUI(this);
    }
    
    /**
     * Darkens the given color by a fraction.
     *
     * @param color    the original color
     * @param fraction fraction to darken (e.g., 0.3 for 30%)
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
    
    /**
     * Custom panel with radial gradient background
     */
    private class GradientPanel extends JPanel {
        
        public GradientPanel() {
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Create a radial gradient from center to edges
            Point2D center = new Point2D.Float(width / 2.0f, height / 2.0f);
            float radius = Math.max(width, height) * 0.5f; // Adjust this value to control gradient spread
            
            // Create gradient paint
            RadialGradientPaint paint = new RadialGradientPaint(
                center,
                radius,
                new float[] { 0.0f, 1.0f },
                new Color[] { centerColor, edgeColor }
            );
            
            g2d.setPaint(paint);
            g2d.fill(new Rectangle2D.Double(0, 0, width, height));
            
            g2d.dispose();
        }
    }
}
