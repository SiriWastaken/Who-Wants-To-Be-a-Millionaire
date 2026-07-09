import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

/** Draws the Lock In gameplay screen using the current session and panel state.
 * @author Sri Ganty, with refactoring help from Copilot (GPT 5.4 Mini)
 */
public class GameScreenRenderer {

    private final Font titleFont = new Font("SansSerif", Font.BOLD, 40);
    private final Font subtitleFont = new Font("SansSerif", Font.PLAIN, 15);
    private final Font bodyFont = new Font("SansSerif", Font.PLAIN, 22);
    private final Font bodySmallFont = new Font("SansSerif", Font.PLAIN, 18);
    private final Font buttonFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font statFont = new Font("SansSerif", Font.BOLD, 19);
    private final Font statSmallFont = new Font("SansSerif", Font.PLAIN, 14);

    private final Color backgroundTop = new Color(10, 8, 28);
    private final Color backgroundBottom = new Color(3, 2, 10);
    private final Color glowColor = new Color(99, 102, 241, 25);
    private final Color accentGlow = new Color(245, 158, 11, 20);
    private final Color titlePrimary = Color.WHITE;
    private final Color titleAccent = new Color(245, 158, 11); // Orange
    private final Color titleShadow = new Color(168, 85, 247, 45);
    private final Color subtitleColor = new Color(156, 163, 175);
    private final Color cardBg = new Color(17, 24, 39, 175);
    private final Color cardBorder = new Color(55, 65, 81, 110);
    private final Color buttonBg = new Color(17, 24, 39, 170);
    private final Color buttonBgHover = new Color(30, 27, 75, 210);
    private final Color buttonBorder = new Color(55, 65, 81, 130);
    private final Color buttonBorderHover = new Color(245, 158, 11);
    private final Color buttonText = new Color(229, 231, 235);
    private final Color disabledText = new Color(107, 114, 128);
    private final Color success = new Color(34, 197, 94); // Green
    private final Color failure = new Color(239, 68, 68); // Red

    /** Paints the full gameplay screen. */
    public void paint(Graphics2D g2, GameScreenPanel panel) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(0, 0, backgroundTop, 0, panel.getHeight(), backgroundBottom);
        g2.setPaint(background);
        g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        Point2D center = new Point2D.Float(panel.getWidth() / 2.0f, panel.getHeight() / 2.0f);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {glowColor, new Color(0, 0, 0, 0)};
        g2.setPaint(new java.awt.RadialGradientPaint(center, 560f, dist, colors));
        g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        g2.setStroke(new BasicStroke(1f));
        g2.setColor(accentGlow);
        g2.drawOval(-170, -170, 540, 540);
        g2.drawOval(panel.getWidth() - 390, panel.getHeight() - 440, 620, 620);

        drawBackButton(g2, panel);
        drawHeader(g2, panel);
        drawQuestionCard(g2, panel);
        drawAnswerButtons(g2, panel);
        drawLifelineButtons(g2, panel);
        drawFooter(g2, panel);
    }

    private void drawBackButton(Graphics2D g2, GameScreenPanel panel) {
        Rectangle backButton = panel.getBackButtonBounds();
        boolean hover = panel.isHoveringBack();
        g2.setColor(hover ? buttonBgHover : buttonBg);
        g2.fillRoundRect(backButton.x, backButton.y, backButton.width, backButton.height, 16, 16);
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(hover ? buttonBorderHover : buttonBorder);
        g2.drawRoundRect(backButton.x, backButton.y, backButton.width, backButton.height, 16, 16);

        g2.setFont(buttonFont);
        g2.setColor(hover ? titlePrimary : buttonText);
        drawCenteredText(g2, "MENU", backButton, 1);
    }

    private void drawHeader(Graphics2D g2, GameScreenPanel panel) {
        g2.setFont(titleFont);
        g2.setColor(titleShadow);
        drawCentered(g2, "LOCK IN", 70, panel);
        g2.setColor(titlePrimary);
        drawCentered(g2, "LOCK IN", 68, panel);

        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleWidth = titleMetrics.stringWidth("LOCK IN");
        int titleX = (panel.getWidth() - titleWidth) / 2;
        g2.setColor(titleAccent);
        g2.fillRoundRect(titleX + 34, 80, titleWidth - 68, 4, 4, 4);

        drawStatCard(g2, 90, 140, 250, 68, "QUESTION", panel.getSession().getCurrentQuestionNumber() + " / " + panel.getSession().getTotalQuestions());
        drawTimerWidget(g2, panel, 438, 118, 214, 116);
        drawStatCard(g2, 760, 140, 250, 68, "MONEY", "$" + String.format("%,d", panel.getSession().getScore()));
    }

    private void drawStatCard(Graphics2D g2, int x, int y, int width, int height, String label, String value) {
        g2.setColor(cardBg);
        g2.fillRoundRect(x, y, width, height, 18, 18);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(cardBorder);
        g2.drawRoundRect(x, y, width, height, 18, 18);

        g2.setFont(statSmallFont);
        g2.setColor(subtitleColor);
        g2.drawString(label, x + 18, y + 24);

        g2.setFont(statFont);
        g2.setColor(titlePrimary);
        g2.drawString(value, x + 18, y + 49);
    }

    private void drawTimerWidget(Graphics2D g2, GameScreenPanel panel, int x, int y, int width, int height) {
        int diameter = Math.min(width, height);
        int circleX = x + (width - diameter) / 2;
        int circleY = y + (height - diameter) / 2;
        int padding = 10;
        int ringSize = diameter - padding * 2;
        int ringX = circleX + padding;
        int ringY = circleY + padding;

        g2.setColor(cardBg);
        g2.fillOval(circleX, circleY, diameter, diameter);
        g2.setColor(cardBorder);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawOval(circleX, circleY, diameter, diameter);

        int timeLimit = panel.getSession().getCurrentQuestionTimeLimit();
        int timeRemaining = panel.getSession().getTimeRemaining();
        float fraction = timeLimit <= 0 ? 0.0f : Math.max(0.0f, Math.min(1.0f, timeRemaining / (float) timeLimit));
        int extent = Math.round(360f * fraction);
        
        Color timerColor = timerColorForFraction(fraction);

        g2.setColor(new Color(255, 255, 255, 24));
        g2.fillOval(ringX, ringY, ringSize, ringSize);

        // Ring thickness remaining upgraded at 14f
        g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 28));
        g2.drawArc(ringX, ringY, ringSize, ringSize, 90, -360);
        
        g2.setColor(timerColor);
        g2.drawArc(ringX, ringY, ringSize, ringSize, 90, -extent);

        g2.setFont(statSmallFont);
        g2.setColor(subtitleColor);
        drawCenteredText(g2, "TIME", new Rectangle(circleX, circleY, diameter, diameter), -28);

        g2.setFont(statFont);
        g2.setColor(titlePrimary);
        drawCenteredText(g2, timeRemaining + "s", new Rectangle(circleX, circleY, diameter, diameter), 4);
    }

    private Color timerColorForFraction(float fraction) {
        if (fraction > 0.60f) return success;
        if (fraction > 0.25f) return titleAccent;
        return failure;
    }

    private void drawQuestionCard(Graphics2D g2, GameScreenPanel panel) {
        // Reverted spacing back to original y coordinate (236)
        Rectangle card = new Rectangle(80, 236, 940, 132);
        g2.setColor(cardBg);
        g2.fillRoundRect(card.x, card.y, card.width, card.height, 26, 26);
        g2.setStroke(new BasicStroke(1.3f));
        g2.setColor(cardBorder);
        g2.drawRoundRect(card.x, card.y, card.width, card.height, 26, 26);

        Question question = panel.getSession().getCurrentQuestion();
        if (question == null) return;

        g2.setFont(statSmallFont);
        g2.setColor(titleAccent);
        g2.drawString(question.getCategory().toUpperCase(), card.x + 24, card.y + 28);

        g2.setFont(bodyFont);
        g2.setColor(titlePrimary);
        drawWrappedText(g2, question.getQuestion(), card.x + 24, card.y + 58, card.width - 48, 28);

        String statusMessage = panel.getSession().getStatusMessage();
        if (statusMessage != null && !statusMessage.isEmpty()) {
            g2.setFont(bodySmallFont);
            g2.setColor(colorForStatus(panel.getSession().getStatusType()));
            g2.drawString(statusMessage, card.x + 24, card.y + 112);
        }
    }

    private void drawAnswerButtons(Graphics2D g2, GameScreenPanel panel) {
        Question question = panel.getSession().getCurrentQuestion();
        if (question == null) return;

        String[] labels = {"A", "B", "C", "D"};
        String[] answers = {
                question.getAnswerA(),
                question.getAnswerB(),
                question.getAnswerC(),
                question.getAnswerD()
        };

        int correctIndex = question.getCorrectAnswer().toUpperCase().trim().charAt(0) - 'A';

        for (int i = 0; i < panel.getAnswerBounds().length; i++) {
            Rectangle rect = panel.getAnswerBounds()[i];
            boolean hover = panel.getHoveredAnswerIndex() == i && !panel.isAnswerLocked(i) && !panel.getSession().isFinished();
            boolean disabled = panel.isAnswerLocked(i);

            boolean animationRunning = panel.isAnswerAnimationRunning();
            boolean isSelected = (panel.getSelectedAnswer() == i);
            boolean flashState = panel.getFlashState();
            boolean wasCorrect = panel.wasLastAnswerCorrect();

            Color currentBg = disabled ? new Color(17, 24, 39, 110) : (hover ? buttonBgHover : buttonBg);
            Color currentBorder = disabled ? new Color(55, 65, 81, 80) : (hover ? buttonBorderHover : buttonBorder);
            Color currentLabelColor = disabled ? disabledText : (hover ? titlePrimary : buttonText);

            if (isSelected || (animationRunning && i == correctIndex)) {
                // If an animation is not currently running (the first 2 seconds), keep it steady orange.
                if (!animationRunning) {
                    if (isSelected) {
                        currentBg = new Color(245, 158, 11, 40);
                        currentBorder = titleAccent;
                        currentLabelColor = titleAccent;
                    }
                } else {
                    // Animation is running (the next 2 seconds) -> Start flashing sequence
                    if (wasCorrect) {
                        if (flashState) {
                            currentBg = new Color(34, 197, 94, 50); // Green pulse
                            currentBorder = success;
                            currentLabelColor = success;
                        } else {
                            currentBg = new Color(245, 158, 11, 40); // Orange pulse
                            currentBorder = titleAccent;
                            currentLabelColor = titleAccent;
                        }
                    } else {
                        if (isSelected) {
                            if (flashState) {
                                currentBg = new Color(239, 68, 68, 50); // Red pulse
                                currentBorder = failure;
                                currentLabelColor = failure;
                            } else {
                                currentBg = new Color(245, 158, 11, 40); // Orange pulse
                                currentBorder = titleAccent;
                                currentLabelColor = titleAccent;
                            }
                        }
                        if (i == correctIndex) {
                            if (flashState) {
                                currentBg = new Color(34, 197, 94, 50); // Green pulse reveal
                                currentBorder = success;
                                currentLabelColor = success;
                            } else {
                                currentBg = new Color(17, 24, 39, 170);
                                currentBorder = buttonBorder;
                            }
                        }
                    }
                }
            }

            g2.setColor(currentBg);
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);
            g2.setStroke(new BasicStroke(1.4f));
            g2.setColor(currentBorder);
            g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);

            g2.setFont(buttonFont);
            g2.setColor(currentLabelColor);
            g2.drawString(labels[i], rect.x + 18, rect.y + 31);

            g2.setFont(bodySmallFont);
            drawWrappedText(g2, answers[i], rect.x + 58, rect.y + 28, rect.width - 74, 20, disabled ? disabledText : buttonText);
        }
    }

    private void drawLifelineButtons(Graphics2D g2, GameScreenPanel panel) {
        boolean[] used = {
                panel.getSession().isSwapUsed(),
                panel.getSession().isAudiencePollUsed(),
                panel.getSession().isFiftyFiftyUsed(),
                panel.getSession().isPhoneAFriendUsed()
        };
        String[] labels = {"SWAP", "AUDIENCE", "25/75", "PHONE"};

        for (int i = 0; i < panel.getLifelineBounds().length; i++) {
            Rectangle rect = panel.getLifelineBounds()[i];
            boolean hover = panel.getHoveredLifelineIndex() == i && !used[i] && !panel.getSession().isFinished();
            boolean disabled = used[i];

            g2.setColor(disabled ? new Color(17, 24, 39, 110) : (hover ? buttonBgHover : buttonBg));
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 16, 16);
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(disabled ? new Color(55, 65, 81, 80) : (hover ? buttonBorderHover : buttonBorder));
            g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 16, 16);

            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(disabled ? disabledText : (hover ? titlePrimary : titleAccent));
            drawCenteredText(g2, labels[i], rect, -2);
        }
    }

    private void drawFooter(Graphics2D g2, GameScreenPanel panel) {
        g2.setFont(subtitleFont);
        g2.setColor(subtitleColor);
        // Reverted footer Y position to match original layout
        drawCentered(g2, "Use the mouse to answer, activate lifelines, or return to the menu.", 708, panel);
    }

    private Color colorForStatus(GameSession.StatusType statusType) {
        if (statusType == GameSession.StatusType.SUCCESS) return success;
        if (statusType == GameSession.StatusType.FAILURE) return failure;
        if (statusType == GameSession.StatusType.COMPLETE) return titleAccent;
        return subtitleColor;
    }

    private void drawCentered(Graphics2D g2, String text, int y, GameScreenPanel panel) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (panel.getWidth() - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    private void drawCenteredText(Graphics2D g2, String text, Rectangle rect, int verticalOffset) {
        FontMetrics fm = g2.getFontMetrics();
        int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent() + verticalOffset;
        g2.drawString(text, x, y);
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int width, int lineHeight) {
        drawWrappedText(g2, text, x, y, width, lineHeight, titlePrimary);
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int width, int lineHeight, Color color) {
        g2.setColor(color);
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int currentY = y;
        FontMetrics fm = g2.getFontMetrics();

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) > width && line.length() > 0) {
                g2.drawString(line.toString(), x, currentY);
                currentY += lineHeight;
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) {
                    line.append(' ');
                }
                line.append(word);
            }
        }

        if (line.length() > 0) {
            g2.drawString(line.toString(), x, currentY);
        }
    }
}