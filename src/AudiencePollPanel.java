import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Displays the Audience Poll lifeline with animated percentage bars.
 * Matches the game's dark theme and color scheme.
 */
public class AudiencePollPanel extends JPanel {

    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 26);
    private final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 20);
    private final Font PERCENT_FONT = new Font("SansSerif", Font.BOLD, 18);
    private final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 12);

    // Match your game's colors
    private final Color BACKGROUND_TOP = new Color(10, 8, 28);
    private final Color BACKGROUND_BOTTOM = new Color(3, 2, 10);
    private final Color GLOW_COLOR = new Color(99, 102, 241, 15);
    private final Color CARD_BG = new Color(17, 24, 39, 230);
    private final Color CARD_BORDER = new Color(55, 65, 81, 200);
    private final Color BAR_BG = new Color(30, 30, 50, 180);

    // Match your game's accent colors
    private final Color ACCENT_GOLD = new Color(245, 158, 11);
    private final Color SUBTITLE_COLOR = new Color(156, 163, 175);

    // Answer choice colors (matching your game's answer buttons)
    private final Color[] BAR_COLORS = {
        new Color(99, 102, 241), // A - Blue (matches studio blue)
        new Color(34, 197, 94),  // B - Green (success)
        new Color(245, 158, 11), // C - Gold (accent)
        new Color(239, 68, 68)   // D - Red (failure)
    };

    private final Color[] BAR_GLOWS = {
        new Color(99, 102, 241, 40),
        new Color(34, 197, 94, 40),
        new Color(245, 158, 11, 40),
        new Color(239, 68, 68, 40)
    };

    private final String[] LABELS = {"A", "B", "C", "D"};
    private final int[] percentages;
    private final int[] targetPercentages;
    private final double[] animationProgress;
    private final boolean[] isComplete;
    private final Timer animationTimer;
    private final int correctIndex;
    private boolean allComplete = false;

    /**
     * Creates a new Audience Poll panel with the given question.
     *
     * @param question the current question to poll about
     */
    public AudiencePollPanel(Question question) {
        setOpaque(false);
        setFocusable(true);

        targetPercentages = calculatePercentages(question);
        percentages = new int[4];
        animationProgress = new double[4];
        isComplete = new boolean[4];

        for (int i = 0; i < 4; i++) {
            percentages[i] = 0;
            animationProgress[i] = 0;
            isComplete[i] = false;
        }

        correctIndex = question.getCorrectAnswer().trim().toUpperCase().charAt(0) - 'A';

        animationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (allComplete) {
                    animationTimer.stop();
                    return;
                }

                for (int i = 0; i < 4; i++) {
                    if (!isComplete[i]) {
                        animationProgress[i] += 0.05;
                        if (animationProgress[i] >= 1.0) {
                            animationProgress[i] = 1.0;
                            isComplete[i] = true;
                            percentages[i] = targetPercentages[i];
                        } else {
                            double eased = 1 - Math.pow(1 - animationProgress[i], 3);
                            percentages[i] = (int) (targetPercentages[i] * eased);
                        }
                    }
                }

                allComplete = true;
                for (int i = 0; i < 4; i++) {
                    if (!isComplete[i]) {
                        allComplete = false;
                        break;
                    }
                }

                repaint();
            }
        });

        animationTimer.start();
    }

    /**
     * Calculates the poll percentages for the question.
     *
     * @param question the question to poll
     * @return an array of percentages for A, B, C, D
     */
    private int[] calculatePercentages(Question question) {
        int[] result = new int[4];
        int correctIdx = question.getCorrectAnswer().trim().toUpperCase().charAt(0) - 'A';

        int correctPercent = 40 + (int)(Math.random() * 15);
        result[correctIdx] = correctPercent;

        int remaining = 100 - correctPercent;
        for (int i = 0; i < 4; i++) {
            if (i != correctIdx) {
                if (remaining > 0) {
                    int share = (int)(remaining * (0.2 + Math.random() * 0.3));
                    result[i] = Math.min(share, remaining);
                    remaining -= result[i];
                }
            }
        }

        if (remaining > 0) {
            result[correctIdx] += remaining;
        }

        for (int i = 0; i < 4; i++) {
            result[i] = Math.max(0, Math.min(100, result[i]));
        }

        int total = 0;
        for (int i = 0; i < 4; i++) {
            total += result[i];
        }
        if (total != 100 && total > 0) {
            int diff = 100 - total;
            result[correctIdx] += diff;
        }

        return result;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background gradient matching your game
        GradientPaint background = new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM);
        g2.setPaint(background);
        g2.fillRoundRect(0, 0, w, h, 24, 24);

        // Background glow
        g2.setColor(GLOW_COLOR);
        g2.fillOval(w/2 - 150, -50, 300, 300);
        g2.fillOval(w/2 - 200, h - 150, 400, 400);

        // Card border
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(0, 0, w, h, 24, 24);

        // Title with gold accent
        g2.setFont(TITLE_FONT);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String title = "Audience Poll";
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, 50);

        // Gold underline
        g2.setColor(ACCENT_GOLD);
        g2.fillRoundRect(w/2 - 60, 58, 120, 3, 3, 3);

        // Draw bars
        int barWidth = w - 80;
        int barHeight = 34;
        int startX = 40;
        int startY = 90;
        int gap = 16;

        for (int i = 0; i < 4; i++) {
            int y = startY + i * (barHeight + gap);

            // Label with gold accent
            g2.setFont(LABEL_FONT);
            g2.setColor(Color.WHITE);
            String labelText = LABELS[i] + ".";
            g2.drawString(labelText, 15, y + barHeight - 8);

            // Background bar (dark, matching CARD_BG)
            g2.setColor(BAR_BG);
            g2.fillRoundRect(startX, y, barWidth, barHeight, 12, 12);

            // Fill bar
            int fillWidth = (int)(barWidth * (percentages[i] / 100.0));
            if (fillWidth > 0) {
                // Glow effect
                g2.setColor(BAR_GLOWS[i]);
                g2.fillRoundRect(startX - 2, y - 2, fillWidth + 4, barHeight + 4, 14, 14);

                // Main bar with gradient
                GradientPaint barGradient = new GradientPaint(
                    startX, y, BAR_COLORS[i],
                    startX + fillWidth, y, BAR_COLORS[i].brighter()
                );
                g2.setPaint(barGradient);
                g2.fillRoundRect(startX, y, fillWidth, barHeight, 12, 12);

                // Highlight the correct answer with a subtle glow
                if (i == correctIndex && allComplete) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(startX, y, fillWidth, barHeight / 2, 12, 12);
                }
            }

            // Percentage text
            g2.setFont(PERCENT_FONT);
            String percentText = percentages[i] + "%";
            FontMetrics pf = g2.getFontMetrics();
            int textX = startX + barWidth - pf.stringWidth(percentText) - 12;
            int textY = y + barHeight - 8;

            if (percentages[i] > 50) {
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(SUBTITLE_COLOR);
            }
            g2.drawString(percentText, textX, textY);
        }

        // Subtitle
        g2.setFont(SUBTITLE_FONT);
        g2.setColor(SUBTITLE_COLOR);
        String subtitle = "Based on audience responses";
        fm = g2.getFontMetrics();
        g2.drawString(subtitle, (w - fm.stringWidth(subtitle)) / 2, h - 25);

        // Close hint
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(107, 114, 128));
        String hint = "Press ESC or click anywhere to close";
        fm = g2.getFontMetrics();
        g2.drawString(hint, (w - fm.stringWidth(hint)) / 2, h - 8);
    }

    /**
     * Stops the animation and cleans up resources.
     */
    public void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }
}