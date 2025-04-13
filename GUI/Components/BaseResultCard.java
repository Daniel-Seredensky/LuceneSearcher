package GUI.Components;

import GUI.Utilities.ScalingUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class BaseResultCard extends JPanel {

    // Fields parsed from the result string
    protected int resultNumber;
    protected String filename;
    protected String title;
    protected String author;
    protected double score;
    protected String bestFragment;
    protected String fullExplanation;

    // Colors from the theme (declared as protected so the subclass can use them)
    protected final Color PUNGA = new Color(0x4D483D);
    protected final Color FETA = new Color(0xDDE0D4);
    protected final Color ACCENT_COLOR = new Color(0x587785);

    // Hover state and animation-related variables
    protected boolean isHovered = false;
    protected final int COLLAPSED_HEIGHT = ScalingUtil.scaleHeight(200);
    protected final int EXPANDED_HEIGHT = ScalingUtil.scaleHeight(300);
    protected int targetHeight = COLLAPSED_HEIGHT;
    protected int currentHeight = COLLAPSED_HEIGHT;
    protected Timer animationTimer;
    protected final int ANIMATION_DURATION = 500; // milliseconds
    protected final int ANIMATION_STEPS = 50;
    protected int animationStep = 0;
    protected Dimension originalSize = new Dimension(ScalingUtil.scaleWidth(1000), COLLAPSED_HEIGHT);

    /**
     * Constructs a new BaseResultCard by parsing the provided result string.
     *
     * @param resultString the raw string representing the result output
     */
    public BaseResultCard(String resultString) {
        parseResultString(resultString);

        // Set an initial preferred size
        Dimension curSize = new Dimension(ScalingUtil.scaleWidth(1000), COLLAPSED_HEIGHT);
        setPreferredSize(curSize);
        setMinimumSize(new Dimension(ScalingUtil.scaleWidth(curSize.width),
                ScalingUtil.scaleHeight(curSize.height - 50)));
        setMaximumSize(new Dimension(ScalingUtil.scaleWidth(curSize.width),
                ScalingUtil.scaleHeight(curSize.height + 50)));
        setOpaque(false);

        // Set up mouse listeners for hover effect and animation trigger
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                targetHeight = EXPANDED_HEIGHT;
                startAnimation();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                targetHeight = COLLAPSED_HEIGHT;
                startAnimation();
            }
        });

        // Initialize animation timer
        animationTimer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, e -> {
            animationStep++;

            // Calculate new height using animation progress
            double progress = (double) animationStep / ANIMATION_STEPS;
            currentHeight = getHeight();
            int heightDiff = targetHeight - currentHeight;
            currentHeight = currentHeight + (int) (heightDiff * progress);

            // Update size constraints based on the current height
            Dimension newSize = new Dimension(originalSize.width, currentHeight);
            setPreferredSize(newSize);
            setMinimumSize(new Dimension(ScalingUtil.scaleWidth(newSize.width),
                    ScalingUtil.scaleHeight(newSize.height - 50)));
            setMaximumSize(new Dimension(ScalingUtil.scaleWidth(newSize.width),
                    ScalingUtil.scaleHeight(newSize.height + 50)));
            revalidate();
            repaint();

            // End the animation when complete
            if (animationStep >= ANIMATION_STEPS) {
                animationTimer.stop();
                currentHeight = targetHeight;

                Dimension finalSize = new Dimension(originalSize.width, targetHeight);
                setPreferredSize(finalSize);
                setMinimumSize(new Dimension(ScalingUtil.scaleWidth(finalSize.width),
                        ScalingUtil.scaleHeight(finalSize.height - 50)));
                setMaximumSize(new Dimension(ScalingUtil.scaleWidth(finalSize.width),
                        ScalingUtil.scaleHeight(finalSize.height + 50)));
                revalidate();
                repaint();
            }
        });
    }

    /**
     * Starts the animation for expanding/collapsing the card.
     */
    protected void startAnimation() {
        animationStep = 0;
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        animationTimer.start();
    }

    /**
     * Parses the result string into component fields.
     *
     * @param resultString the raw result string
     */
    protected void parseResultString(String resultString) {
        String[] lines = resultString.split("\\n");
        StringBuilder explanationBuilder = new StringBuilder();
        boolean readingExplanation = false;

        for (String line : lines) {
            if (line.startsWith("Result Number:")) {
                try {
                    resultNumber = Integer.parseInt(line.substring("Result Number:".length()).trim());
                } catch (NumberFormatException e) {
                    resultNumber = -1;
                }
            } else if (line.startsWith("Filename:")) {
                filename = line.substring("Filename:".length()).trim();
            } else if (line.startsWith("Title:")) {
                title = line.substring("Title:".length()).trim();
            } else if (line.startsWith("Author:")) {
                author = line.substring("Author:".length()).trim();
            } else if (line.startsWith("Score:")) {
                try {
                    score = Double.parseDouble(line.substring("Score:".length()).trim());
                } catch (NumberFormatException e) {
                    score = 0.0;
                }
            } else if (line.startsWith("Best Fragment")) {
                int colonIndex = line.indexOf(":");
                if (colonIndex != -1) {
                    bestFragment = line.substring(colonIndex + 1).trim();
                }
            } else if (line.startsWith("Full Explanation:")) {
                readingExplanation = true;
            } else {
                if (readingExplanation) {
                    explanationBuilder.append(line).append("\n");
                }
            }
        }
        if (explanationBuilder.length() > 0) {
            fullExplanation = explanationBuilder.toString().trim();
        }
    }
}