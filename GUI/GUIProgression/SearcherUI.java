package GUI.GUIProgression;

import src.SearchManager; 
import GUI.Components.ResultsPanel;
import GUI.Components.CustomTextField;
import GUI.Utilities.ScalingUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import src.Results;
import src.Indexers.TextFileIndexer.IndexingResult;

public class SearcherUI extends ComponentLayout {

    private SearchManager searchManager;
    private Results results;                   
    private ResultsPanel resultsPanel;
    private IndexingResult indexResults;
    private boolean isShowingIndexingResults = false;

    /**
     * Constructor that also initializes the SearchManager with given parameters.
     */
    public SearcherUI(String title, String indexPath, boolean explain, int maxResults,IndexingResult indexResults) {
        super(title);
        this.indexResults = indexResults;
        // Create the SearchManager using the specified parameters
        try {
            searchManager = new SearchManager(indexPath, explain, maxResults);
            searchManager.setIsLenientQuery(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Initialize the handlers
        addSearchButtonHandler();
        addIndexingResultsButtonHandler();
    }

    public void setShowingIndexingResults(boolean isShowingIndexingResults) {
        this.isShowingIndexingResults = isShowingIndexingResults;
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

            // If there are no current results, create a new ResultsPanel else update the existing one
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
     * Attaches a handler to the search button. When pressed:<p>
     * 1) Reads the text field for the query,<p>
     * 2) Runs the search via SearchManager,<p>
     * 3) Saves the results,<p>
     * 4) Flags that we have results to show,<p>
     * 5) Relayouts and repaints.<p>
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

            CustomTextField maxResultsField = searchBar.getMaxResults();
            String maxResultsString = maxResultsField.getText();
            maxResultsField.setText("");
            if (maxResultsString.isEmpty()){
                maxResultsString = maxResultsField.getPrompt();
            }
            int maxResultsNumber;
            try {
                maxResultsNumber = Integer.parseInt(maxResultsString);
            } catch (NumberFormatException ex) {
                maxResultsString = "5";
                maxResultsNumber = 5;
            }

            maxResultsField.setPrompt(maxResultsString);
            searchManager.setMaxResults(maxResultsNumber);
            searchManager.setExplain(searchBar.getExplainButton().getState());

            String query = searchBar.getTextField().getText();

            ArrayList<Object> fields = new ArrayList<>(searchBar.getComboBox().getSelectedItems());
            boolean hasLiteralSearch = fields.contains("Literal Search");
            query = hasLiteralSearch ? "\"" + query + "\"" : query;
            

            String finalQuery = "";
            // Add the fields to the query
            for (int i = 0; i < fields.size(); i++) {
                if (fields.get(i).equals("Literal Search")) continue;
                boolean isNextOutOfBounds = i + 1 >= fields.size();
                boolean isNextFieldLiteral = isNextOutOfBounds ? false : fields.get(i + 1).equals("Literal Search");
                finalQuery += isNextOutOfBounds || isNextFieldLiteral ? fields.get(i) + ": " + query : fields.get(i) + ": " + query + " AND ";
            }
            query = finalQuery.isEmpty() ? query : finalQuery;

            System.out.println("Final Query: " + query);

            // Execute the search and store results
            try{
                results = searchManager.searchIndex(query, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            isShowingResults = true; // triggers the "results" layout

            // close the searcher and reset text field
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

    public void addIndexingResultsButtonHandler(){
        searchBar.getIndexStatsButton().addActionListener(e -> {
            if (!this.isShowingIndexingResults){
                SwingUtilities.invokeLater(() -> {
                    IndexingStatsGUI indexingUI = new IndexingStatsGUI(this.indexResults,this);
                    indexingUI.setVisible(true);
                });
            }
        });
    }
}