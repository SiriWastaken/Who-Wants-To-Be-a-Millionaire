import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
 * - Lifeline icons
 * - Prize ladder with a centered active level highlight glow
 *
 * This class is purely responsible for drawing the interface and
 * does not contain any gameplay logic.
 *
 * @author Sri Ganty, some assistance with styling was done by Copilot,
 *         specifically GPT 5.4-mini
 */
public class MoneyLadder extends JPanel {

    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 760;

    // Track the active game state level (Values from 1 to 15; 0 means no active
    // level yet)
    private int currentLevel = 1;

    // Fonts used throughout the ladder to keep styling consistent
    private final Font TIER_FONT = new Font("Serif", Font.BOLD, 34);
    private final Font AMOUNT_FONT = new Font("Serif", Font.BOLD, 31);
    private final Font BADGE_FONT = new Font("SansSerif", Font.BOLD, 27);

    // Background color components
    private final Color BACKGROUND_TOP = new Color(6, 6, 16);
    private final Color BACKGROUND_BOTTOM = new Color(18, 12, 36);

    // Colour palette used for the background, decorations, and prize ladder.
    private final Color BLUE_GLOW = new Color(59, 130, 246, 28);
    private final Color LINE_BLUE = new Color(95, 164, 236, 75);
    private final Color LINE_GOLD = new Color(245, 179, 92, 86);
    private final Color LINE_GREY = new Color(170, 176, 196, 36);
    private final Color BADGE_BLUE = new Color(57, 146, 224);
    private final Color CREAM = new Color(246, 230, 194);
    private final Color ORANGE = new Color(255, 170, 36);

    // Active tier aesthetic highlighting assets
    private final Color GLOW_GOLD_INNER = new Color(245, 170, 36, 140);
    private final Color GLOW_GOLD_OUTER = new Color(255, 215, 0, 40);
    private final Color GLOW_GOLD_BORDER = new Color(255, 230, 150, 220);

    // Prize values displayed from lowest to highest, in GBP
    private final String[] amounts = {
            "£100",
            "£200",
            "£300",
            "£500",
            "£1,000",
            "£2,000",
            "£4,000",
            "£8,000",
            "£16,000",
            "£32,000",
            "£64,000",
            "£125,000",
            "£250,000",
            "£500,000",
            "£1 MILLION"
    };

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
     * @param level the current active level tier (1 to 15)
     */
    public void setCurrentLevel(int level) {
        this.currentLevel = level;
        repaint(); // Re-trigger paint pipeline to update visual position
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
            drawLifelineBadges(g2);
            drawLadderRows(g2);
        } finally {
            g2.dispose();
        }
    }

    /**
     * Draws the array of abstract intersecting background curves behind the ladder.
     * * @param g2 the Graphics2D context used for drawing shapes
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
     * Calculates layout grid tracking positions and draws the three top lifeline
     * badges.
     *
     * @param g2 the Graphics2D context used for drawing the icons
     */
    private void drawLifelineBadges(Graphics2D g2) {
        int badgeY = 36;
        int badgeWidth = 98;
        int badgeHeight = 64;
        int gap = 14;
        int startX = 18;

        drawBadgeOutline(g2, startX, badgeY, badgeWidth, badgeHeight);

        int phoneX = startX + badgeWidth + gap;
        drawBadgeOutline(g2, phoneX, badgeY, badgeWidth, badgeHeight);

        int audienceX = phoneX + badgeWidth + gap;
        drawBadgeOutline(g2, audienceX, badgeY, badgeWidth, badgeHeight);
    }

    private void drawBadgeOutline(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(BADGE_BLUE);
        g2.setStroke(new BasicStroke(5.0f));
        g2.drawOval(x, y, width, height);
    }

    /**
     * Loops through all available prize values and draws each step of the ladder.
     * Automatically overlays a custom gold glow plate on the currently active index
     * tier.
     *
     * @param g2 the Graphics2D context used to render the list text rows
     */
    private void drawLadderRows(Graphics2D g2) {
        int startY = 212;
        int rowHeight = 38;
        int leftX = 20;
        int amountX = 92;
        int rowCount = amounts.length;

        for (int i = rowCount - 1; i >= 0; i--) {
            int y = startY + (rowCount - 1 - i) * rowHeight;
            boolean isCurrent = (i + 1) == currentLevel;

            // Render active level glow capsule BEFORE drawing text over it
            if (isCurrent) {
                int capsuleHeight = 32;
                int capsuleY = y - capsuleHeight + 6; // Center capsule over the baseline
                int paddingX = 8;
                int capsuleX = paddingX;
                int capsuleWidth = getWidth() - (paddingX * 2);

                // 1. Draw broad ambient background glow
                g2.setColor(GLOW_GOLD_OUTER);
                g2.fillRoundRect(capsuleX - 4, capsuleY - 4, capsuleWidth + 8, capsuleHeight + 8, 16, 16);

                // 2. Draw centered core gradient container plate
                GradientPaint goldPlate = new GradientPaint(
                        capsuleX, capsuleY, GLOW_GOLD_INNER,
                        capsuleX + capsuleWidth, capsuleY, new Color(255, 140, 0, 80));
                g2.setPaint(goldPlate);
                g2.fillRoundRect(capsuleX, capsuleY, capsuleWidth, capsuleHeight, 12, 12);

                // 3. Draw sharp outer metallic frame border
                g2.setColor(GLOW_GOLD_BORDER);
                g2.setStroke(new BasicStroke(1.75f));
                g2.drawRoundRect(capsuleX, capsuleY, capsuleWidth, capsuleHeight, 12, 12);
            }

            // Milestone formatting configuration
            boolean milestone = i == 14 || i == 9 || i == 4 || i == 0;

            // If active, keep text pure dark/white contrast so it jumps off the gold
            // surface
            Color rowColor;
            if (isCurrent) {
                rowColor = Color.WHITE;
            } else {
                rowColor = milestone ? CREAM : ORANGE;
            }

            // Draw level index number
            g2.setFont(TIER_FONT);
            g2.setColor(rowColor);
            g2.drawString(String.format("%2d", i + 1).trim(), leftX, y);

            // Draw money values string
            g2.setFont(AMOUNT_FONT);
            g2.drawString(amounts[i], amountX, y);
        }
    }
}