package GUI.GUIProgression;

import src.SearchManager; 
import GUI.Components.ResultsPanel;
import GUI.Utilities.ScalingUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SearcherUI extends ComponentLayout {

    private SearchManager searchManager;
    private boolean hasCurrentResults = false;           // tracks if there is current results
    private ArrayList<String> results;                   // holds the result strings
    private ResultsPanel resultsPanel;

    /**
     * Constructor that also initializes the SearchManager with given parameters.
     */
    public SearcherUI(String title, String indexPath, boolean explain, int maxResults) {
        super(title);
        // Create the SearchManager using the specified parameters
        try {
            searchManager = new SearchManager(indexPath, explain, maxResults);
            searchManager.setIsLenientQuery(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Initialize the handlers
        addSearchButtonHandler();
    }

    /**
     * Overrides layoutComponents.
     * If isShowingResults is false, calls super.layoutComponents (the default layout).
     * If true, either creates or updates the ResultsPanel, shrinks the search bar, and adds it near the bottom.
     */
    @Override
    protected void layoutComponents() {
        if (!isShowingResults) {
            // Default layout (no results), just call superclass
            super.layoutComponents();
        } else {
            // Show the results layout
            Container contentPane = getContentPane();
            contentPane.removeAll(); // remove anything previously added
            contentPane.setLayout(new BorderLayout());

            JPanel mainPanel = new JPanel();
            mainPanel.setOpaque(false);
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

            // If there are current results, either create a new ResultsPanel or update the existing one
            if (resultsPanel == null) {
                resultsPanel = new ResultsPanel(results);
            } else {
                resultsPanel.updateResults(results);
            }

            mainPanel.add(resultsPanel);
            mainPanel.add(Box.createVerticalStrut(ScalingUtil.scaleHeight(60)));

            // Shrink the search bar slightly if results are showing
            Dimension originalSize = searchBar.getPreferredSize();
            Dimension shrunkSize = new Dimension(
                    originalSize.width,
                    Math.max(originalSize.height - ScalingUtil.scalePadding(40), 10)
            );
            searchBar.setPreferredSize(shrunkSize);

            Box horizontalBox = Box.createHorizontalBox();
            horizontalBox.add(Box.createHorizontalGlue());
            horizontalBox.add(searchBar);
            horizontalBox.add(Box.createHorizontalGlue());
            mainPanel.add(horizontalBox);

            mainPanel.add(Box.createVerticalStrut(ScalingUtil.scalePadding(100)));

            contentPane.add(mainPanel, BorderLayout.CENTER);
        }
    }

    /**
     * Attaches a handler to the search button. When pressed:
     * 1) Reads the text field for the query,
     * 2) Runs the search via SearchManager,
     * 3) Saves the results,
     * 4) Flags that we have results to show,
     * 5) Relayouts and repaints.
     */
    public void addSearchButtonHandler() {
        searchBar.getSearchButton().addActionListener(e -> {
            if (!searchManager.luceneSearcher.isOpen){
                try{
                    searchManager.luceneSearcher.open();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Search button pressed");
            // Grab the text from the search bar
            String query = searchBar.getTextField().getText();
            System.out.println("Query: " + query);
            
            // Execute the search and store results
            try{
                String resultString = searchManager.searchIndex(query);
                System.out.println("Result String: " + resultString);
                results = parseSearchResults(resultString);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            hasCurrentResults = true;
            isShowingResults = true; // triggers the "results" layout
            try{
                searchManager.luceneSearcher.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            searchBar.getTextField().setText("");
            // Update UI
            layoutComponents();
            revalidate();
            repaint();
        });
    }

    /**
     * Parses a large raw result string and splits it into substrings,
     * each starting with "Result Number: X" and ending right before the next "Result Number: Y".
     *
     * @param rawResult The full multi-line string containing one or more result blocks.
     * @return A list of individual result blocks.
     */
    private ArrayList<String> parseSearchResults(String rawResult) {
        if (rawResult.isEmpty() ||rawResult.equals("No results found.")){
            return new ArrayList<>();
        }
        ArrayList<String> blocks = new ArrayList<>();
        
        // Split on a lookahead for "Result Number: " followed by one or more digits.
        // This means each substring will start with "Result Number: <digits>"
        String[] splitBlocks = rawResult.split("(?=Result Number: \\d+)");

        for (String block : splitBlocks) {
            // Clean up the block (trim whitespace, etc.)
            block = block.trim();
            // Skip empty pieces (in case there's leading/trailing newlines)
            if (!block.isEmpty()) {
                blocks.add(block);
            }
        }
        return blocks;
    }
}

