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
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Celebration screen shown when the player wins one million pounds.
 * Features animated confetti, glowing text, and celebration effects.
 */
public class WinScreen extends JPanel {

    // Background and color scheme
    private static final Color BACKGROUND_TOP = new Color(6, 6, 16);
    private static final Color BACKGROUND_BOTTOM = new Color(18, 12, 36);
    private static final Color GOLD = new Color(245, 158, 11);
    private static final Color GOLD_BRIGHT = new Color(255, 215, 0);
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color BUTTON_BG = new Color(17, 24, 39, 170);
    private static final Color BUTTON_HOVER = new Color(30, 27, 75, 210);
    private static final Color BUTTON_BORDER = new Color(55, 65, 81, 130);
    private static final Color BUTTON_BORDER_HOVER = new Color(245, 158, 11);

    private final Font titleFont = new Font("SansSerif", Font.BOLD, 42);
    private final Font millionFont = new Font("SansSerif", Font.BOLD, 56);
    private final Font subtitleFont = new Font("SansSerif", Font.PLAIN, 24);
    private final Font buttonFont = new Font("SansSerif", Font.BOLD, 18);

    private final Rectangle menuButton = new Rectangle(350, 520, 300, 50);
    private final Rectangle playAgainButton = new Rectangle(350, 590, 300, 50);

    private int hoveredButton = -1;
    private final ArrayList<ConfettiPiece> confetti = new ArrayList<>();
    private final Random random = new Random();
    private Timer animationTimer;
    private long startTime;
    private int textGlowPhase = 0;

    /**
     * Creates the million pound win celebration screen.
     */
    public WinScreen() {
        setPreferredSize(new Dimension(1100, 760));
        setLayout(null);
        setFocusable(true);

        // Initialize confetti
        for (int i = 0; i < 150; i++) {
            confetti.add(new ConfettiPiece(random));
        }

        startTime = System.currentTimeMillis();

        installListeners();
        startAnimation();
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

    private void startAnimation() {
        animationTimer = new Timer(30, e -> {
            textGlowPhase = (textGlowPhase + 5) % 360;
            for (ConfettiPiece piece : confetti) {
                piece.update();
            }
            repaint();
        });
        animationTimer.start();
    }

    private void returnToMenu() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
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
        if (animationTimer != null) {
            animationTimer.stop();
        }
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

        // Draw confetti
        for (ConfettiPiece piece : confetti) {
            piece.draw(g2);
        }

        // Decorative glow
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {new Color(245, 158, 11, 40), new Color(0, 0, 0, 0)};
        g2.setPaint(new java.awt.RadialGradientPaint(center, 560f, dist, colors));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Calculate text glow effect
        float glowIntensity = (float) (0.5 + 0.5 * Math.sin(Math.toRadians(textGlowPhase)));
        int glowSize = 10 + (int) (glowIntensity * 20);

        // "YOU'VE JUST WON" text
        g2.setFont(titleFont);
        g2.setColor(new Color(245, 158, 11, 100 + (int) (glowIntensity * 155)));
        String line1 = "YOU'VE JUST WON";
        int line1Width = g2.getFontMetrics().stringWidth(line1);
        g2.drawString(line1, (getWidth() - line1Width) / 2, 150);

        // "ONE MILLION POUNDS!" text with glow
        g2.setFont(millionFont);
        // Outer glow
        for (int i = glowSize; i > 0; i -= 3) {
            int alpha = 20 + (int) (glowIntensity * 30);
            g2.setColor(new Color(255, 215, 0, alpha));
            g2.drawString("ONE MILLION", (getWidth() - g2.getFontMetrics().stringWidth("ONE MILLION")) / 2 + random.nextInt(2), 
                    240 + random.nextInt(2));
        }
        // Main text
        g2.setColor(GOLD_BRIGHT);
        String millionText = "ONE MILLION";
        int millionWidth = g2.getFontMetrics().stringWidth(millionText);
        g2.drawString(millionText, (getWidth() - millionWidth) / 2, 240);

        g2.setColor(GOLD);
        String poundsText = "POUNDS!";
        int poundsWidth = g2.getFontMetrics().stringWidth(poundsText);
        g2.drawString(poundsText, (getWidth() - poundsWidth) / 2, 310);

        // Subtitle
        g2.setFont(subtitleFont);
        g2.setColor(new Color(156, 163, 175, 150 + (int) (glowIntensity * 105)));
        String subtitle = "Congratulations! You've cleared the entire board!";
        int subWidth = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (getWidth() - subWidth) / 2, 380);

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

    /**
     * Simple confetti piece for celebration animation.
     */
    private class ConfettiPiece {
        float x, y;
        float vx, vy;
        Color color;
        float size;
        float rotation;
        float rotationSpeed;

        ConfettiPiece(Random rand) {
            reset(rand);
            y = rand.nextInt(800) - 800; // Start above screen
        }

        void reset(Random rand) {
            x = rand.nextInt(1100);
            y = -20 - rand.nextInt(100);
            vx = (rand.nextFloat() - 0.5f) * 3;
            vy = 1 + rand.nextFloat() * 3;
            size = 5 + rand.nextFloat() * 10;
            rotation = rand.nextFloat() * 360;
            rotationSpeed = (rand.nextFloat() - 0.5f) * 10;

            // Gold and celebratory colors
            Color[] colors = {
                GOLD, GOLD_BRIGHT,
                new Color(255, 100, 0), new Color(255, 200, 100),
                new Color(255, 215, 0, 200), new Color(255, 255, 100)
            };
            color = colors[rand.nextInt(colors.length)];
        }

        void update() {
            x += vx;
            y += vy;
            rotation += rotationSpeed;
            vx += (Math.sin(rotation * 0.1f) * 0.1f);

            // Reset when off screen
            if (y > 800 || x < -50 || x > 1150) {
                reset(random);
            }
        }

        void draw(Graphics2D g2) {
            Graphics2D g2d = (Graphics2D) g2.create();
            g2d.translate(x, y);
            g2d.rotate(Math.toRadians(rotation));
            g2d.setColor(color);
            g2d.fillRoundRect((int) (-size / 2), (int) (-size / 4), (int) size, (int) (size / 2), 2, 2);
            g2d.dispose();
        }
    }
}