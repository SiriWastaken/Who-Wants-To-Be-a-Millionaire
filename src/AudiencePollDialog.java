import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * A dialog that displays an animated bar graph showing audience poll results.
 * The audience is correct approximately 60% of the time.
 */
public class AudiencePollDialog extends JDialog {

    private static final int BAR_WIDTH = 80;
    private static final int BAR_MAX_HEIGHT = 250;
    private static final int ANIMATION_DURATION = 1500; // milliseconds
    private static final int FRAME_DELAY = 30; // milliseconds between frames

    private final BarChartPanel barChartPanel;
    private final Timer animationTimer;
    private final List<Double> targetPercentages;
    private final List<Double> currentPercentages;
    private long animationStartTime;

    /**
     * Creates the audience poll dialog with animated bar graph.
     *
     * @param parent the parent window
     * @param question the current question
     */
    public AudiencePollDialog(JDialog parent, Question question) {
        super(parent, "Audience Poll", true);
        
        // Generate percentages with 60% chance of being correct
        targetPercentages = generatePercentages(question.getCorrectAnswer());
        currentPercentages = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            currentPercentages.add(0.0);
        }

        setLayout(new java.awt.BorderLayout());
        setBackground(new Color(10, 8, 28));

        // Title
        JLabel titleLabel = new JLabel("AUDIENCE POLL RESULTS", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(245, 158, 11));
        titleLabel.setBorder(new EmptyBorder(15, 0, 10, 0));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        // Bar chart panel
        barChartPanel = new BarChartPanel();
        barChartPanel.setPreferredSize(new Dimension(420, 320));
        add(barChartPanel, java.awt.BorderLayout.CENTER);

        // Labels panel
        JPanel labelPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        labelPanel.setBackground(new Color(10, 8, 28));
        String[] labels = {"A", "B", "C", "D"};
        for (String label : labels) {
            JLabel l = new JLabel(label, JLabel.CENTER);
            l.setFont(new Font("SansSerif", Font.BOLD, 18));
            l.setForeground(new Color(245, 158, 11));
            labelPanel.add(l);
        }
        add(labelPanel, java.awt.BorderLayout.SOUTH);

        // Animation timer
        animationTimer = new Timer(FRAME_DELAY, this::animateBars);
        animationStartTime = System.currentTimeMillis();

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * Generates audience poll percentages with ~60% chance of being correct.
     */
    private List<Double> generatePercentages(String correctAnswer) {
        List<Double> percentages = new ArrayList<>();
        Random random = new Random();
        String correct = correctAnswer.trim().toUpperCase();
        int correctIndex = correct.charAt(0) - 'A';

        // 60% chance the audience picks the correct answer as highest
        boolean audienceCorrect = random.nextDouble() < 0.60;

        if (audienceCorrect) {
            // Correct answer gets 40-65%, others split the rest
            int correctPercent = 40 + random.nextInt(26);
            int remaining = 100 - correctPercent;
            
            // Distribute remaining among other answers
            int[] others = distributeRemaining(remaining, 3, random);
            for (int i = 0; i < 4; i++) {
                if (i == correctIndex) {
                    percentages.add((double) correctPercent);
                } else {
                    percentages.add((double) others[i < correctIndex ? i : i - 1]);
                }
            }
        } else {
            // A wrong answer gets highest, correct answer gets lower
            int wrongIndex;
            do {
                wrongIndex = random.nextInt(4);
            } while (wrongIndex == correctIndex);

            int wrongPercent = 35 + random.nextInt(25);
            int correctPercent = 15 + random.nextInt(20);
            int remaining = 100 - wrongPercent - correctPercent;
            int[] others = distributeRemaining(remaining, 2, random);

            int otherIdx = 0;
            for (int i = 0; i < 4; i++) {
                if (i == wrongIndex) {
                    percentages.add((double) wrongPercent);
                } else if (i == correctIndex) {
                    percentages.add((double) correctPercent);
                } else {
                    percentages.add((double) others[otherIdx++]);
                }
            }
        }

        return percentages;
    }

    /**
     * Distributes a remaining percentage among n answers.
     */
    private int[] distributeRemaining(int total, int count, Random random) {
        int[] result = new int[count];
        int remaining = total;
        
        for (int i = 0; i < count - 1; i++) {
            int max = remaining - (count - i - 1) * 5; // Ensure at least 5% each
            result[i] = 5 + random.nextInt(Math.max(1, max - 4));
            remaining -= result[i];
        }
        result[count - 1] = remaining;
        
        return result;
    }

    /**
     * Animates the bars from 0% to target percentages.
     */
    private void animateBars(ActionEvent e) {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        double progress = Math.min(1.0, (double) elapsed / ANIMATION_DURATION);
        
        // Ease out cubic
        double eased = 1 - Math.pow(1 - progress, 3);

        for (int i = 0; i < 4; i++) {
            currentPercentages.set(i, targetPercentages.get(i) * eased);
        }

        barChartPanel.repaint();

        if (progress >= 1.0) {
            animationTimer.stop();
        }
    }

    /**
     * Shows the dialog and starts the animation.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            animationStartTime = System.currentTimeMillis();
            for (int i = 0; i < 4; i++) {
                currentPercentages.set(i, 0.0);
            }
            animationTimer.start();
        } else {
            animationTimer.stop();
        }
        super.setVisible(visible);
    }

    /**
     * Custom panel that draws the animated bar chart.
     */
    private class BarChartPanel extends JPanel {

        private static final Color BAR_COLOR = new Color(245, 158, 11);
        private static final Color BAR_GRADIENT_END = new Color(255, 140, 0);
        private static final Color BACKGROUND = new Color(17, 24, 39);
        private static final Color GRID_LINE = new Color(55, 65, 81);

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background
            g2.setColor(BACKGROUND);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Grid lines
            g2.setColor(GRID_LINE);
            g2.setStroke(new BasicStroke(1f));
            for (int i = 1; i <= 4; i++) {
                int y = 20 + (int) (i * (BAR_MAX_HEIGHT + 20) / 5.0);
                g2.drawLine(30, y, getWidth() - 30, y);
                
                // Percentage labels
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.setColor(new Color(156, 163, 175));
                g2.drawString(i * 25 + "%", 5, y + 4);
            }

            // Draw bars
            int barSpacing = 20;
            int totalWidth = 4 * BAR_WIDTH + 3 * barSpacing;
            int startX = (getWidth() - totalWidth) / 2;
            String[] labels = {"A", "B", "C", "D"};

            for (int i = 0; i < 4; i++) {
                double percent = currentPercentages.get(i);
                int barHeight = (int) (percent / 100.0 * BAR_MAX_HEIGHT);
                int x = startX + i * (BAR_WIDTH + barSpacing);
                int y = 20 + BAR_MAX_HEIGHT - barHeight;

                // Bar gradient
                java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                        x, y, BAR_COLOR,
                        x, y + barHeight, BAR_GRADIENT_END);
                g2.setPaint(gradient);
                g2.fillRoundRect(x, y, BAR_WIDTH, barHeight, 8, 8);

                // Bar border
                g2.setColor(new Color(255, 215, 0));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, BAR_WIDTH, barHeight, 8, 8);

                // Percentage on top of bar
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.setColor(Color.WHITE);
                String percentText = String.format("%.0f%%", percent);
                int textWidth = g2.getFontMetrics().stringWidth(percentText);
                g2.drawString(percentText, x + (BAR_WIDTH - textWidth) / 2, y - 5);
            }

            g2.dispose();
        }
    }
}