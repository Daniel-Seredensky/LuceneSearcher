package GUI.Components;

import GUI.Utilities.ScalingUtil;
import src.Results;
import src.ResultItem;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

import java.util.List;

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
    public ResultsPanel(Results results) {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Create a container for the cards
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setOpaque(false);
        
        int topPadding = ScalingUtil.scalePadding(50);
        int sidePadding = ScalingUtil.scalePadding(50);
        int bottomPadding = ScalingUtil.scalePadding(20); 
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
    private void populateResults(Results results) {
        cardsContainer.removeAll();
        
        // Add vertical glue at the top so that the added cards are pushed down to the search bar
        cardsContainer.add(Box.createVerticalGlue());
        
        // default to no results found
        if (results == null || results.getItems().size()==0) {
            ResultItem noResultItem = new ResultItem("No Results", "No search results found", "System","","","",0,0);
            ResultCard noResultCard = new ResultCard(noResultItem);
            noResultCard.setAlignmentX(CENTER_ALIGNMENT);
            cardsContainer.add(noResultCard);
            return;
        }
        
        List<ResultItem> items = results.getItems();
        int size = items.size();
        for (int i = 0; i < size; i++) {
            ResultCard card = new ResultCard(items.get(i));
            
            card.setAlignmentX(CENTER_ALIGNMENT);
            cardsContainer.add(card);
        
            cardsContainer.add(Box.createVerticalStrut(ScalingUtil.scalePadding(40)));
        }
    }    
    
    /**
     * Updates the panel with new search results.
     * 
     * @param results ArrayList of new result strings
     */
    public void updateResults(Results results) {
        cardsContainer.removeAll();
        populateResults(results);
        revalidate();
        repaint();
    }
}
