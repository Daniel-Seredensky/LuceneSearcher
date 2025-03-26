package GUI.Components;

import GUI.Utilities.ScalingUtil;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import javax.swing.SwingUtilities;

/**
 * A unified search bar with integrated components and a modern design.
 * Dynamically resizes based on the parent frame's size.
 */
public class SearchBar extends JPanel {
    private final JTextField textField;
    private final SearchButton searchButton;
    private final ModernButton helpButton;
    private final ModernButton settingsButton;
    private final int arcRadius = 30;
    private final int shadowSize = 10;
    private final Color BACKGROUND_COLOR = Color.decode("#DDE0D4"); // FETA color
    
    // Padding constants
    private final int PANEL_PADDING = 25; 
    private final int COMPONENT_SPACING = 15;
    
    // Maximum size values (for fullscreen)
    private final int MAX_WIDTH = 600;
    private final int MAX_HEIGHT = 130;
    private final int MAX_USABLE_WIDTH = 580;
    private final int MAX_TEXT_FIELD_HEIGHT = 80;
    private final int MAX_SEARCH_BUTTON_SIZE = 50;
    private final int MAX_BUTTON_WIDTH = 100;
    private final int MAX_BUTTON_HEIGHT = 35;
    
    // Minimum size values (for 800x600 frame)
    private final int MIN_WIDTH = 400;
    private final int MIN_HEIGHT = 100;
    private final int MIN_USABLE_WIDTH = 350;
    private final int MIN_TEXT_FIELD_HEIGHT = 60;
    private final int MIN_SEARCH_BUTTON_SIZE = 40;
    private final int MIN_BUTTON_WIDTH = 80;
    private final int MIN_BUTTON_HEIGHT = 30;
    
    // Current size values
    private int currentWidth = MAX_WIDTH;
    private int currentHeight = MAX_HEIGHT;
    private int currentUsableWidth = MAX_USABLE_WIDTH;
    private int currentTextFieldHeight = MAX_TEXT_FIELD_HEIGHT;
    private int currentSearchButtonSize = MAX_SEARCH_BUTTON_SIZE;
    private int currentButtonWidth = MAX_BUTTON_WIDTH;
    private int currentButtonHeight = MAX_BUTTON_HEIGHT;

    public SearchBar() {
        setLayout(null);
        setOpaque(false);

        textField = new CustomTextField();
        
        searchButton = new SearchButton();
        
        helpButton = new ModernButton(MAX_BUTTON_WIDTH, MAX_BUTTON_HEIGHT, "Help");
        settingsButton = new ModernButton(MAX_BUTTON_WIDTH, MAX_BUTTON_HEIGHT, "Settings");

        customizeInternalButtons();

        add(textField);
        add(searchButton);
        add(helpButton);
        add(settingsButton);

        layoutComponentsWithPadding();

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
                Window window = SwingUtilities.getWindowAncestor(SearchBar.this);
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

            // not needed 
            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }
    
    /**
     * Updates the size of the search bar based on parent window size
     */
    private void updateSizeBasedOnParent(Window window) {
        if (window == null) return;
        
        // Calculate scaling ratio based on window size
        double ratio = ScalingUtil.calculateWindowSizeRatio(window);
        
        // Apply scaling to search bar dimensions
        currentWidth = ScalingUtil.calculateScaledValue(MIN_WIDTH, MAX_WIDTH, ratio);
        currentHeight = ScalingUtil.calculateScaledValue(MIN_HEIGHT, MAX_HEIGHT, ratio);
        currentUsableWidth = ScalingUtil.calculateScaledValue(MIN_USABLE_WIDTH, MAX_USABLE_WIDTH, ratio);
        currentTextFieldHeight = ScalingUtil.calculateScaledValue(MIN_TEXT_FIELD_HEIGHT, MAX_TEXT_FIELD_HEIGHT, ratio);
        currentSearchButtonSize = ScalingUtil.calculateScaledValue(MIN_SEARCH_BUTTON_SIZE, MAX_SEARCH_BUTTON_SIZE, ratio);
        currentButtonWidth = ScalingUtil.calculateScaledValue(MIN_BUTTON_WIDTH, MAX_BUTTON_WIDTH, ratio);
        currentButtonHeight = ScalingUtil.calculateScaledValue(MIN_BUTTON_HEIGHT, MAX_BUTTON_HEIGHT, ratio);
        
        // Update the component size and layout
        updatePreferredSize();
        layoutComponentsWithPadding();
        revalidate();
        repaint();
    }
    
    /**
     * Updates the preferred size based on current dimensions
     */
    private void updatePreferredSize() {
        setPreferredSize(new Dimension(
            currentWidth + (PANEL_PADDING * 2), 
            currentHeight + (PANEL_PADDING * 2)
        ));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
    }

    /**
     * Layout components with proper padding and spacing
     */
    private void layoutComponentsWithPadding() {
        // TextField positioning (with padding from panel edges)
        textField.setBounds(
            PANEL_PADDING, 
            PANEL_PADDING, 
            currentUsableWidth - currentSearchButtonSize - COMPONENT_SPACING, 
            currentTextFieldHeight
        );
        
        // Search button positioning (right of text field with spacing)
        searchButton.setBounds(
            PANEL_PADDING + currentUsableWidth - currentSearchButtonSize, 
            PANEL_PADDING + (currentTextFieldHeight - currentSearchButtonSize) / 2, // Vertically center
            currentSearchButtonSize, 
            currentSearchButtonSize
        );
        
        // Help button positioning (with padding and spacing)
        helpButton.setBounds(
            PANEL_PADDING, 
            PANEL_PADDING + currentTextFieldHeight + COMPONENT_SPACING, 
            currentButtonWidth, 
            currentButtonHeight
        );
        
        // Settings button positioning (right of help button with spacing)
        settingsButton.setBounds(
            PANEL_PADDING + currentButtonWidth + COMPONENT_SPACING, 
            PANEL_PADDING + currentTextFieldHeight + COMPONENT_SPACING, 
            currentButtonWidth, 
            currentButtonHeight
        );
        
        // Update button sizes
        helpButton.setPreferredSize(new Dimension(currentButtonWidth, currentButtonHeight));
        settingsButton.setPreferredSize(new Dimension(currentButtonWidth, currentButtonHeight));
    }



    /**
     * Customize internal buttons to match the panel's aesthetic
     */
    private void customizeInternalButtons() {
        helpButton.setBackground(BACKGROUND_COLOR);
        settingsButton.setBackground(BACKGROUND_COLOR);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Shape roundedRect = new RoundRectangle2D.Float(
            0, 0, 
            getWidth() - shadowSize, 
            getHeight() - shadowSize, 
            arcRadius, arcRadius
        );
        
        // Draw the shadow first
        drawShadow(g2, roundedRect);
        
        // Set clip to the component shape for further painting
        g2.setClip(roundedRect);
        
        // Fill the background
        g2.setColor(BACKGROUND_COLOR);
        g2.fill(roundedRect);
        
        g2.setColor(Color.decode("#587785"));
        g2.draw(roundedRect);
        
        g2.dispose();
    }

    /**
     * Draws a shadow effect behind the component shape.
     */
    private void drawShadow(Graphics2D g2, Shape shape) {
        AffineTransform originalTransform = g2.getTransform();
        
        Color shadowColor = new Color(0, 0, 0, 50);
        
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

    // Getter methods for implementing additional handlers
    public JTextField getTextField() {
        return textField;
    }

    public SearchButton getSearchButton() {
        return searchButton;
    }

    public ModernButton getHelpButton() {
        return helpButton;
    }

    public ModernButton getSettingsButton() {
        return settingsButton;
    }
}