import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A game over screen panel that displays final winnings and provides
 * options to return to menu or play again. No JOptionPane used.
 */
public class GameOverScreen extends GameScreen {

    // Background colors matching the game's visual style
    private static final Color BACKGROUND_TOP = new Color(6, 6, 16);
    private static final Color BACKGROUND_BOTTOM = new Color(18, 12, 36);
    private static final Color GOLD = new Color(245, 158, 11);
    private static final Color GOLD_DARK = new Color(200, 130, 0);
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color BUTTON_BG = new Color(17, 24, 39, 170);
    private static final Color BUTTON_HOVER = new Color(30, 27, 75, 210);
    private static final Color BUTTON_BORDER = new Color(55, 65, 81, 130);
    private static final Color BUTTON_BORDER_HOVER = new Color(245, 158, 11);
    private static final Color FAILURE = new Color(239, 68, 68);

    private final Font titleFont = new Font("SansSerif", Font.BOLD, 48);
    private final Font subtitleFont = new Font("SansSerif", Font.PLAIN, 24);
    private final Font amountFont = new Font("SansSerif", Font.BOLD, 36);
    private final Font buttonFont = new Font("SansSerif", Font.BOLD, 18);

    private final Rectangle menuButton = new Rectangle(350, 420, 300, 50);
    private final Rectangle playAgainButton = new Rectangle(350, 490, 300, 50);

    private int hoveredButton = -1; // -1 = none, 0 = menu, 1 = play again
    private final int finalWinnings;
    private final boolean failed;

    /**
     * Creates the game over screen.
     *
     * @param finalWinnings the amount of money the player earned
     * @param failed true if the player lost (answered incorrectly), false if they walked away or completed
     */
    public GameOverScreen(int finalWinnings, boolean failed) {
        super(new GameSession());
        this.finalWinnings = finalWinnings;
        this.failed = failed;

        setPreferredSize(new Dimension(1100, 760));
        setLayout(null);
        setFocusable(true);

        installListeners();
    }

    private void installListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                hoveredButton = -1;
                if (menuButton.contains(p)) {
                    hoveredButton = 0;
                } else if (playAgainButton.contains(p)) {
                    hoveredButton = 1;
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (menuButton.contains(p)) {
                    returnToMenu();
                } else if (playAgainButton.contains(p)) {
                    playAgain();
                }
            }
        });
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

    private void playAgain() {
        SwingUtilities.invokeLater(() -> {
            GameScreen game = new GameScreen();
            game.setVisible(true);
        });
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    @Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    // Background gradient
    java.awt.GradientPaint background = new java.awt.GradientPaint(0, 0, BACKGROUND_TOP, 0, getHeight(), BACKGROUND_BOTTOM);
    g2.setPaint(background);
    g2.fillRect(0, 0, getWidth(), getHeight());

    // Decorative glow
    Point center = new Point(getWidth() / 2, getHeight() / 2);
    float[] dist = {0.0f, 1.0f};
    Color[] colors = {new Color(99, 102, 241, 25), new Color(0, 0, 0, 0)};
    g2.setPaint(new java.awt.RadialGradientPaint(center, 560f, dist, colors));
    g2.fillRect(0, 0, getWidth(), getHeight());

    // Final winnings - centered prominently
    g2.setFont(amountFont);
    g2.setColor(GOLD);
    String amountText = "£" + String.format("%,d", finalWinnings);
    int amountWidth = g2.getFontMetrics().stringWidth(amountText);
    g2.drawString(amountText, (getWidth() - amountWidth) / 2, 280);

    // Subtitle below amount
    g2.setFont(subtitleFont);
    g2.setColor(TEXT_SECONDARY);
    String subtitle = failed ? "You answered incorrectly" : "Thank you for playing!";
    int subWidth = g2.getFontMetrics().stringWidth(subtitle);
    g2.drawString(subtitle, (getWidth() - subWidth) / 2, 340);

    // Center buttons vertically and horizontally
    int buttonWidth = 300;
    int buttonHeight = 50;
    int buttonSpacing = 20;
    int totalButtonsHeight = buttonHeight * 2 + buttonSpacing;
    int buttonsStartY = (getHeight() - totalButtonsHeight) / 2 + 50;

    menuButton.setBounds((getWidth() - buttonWidth) / 2, buttonsStartY, buttonWidth, buttonHeight);
    playAgainButton.setBounds((getWidth() - buttonWidth) / 2, buttonsStartY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight);

    // Draw buttons
    drawButton(g2, menuButton, "RETURN TO MENU", hoveredButton == 0);
    drawButton(g2, playAgainButton, "PLAY AGAIN", hoveredButton == 1);

    g2.dispose();
}

    private void drawButton(Graphics2D g2, Rectangle bounds, String text, boolean hover) {
        g2.setColor(hover ? BUTTON_HOVER : BUTTON_BG);
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 16, 16);
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(hover ? BUTTON_BORDER_HOVER : BUTTON_BORDER);
        g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 16, 16);

        g2.setFont(buttonFont);
        g2.setColor(hover ? TEXT_PRIMARY : GOLD);
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, bounds.x + (bounds.width - textWidth) / 2, 
                bounds.y + (bounds.height + g2.getFontMetrics().getAscent()) / 2 - 4);
    }
}