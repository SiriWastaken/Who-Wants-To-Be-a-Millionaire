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
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Displays an epic victory screen when the player wins £1,000,000.
 * Features confetti, fireworks, and celebratory text.
 */
public class WinScreenPanel extends JPanel {

    // Fonts matching the game's style
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 52);
    private final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private final Font MONEY_FONT = new Font("SansSerif", Font.BOLD, 48);
    private final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 18);
    private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 18);

    // Colors matching the game's theme
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

    private final Random RANDOM = new Random();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final Timer particleTimer;
    private final Timer fireworkTimer;
    private float titleGlow = 0.0f;
    private boolean titleGlowIncreasing = true;

    // Button bounds
    private final Rectangle PLAY_AGAIN_BUTTON = new Rectangle(0, 0, 200, 50);
    private final Rectangle GO_HOME_BUTTON = new Rectangle(0, 0, 200, 50);

    private boolean hoverPlayAgain = false;
    private boolean hoverGoHome = false;
    private final Runnable onPlayAgain;
    private final Runnable onGoHome;

    /**
     * Represents a single particle (confetti or firework spark).
     */
    private class Particle {
        float x, y;
        float vx, vy;
        int size;
        Color color;
        int life;
        int maxLife;
        boolean isFirework;
        float gravity = 0.15f;

        Particle(float x, float y, Color color, boolean isFirework) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.isFirework = isFirework;
            this.size = isFirework ? 4 + RANDOM.nextInt(6) : 6 + RANDOM.nextInt(10);
            this.maxLife = isFirework ? 40 + RANDOM.nextInt(30) : 80 + RANDOM.nextInt(60);
            this.life = maxLife;

            float angle = (float) (RANDOM.nextFloat() * 2 * Math.PI);
            float speed = isFirework ? 4 + RANDOM.nextFloat() * 6 : 2 + RANDOM.nextFloat() * 4;
            this.vx = (float) (Math.cos(angle) * speed);
            this.vy = (float) (Math.sin(angle) * speed) - (isFirework ? 2 : 0);
        }

        void update() {
            x += vx;
            y += vy;
            vy += gravity;
            life--;
        }

        void draw(Graphics2D g2) {
            float alpha = (float) life / maxLife;
            if (alpha < 0)
                alpha = 0;

            if (isFirework) {
                // Draw firework spark with glow
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 200)));
                g2.fillOval((int) x - size / 2, (int) y - size / 2, size, size);

                // Glow effect
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 60)));
                g2.fillOval((int) x - size, (int) y - size, size * 2, size * 2);
            } else {
                // Draw confetti
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 220)));
                g2.fillRect((int) x, (int) y, size, size / 2);
            }
        }

        boolean isDead() {
            return life <= 0;
        }
    }

    /**
     * Creates a new Win Screen panel.
     *
     * @param onPlayAgain callback when player chooses to play again
     * @param onGoHome    callback when player chooses to go home
     * @return void
     */
    public WinScreenPanel(Runnable onPlayAgain, Runnable onGoHome) {
        this.onPlayAgain = onPlayAgain;
        this.onGoHome = onGoHome;

        setOpaque(false);
        setFocusable(true);
        setLayout(null);
        installListeners();

        // Confetti timer - spawns confetti every 50ms
        particleTimer = new Timer(50, e -> {
            spawnConfetti();
            updateParticles();
            repaint();
        });
        particleTimer.start();

        // Firework timer - spawns fireworks every 400ms
        fireworkTimer = new Timer(400, e -> {
            spawnFirework();
        });
        fireworkTimer.start();

        // Spawn initial burst of particles
        for (int i = 0; i < 80; i++) {
            spawnConfetti();
        }
        for (int i = 0; i < 8; i++) {
            spawnFirework();
        }
    }

    private void installListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                hoverPlayAgain = PLAY_AGAIN_BUTTON.contains(p);
                hoverGoHome = GO_HOME_BUTTON.contains(p);
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (PLAY_AGAIN_BUTTON.contains(p)) {
                    cleanup();
                    onPlayAgain.run();
                } else if (GO_HOME_BUTTON.contains(p)) {
                    cleanup();
                    onGoHome.run();
                }
            }
        });
    }

    private void spawnConfetti() {
        Color[] colors = {
                new Color(255, 50, 50), // Red
                new Color(50, 255, 50), // Green
                new Color(50, 50, 255), // Blue
                new Color(255, 255, 50), // Yellow
                new Color(255, 50, 255), // Purple
                new Color(50, 255, 255), // Cyan
                new Color(255, 150, 50), // Orange
                new Color(255, 50, 150), // Pink
                new Color(245, 158, 11), // Gold
                new Color(255, 255, 255) // White
        };

        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0)
            return;

        float x = RANDOM.nextFloat() * width;
        float y = -10;
        Color color = colors[RANDOM.nextInt(colors.length)];
        particles.add(new Particle(x, y, color, false));
    }

    private void spawnFirework() {
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0)
            return;

        // Random position in the upper 2/3 of the screen
        float x = 50 + RANDOM.nextFloat() * (width - 100);
        float y = 50 + RANDOM.nextFloat() * (height * 0.5f);

        Color[] fireworkColors = {
                new Color(255, 50, 50), // Red
                new Color(50, 255, 50), // Green
                new Color(50, 50, 255), // Blue
                new Color(255, 255, 50), // Yellow
                new Color(255, 50, 255), // Purple
                new Color(50, 255, 255), // Cyan
                new Color(255, 150, 50), // Orange
                new Color(245, 158, 11), // Gold
                new Color(255, 255, 255) // White
        };

        // Spawn 20-40 sparks per firework
        int count = 20 + RANDOM.nextInt(20);
        for (int i = 0; i < count; i++) {
            Color color = fireworkColors[RANDOM.nextInt(fireworkColors.length)];
            Particle spark = new Particle(x, y, color, true);
            spark.gravity = 0.05f + RANDOM.nextFloat() * 0.1f;
            particles.add(spark);
        }
    }

    private void updateParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead()) {
                particles.remove(i);
            }
        }

        // Keep particle count manageable
        if (particles.size() > 600) {
            particles.subList(0, particles.size() - 600).clear();
        }
    }

    /**
     * Cleans up timers when the panel is done.
     */
    public void cleanup() {
        if (particleTimer != null && particleTimer.isRunning()) {
            particleTimer.stop();
        }
        if (fireworkTimer != null && fireworkTimer.isRunning()) {
            fireworkTimer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background gradient matching the game
            GradientPaint bgGradient = new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM);
            g2.setPaint(bgGradient);
            g2.fillRect(0, 0, w, h);

            // Background glow matching the game
            Point2D center = new Point2D.Float(w / 2.0f, h / 2.0f);
            float[] dist = { 0.0f, 1.0f };
            Color[] colors = { GLOW_COLOR, new Color(0, 0, 0, 0) };
            g2.setPaint(new java.awt.RadialGradientPaint(center, 560f, dist, colors));
            g2.fillRect(0, 0, w, h);

            // Decorative glow circles matching the game
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(ACCENT_GLOW);
            g2.drawOval(-170, -170, 540, 540);
            g2.drawOval(w - 390, h - 440, 620, 620);

            // Animated glow behind title
            if (titleGlowIncreasing) {
                titleGlow += 0.015f;
                if (titleGlow >= 1.0f)
                    titleGlowIncreasing = false;
            } else {
                titleGlow -= 0.015f;
                if (titleGlow <= 0.0f)
                    titleGlowIncreasing = true;
            }

            // Draw particles (confetti and fireworks)
            for (Particle p : particles) {
                p.draw(g2);
            }

            // Card dimensions
            int cardWidth = Math.min(650, w - 80);
            int cardHeight = 340;
            int cardX = (w - cardWidth) / 2;
            int cardY = (h - cardHeight) / 2 - 10;

            // Position buttons
            int buttonY = cardY + cardHeight - 70;
            PLAY_AGAIN_BUTTON.x = cardX + 50;
            PLAY_AGAIN_BUTTON.y = buttonY;
            GO_HOME_BUTTON.x = cardX + cardWidth - 50 - GO_HOME_BUTTON.width;
            GO_HOME_BUTTON.y = buttonY;

            // Card background (matching the game's card style)
            g2.setColor(CARD_BG);
            g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 26, 26);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 26, 26);

            // "CONGRATULATIONS!" with shadow
            g2.setFont(TITLE_FONT);
            g2.setColor(TITLE_SHADOW);
            FontMetrics fm = g2.getFontMetrics();
            String title = "CONGRATULATIONS!";
            g2.drawString(title, (w - fm.stringWidth(title)) / 2 + 2, cardY + 90);

            // Gold text
            g2.setColor(TITLE_ACCENT);
            g2.drawString(title, (w - fm.stringWidth(title)) / 2, cardY + 88);

            // "YOU ARE A MILLIONAIRE!" subtitle
            g2.setFont(SUBTITLE_FONT);
            g2.setColor(TITLE_PRIMARY);
            String subTitle = "YOU ARE A MILLIONAIRE!";
            fm = g2.getFontMetrics();
            g2.drawString(subTitle, (w - fm.stringWidth(subTitle)) / 2, cardY + 140);

            // Money display
            g2.setFont(MONEY_FONT);
            g2.setColor(TITLE_ACCENT);
            String moneyText = "£1,000,000";
            fm = g2.getFontMetrics();
            g2.drawString(moneyText, (w - fm.stringWidth(moneyText)) / 2, cardY + 205);

            // Subtext
            g2.setFont(BODY_FONT);
            g2.setColor(SUBTITLE_COLOR);
            String message = "You've beaten the game and joined an elite club!";
            fm = g2.getFontMetrics();
            g2.drawString(message, (w - fm.stringWidth(message)) / 2, cardY + 250);

            // Divider
            g2.setColor(new Color(55, 65, 81, 100));
            g2.drawLine(cardX + 80, cardY + 270, cardX + cardWidth - 80, cardY + 270);

            // Buttons
            drawCustomButton(g2, PLAY_AGAIN_BUTTON, "PLAY AGAIN", hoverPlayAgain);
            drawCustomButton(g2, GO_HOME_BUTTON, "GO HOME", hoverGoHome);

        } finally {
            g2.dispose();
        }
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
}