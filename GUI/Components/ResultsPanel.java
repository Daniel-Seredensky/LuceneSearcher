package GUI.Components;

import GUI.Utilities.ScalingUtil;
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

/**
 * A panel that displays search results as a scrollable list of ResultCard components.
 * The panel is set to a fixed width of about 600 pixels (scaled) and uses a layout
 * that anchors cards to the bottom.
 */
public class ResultsPanel extends JPanel {
    
    private JPanel cardsContainer;
    private JScrollPane scrollPane;
    
    /**
     * Constructs a new ResultsPanel with the given search results.
     * 
     * @param results ArrayList of result strings to display as cards
     */
    public ResultsPanel(ArrayList<String> results) {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Create a container for the cards
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setOpaque(false);
        
        int topPadding = ScalingUtil.scalePadding(50);
        int sidePadding = ScalingUtil.scalePadding(50);
        int bottomPadding = ScalingUtil.scalePadding(20); // Reduced bottom padding
        cardsContainer.setBorder(new EmptyBorder(topPadding, sidePadding, bottomPadding, sidePadding));
        
        // Create a scroll pane for the cards container
        scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollPane, BorderLayout.CENTER);
        
        populateResults(results);
    }
    
    /**
     * Creates and adds ResultCard components for each result string.
     * Cards are added in reverse order so that the first result appears at the bottom.
     * A vertical glue is added at the top to push components down.
     * 
     * @param results ArrayList of result strings
     */
    private void populateResults(ArrayList<String> results) {
        cardsContainer.removeAll();
        
        // Add vertical glue at the top so that the added cards are pushed down to the search bar
        cardsContainer.add(Box.createVerticalGlue());
        
        // default to no results found
        if (results == null || results.isEmpty()) {
            ResultCard noResultCard = new ResultCard("Result Number: 0\nFilename: No Results\nTitle: No search results found\nAuthor: System");
            noResultCard.setAlignmentX(LEFT_ALIGNMENT);
            cardsContainer.add(noResultCard);
            return;
        }
        
        // Iterate in reverse order so that the first result is at the bottom
        for (int i = results.size() - 1; i >= 0; i--) {
            String resultString = results.get(i);
            ResultCard card = new ResultCard(resultString);
            
            card.setAlignmentX(CENTER_ALIGNMENT);
            cardsContainer.add(card);
            
            // Only add vertical spacing if there will be another card above
            if (i != 0) {
                cardsContainer.add(Box.createVerticalStrut(ScalingUtil.scalePadding(40)));
            }
        }
    }    
    
    /**
     * Updates the panel with new search results.
     * 
     * @param results ArrayList of new result strings
     */
    public void updateResults(ArrayList<String> results) {
        cardsContainer.removeAll();
        populateResults(results);
        revalidate();
        repaint();
    }
}
