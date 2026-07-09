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

/* Class responsible for rendering the game over screen */
public class GameOverPanel extends JPanel {

    private final Font titleFont = new Font("SansSerif", Font.BOLD, 46);
    private final Font bodyFont = new Font("SansSerif", Font.PLAIN, 18);
    private final Font highlightFont = new Font("SansSerif", Font.BOLD, 20);
    private final Font buttonFont = new Font("SansSerif", Font.BOLD, 16);

    private final Color backgroundTop = new Color(10, 8, 28);
    private final Color backgroundBottom = new Color(3, 2, 10);
    private final Color glowColor = new Color(239, 68, 68, 15); 
    private final Color titlePrimary = Color.WHITE;
    private final Color titleAccent = new Color(245, 158, 11); 
    private final Color cardBg = new Color(17, 24, 39, 195);
    private final Color cardBorder = new Color(55, 65, 81, 140);
    
    private final Color buttonBg = new Color(17, 24, 39, 170);
    private final Color buttonBgHover = new Color(30, 27, 75, 210);
    private final Color buttonBorder = new Color(55, 65, 81, 130);
    private final Color buttonBorderHover = new Color(245, 158, 11);
    
    private final Color success = new Color(34, 197, 94); 
    private final Color failure = new Color(239, 68, 68); 

    // Dynamic buttons track actual dimensions inside paintComponent
    private final Rectangle tryAgainButton = new Rectangle(0, 0, 180, 46);
    private final Rectangle goHomeButton = new Rectangle(0, 0, 180, 46);

    private boolean hoverTryAgain = false;
    private boolean hoverGoHome = false;

    private final String finalWinnings;
    private final Runnable onPlayAgain;

    /**
     * Constructs the updated Game Over Panel using British Pounds currency.
     */
    public GameOverPanel(String selectedAnswer, String correctAnswer, int score, Runnable onPlayAgain) {
        // Formatted with the Pounds symbol instead of Dollars
        this.finalWinnings = "£" + String.format("%,d", score);
        this.onPlayAgain = onPlayAgain;

        setFocusable(true);
        installListeners();
    }

    private void installListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                hoverTryAgain = tryAgainButton.contains(p);
                hoverGoHome = goHomeButton.contains(p);
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (tryAgainButton.contains(p)) {
                    onPlayAgain.run();
                } else if (goHomeButton.contains(p)) {
                    returnToMenu();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Calculate dynamic layouts relative to current window sizes
        int cardWidth = Math.min(940, w - 80);
        int cardHeight = 340;
        int cardX = (w - cardWidth) / 2;
        int cardY = (h - cardHeight) / 2 - 20;

        // Reposition controls perfectly inside screen context
        tryAgainButton.x = cardX + (cardWidth / 2) - 190;
        tryAgainButton.y = cardY + cardHeight + 30;
        goHomeButton.x = cardX + (cardWidth / 2) + 10;
        goHomeButton.y = cardY + cardHeight + 30;

        // Draw Full Screen Gradients
        GradientPaint background = new GradientPaint(0, 0, backgroundTop, 0, h, backgroundBottom);
        g2.setPaint(background);
        g2.fillRect(0, 0, w, h);

        Point2D center = new Point2D.Float(w / 2.0f, h / 2.0f);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {glowColor, new Color(0, 0, 0, 0)};
        g2.setPaint(new java.awt.RadialGradientPaint(center, Math.max(w, h) * 0.6f, dist, colors));
        g2.fillRect(0, 0, w, h);

        // Display Central Slate Envelope Box
        g2.setColor(cardBg);
        g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 24, 24);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(cardBorder);
        g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 24, 24);

        // Title
        g2.setFont(titleFont);
        g2.setColor(failure);
        drawCenteredText(g2, "GAME OVER", cardY - 40);

        // Text Row: Ouch String
        g2.setFont(bodyFont);
        g2.setColor(titlePrimary);
        drawCenteredText(g2, "Ouch... That's gotta sting... You are not joining 1.5% of the adult population who are millionaires (yet).", cardY + 80);

        // Score metrics display
        FontMetrics fm = g2.getFontMetrics(bodyFont);
        FontMetrics fmBold = g2.getFontMetrics(highlightFont);
        
        g2.setFont(bodyFont);
        g2.setColor(titlePrimary);
        String winningsLabel = "You have won: ";
        int winningsWidth = fm.stringWidth(winningsLabel) + fmBold.stringWidth(finalWinnings);
        int startXWinnings = (w - winningsWidth) / 2;
        g2.drawString(winningsLabel, startXWinnings, cardY + 180);
        
        g2.setFont(highlightFont);
        g2.setColor(titleAccent);
        g2.drawString(finalWinnings, startXWinnings + fm.stringWidth(winningsLabel), cardY + 180);

        // Prompt Row
        g2.setFont(bodyFont);
        g2.setColor(titlePrimary);
        drawCenteredText(g2, "Would you like to go home or try again?", cardY + 260);

        // Render Action Control elements
        drawCustomButton(g2, tryAgainButton, "TRY AGAIN", hoverTryAgain);
        drawCustomButton(g2, goHomeButton, "GO HOME", hoverGoHome);
    }

    private void drawCustomButton(Graphics2D g2, Rectangle r, String text, boolean isHovered) {
        g2.setColor(isHovered ? buttonBgHover : buttonBg);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(isHovered ? buttonBorderHover : buttonBorder);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        g2.setFont(buttonFont);
        g2.setColor(isHovered ? titlePrimary : titleAccent);
        FontMetrics fm = g2.getFontMetrics();
        int x = r.x + (r.width - fm.stringWidth(text)) / 2;
        int y = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, x, y);
    }

    private void drawCenteredText(Graphics2D g2, String text, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    private void returnToMenu() {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }
}