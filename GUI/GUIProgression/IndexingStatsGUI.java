package GUI.GUIProgression;

import GUI.Components.ModernButton;
import GUI.Components.Title;
import GUI.Utilities.DrawingUtils;
import GUI.Utilities.ScalingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;

import src.Indexers.TextFileIndexer.IndexingResult;

/**
 * GUI for displaying Lucene indexing statistics.
 * Shows information about documents added, changed, removed, and elapsed time.
 */
public class IndexingStatsGUI extends BaseGUI {
    
    private Title titleComponent;
    private JPanel statsPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private SearcherUI caller;
    private IndexingResult result;
    private final DecimalFormat timeFormat = new DecimalFormat("#0.000");
        
    /**
     * Creates a new IndexingStatsGUI with the given indexing result.
     * 
     * @param result The indexing result to display
     * @param caller The SearcherUI instance that opened this GUI
     */
    public IndexingStatsGUI(IndexingResult result, SearcherUI caller) {
        super("Lucene Indexing Statistics");
        this.caller = caller;
        this.caller.setShowingIndexingResults(true);
        this.result = result;
    }
    
    @Override
    protected void initializeComponents() {
        titleComponent = new Title("Indexing Statistics", "Document Processing Summary");
        
        statsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                if (result != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    
                    Font statFont = new Font("SansSerif", Font.BOLD, ScalingUtil.scaleHeight(18));
                    Font valueFont = new Font("SansSerif", Font.PLAIN, ScalingUtil.scaleHeight(24));
                    Color textColor = Color.decode("#DDE0D4");
                    Color shadowColor = new Color(0, 0, 0, 50);
                    
                    int y = ScalingUtil.scaleHeight(50);
                    int leftMargin = ScalingUtil.scaleWidth(50);
                    int lineHeight = ScalingUtil.scaleHeight(40);
                    int labelValueGap = ScalingUtil.scaleWidth(250);
                    
                    int textShadowOffset = ScalingUtil.scalePadding(3);
                    int valueShadowOffset = ScalingUtil.scalePadding(4);
                    
                    DrawingUtils.drawTextWithShadow(g2d, "Documents Added:", leftMargin, y, statFont, 
                                                  textColor, shadowColor, textShadowOffset);
                    DrawingUtils.drawTextWithShadow(g2d, String.valueOf(result.added), leftMargin + labelValueGap, y, valueFont, 
                                                  textColor, shadowColor, valueShadowOffset);
                    
                    y += lineHeight;
                    DrawingUtils.drawTextWithShadow(g2d, "Documents Changed:", leftMargin, y, statFont, 
                                                  textColor, shadowColor, textShadowOffset);
                    DrawingUtils.drawTextWithShadow(g2d, String.valueOf(result.changed), leftMargin + labelValueGap, y, valueFont, 
                                                  textColor, shadowColor, valueShadowOffset);
                
                    y += lineHeight;
                    DrawingUtils.drawTextWithShadow(g2d, "Documents Removed:", leftMargin, y, statFont, 
                                                  textColor, shadowColor, textShadowOffset);
                    DrawingUtils.drawTextWithShadow(g2d, String.valueOf(result.removed), leftMargin + labelValueGap, y, valueFont, 
                                                  textColor, shadowColor, valueShadowOffset);
                    
                    y += lineHeight;
                    DrawingUtils.drawTextWithShadow(g2d, "Elapsed Time:", leftMargin, y, statFont, 
                                                  textColor, shadowColor, textShadowOffset);
                    DrawingUtils.drawTextWithShadow(g2d, timeFormat.format(result.elapsedTime) + " ms", 
                                                  leftMargin + labelValueGap, y, valueFont, 
                                                  textColor, shadowColor, valueShadowOffset);
                    g2d.dispose();
                }
            }
        };
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(ScalingUtil.scaleDimension(650, 425));
        
        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        closeButton = new ModernButton(ScalingUtil.scaleWidth(120), ScalingUtil.scaleHeight(40), "Close");
        closeButton.addActionListener(e -> {
            this.caller.setShowingIndexingResults(false); // makes the button no longer usable 
            dispose();
        });
        
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(
            ScalingUtil.scalePadding(10), 
            ScalingUtil.scalePadding(10), 
            ScalingUtil.scalePadding(10), 
            ScalingUtil.scalePadding(10)
        ));
        
        buttonPanel.add(closeButton);
    }
    
    @Override
    protected void layoutComponents() {
        setLayout(new BorderLayout(
            ScalingUtil.scalePadding(5), 
            ScalingUtil.scalePadding(5)
        ));
        
        add(titleComponent, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setSize(ScalingUtil.scaleDimension(650, 425));
        setLocationRelativeTo(null); // Center on screen
    }
}
