package GUI;

import GUI.GUIProgression.BaseGUI;
import GUI.Components.SearchBar;
import javax.swing.*;
import java.awt.*;

/**
 * test GUI for components
 */
public class ComponentTest extends BaseGUI {
    
    private SearchBar searchBar;
    
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
        
        JPanel centeringPanel = new JPanel();
        centeringPanel.setOpaque(false); 
        centeringPanel.setLayout(new BoxLayout(centeringPanel, BoxLayout.Y_AXIS));
        
        // Add glue before the search bar to push it down from the top
        centeringPanel.add(Box.createVerticalGlue());
        
        // Create a horizontal box for horizontal centering
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(Box.createHorizontalGlue());
        horizontalBox.add(searchBar);
        horizontalBox.add(Box.createHorizontalGlue());
        
        // Add the horizontal box to the centering panel
        centeringPanel.add(horizontalBox);
        
        // Add glue after the search bar to push it up from the bottom
        centeringPanel.add(Box.createVerticalGlue());
        
        // Add the centering panel to the content pane
        contentPane.add(centeringPanel, BorderLayout.CENTER);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ComponentTest test = new ComponentTest();
            test.setVisible(true);
        });
    }
}
