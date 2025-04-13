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
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class ResultCard extends BaseResultCard {
    private JPanel contentPanel;
    private JScrollPane scrollPane;
    private boolean showDetails = false;
    
    /**
     * Constructs a new ResultCard using the provided result string.
     *
     * @param resultString the raw result string
     */
    public ResultCard(String resultString) {
        super(resultString);
        
        setLayout(new BorderLayout());
        
        // Create the content panel that will be painted
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                paintCardContent(g);
            }
        };
        contentPanel.setOpaque(false);
        
        // Create and configure the scroll pane
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Initialize with hidden scrollbars (they will be shown on hover)
        scrollPane.getVerticalScrollBar().setVisible(false);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Override mouse listeners to handle scrollbar visibility ^^
        removeMouseListener(getMouseListeners()[0]);
        
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                showDetails = true;
                targetHeight = EXPANDED_HEIGHT;
                scrollPane.getVerticalScrollBar().setVisible(true);
                startAnimation();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                showDetails = false;
                targetHeight = COLLAPSED_HEIGHT;
                scrollPane.getVerticalScrollBar().setVisible(false);
                startAnimation();
            }
        };
        
        addMouseListener(mouseAdapter);
        contentPanel.addMouseListener(mouseAdapter);
        scrollPane.addMouseListener(mouseAdapter);
        
        // Prevent the scrollbar from triggering mouseExit
        scrollPane.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                showDetails = true;
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // Don't change the hover state when leaving the scrollbar
                // This prevents the scrollbar from disappearing when using it
            }
        });
    }
    
    /**
     * Override the startAnimation method to handle content panel resizing
     */
    @Override
    protected void startAnimation() {
        animationStep = 0;
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        // Update the animation timer to also adjust the content panel height
        animationTimer.addActionListener(e -> {
            // Make sure the content panel is tall enough to show all content when hovered
            if (showDetails) {
                int minContentHeight = calculateMinimumContentHeight();
                contentPanel.setPreferredSize(new Dimension(originalSize.width, 
                    Math.max(targetHeight, minContentHeight)));
            } else {
                contentPanel.setPreferredSize(new Dimension(originalSize.width, COLLAPSED_HEIGHT));
            }
            contentPanel.revalidate();
            contentPanel.repaint();
        });
        
        animationTimer.start();
    }
    
    /**
     * Calculate the minimum height needed to display all content
     */
    private int calculateMinimumContentHeight() {
        int padding = ScalingUtil.scalePadding(25);
        Font normalFont = new Font("SansSerif", Font.PLAIN, ScalingUtil.scaleWidth(14));
        int baseHeight = padding * 3 + ScalingUtil.scaleWidth(40) + ScalingUtil.scalePadding(25) + 
                normalFont.getSize() + ScalingUtil.scaleHeight(30) + ScalingUtil.scalePadding(25);
        
        int textContentHeight = 0;
        if (bestFragment != null && !bestFragment.isEmpty()) {
            textContentHeight += (normalFont.getSize() + ScalingUtil.scalePadding(5)) * 2;
            textContentHeight += (normalFont.getSize() + ScalingUtil.scalePadding(5)) * 
                    (bestFragment.length() / 50 + 1); 
        }
        
        if (fullExplanation != null && !fullExplanation.isEmpty()) {
            textContentHeight += (normalFont.getSize() + ScalingUtil.scalePadding(5)) * 2;
            String[] explanationLines = fullExplanation.split("\n");
            textContentHeight += (normalFont.getSize() + ScalingUtil.scalePadding(5)) * explanationLines.length;
        }
        
        return baseHeight + textContentHeight ;
    }
    
    /**
     * Custom painting of the card content.
     *
     * @param g the Graphics context
     */
    protected void paintCardContent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Enable anti-aliasing for smooth edges and text
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = showDetails ? Math.max(getHeight(), calculateMinimumContentHeight()) : COLLAPSED_HEIGHT;
        width -= ScalingUtil.scalePadding(10);
        height -= ScalingUtil.scalePadding(10);

        // Create a rounded rectangle for the card shape
        int arc = ScalingUtil.scaleWidth(30);
        Shape cardShape = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);

        // Draw shadow behind the card using a helper from DrawingUtils
        Color shadowColor = DrawingUtils.createShadowColor(100); // Semi-transparent shadow
        int shadowSize = ScalingUtil.scalePadding(10);
        DrawingUtils.drawShadow(g2, cardShape, shadowSize, shadowColor);

        // Fill the card background with a darkened version of the theme color PUNGA
        Color backgroundColor = DrawingUtils.darkenColor(PUNGA, 0.4f);
        backgroundColor = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), 
                                    backgroundColor.getBlue(), 150); // Slight transparency
        g2.setColor(backgroundColor);
        g2.fill(cardShape);
        // Setup font and padding parameters
        int padding = ScalingUtil.scalePadding(25);
        Font titleFont = new Font("SansSerif", Font.BOLD, ScalingUtil.scaleWidth(22));
        Font normalFont = new Font("SansSerif", Font.PLAIN, ScalingUtil.scaleWidth(14));
        Font circleFont = new Font("SansSerif", Font.BOLD, ScalingUtil.scaleWidth(16));
        Color textColor = FETA;
        int textShadowSize = ScalingUtil.scalePadding(8);

        // Draw the result number inside a circle
        int circleSize = ScalingUtil.scaleWidth(40);
        int circleX = padding;
        int circleY = padding;
        Ellipse2D.Double circle = new Ellipse2D.Double(circleX, circleY, circleSize, circleSize);
        g2.setColor(ACCENT_COLOR);
        g2.fill(circle);
        String resultNumberStr = String.valueOf(resultNumber);
        int textWidth = g2.getFontMetrics(circleFont).stringWidth(resultNumberStr);
        int textHeight = g2.getFontMetrics(circleFont).getHeight();
        g2.setColor(textColor);
        g2.setFont(circleFont);
        g2.drawString(resultNumberStr, circleX + (circleSize - textWidth) / 2, 
                circleY + circleSize / 2 + textHeight / 4);

        // Draw centered title
        g2.setFont(titleFont);
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        int titleX = (width - titleWidth) / 2;
        int titleY = padding + circleSize + ScalingUtil.scalePadding(25);
        DrawingUtils.drawTextWithShadow(g2, title, titleX, titleY, titleFont, textColor, shadowColor, textShadowSize);

        // Draw author and filename inside ellipses
        int ellipseHeight = ScalingUtil.scaleHeight(30);
        int ellipseWidth = ScalingUtil.scaleWidth(150);
        int ellipseSpacing = ScalingUtil.scaleWidth(20);
        int ellipseY = titleY + ScalingUtil.scalePadding(25);

        // Author ellipse and text
        int authorEllipseX = width / 2 - ellipseWidth - ellipseSpacing / 2;
        Shape authorEllipse = new RoundRectangle2D.Float(authorEllipseX, ellipseY, 
                ellipseWidth, ellipseHeight, ellipseHeight, ellipseHeight);
        g2.setColor(ACCENT_COLOR);
        g2.fill(authorEllipse);
        g2.setFont(normalFont);
        g2.setColor(textColor);
        int authorTextWidth = g2.getFontMetrics().stringWidth(author);
        int authorTextX = authorEllipseX + (ellipseWidth - authorTextWidth) / 2;
        int textVerticalCenter = ellipseY + ellipseHeight / 2 + g2.getFontMetrics().getHeight() / 4;
        g2.drawString(author, authorTextX, textVerticalCenter);

        // Filename ellipse and text
        int filenameEllipseX = width / 2 + ellipseSpacing / 2;
        Shape filenameEllipse = new RoundRectangle2D.Float(filenameEllipseX, ellipseY, 
                ellipseWidth, ellipseHeight, ellipseHeight, ellipseHeight);
        g2.setColor(ACCENT_COLOR);
        g2.fill(filenameEllipse);
        g2.setColor(textColor);
        String displayFilename = filename;
        if (displayFilename.length() > 15) {
            displayFilename = displayFilename.substring(0, 12) + "...";
        }
        int filenameTextWidth = g2.getFontMetrics().stringWidth(displayFilename);
        int filenameTextX = filenameEllipseX + (ellipseWidth - filenameTextWidth) / 2;
        g2.drawString(displayFilename, filenameTextX, textVerticalCenter);

        // Only show additional details when hovered/expanded
        if (showDetails) {
            int detailsY = ellipseY + ellipseHeight + ScalingUtil.scalePadding(25);
            DrawingUtils.drawTextWithShadow(g2, "Score: " + score, padding, detailsY, normalFont, textColor, shadowColor, textShadowSize);
            detailsY += normalFont.getSize() + padding;

            // Render best fragment text if provided
            if (bestFragment != null && !bestFragment.isEmpty()) {
                DrawingUtils.drawTextWithShadow(g2, "Best Fragment:", padding, detailsY, normalFont, textColor, shadowColor, textShadowSize);
                detailsY += normalFont.getSize() + ScalingUtil.scalePadding(5);

                String[] words = bestFragment.split("\\s+");
                StringBuilder line = new StringBuilder();
                int maxWidth = width - (padding * 2);
                for (String word : words) {
                    String testLine = line.toString() + (line.length() > 0 ? " " : "") + word;
                    int testWidth = g2.getFontMetrics(normalFont).stringWidth(testLine);
                    if (testWidth > maxWidth) {
                        DrawingUtils.drawTextWithShadow(g2, line.toString(), padding, detailsY, normalFont, textColor, shadowColor, textShadowSize);
                        detailsY += normalFont.getSize() + ScalingUtil.scalePadding(5);
                        line = new StringBuilder(word);
                    } else {
                        if (line.length() > 0) {
                            line.append(" ");
                        }
                        line.append(word);
                    }
                }
                if (line.length() > 0) {
                    DrawingUtils.drawTextWithShadow(g2, line.toString(), padding, detailsY, normalFont, textColor, shadowColor, textShadowSize);
                    detailsY += normalFont.getSize() + padding;
                }
            }

            // Render full explanation (if available), line by line
            if (fullExplanation != null && !fullExplanation.isEmpty()) {
                DrawingUtils.drawTextWithShadow(g2, "Full Explanation:", padding, detailsY, normalFont, textColor, shadowColor, textShadowSize);
                detailsY += normalFont.getSize() + ScalingUtil.scalePadding(5);
                String[] explanationLines = fullExplanation.split("\n");
                for (String line : explanationLines) {
                    DrawingUtils.drawTextWithShadow(g2, line, padding, detailsY, normalFont, textColor, shadowColor, textShadowSize);
                    detailsY += normalFont.getSize() + ScalingUtil.scalePadding(5);
                }
            }
        }
        g2.dispose();
    }
    /**
     * Override the paint component to handle background and scrolling
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (showDetails) {
            int minContentHeight = calculateMinimumContentHeight();
            if (contentPanel.getPreferredSize().height < minContentHeight) {
                contentPanel.setPreferredSize(new Dimension(originalSize.width, minContentHeight));
                contentPanel.revalidate();
            }
        }
    }
}
