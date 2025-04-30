package GUI.GUIProgression;

import GUI.Components.Title;
import GUI.Utilities.DrawingUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

/**
 * GUI for displaying Lucene indexing statistics.
 * Shows information about documents added, changed, removed, and elapsed time.
 */
public class IndexingStatsGUI extends BaseGUI {
    
    private Title titleComponent;
    private JPanel statsPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    
    private final DecimalFormat timeFormat = new DecimalFormat("#0.000");
    
    private IndexingResult result;
    
    /**
     * Creates a new IndexingStatsGUI with the given indexing result.
     * 
     * @param result The indexing result to display
     */
    public IndexingStatsGUI(IndexingResult result) {
        super("Lucene Indexing Statistics");
        this.result = result;
        updateStats();
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
                    
                    Font statFont = new Font("SansSerif", Font.BOLD, 18);
                    Font valueFont = new Font("SansSerif", Font.PLAIN, 24);
                    Color textColor = Color.decode("#DDE0D4");
                    Color shadowColor = new Color(0, 0, 0, 50);
                    
                    int y = 50;
                    int leftMargin = 50;
                    int lineHeight = 40;
                    
                    DrawingUtils.drawTextWithShadow(g2d, "Documents Added:", leftMargin, y, statFont, 
                                                  textColor, shadowColor, 3);
                    DrawingUtils.drawTextWithShadow(g2d, String.valueOf(result.added), leftMargin + 250, y, valueFont, 
                                                  textColor, shadowColor, 4);
                    
                    y += lineHeight;
                    DrawingUtils.drawTextWithShadow(g2d, "Documents Changed:", leftMargin, y, statFont, 
                                                  textColor, shadowColor, 3);
                    DrawingUtils.drawTextWithShadow(g2d, String.valueOf(result.changed), leftMargin + 250, y, valueFont, 
                                                  textColor, shadowColor, 4);
                    
                    y += lineHeight;
                    DrawingUtils.drawTextWithShadow(g2d, "Documents Removed:", leftMargin, y, statFont, 
                                                  textColor, shadowColor, 3);
                    DrawingUtils.drawTextWithShadow(g2d, String.valueOf(result.removed), leftMargin + 250, y, valueFont, 
                                                  textColor, shadowColor, 4);
                    
                    y += lineHeight;
                    DrawingUtils.drawTextWithShadow(g2d, "Elapsed Time:", leftMargin, y, statFont, 
                                                  textColor, shadowColor, 3);
                    DrawingUtils.drawTextWithShadow(g2d, timeFormat.format(result.elapsedTime) + " seconds", 
                                                  leftMargin + 250, y, valueFont, 
                                                  textColor, shadowColor, 4);
                    
                    g2d.dispose();
                }
            }
        };
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(600, 200));
        
        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
    }
    
    @Override
    protected void layoutComponents() {
        setLayout(new BorderLayout());
        
        add(titleComponent, BorderLayout.NORTH);
        
        add(statsPanel, BorderLayout.CENTER);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Updates the statistics display with new data.
     */
    public void updateStats() {
        if (statsPanel != null) {
            statsPanel.repaint();
        }
    }
    
    /**
     * Updates the indexing result and refreshes the display.
     * 
     * @param result The new indexing result
     */
    public void setIndexingResult(IndexingResult result) {
        this.result = result;
        updateStats();
    }
    
    /**
     * Adds a listener to the close button.
     * 
     * @param listener The action listener to add
     */
    public void addCloseButtonListener(ActionListener listener) {
        closeButton.addActionListener(listener);
    }
    
    /**
     * Class representing indexing operation results.
     */
    public static class IndexingResult {
        int added;
        int changed;
        int removed;
        double elapsedTime;
        
        public IndexingResult(int added, int changed, int removed) {
            this.added = added;
            this.changed = changed;
            this.removed = removed;
        }
        
        public void addElapsedTime(double elapsedTime) {
            this.elapsedTime = elapsedTime;
        }
    }
    
    /**
     * Main method for testing the GUI.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IndexingResult result = new IndexingResult(125, 37, 14);
            result.addElapsedTime(3.456);
            
            IndexingStatsGUI gui = new IndexingStatsGUI(result);
            gui.setVisible(true);
        });
    }
}
