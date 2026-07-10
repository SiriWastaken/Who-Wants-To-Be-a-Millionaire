import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

/**
 * Draws the Final Answer? gameplay screen using the current session and panel
 * state.
 */
public class GameScreenRenderer {

    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 40);
    private final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 15);
    private final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 22);
    private final Font BODY_SMALL_FONT = new Font("SansSerif", Font.PLAIN, 18);
    private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 18);
    private final Font STAT_FONT = new Font("SansSerif", Font.BOLD, 19);
    private final Font STAT_SMALL_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private final Font LIFELINE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private final Font SUGGESTION_FONT = new Font("SansSerif", Font.ITALIC, 14);

    private final Color BACKGROUND_TOP = new Color(10, 8, 28);
    private final Color BACKGROUND_BOTTOM = new Color(3, 2, 10);
    private final Color GLOW_COLOR = new Color(99, 102, 241, 25);
    private final Color ACCENT_GLOW = new Color(245, 158, 11, 20);
    private final Color TITLE_PRIMARY = Color.WHITE;
    private final Color TITLE_ACCENT = new Color(245, 158, 11);
    private final Color TITLE_SHADOW = new Color(168, 85, 247, 45);
    private final Color SUBTITLE_COLOR = new Color(156, 163, 175);
    private final Color CARD_BG = new Color(17, 24, 39, 175);
    private final Color CARD_BORDER = new Color(55, 65, 81, 110);
    private final Color BUTTON_BG = new Color(17, 24, 39, 170);
    private final Color BUTTON_BG_HOVER = new Color(30, 27, 75, 210);
    private final Color BUTTON_BORDER = new Color(55, 65, 81, 130);
    private final Color BUTTON_BORDER_HOVER = new Color(245, 158, 11);
    private final Color BUTTON_TEXT = new Color(229, 231, 235);
    private final Color DISABLED_TEXT = new Color(107, 114, 128);
    private final Color SUCCESS = new Color(34, 197, 94);
    private final Color FAILURE = new Color(239, 68, 68);

    /** Paints the full gameplay screen. */
    public void paint(Graphics2D g2, GameScreenPanel panel) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(0, 0, BACKGROUND_TOP, 0, panel.getHeight(), BACKGROUND_BOTTOM);
        g2.setPaint(background);
        g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        Point2D center = new Point2D.Float(panel.getWidth() / 2.0f, panel.getHeight() / 2.0f);
        float[] dist = { 0.0f, 1.0f };
        Color[] colors = { GLOW_COLOR, new Color(0, 0, 0, 0) };
        g2.setPaint(new java.awt.RadialGradientPaint(center, 560f, dist, colors));
        g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        g2.setStroke(new BasicStroke(1f));
        g2.setColor(ACCENT_GLOW);
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
        g2.setColor(hover ? BUTTON_BG_HOVER : BUTTON_BG);
        g2.fillRoundRect(backButton.x, backButton.y, backButton.width, backButton.height, 16, 16);
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(hover ? BUTTON_BORDER_HOVER : BUTTON_BORDER);
        g2.drawRoundRect(backButton.x, backButton.y, backButton.width, backButton.height, 16, 16);

        g2.setFont(BUTTON_FONT);
        g2.setColor(hover ? TITLE_PRIMARY : BUTTON_TEXT);
        drawCenteredText(g2, "MENU", backButton, 1);
    }

    private void drawHeader(Graphics2D g2, GameScreenPanel panel) {
        g2.setFont(TITLE_FONT);
        g2.setColor(TITLE_SHADOW);
        drawCentered(g2, "FINAL ANSWER?", 70, panel);
        g2.setColor(TITLE_PRIMARY);
        drawCentered(g2, "FINAL ANSWER?", 68, panel);

        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleWidth = titleMetrics.stringWidth("FINAL ANSWER?");
        int titleX = (panel.getWidth() - titleWidth) / 2;
        g2.setColor(TITLE_ACCENT);
        g2.fillRoundRect(titleX + 34, 80, titleWidth - 68, 4, 4, 4);

        drawStatCard(g2, 90, 140, 250, 68, "QUESTION",
                panel.getSession().getCurrentQuestionNumber() + " / " + panel.getSession().getTotalQuestions());
        drawTimerWidget(g2, panel, 438, 118, 214, 116);
        drawStatCard(g2, 760, 140, 250, 68, "MONEY", "$" + String.format("%,d", panel.getSession().getScore()));
    }

    private void drawStatCard(Graphics2D g2, int x, int y, int width, int height, String label, String value) {
        g2.setColor(CARD_BG);
        g2.fillRoundRect(x, y, width, height, 18, 18);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(x, y, width, height, 18, 18);

        g2.setFont(STAT_SMALL_FONT);
        g2.setColor(SUBTITLE_COLOR);
        g2.drawString(label, x + 18, y + 24);

        g2.setFont(STAT_FONT);
        g2.setColor(TITLE_PRIMARY);
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

        g2.setColor(CARD_BG);
        g2.fillOval(circleX, circleY, diameter, diameter);
        g2.setColor(CARD_BORDER);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawOval(circleX, circleY, diameter, diameter);

        int timeLimit = panel.getSession().getCurrentQuestionTimeLimit();
        int timeRemaining = panel.getSession().getTimeRemaining();
        float fraction = timeLimit <= 0 ? 0.0f : Math.max(0.0f, Math.min(1.0f, timeRemaining / (float) timeLimit));
        int extent = Math.round(360f * fraction);

        Color timerColor = timerColorForFraction(fraction);

        g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 28));
        g2.drawArc(ringX, ringY, ringSize, ringSize, 90, -360);

        g2.setColor(timerColor);
        g2.drawArc(ringX, ringY, ringSize, ringSize, 90, -extent);

        g2.setFont(STAT_SMALL_FONT);
        g2.setColor(SUBTITLE_COLOR);
        drawCenteredText(g2, "TIME", new Rectangle(circleX, circleY, diameter, diameter), -28);

        g2.setFont(STAT_FONT);
        g2.setColor(TITLE_PRIMARY);
        drawCenteredText(g2, timeRemaining + "s", new Rectangle(circleX, circleY, diameter, diameter), 4);
    }

    private Color timerColorForFraction(float fraction) {
        if (fraction > 0.60f) return SUCCESS;
        if (fraction > 0.25f) return TITLE_ACCENT;
        return FAILURE;
    }

    private void drawQuestionCard(Graphics2D g2, GameScreenPanel panel) {
        Rectangle card = new Rectangle(80, 236, 940, 132);
        g2.setColor(CARD_BG);
        g2.fillRoundRect(card.x, card.y, card.width, card.height, 26, 26);
        g2.setStroke(new BasicStroke(1.3f));
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(card.x, card.y, card.width, card.height, 26, 26);

        Question question = panel.getSession().getCurrentQuestion();
        if (question == null) {
            g2.setFont(BODY_FONT);
            g2.setColor(SUBTITLE_COLOR);
            String noQuestionText = "No question available";
            FontMetrics fm = g2.getFontMetrics();
            int textX = card.x + (card.width - fm.stringWidth(noQuestionText)) / 2;
            g2.drawString(noQuestionText, textX, card.y + card.height / 2 + fm.getAscent() / 2);
            return;
        }

        g2.setFont(STAT_SMALL_FONT);
        g2.setColor(TITLE_ACCENT);
        g2.drawString(question.getCategory().toUpperCase(), card.x + 24, card.y + 28);

        g2.setFont(BODY_FONT);
        g2.setColor(TITLE_PRIMARY);
        drawWrappedText(g2, question.getQuestion(), card.x + 24, card.y + 58, card.width - 48, 28);

        String statusMessage = panel.getSession().getStatusMessage();
        if (statusMessage != null && !statusMessage.isEmpty()) {
            g2.setFont(BODY_SMALL_FONT);
            g2.setColor(colorForStatus(panel.getSession().getStatusType()));
            g2.drawString(statusMessage, card.x + 24, card.y + 112);
        }
    }

    private void drawAnswerButtons(Graphics2D g2, GameScreenPanel panel) {
        Question question = panel.getSession().getCurrentQuestion();
        if (question == null) return;

        String[] labels = { "A", "B", "C", "D" };
        String[] answers = {
                question.getAnswerA(),
                question.getAnswerB(),
                question.getAnswerC(),
                question.getAnswerD()
        };

        int correctIndex = question.getCorrectAnswer().toUpperCase().trim().charAt(0) - 'A';

        for (int i = 0; i < panel.getAnswerBounds().length; i++) {
            Rectangle rect = panel.getAnswerBounds()[i];
            boolean hover = panel.getHoveredAnswerIndex() == i && !panel.isAnswerLocked(i)
                    && !panel.getSession().isFinished();
            boolean disabled = panel.isAnswerLocked(i);

            boolean animationRunning = panel.isAnswerAnimationRunning();
            boolean isSelected = (panel.getSelectedAnswer() == i);
            boolean flashState = panel.getFlashState();
            boolean wasCorrect = panel.wasLastAnswerCorrect();

            Color currentBg = disabled ? new Color(17, 24, 39, 110) : (hover ? BUTTON_BG_HOVER : BUTTON_BG);
            Color currentBorder = disabled ? new Color(55, 65, 81, 80) : (hover ? BUTTON_BORDER_HOVER : BUTTON_BORDER);
            Color currentLabelColor = disabled ? DISABLED_TEXT : (hover ? TITLE_PRIMARY : BUTTON_TEXT);

            if (isSelected || (animationRunning && i == correctIndex)) {
                if (!animationRunning) {
                    if (isSelected) {
                        currentBg = new Color(245, 158, 11, 40);
                        currentBorder = TITLE_ACCENT;
                        currentLabelColor = TITLE_ACCENT;
                    }
                } else {
                    if (wasCorrect) {
                        if (flashState) {
                            currentBg = new Color(34, 197, 94, 50);
                            currentBorder = SUCCESS;
                            currentLabelColor = SUCCESS;
                        } else {
                            currentBg = new Color(245, 158, 11, 40);
                            currentBorder = TITLE_ACCENT;
                            currentLabelColor = TITLE_ACCENT;
                        }
                    } else {
                        if (isSelected) {
                            if (flashState) {
                                currentBg = new Color(239, 68, 68, 50);
                                currentBorder = FAILURE;
                                currentLabelColor = FAILURE;
                            } else {
                                currentBg = new Color(245, 158, 11, 40);
                                currentBorder = TITLE_ACCENT;
                                currentLabelColor = TITLE_ACCENT;
                            }
                        }
                        if (i == correctIndex) {
                            if (flashState) {
                                currentBg = new Color(34, 197, 94, 50);
                                currentBorder = SUCCESS;
                                currentLabelColor = SUCCESS;
                            } else {
                                currentBg = new Color(17, 24, 39, 170);
                                currentBorder = BUTTON_BORDER;
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

            g2.setFont(BUTTON_FONT);
            g2.setColor(currentLabelColor);
            g2.drawString(labels[i], rect.x + 18, rect.y + 31);

            g2.setFont(BODY_SMALL_FONT);
            drawWrappedText(g2, answers[i], rect.x + 58, rect.y + 28, rect.width - 74, 20,
                    disabled ? DISABLED_TEXT : BUTTON_TEXT);

            // Draw audience poll percentage if available
            int percentage = panel.getAudiencePollPercentage(i);
            if (percentage >= 0) {
                g2.setFont(LIFELINE_FONT);
                g2.setColor(TITLE_ACCENT);
                String percentText = "Audience: " + percentage + "%";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(percentText, rect.x + rect.width - fm.stringWidth(percentText) - 15, rect.y + rect.height - 10);
            }

            // Draw phone a friend suggestion if this is the suggested answer
            String suggestion = panel.getPhoneAFriendSuggestion();
            int suggestedIndex = panel.getPhoneAFriendSuggestedIndex();
            if (suggestion != null && suggestedIndex == i) {
                g2.setFont(SUGGESTION_FONT);
                g2.setColor(SUCCESS);
                g2.drawString(suggestion, rect.x + 60, rect.y + rect.height - 10);
            }
        }
    }

    private void drawLifelineButtons(Graphics2D g2, GameScreenPanel panel) {
        boolean[] used = {
                panel.getSession().isSwapUsed(),
                panel.getSession().isAudiencePollUsed(),
                panel.getSession().isFiftyFiftyUsed(),
                panel.getSession().isPhoneAFriendUsed()
        };
        String[] labels = { "SWAP", "AUDIENCE", "25/75", "PHONE" };

        for (int i = 0; i < panel.getLifelineBounds().length; i++) {
            Rectangle rect = panel.getLifelineBounds()[i];
            boolean hover = panel.getHoveredLifelineIndex() == i && !used[i] && !panel.getSession().isFinished();
            boolean disabled = used[i];

            g2.setColor(disabled ? new Color(17, 24, 39, 110) : (hover ? BUTTON_BG_HOVER : BUTTON_BG));
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 16, 16);
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(disabled ? new Color(55, 65, 81, 80) : (hover ? BUTTON_BORDER_HOVER : BUTTON_BORDER));
            g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 16, 16);

            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(disabled ? DISABLED_TEXT : (hover ? TITLE_PRIMARY : TITLE_ACCENT));
            drawCenteredText(g2, labels[i], rect, -2);
        }
    }

    private void drawFooter(Graphics2D g2, GameScreenPanel panel) {
        g2.setFont(SUBTITLE_FONT);
        g2.setColor(SUBTITLE_COLOR);
        drawCentered(g2, "Use the mouse to answer, activate lifelines, or return to the menu.", 708, panel);
    }

    private Color colorForStatus(GameSession.StatusType statusType) {
        if (statusType == GameSession.StatusType.SUCCESS) return SUCCESS;
        if (statusType == GameSession.StatusType.FAILURE) return FAILURE;
        if (statusType == GameSession.StatusType.COMPLETE) return TITLE_ACCENT;
        return SUBTITLE_COLOR;
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
        drawWrappedText(g2, text, x, y, width, lineHeight, TITLE_PRIMARY);
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
                if (line.length() > 0) line.append(' ');
                line.append(word);
            }
        }

        if (line.length() > 0) {
            g2.drawString(line.toString(), x, currentY);
        }
    }
}