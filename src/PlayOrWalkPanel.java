import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Displays the Play or Walk Away screen when the player reaches a safe point.
 * Gives the player the choice to walk away with their guaranteed winnings
 * or continue playing for a higher prize.
 */
public class PlayOrWalkPanel extends JPanel {

    // Fonts matching your game's style
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 40);
    private final Font MONEY_FONT = new Font("SansSerif", Font.BOLD, 52);
    private final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 18);
    private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 18);

    // Colors matching your game's theme
    private final Color BACKGROUND_TOP = new Color(10, 8, 28);
    private final Color BACKGROUND_BOTTOM = new Color(3, 2, 10);
    private final Color GLOW_COLOR = new Color(99, 102, 241, 15);
    private final Color ACCENT_GLOW = new Color(245, 158, 11, 20);
    private final Color CARD_BG = new Color(17, 24, 39, 220);
    private final Color CARD_BORDER = new Color(55, 65, 81, 180);
    private final Color TITLE_PRIMARY = Color.WHITE;
    private final Color TITLE_ACCENT = new Color(245, 158, 11);
    private final Color TITLE_SHADOW = new Color(168, 85, 247, 45);
    private final Color SUBTITLE_COLOR = new Color(156, 163, 175);
    private final Color BUTTON_BG = new Color(17, 24, 39, 170);
    private final Color BUTTON_BG_HOVER = new Color(30, 27, 75, 210);
    private final Color BUTTON_BORDER = new Color(55, 65, 81, 130);
    private final Color BUTTON_BORDER_HOVER = new Color(245, 158, 11);
    private final Color BUTTON_TEXT = new Color(229, 231, 235);
    private final Color SUCCESS = new Color(34, 197, 94);
    private final Color FAILURE = new Color(239, 68, 68);

    private final int safeMoney;
    private final int nextLevelMoney;
    private final Runnable onWalkAway;
    private final Runnable onContinue;

    // Button bounds
    private final Rectangle WALK_AWAY_BUTTON = new Rectangle(0, 0, 200, 50);
    private final Rectangle CONTINUE_BUTTON = new Rectangle(0, 0, 200, 50);
    private final Rectangle PLAY_AGAIN_BUTTON = new Rectangle(0, 0, 200, 50);
    private final Rectangle GO_HOME_BUTTON = new Rectangle(0, 0, 200, 50);

    private boolean hoverWalkAway = false;
    private boolean hoverContinue = false;
    private boolean hoverPlayAgain = false;
    private boolean hoverGoHome = false;
    private boolean isDisappearing = false;
    private boolean showWalkAwayConfirmation = false;
    private Timer fadeTimer;
    private float fadeAlpha = 1.0f;

    /**
     * Creates a new Play or Walk Away panel.
     *
     * @param safeMoney      the amount the player has guaranteed
     * @param nextLevelMoney the amount the player could win
     * @param onWalkAway     callback when player chooses to walk away
     * @param onContinue     callback when player chooses to continue
     */
    public PlayOrWalkPanel(int safeMoney, int nextLevelMoney,
            Runnable onWalkAway, Runnable onContinue) {
        this.safeMoney = safeMoney;
        this.nextLevelMoney = nextLevelMoney;
        this.onWalkAway = onWalkAway;
        this.onContinue = onContinue;

        setOpaque(false);
        setFocusable(true);
        installListeners();
    }

    private void installListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                if (!showWalkAwayConfirmation) {
                    hoverWalkAway = WALK_AWAY_BUTTON.contains(p);
                    hoverContinue = CONTINUE_BUTTON.contains(p);
                } else {
                    hoverPlayAgain = PLAY_AGAIN_BUTTON.contains(p);
                    hoverGoHome = GO_HOME_BUTTON.contains(p);
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (!showWalkAwayConfirmation) {
                    if (WALK_AWAY_BUTTON.contains(p)) {
                        // Show the confirmation screen
                        showWalkAwayConfirmation = true;
                        repaint();
                    } else if (CONTINUE_BUTTON.contains(p)) {
                        startFadeAndRun(onContinue);
                    }
                } else {
                    if (PLAY_AGAIN_BUTTON.contains(p)) {
                        // Play again - execute the walk away callback (which triggers restart)
                        startFadeAndRun(onWalkAway);
                    } else if (GO_HOME_BUTTON.contains(p)) {
                        // Go home - execute the walk away callback (which triggers menu)
                        startFadeAndRun(onWalkAway);
                    }
                }
            }
        });
    }

    /**
     * Fades the panel out before executing the callback.
     *
     * @param callback the runnable to execute after fade
     */
    private void startFadeAndRun(Runnable callback) {
        if (isDisappearing)
            return;
        isDisappearing = true;

        fadeTimer = new Timer(20, e -> {
            fadeAlpha -= 0.04;
            if (fadeAlpha <= 0) {
                fadeAlpha = 0;
                fadeTimer.stop();
                callback.run();
            }
            repaint();
        });
        fadeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Apply fade effect
            g2.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, fadeAlpha));

            int w = getWidth();
            int h = getHeight();

            // Full screen background with gradient matching your game
            GradientPaint bgGradient = new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM);
            g2.setPaint(bgGradient);
            g2.fillRect(0, 0, w, h);

            // Background glow matching your game
            Point2D center = new Point2D.Float(w / 2.0f, h / 2.0f);
            float[] dist = { 0.0f, 1.0f };
            Color[] colors = { GLOW_COLOR, new Color(0, 0, 0, 0) };
            g2.setPaint(new java.awt.RadialGradientPaint(center, 560f, dist, colors));
            g2.fillRect(0, 0, w, h);

            // Decorative glow circles matching your game
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(ACCENT_GLOW);
            g2.drawOval(-170, -170, 540, 540);
            g2.drawOval(w - 390, h - 440, 620, 620);

            if (!showWalkAwayConfirmation) {
                drawPlayOrWalkScreen(g2, w, h);
            } else {
                drawWalkAwayConfirmationScreen(g2, w, h);
            }

        } finally {
            g2.dispose();
        }
    }

    private void drawPlayOrWalkScreen(Graphics2D g2, int w, int h) {
        // Card dimensions
        int cardWidth = Math.min(680, w - 80);
        int cardHeight = 360;
        int cardX = (w - cardWidth) / 2;
        int cardY = (h - cardHeight) / 2 - 20;

        // Position buttons
        int buttonY = cardY + cardHeight - 70;
        WALK_AWAY_BUTTON.x = cardX + 50;
        WALK_AWAY_BUTTON.y = buttonY;
        CONTINUE_BUTTON.x = cardX + cardWidth - 50 - CONTINUE_BUTTON.width;
        CONTINUE_BUTTON.y = buttonY;

        // Card background (matching your game's card style)
        g2.setColor(CARD_BG);
        g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 26, 26);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 26, 26);

        // Title with shadow (matching your game's title style)
        g2.setFont(TITLE_FONT);
        g2.setColor(TITLE_SHADOW);
        String title = "HIGH STAKES!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (w - fm.stringWidth(title)) / 2 + 2, cardY + 78);
        g2.setColor(TITLE_PRIMARY);
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, cardY + 76);

        // Gold underline (matching your game)
        g2.setColor(TITLE_ACCENT);
        g2.fillRoundRect(w / 2 - 80, cardY + 85, 160, 3, 3, 3);

        // Subtitle
        g2.setFont(BODY_FONT);
        g2.setColor(SUBTITLE_COLOR);
        String subtitle = "You've reached a guaranteed payout!";
        fm = g2.getFontMetrics();
        g2.drawString(subtitle, (w - fm.stringWidth(subtitle)) / 2, cardY + 125);

        // Money display with gold accent
        g2.setFont(MONEY_FONT);
        g2.setColor(TITLE_ACCENT);
        String moneyText = "£" + String.format("%,d", safeMoney);
        fm = g2.getFontMetrics();
        g2.drawString(moneyText, (w - fm.stringWidth(moneyText)) / 2, cardY + 195);

        // Next level info
        g2.setFont(BODY_FONT);
        g2.setColor(SUBTITLE_COLOR);
        String nextText = "Next question: £" + String.format("%,d", nextLevelMoney);
        fm = g2.getFontMetrics();
        g2.drawString(nextText, (w - fm.stringWidth(nextText)) / 2, cardY + 235);

        // Buttons (matching your game's button style)
        drawCustomButton(g2, WALK_AWAY_BUTTON, "WALK AWAY", hoverWalkAway);
        drawCustomButton(g2, CONTINUE_BUTTON, "CONTINUE", hoverContinue);
    }

    private void drawWalkAwayConfirmationScreen(Graphics2D g2, int w, int h) {
        // Card dimensions
        int cardWidth = Math.min(680, w - 80);
        int cardHeight = 380;
        int cardX = (w - cardWidth) / 2;
        int cardY = (h - cardHeight) / 2 - 20;

        // Position buttons
        int buttonY = cardY + cardHeight - 70;
        PLAY_AGAIN_BUTTON.x = cardX + 50;
        PLAY_AGAIN_BUTTON.y = buttonY;
        GO_HOME_BUTTON.x = cardX + cardWidth - 50 - GO_HOME_BUTTON.width;
        GO_HOME_BUTTON.y = buttonY;

        // Card background
        g2.setColor(CARD_BG);
        g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 26, 26);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 26, 26);

        // Title with shadow - "CONGRATULATIONS!" in gold
        g2.setFont(TITLE_FONT);
        g2.setColor(TITLE_SHADOW);
        String title = "CONGRATS!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (w - fm.stringWidth(title)) / 2 + 2, cardY + 78);
        g2.setColor(TITLE_ACCENT);
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, cardY + 76);

        // Gold underline
        g2.setColor(TITLE_ACCENT);
        g2.fillRoundRect(w / 2 - 100, cardY + 85, 200, 3, 3, 3);

        // Walk away message
        g2.setFont(BODY_FONT);
        g2.setColor(SUBTITLE_COLOR);
        String message = "You've walked away with:";
        fm = g2.getFontMetrics();
        g2.drawString(message, (w - fm.stringWidth(message)) / 2, cardY + 135);

        // Big money display
        g2.setFont(MONEY_FONT);
        g2.setColor(TITLE_ACCENT);
        String moneyText = "£" + String.format("%,d", safeMoney);
        fm = g2.getFontMetrics();
        g2.drawString(moneyText, (w - fm.stringWidth(moneyText)) / 2, cardY + 210);

        // Smart decision message
        g2.setFont(new Font("SansSerif", Font.ITALIC, 16));
        g2.setColor(SUBTITLE_COLOR);
        String smartText = "A smart decision!";
        fm = g2.getFontMetrics();
        g2.drawString(smartText, (w - fm.stringWidth(smartText)) / 2, cardY + 250);

        // Divider line
        g2.setColor(CARD_BORDER);
        g2.drawLine(cardX + 80, cardY + 270, cardX + cardWidth - 80, cardY + 270);

        // Buttons
        drawCustomButton(g2, PLAY_AGAIN_BUTTON, "PLAY AGAIN", hoverPlayAgain);
        drawCustomButton(g2, GO_HOME_BUTTON, "GO HOME", hoverGoHome);
    }

    private void drawCustomButton(Graphics2D g2, Rectangle r, String text, boolean isHovered) {
        // Background
        g2.setColor(isHovered ? BUTTON_BG_HOVER : BUTTON_BG);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        // Border
        g2.setStroke(new BasicStroke(isHovered ? 1.5f : 1f));
        g2.setColor(isHovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        // Text
        g2.setFont(BUTTON_FONT);
        g2.setColor(isHovered ? TITLE_PRIMARY : BUTTON_TEXT);
        FontMetrics fm = g2.getFontMetrics();
        int x = r.x + (r.width - fm.stringWidth(text)) / 2;
        int y = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, x, y);
    }

    /**
     * Returns whether the panel is currently fading out.
     *
     * @return true if fading, false otherwise
     */
    public boolean isDisappearing() {
        return isDisappearing;
    }

    /**
     * Stops the fade timer if it's running.
     */
    public void cleanup() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
    }
}