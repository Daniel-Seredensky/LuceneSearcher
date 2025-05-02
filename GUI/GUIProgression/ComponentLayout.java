package GUI.GUIProgression;

import GUI.Components.SearchBar;
import GUI.Utilities.ScalingUtil;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Container;

/**
 * Superclass that handles the default (non-results) layout.
 */
public class ComponentLayout extends BaseGUI {

    protected SearchBar searchBar;
    // This field can remain here so the subclass knows whether to show results or not
    protected boolean isShowingResults = false; 

    public ComponentLayout(String title) {
        super(title);
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

        // Default layout (show just the search bar centered vertically)
        mainPanel.add(Box.createVerticalStrut(ScalingUtil.scalePadding(350)));
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
            ComponentLayout testFrame = new ComponentLayout("UI Components Test");
            testFrame.setVisible(true);
        });
    }
}
