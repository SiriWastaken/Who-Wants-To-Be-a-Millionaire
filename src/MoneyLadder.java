import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import javax.swing.JPanel;

/**
 * Displays the money ladder shown during gameplay.
 *
 * The panel is custom-drawn using Java2D and includes:
 * - Background gradient
 * - Decorative curves
 * - Prize ladder with perfectly centered active level highlights
 *
 * This class is purely responsible for drawing the interface and
 * does not contain any gameplay logic.
 *
 * @author Sri Ganty, some assistance with styling was done by Copilot,
 * specifically GPT 5.4-mini
 */
public class MoneyLadder extends JPanel {

    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 760;

    // Track the active money tier (Values from 1 to 16; 0 means no active level yet)
    private int currentLevel = 1;

    // Prize values used to map the live score onto the matching ladder row.
    private final int[] moneyValues = {
            100, 200, 300, 500, 1000, 2000, 4000, 8000,
            16000, 32000, 64000, 125000, 250000, 500000, 750000, 1000000
    };

    // Prize values displayed from lowest to highest, in GBP
    private final String[] amounts = {
            "£100", "£200", "£300", "£500", "£1,000", "£2,000", "£4,000", "£8,000",
            "£16,000", "£32,000", "£64,000", "£125,000", "£250,000", "£500,000", "£750,000", "£1 MILLION"
    };

    // Fonts used throughout the ladder to keep styling consistent
    private final Font TIER_FONT = new Font("Serif", Font.BOLD, 30);
    private final Font AMOUNT_FONT = new Font("Serif", Font.BOLD, 28);
    
    // Background color components
    private final Color BACKGROUND_TOP = new Color(6, 6, 16);
    private final Color BACKGROUND_BOTTOM = new Color(18, 12, 36);

    // Colour palette used for the background, decorations, and prize ladder.
    private final Color BLUE_GLOW = new Color(59, 130, 246, 28);
    private final Color LINE_BLUE = new Color(95, 164, 236, 75);
    private final Color LINE_GOLD = new Color(245, 179, 92, 86);
    private final Color LINE_GREY = new Color(170, 176, 196, 36);
    private final Color CREAM = new Color(246, 230, 194);
    private final Color ORANGE = new Color(255, 170, 36);

    // Active tier aesthetic highlighting assets
    private final Color GLOW_GOLD_INNER = new Color(245, 170, 36, 140);
    private final Color GLOW_GOLD_OUTER = new Color(255, 215, 0, 40);
    private final Color GLOW_GOLD_BORDER = new Color(255, 230, 150, 220);

    /**
     * Creates the money ladder panel and sets its preferred size.
     */
    public MoneyLadder() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setOpaque(true);
    }

    /**
     * Dynamically updates the active level highlighted on the board.
     *
     * @param level the current active level tier (1 to 16)
     */
    public void setCurrentLevel(int level) {
        this.currentLevel = Math.max(1, Math.min(level, amounts.length));
        repaint(); // Re-trigger paint pipeline to update visual position
    }

    /**
     * Updates the highlighted row to match the player's current money total.
     *
     * @param money the current score from the game session
     */
    public void setCurrentMoney(int money) {
        this.currentLevel = resolveLevelForMoney(money);
        repaint();
    }

    /**
     * Draws the complete money ladder component.
     *
     * @param g the Graphics object used for drawing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Paint the vertical dark background gradient
            GradientPaint background = new GradientPaint(0, 0, BACKGROUND_TOP, 0, getHeight(), BACKGROUND_BOTTOM);
            g2.setPaint(background);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw faint, translucent background glow shapes
            g2.setColor(BLUE_GLOW);
            g2.fillOval(-90, -20, 300, 300);
            g2.fillOval(110, 440, 280, 280);

            // Render UI sub-layers
            drawBackdropCurves(g2);
            drawLadderRows(g2);
        } finally {
            g2.dispose();
        }
    }

    /**
     * Draws the array of abstract intersecting background curves behind the ladder.
     * @param g2 the Graphics2D context used for drawing shapes
     */
    private void drawBackdropCurves(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        drawCurve(g2, 8, 685, 120, 410, 214, 250, 286, 110, LINE_GREY);
        drawCurve(g2, 32, 645, 120, 510, 200, 420, 300, 350, LINE_BLUE);
        drawCurve(g2, 44, 585, 145, 520, 185, 350, 305, 285, LINE_GOLD);
        drawCurve(g2, 18, 525, 122, 460, 190, 340, 312, 220, LINE_BLUE);
        drawCurve(g2, 0, 470, 116, 390, 180, 280, 304, 165, LINE_GREY);
        drawCurve(g2, 20, 410, 120, 330, 168, 235, 294, 120, LINE_GOLD);
        drawCurve(g2, 40, 350, 120, 285, 178, 205, 290, 90, LINE_BLUE);
        drawCurve(g2, 0, 305, 130, 235, 188, 165, 292, 50, LINE_GREY);
        drawCurve(g2, 22, 250, 132, 200, 182, 112, 296, 22, LINE_GOLD);
        drawCurve(g2, 70, 710, 120, 630, 180, 575, 300, 460, LINE_BLUE);
        drawCurve(g2, 160, 720, 210, 610, 256, 500, 316, 388, LINE_GREY);
        drawCurve(g2, 180, 690, 230, 560, 266, 430, 318, 318, LINE_BLUE);
        drawCurve(g2, 175, 620, 226, 490, 266, 382, 318, 270, LINE_GOLD);
    }

    /**
     * Helper method to render a single cubic Bezier curve.
     */
    private void drawCurve(Graphics2D g2, int x1, int y1, int ctrl1X, int ctrl1Y, int ctrl2X, int ctrl2Y, int x2,
            int y2, Color color) {
        g2.setColor(color);
        g2.draw(new CubicCurve2D.Double(x1, y1, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x2, y2));
    }

    /**
     * Loops through all available prize values and draws each step of the ladder.
     * Center-aligns the labels and layout plates cleanly.
     *
     * @param g2 the Graphics2D context used to render the list text rows
     */
    private void drawLadderRows(Graphics2D g2) {
        int startY = 165; // Positioned for 16-tier layouts
        int rowHeight = 35;
        int rowCount = amounts.length;
        int panelWidth = getWidth();

        FontMetrics tierMetrics = g2.getFontMetrics(TIER_FONT);
        FontMetrics amountMetrics = g2.getFontMetrics(AMOUNT_FONT);

        for (int i = rowCount - 1; i >= 0; i--) {
            int y = startY + (rowCount - 1 - i) * rowHeight;
            boolean isCurrent = (i + 1) == currentLevel;

            String tierLabel = String.valueOf(i + 1);
            String amountLabel = amounts[i];

            // Calculate total line width for precise side-by-side spacing center calculations
            int spacing = 20;
            int totalTextWidth = tierMetrics.stringWidth(tierLabel) + amountMetrics.stringWidth(amountLabel) + spacing;
            
            int startX = (panelWidth - totalTextWidth) / 2;
            int tierX = startX;
            int amountX = startX + tierMetrics.stringWidth(tierLabel) + spacing;

            // Render active level glow capsule BEFORE drawing text over it
            if (isCurrent) {
                int capsuleHeight = 30;
                int capsuleY = y - capsuleHeight + 4; // Shift vertically to center behind font text path
                int paddingX = 12;
                int capsuleX = paddingX;
                int capsuleWidth = panelWidth - (paddingX * 2);

                // 1. Draw broad ambient background glow
                g2.setColor(GLOW_GOLD_OUTER);
                g2.fillRoundRect(capsuleX - 3, capsuleY - 3, capsuleWidth + 6, capsuleHeight + 6, 14, 14);

                // 2. Draw centered core gradient container plate
                GradientPaint goldPlate = new GradientPaint(
                        capsuleX, capsuleY, GLOW_GOLD_INNER,
                        capsuleX + capsuleWidth, capsuleY, new Color(255, 140, 0, 95));
                g2.setPaint(goldPlate);
                g2.fillRoundRect(capsuleX, capsuleY, capsuleWidth, capsuleHeight, 10, 10);

                // 3. Draw sharp outer metallic frame border
                g2.setColor(GLOW_GOLD_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(capsuleX, capsuleY, capsuleWidth, capsuleHeight, 10, 10);
            }

            // Milestone formatting configuration
            boolean milestone = i == 15 || i == 9 || i == 4 || i == 0;

            // If active, keep text pure white contrast so it jumps off the gold surface
            Color rowColor = isCurrent ? Color.WHITE : (milestone ? CREAM : ORANGE);

            // Draw level index number
            g2.setFont(TIER_FONT);
            g2.setColor(rowColor);
            g2.drawString(tierLabel, tierX, y);

            // Draw money values string
            g2.setFont(AMOUNT_FONT);
            g2.drawString(amountLabel, amountX, y);
        }
    }

    /**
     * Finds the ladder row that matches the current money total.
     *
     * @param money the current score from the game session
     * @return the 1-based ladder row to highlight
     */
    private int resolveLevelForMoney(int money) {
        if (money <= 0) {
            return 1;
        }

        // Exact match lookup
        for (int i = 0; i < moneyValues.length; i++) {
            if (moneyValues[i] == money) {
                return i + 1;
            }
        }

        // Fallback closest bounds check
        for (int i = moneyValues.length - 1; i >= 0; i--) {
            if (money >= moneyValues[i]) {
                return i + 1;
            }
        }

        return 1;
    }
}