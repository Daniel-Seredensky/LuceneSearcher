package GUI.Components;

import GUI.Components.CustomTextField;
import GUI.Utilities.DrawingUtils;
import GUI.Utilities.ScalingUtil;
import raven.combobox.CustomComboBoxMultiSelection;
import src.QueryManager;

import javax.swing.JComboBox;
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
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import javax.swing.UIManager;
import java.awt.Font;

/**
 * A unified search bar with integrated components and a modern design.
 * Dynamically resizes based on the parent frame's size.
 */
public class SearchBar extends JPanel {
    private final CustomTextField textField;
    private final SearchButton searchButton;
    private final ModernButton indexStatsButton;
    private final ModernButtonBoolean explainButton;
    private final CustomTextField maxResults;
    private final CustomComboBoxMultiSelection<String> comboBox;
    private final double ratio = 1.125;

    private final int arcRadius = (int)(30 * ratio);
    private final int shadowSize = (int)(10 * ratio);
    private final Color BACKGROUND_COLOR = Color.decode("#DDE0D4"); // FETA color
    private final Color shadowColor = new Color(0, 0, 0, 50);

    // Padding constants
    private final int PANEL_PADDING = (int)(40 * ratio);
    private final int COMPONENT_SPACING = (int)(15 * ratio);

    // Maximum size values (for fullscreen)
    private final int MAX_WIDTH = (int)(600 * ratio);
    private final int MAX_HEIGHT = (int)(100 * ratio);
    private final int MAX_USABLE_WIDTH = (int)(600 * ratio);
    private final int MAX_TEXT_FIELD_HEIGHT = (int)(60 * ratio);
    private final int MAX_SEARCH_BUTTON_SIZE = (int)(50 * ratio);
    private final int MAX_BUTTON_WIDTH = (int)(140 * ratio);
    private final int MAX_BUTTON_HEIGHT = (int)(45 * ratio);
    private final int MAX_COMBOBOX_WIDTH = (int)(175 * ratio);
    private final int MAX_COMBOBOX_HEIGHT = (int)(35 * ratio);

    // Minimum size values (for 800x600 frame)
    private final int MIN_WIDTH = (int)(550 * ratio);
    private final int MIN_HEIGHT = (int)(70 * ratio);
    private final int MIN_USABLE_WIDTH = (int)(550 * ratio);
    private final int MIN_TEXT_FIELD_HEIGHT = (int)(60 * ratio);
    private final int MIN_SEARCH_BUTTON_SIZE = (int)(40 * ratio);
    private final int MIN_BUTTON_WIDTH = (int)(110 * ratio);
    private final int MIN_BUTTON_HEIGHT = (int)(35 * ratio);
    private final int MIN_COMBOBOX_WIDTH = (int)(150 * ratio);
    private final int MIN_COMBOBOX_HEIGHT = (int)(30 * ratio);

    
    // Current size values
    private int currentWidth = MAX_WIDTH;
    private int currentHeight = MAX_HEIGHT;
    private int currentUsableWidth = MAX_USABLE_WIDTH;
    private int currentTextFieldHeight = MAX_TEXT_FIELD_HEIGHT;
    private int currentSearchButtonSize = MAX_SEARCH_BUTTON_SIZE;
    private int currentButtonWidth = MAX_BUTTON_WIDTH;
    private int currentButtonHeight = MAX_BUTTON_HEIGHT;
    private int currentComboBoxWidth = MAX_COMBOBOX_WIDTH;
    private int currentComboBoxHeight = MAX_COMBOBOX_HEIGHT;
    private Dimension MAXRESULTS_SIZE = new Dimension(ScalingUtil.scaleWidth(60), ScalingUtil.scaleHeight(35));

    public SearchBar() {
        initFlatlafForComboBox();
        setLayout(null);
        setOpaque(false);

        textField = new CustomTextField();
        maxResults = new CustomTextField();
        maxResults.setPrompt("5");
        
        searchButton = new SearchButton();

        comboBox = new CustomComboBoxMultiSelection<>();
        setData(comboBox);

        indexStatsButton = new ModernButton(MAX_BUTTON_WIDTH + ScalingUtil.scaleWidth(20), MAX_BUTTON_HEIGHT + ScalingUtil.scaleHeight(10), "Indexing Statistics");
        explainButton = new ModernButtonBoolean(MAX_BUTTON_WIDTH, MAX_BUTTON_HEIGHT, "Explain");

        add(textField);
        add(searchButton);
        add(indexStatsButton);
        add(explainButton);
        add(comboBox);
        add(maxResults);

        layoutComponentsWithPadding();

        updatePreferredSize();
        
        setupResizeListeners();
    }

    public void initFlatlafForComboBox() {
        // Set the custom font for the combo box
        FlatMacDarkLaf.setup();
        FlatRobotoFont.install();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        FlatLaf.registerCustomDefaultsSource("raven.combobox");
        UIManager.put("ComboBox.background", new Color(0x4D483D));       // PUNGA for primary background
        UIManager.put("ComboBox.foreground", new Color(0xDDE0D4));       // FETA for primary text/foreground
        UIManager.put("ComboBox.editableBackground", new Color(0x4D483D)); // Ensure editable areas match the background
        UIManager.put("ComboBox.buttonArrowColor", new Color(0x5E6C5E));  // FIN_LANDIA for the drop-down arrow
        UIManager.put("ComboBox.borderColor", new Color(0x5E6C5E));       // FIN_LANDIA for standard border
        UIManager.put("ComboBox.focusedBorderColor", new Color(0x5E6C5E));  // FIN_LANDIA for the focused/hover border
        UIManager.put("ComboBox.hoverBorderColor", new Color(0x5E6C5E)); 
        FlatLaf.updateUI();
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
        
        currentWidth = ScalingUtil.calculateScaledValue(MIN_WIDTH, MAX_WIDTH, ratio);
        currentHeight = ScalingUtil.calculateScaledValue(MIN_HEIGHT, MAX_HEIGHT, ratio);
        currentUsableWidth = ScalingUtil.calculateScaledValue(MIN_USABLE_WIDTH, MAX_USABLE_WIDTH, ratio);
        currentTextFieldHeight = ScalingUtil.calculateScaledValue(MIN_TEXT_FIELD_HEIGHT, MAX_TEXT_FIELD_HEIGHT, ratio);
        currentSearchButtonSize = ScalingUtil.calculateScaledValue(MIN_SEARCH_BUTTON_SIZE, MAX_SEARCH_BUTTON_SIZE, ratio);
        currentButtonWidth = ScalingUtil.calculateScaledValue(MIN_BUTTON_WIDTH, MAX_BUTTON_WIDTH, ratio);
        currentButtonHeight = ScalingUtil.calculateScaledValue(MIN_BUTTON_HEIGHT, MAX_BUTTON_HEIGHT, ratio);
        currentComboBoxWidth = ScalingUtil.calculateScaledValue(MIN_COMBOBOX_WIDTH, MAX_COMBOBOX_WIDTH, ratio);
        currentComboBoxHeight = ScalingUtil.calculateScaledValue(MIN_COMBOBOX_HEIGHT, MAX_COMBOBOX_HEIGHT, ratio);

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
        // TextField positioning 
        textField.setBounds(
            PANEL_PADDING, 
            PANEL_PADDING, 
            currentUsableWidth - currentSearchButtonSize - COMPONENT_SPACING, 
            currentTextFieldHeight
        );
        
        // Search button positioning 
        searchButton.setBounds(
            PANEL_PADDING + currentUsableWidth - currentSearchButtonSize, 
            PANEL_PADDING + (currentTextFieldHeight - currentSearchButtonSize) / 2, // Vertically center
            currentSearchButtonSize, 
            currentSearchButtonSize
        );
        
        // Help button positioning
        indexStatsButton.setBounds(
            PANEL_PADDING, 
            PANEL_PADDING + currentTextFieldHeight + COMPONENT_SPACING, 
            currentButtonWidth, 
            currentButtonHeight
        );
        
        // Settings button positioning 
        explainButton.setBounds(
            PANEL_PADDING + currentButtonWidth + COMPONENT_SPACING, 
            PANEL_PADDING + currentTextFieldHeight + COMPONENT_SPACING, 
            currentButtonWidth, 
            currentButtonHeight
        );

        // ComboBox positioning
        comboBox.setBounds(
            PANEL_PADDING + ((currentButtonWidth + COMPONENT_SPACING) * 2),
            PANEL_PADDING + currentTextFieldHeight + COMPONENT_SPACING + ((currentButtonHeight - currentComboBoxHeight)/2) - ScalingUtil.scalePadding(10)/2, // Center vertically relative to the button position while accounting for button shadow
            currentComboBoxWidth,
            currentComboBoxHeight 
        );

        // Max Results positioning 
        maxResults.setBounds(
            PANEL_PADDING + currentUsableWidth - currentSearchButtonSize, 
            PANEL_PADDING + currentSearchButtonSize + COMPONENT_SPACING + ((currentButtonHeight - MAXRESULTS_SIZE.height)/2), // center vertically relative to the button position
            MAXRESULTS_SIZE.width,
            MAXRESULTS_SIZE.height
        );

        comboBox.setPreferredSize(new Dimension(currentComboBoxWidth, currentComboBoxHeight));
        comboBox.setMaximumSize(new Dimension(currentComboBoxWidth, currentComboBoxHeight));
        comboBox.setMinimumSize(new Dimension(currentComboBoxWidth, currentComboBoxHeight));
        maxResults.setPreferredSize(MAXRESULTS_SIZE);
        maxResults.setMaximumSize(MAXRESULTS_SIZE);
        maxResults.setMinimumSize(MAXRESULTS_SIZE);

        indexStatsButton.setPreferredSize(new Dimension(currentButtonWidth, currentButtonHeight));
        explainButton.setPreferredSize(new Dimension(currentButtonWidth, currentButtonHeight));
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
        
        DrawingUtils.drawShadow(g2, roundedRect, shadowSize, shadowColor);
        
        g2.setClip(roundedRect);
        
        g2.setColor(BACKGROUND_COLOR);
        g2.fill(roundedRect);
        
        g2.setColor(Color.decode("#587785"));
        g2.draw(roundedRect);
        
        g2.dispose();
    }

    // Getter methods for implementing additional handlers
    public CustomTextField getTextField() {
        return textField;
    }
    
    public CustomTextField getMaxResults() {
        return maxResults;
    }

    public SearchButton getSearchButton() {
        return searchButton;
    }

    public ModernButton getIndexStatsButton() {
        return indexStatsButton;
    }

    public ModernButtonBoolean getExplainButton() {
        return explainButton;
    }

    public CustomComboBoxMultiSelection<String> getComboBox() {
        return comboBox;
    }

    /** Sets the data for the combo box */
    private void setData(JComboBox<String> combo) {
        combo.setModel(new javax.swing.DefaultComboBoxModel<String>(
            combineArrays(
                new String[]{"Literal Search"},
                QueryManager.ALL_FIELDS
                )
            )
        );
    }

    /** Combines two arrays into one */
    public static String[] combineArrays(String[] array1, String[] array2) {
        return Stream.concat(Arrays.stream(array1), Arrays.stream(array2))
                    .toArray(String[]::new);
    }
}
