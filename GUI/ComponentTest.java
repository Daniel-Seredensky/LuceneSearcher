package GUI;

import GUI.GUIProgression.BaseGUI;
import GUI.Components.SearchBar;
import GUI.Components.ResultsPanel;
import GUI.Utilities.ScalingUtil;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Test GUI for components.
 */
public class ComponentTest extends BaseGUI {
    
    private SearchBar searchBar;
    private static boolean isShowingResults = true; // Set to false to hide the results panel
    
    // test Strings to get a visual for the results panel
    private static String test = "Result Number: 5\n" +
        "Filename: pg304.txt\n" +
        "Title: Rio Grande's Last Race, and Other Verses\n" +
        "Author: A. B. Paterson\n" +
        "Best Fragment from stem:  maxim gun johnni boer men fight all shape and size as the race hors <b>run</b> what have the cavalri done\n" +
        "Full Explanation:\n" +
        "0.1308253 = sum of:\n" +
        "  0.041175276 = weight(content:run in 14) [ClassicSimilarity], result of:\n" +
        "    0.041175276 = score(freq=30.0), product of:\n" +
        "      1.1289337 = idf, computed as log((docCount+1)/(docFreq+1)) + 1 from:\n" +
        "        871 = docFreq, number of documents containing term\n" +
        "        991 = docCount, total number of documents with field\n" +
        "      5.477226 = tf(freq=30.0), with freq of:\n" +
        "        30.0 = freq, occurrences of term within document\n" +
        "      0.0066589764 = fieldNorm\n" +
        "  0.048474748 = weight(stem:run in 14) [ClassicSimilarity], result of:\n" +
        "    0.048474748 = score(freq=45.0), product of:\n" +
        "      1.0851802 = idf, computed as log((docCount+1)/(docFreq+1)) + 1 from:\n" +
        "        910 = docFreq, number of documents containing term\n" +
        "        991 = docCount, total number of documents with field\n" +
        "      6.708204 = tf(freq=45.0), with freq of:\n" +
        "        45.0 = freq, occurrences of term within document\n" +
        "      0.0066589764 = fieldNorm\n" +
        "  0.041175276 = weight(stop:run in 14) [ClassicSimilarity], result of:\n" +
        "    0.041175276 = score(freq=30.0), product of:\n" +
        "      1.1289337 = idf, computed as log((docCount+1)/(docFreq+1)) + 1 from:\n" +
        "        871 = docFreq, number of documents containing term\n" +
        "        991 = docCount, total number of documents with field\n" +
        "      5.477226 = tf(freq=30.0), with freq of:\n" +
        "        30.0 = freq, occurrences of term within document\n" +
        "      0.0066589764 = fieldNorm";

        private static String test1 = "Result Number: 5\n" +
            "Filename: pg304.txt\n" +
            "Title: Rio Grande's Last Race, and Other Verses\n" +
            "Author: A. B. Paterson\n" +
            "Best Fragment from stem:  maxim gun johnni boer men fight all shape and size as the race hors <b>run</b> what have the cavalri done\n" +
            "Full Explanation:\n";

    public ComponentTest() {
        super("UI Components Test");
    }
    
    @Override
    protected void initializeComponents() {
        searchBar = new SearchBar();
    }
    
    @Override
    protected void layoutComponents() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        if (!isShowingResults) {
            // When results are hidden, only show the search bar in its default position
            mainPanel.add(Box.createVerticalStrut(ScalingUtil.scalePadding(350)));
            Box horizontalBox = Box.createHorizontalBox();
            horizontalBox.add(Box.createHorizontalGlue());
            horizontalBox.add(searchBar);
            horizontalBox.add(Box.createHorizontalGlue());
            mainPanel.add(horizontalBox);
            mainPanel.add(Box.createVerticalGlue());
        } else {
            // When showing results drop the search bar to the bottom of the screen and show the results panel
            ArrayList<String> resultsList = new ArrayList<>();
            resultsList.add(test);
            resultsList.add(test1);
            resultsList.add(test1);
            resultsList.add(test);
            resultsList.add(test);
            resultsList.add(test1);
            resultsList.add(test);

            ResultsPanel resultsPanel = new ResultsPanel(resultsList);
            
            mainPanel.add(resultsPanel);
            mainPanel.add(Box.createVerticalStrut(ScalingUtil.scaleHeight(60)));
            
            // Shrink the search bar slightly if the results panel is showing
            Dimension originalSize = searchBar.getPreferredSize();
            Dimension shrunkSize = new Dimension(originalSize.width, Math.max(originalSize.height - ScalingUtil.scalePadding(40), 10));
            searchBar.setPreferredSize(shrunkSize);
            
            // Add the search bar near the bottom of the screen
            Box horizontalBox = Box.createHorizontalBox();
            horizontalBox.add(Box.createHorizontalGlue());
            horizontalBox.add(searchBar);
            horizontalBox.add(Box.createHorizontalGlue());
            mainPanel.add(horizontalBox);
            
            mainPanel.add(Box.createVerticalStrut(ScalingUtil.scalePadding(100)));
        }
        
        contentPane.add(mainPanel, BorderLayout.CENTER);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ComponentTest testFrame = new ComponentTest();
            testFrame.setVisible(true);
        });
    }
}
