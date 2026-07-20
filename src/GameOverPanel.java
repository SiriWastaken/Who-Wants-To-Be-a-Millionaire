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
import javax.swing.SwingUtilities;

/**
 * Class responsible for rendering the game over screen.
 * Provides options to try again or return to the main menu.
 */
public class GameOverPanel extends JPanel {

    // --- Typography / Fonts ---
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 46);
    private final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 18);
    private final Font HIGHLIGHT_FONT = new Font("SansSerif", Font.BOLD, 20);
    private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 16);

    // --- Color Palette (Dark Theme / High-Contrast UI) ---
    private final Color BACKGROUND_TOP = new Color(10, 8, 28);
    private final Color BACKGROUND_BOTTOM = new Color(3, 2, 10);
    private final Color GLOW_COLOR = new Color(239, 68, 68, 15); // Subtle red vignette glow
    private final Color TITLE_PRIMARY = Color.WHITE;
    private final Color TITLE_ACCENT = new Color(245, 158, 11); // Amber accent color
    private final Color CARD_BG = new Color(17, 24, 39, 195);     // Semi-transparent overlay card
    private final Color CARD_BORDER = new Color(55, 65, 81, 140);

    // --- Interactive Component Colors ---
    private final Color BUTTON_BG = new Color(17, 24, 39, 170);
    private final Color BUTTON_BG_HOVER = new Color(30, 27, 75, 210);
    private final Color BUTTON_BORDER = new Color(55, 65, 81, 130);
    private final Color BUTTON_BORDER_HOVER = new Color(245, 158, 11);

    private final Color FAILURE = new Color(239, 68, 68); // Soft red alert color

    // --- Interaction Target Zones ---
    private final Rectangle TRY_AGAIN_BUTTON = new Rectangle(0, 0, 180, 46);
    private final Rectangle GO_HOME_BUTTON = new Rectangle(0, 0, 180, 46);

    // --- State Tracking Variables ---
    private boolean hoverTryAgain = false;
    private boolean hoverGoHome = false;

    private final String finalWinnings;
    private final Runnable onPlayAgain;

    /**
     * Constructs the Game Over Panel.
     *
     * @param selectedAnswer the answer the player selected
     * @param correctAnswer the correct answer
     * @param score the player's final score
     * @param onPlayAgain callback when player chooses to try again (skips intro)
     */
    public GameOverPanel(String selectedAnswer, String correctAnswer, int score, Runnable onPlayAgain) {
        // Pre-format score with regional currency symbol and thousands commas (e.g., £1,000,000)
        this.finalWinnings = "£" + String.format("%,d", score);
        this.onPlayAgain = onPlayAgain;
        
        setFocusable(true);
        installListeners();
    }

    /**
     * Sets up mouse interactivity hooks for standard hover changes and action triggers.
     */
    private void installListeners() {
        // Handle cursor tracking for button color transitions
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean prevHoverTryAgain = hoverTryAgain;
                boolean prevHoverGoHome = hoverGoHome;

                hoverTryAgain = TRY_AGAIN_BUTTON.contains(p);
                hoverGoHome = GO_HOME_BUTTON.contains(p);

                // Re-draw panel state ONLY if hover state transitions to conserve performance
                if (hoverTryAgain != prevHoverTryAgain || hoverGoHome != prevHoverGoHome) {
                    repaint();
                }
            }
        });

        // Process button interaction clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (TRY_AGAIN_BUTTON.contains(p)) {
                    if (onPlayAgain != null) {
                        onPlayAgain.run();
                    }
                } else if (GO_HOME_BUTTON.contains(p)) {
                    returnToMenu();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable anti-aliasing for vectors and text curves
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // --- Layout Math Calculations ---
        // Dynamically compute score display bounding box based on frame scaling boundaries
        int cardWidth = Math.min(940, w - 80);
        int cardHeight = 340;
        int cardX = (w - cardWidth) / 2;
        int cardY = (h - cardHeight) / 2 - 20;

        // Position interaction targets side-by-side centered below the display card
        TRY_AGAIN_BUTTON.x = cardX + (cardWidth / 2) - 190;
        TRY_AGAIN_BUTTON.y = cardY + cardHeight + 30;
        GO_HOME_BUTTON.x = cardX + (cardWidth / 2) + 10;
        GO_HOME_BUTTON.y = cardY + cardHeight + 30;

        // --- Render Pass 1: Background Layouts ---
        // Linear ambient background drop
        GradientPaint background = new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM);
        g2.setPaint(background);
        g2.fillRect(0, 0, w, h);

        // Core vignette focal drop (radial mask overlay)
        Point2D center = new Point2D.Float(w / 2.0f, h / 2.0f);
        float[] dist = { 0.0f, 1.0f };
        Color[] colors = { GLOW_COLOR, new Color(0, 0, 0, 0) };
        g2.setPaint(new java.awt.RadialGradientPaint(center, Math.max(w, h) * 0.6f, dist, colors));
        g2.fillRect(0, 0, w, h);

        // --- Render Pass 2: Main Score Card Overlay ---
        g2.setColor(CARD_BG);
        g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 24, 24);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 24, 24);

        // --- Render Pass 3: Text Data Blocks ---
        g2.setFont(TITLE_FONT);
        g2.setColor(FAILURE);
        drawCenteredText(g2, "GAME OVER", cardY - 40);

        g2.setFont(BODY_FONT);
        g2.setColor(TITLE_PRIMARY);
        drawCenteredText(g2,
                "Ouch... That's gotta sting... You are not joining 1.5% of the adult population who are millionaires (yet).",
                cardY + 100);

        // Mixed-font inline text building (Combining static string + colored accent value)
        FontMetrics fm = g2.getFontMetrics(BODY_FONT);
        FontMetrics fmBold = g2.getFontMetrics(HIGHLIGHT_FONT);

        g2.setFont(BODY_FONT);
        g2.setColor(TITLE_PRIMARY);
        String winningsLabel = "You have won: ";
        
        // Calculate dynamic horizontal offset to ensure the combined multi-font block stays centered
        int winningsWidth = fm.stringWidth(winningsLabel) + fmBold.stringWidth(finalWinnings);
        int startXWinnings = (w - winningsWidth) / 2;
        g2.drawString(winningsLabel, startXWinnings, cardY + 180);

        // Print the colored accent string exactly where the base string bounds end
        g2.setFont(HIGHLIGHT_FONT);
        g2.setColor(TITLE_ACCENT);
        g2.drawString(finalWinnings, startXWinnings + fm.stringWidth(winningsLabel), cardY + 180);

        g2.setFont(BODY_FONT);
        g2.setColor(TITLE_PRIMARY);
        drawCenteredText(g2, "Would you like to try again or go home?", cardY + 260);

        // --- Render Pass 4: Interactive Control Elements ---
        drawCustomButton(g2, TRY_AGAIN_BUTTON, "TRY AGAIN", hoverTryAgain);
        drawCustomButton(g2, GO_HOME_BUTTON, "GO HOME", hoverGoHome);
    }

    /**
     * Renders an isolated interactive rounded button with contextual responsive styling.
     */
    private void drawCustomButton(Graphics2D g2, Rectangle r, String text, boolean isHovered) {
        // Draw base surface
        g2.setColor(isHovered ? BUTTON_BG_HOVER : BUTTON_BG);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        
        // Draw decorative borders
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(isHovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        // Centering typography math vertically and horizontally inside the target bounding container
        g2.setFont(BUTTON_FONT);
        g2.setColor(isHovered ? TITLE_PRIMARY : TITLE_ACCENT);
        FontMetrics fm = g2.getFontMetrics();
        int x = r.x + (r.width - fm.stringWidth(text)) / 2;
        int y = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, x, y);
    }

    /**
     * Helper calculation asset targeting standard structural horizontal tracking centering mechanics.
     */
    private void drawCenteredText(Graphics2D g2, String text, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    /**
     * Re-initializes base navigation layers and cleans up existing system allocations safely.
     */
    private void returnToMenu() {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
        
        // Destroys and cleans up parent layout resources to prevent window thread leaks
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }
}