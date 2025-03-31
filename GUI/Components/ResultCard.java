package GUI.Components;

import GUI.Utilities.DrawingUtils;
import GUI.Utilities.ScalingUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

/**
 * A ResultCard represents one result from the Lucene search output.
 * It parses the provided result string to display the required fields.
 */
public class ResultCard extends JPanel {

    // Fields parsed from the result string
    private int resultNumber;
    private String filename;
    private String title;
    private String author;
    private double score;
    private String bestFragment;
    private String fullExplanation;

    // Colors from the theme
    private final Color PUNGA = new Color(0x4D483D);       // main background (used here for text/shadow)
    private final Color FETA = new Color(0xDDE0D4);        // card background

    /**
     * Constructs a new ResultCard by parsing the provided result string.
     * 
     * @param resultString the raw string representing the result output
     */
    public ResultCard(String resultString) {
        System.out.println("String: "+ resultString);
        parseResultString(resultString);
        // Set a preferred size (could be adjusted dynamically based on content)
        setPreferredSize(new Dimension(ScalingUtil.scaleWidth(400), ScalingUtil.scaleHeight(200)));
        // Make the panel non-opaque so we can paint a custom background
        setOpaque(false);
    }

    /**
     * Parses the result string into component fields.
     * Supports both a basic result and one with a best matching fragment and explanation.
     * 
     * @param resultString the raw result string
     */
    private void parseResultString(String resultString) {
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

    /**
     * Custom painting of the card.
     * This draws the card’s background (with shadow) and all text elements with a shadow effect.
     *
     * @param g the Graphics context to use for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Enable anti-aliasing for smooth edges and text
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Define arc for rounded corners using a scaled value
        int arc = ScalingUtil.scaleWidth(15);
        Shape cardShape = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);

        // Draw shadow behind the card
        Color shadowColor = DrawingUtils.createShadowColor(100); // semi-transparent black
        int shadowSize = ScalingUtil.scalePadding(5);
        DrawingUtils.drawShadow(g2, cardShape, shadowSize, shadowColor);

        // Fill the card background with FETA color
        g2.setColor(FETA);
        g2.fill(cardShape);

        // Prepare text rendering parameters
        int padding = ScalingUtil.scalePadding(10);
        int yPosition = padding + ScalingUtil.scaleWidth(20); // initial vertical position

        // Set up fonts (scaled based on reference resolution)
        Font titleFont = new Font("SansSerif", Font.BOLD, ScalingUtil.scaleWidth(16));
        Font normalFont = new Font("SansSerif", Font.PLAIN, ScalingUtil.scaleWidth(14));

        // Text color (using PUNGA for contrast)
        Color textColor = PUNGA;
        int textShadowSize = ScalingUtil.scalePadding(2);

        // Draw the parsed text lines using our utility method for text with shadow
        DrawingUtils.drawTextWithShadow(g2, "Result Number: " + resultNumber, padding, yPosition, normalFont, textColor, shadowColor, textShadowSize);
        yPosition += normalFont.getSize() + padding;

        DrawingUtils.drawTextWithShadow(g2, "Filename: " + filename, padding, yPosition, normalFont, textColor, shadowColor, textShadowSize);
        yPosition += normalFont.getSize() + padding;

        DrawingUtils.drawTextWithShadow(g2, "Title: " + title, padding, yPosition, titleFont, textColor, shadowColor, textShadowSize);
        yPosition += titleFont.getSize() + padding;

        DrawingUtils.drawTextWithShadow(g2, "Author: " + author, padding, yPosition, normalFont, textColor, shadowColor, textShadowSize);
        yPosition += normalFont.getSize() + padding;

        // If a best fragment was provided, draw it.
        if (bestFragment != null && !bestFragment.isEmpty()) {
            DrawingUtils.drawTextWithShadow(g2, "Best Fragment: " + bestFragment, padding, yPosition, normalFont, textColor, shadowColor, textShadowSize);
            yPosition += normalFont.getSize() + padding;
        }

        DrawingUtils.drawTextWithShadow(g2, "Score: " + score, padding, yPosition, normalFont, textColor, shadowColor, textShadowSize);
        yPosition += normalFont.getSize() + padding;

        // If a full explanation exists, render it line by line.
        if (fullExplanation != null && !fullExplanation.isEmpty()) {
            String[] explanationLines = fullExplanation.split("\n");
            for (String line : explanationLines) {
                DrawingUtils.drawTextWithShadow(g2, line, padding, yPosition, normalFont, textColor, shadowColor, textShadowSize);
                yPosition += normalFont.getSize() + ScalingUtil.scalePadding(2);
            }
        }

        g2.dispose();
    }
}

