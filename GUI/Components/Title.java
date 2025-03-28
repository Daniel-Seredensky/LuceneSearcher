package GUI.Components;

import GUI.Utilities.DrawingUtils;
import GUI.Utilities.ScalingUtil;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.*;


/**
 * A reusable title component that displays a title and subtitle with shadow effects.
 * Dynamically resizes based on the parent frame's size.
 */
public class Title extends JPanel {
    private String titleText;
    private String subtitleText;
    private final Color textColor;
    private final Color shadowColor;
    
    // Size constants
    private final int MAX_HEIGHT = 150;
    private final int MIN_HEIGHT = 80;
    
    // Font size constants
    private final int MAX_TITLE_FONT_SIZE = 60;
    private final int MIN_TITLE_FONT_SIZE = 24;
    private final int MAX_SUBTITLE_FONT_SIZE = 28;
    private final int MIN_SUBTITLE_FONT_SIZE = 14;
    
    // Shadow size constants
    private final int MAX_TITLE_SHADOW_SIZE = 12;
    private final int MIN_TITLE_SHADOW_SIZE = 4;
    private final int MAX_SUBTITLE_SHADOW_SIZE = 8;
    private final int MIN_SUBTITLE_SHADOW_SIZE = 2;
    
    // Current values
    private int currentHeight = MAX_HEIGHT;
    private int currentTitleFontSize = MAX_TITLE_FONT_SIZE;
    private int currentSubtitleFontSize = MAX_SUBTITLE_FONT_SIZE;
    private int currentTitleShadowSize = MAX_TITLE_SHADOW_SIZE;
    private int currentSubtitleShadowSize = MAX_SUBTITLE_SHADOW_SIZE;
    
    /**
     * Creates a new Title component with the specified text.
     * 
     * @param titleText The main title text
     * @param subtitleText The subtitle text
     */
    public Title(String titleText, String subtitleText) {
        this(titleText, subtitleText, Color.decode("#DDE0D4"), new Color(0, 0, 0, 50));
    }
    
    /**
     * Creates a new Title component with the specified text and colors.
     * 
     * @param titleText The main title text
     * @param subtitleText The subtitle text
     * @param textColor The color of the text
     * @param shadowColor The color of the shadow
     */
    public Title(String titleText, String subtitleText, Color textColor, Color shadowColor) {
        this.titleText = titleText;
        this.subtitleText = subtitleText;
        this.textColor = textColor;
        this.shadowColor = shadowColor;
        
        setOpaque(false);
        setLayout(null); // Use absolute positioning
        
        updatePreferredSize();
        setupResizeListeners();
    }
    
    /**
     * Sets up listeners to handle dynamic resizing based on parent frame size
     */
    private void setupResizeListeners() {
        // Add ancestor listener to get notified when this component is added to a container
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                // Find the parent window
                Window window = SwingUtilities.getWindowAncestor(Title.this);
                if (window != null) {
                    // Add component listener to the window to detect resizing
                    window.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            updateSizeBasedOnParent(window);
                        }
                    });
                    updateSizeBasedOnParent(window);
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // Not needed
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // Not needed
            }
        });
    }
    
    /**
     * Updates the size of the title component based on parent window size
     */
    private void updateSizeBasedOnParent(Window window) {
        if (window == null) return;
        
        // Calculate scaling ratio based on window size
        double ratio = ScalingUtil.calculateWindowSizeRatio(window);
        
        // Apply scaling to title dimensions
        currentHeight = ScalingUtil.calculateScaledValue(MIN_HEIGHT, MAX_HEIGHT, ratio);
        currentTitleFontSize = ScalingUtil.calculateScaledValue(MIN_TITLE_FONT_SIZE, MAX_TITLE_FONT_SIZE, ratio);
        currentSubtitleFontSize = ScalingUtil.calculateScaledValue(MIN_SUBTITLE_FONT_SIZE, MAX_SUBTITLE_FONT_SIZE, ratio);
        currentTitleShadowSize = ScalingUtil.calculateScaledValue(MIN_TITLE_SHADOW_SIZE, MAX_TITLE_SHADOW_SIZE, ratio);
        currentSubtitleShadowSize = ScalingUtil.calculateScaledValue(MIN_SUBTITLE_SHADOW_SIZE, MAX_SUBTITLE_SHADOW_SIZE, ratio);
        
        // Update the component size
        updatePreferredSize();
        revalidate();
        repaint();
    }
    
    /**
     * Updates the preferred size based on current dimensions
     */
    private void updatePreferredSize() {
        setPreferredSize(new Dimension(getWidth(), currentHeight));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, currentHeight));
        setMinimumSize(new Dimension(0, currentHeight));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Font titleFont = new Font("SansSerif", Font.BOLD, currentTitleFontSize);
        Font subtitleFont = new Font("SansSerif", Font.PLAIN, currentSubtitleFontSize);
        
        FontMetrics titleMetrics = g2d.getFontMetrics(titleFont);
        FontMetrics subtitleMetrics = g2d.getFontMetrics(subtitleFont);
        
        int titleWidth = titleMetrics.stringWidth(titleText);
        int subtitleWidth = subtitleMetrics.stringWidth(subtitleText);
        
        int titleX = (getWidth() - titleWidth) / 2;
        int titleY = titleMetrics.getAscent() + (currentHeight - titleMetrics.getHeight() - subtitleMetrics.getHeight()) / 3;
        
        int subtitleX = (getWidth() - subtitleWidth) / 2;
        int subtitleY = titleY + titleMetrics.getDescent() + subtitleMetrics.getAscent() + ScalingUtil.scalePadding(5);
        
        DrawingUtils.drawTextWithShadow(g2d, titleText, titleX, titleY, titleFont, 
                                      textColor, shadowColor, currentTitleShadowSize);
        DrawingUtils.drawTextWithShadow(g2d, subtitleText, subtitleX, subtitleY, subtitleFont, 
                                      textColor, shadowColor, currentSubtitleShadowSize);
        
        g2d.dispose();
    }
    
    /**
     * Updates the title text.
     * 
     * @param titleText The new title text
     */
    public void setTitleText(String titleText) {
        this.titleText = titleText;
        repaint();
    }
    
    /**
     * Updates the subtitle text.
     * 
     * @param subtitleText The new subtitle text
     */
    public void setSubtitleText(String subtitleText) {
        this.subtitleText = subtitleText;
        repaint();
    }
    
    /**
     * Gets the current title text.
     * 
     * @return The current title text
     */
    public String getTitleText() {
        return titleText;
    }
    
    /**
     * Gets the current subtitle text.
     * 
     * @return The current subtitle text
     */
    public String getSubtitleText() {
        return subtitleText;
    }
}