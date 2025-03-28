package GUI;

import GUI.GUIProgression.BaseGUI;
import GUI.Components.SearchBar;
import GUI.Components.Title;
import GUI.Utilities.ScalingUtil;
import javax.swing.*;
import java.awt.*;

/**
 * test GUI for components
 */
public class ComponentTest extends BaseGUI {
    
    private SearchBar searchBar;
    private Title titleComponent;
    
    public ComponentTest() {
        super("UI Components Test");
    }
    
    @Override
    protected void initializeComponents() {
        searchBar = new SearchBar();
        titleComponent = new Title("FooBar", "The Code Librarian");
    }
    
    @Override
    protected void layoutComponents() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalStrut(ScalingUtil.scalePadding(50)));
        
        mainPanel.add(titleComponent);
        
        mainPanel.add(Box.createVerticalStrut(ScalingUtil.scalePadding(200)));
        
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(Box.createHorizontalGlue());
        horizontalBox.add(searchBar);
        horizontalBox.add(Box.createHorizontalGlue());
        
        mainPanel.add(horizontalBox);
        
        mainPanel.add(Box.createVerticalGlue());
        
        contentPane.add(mainPanel, BorderLayout.CENTER);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ComponentTest test = new ComponentTest();
            test.setVisible(true);
        });
    }
}
