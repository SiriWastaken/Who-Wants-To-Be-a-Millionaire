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
import java.awt.geom.AffineTransform;
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

    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 54);
    private final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private final Font MONEY_FONT = new Font("SansSerif", Font.BOLD, 58);
    private final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 18);
    private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 18);

    private final Color BACKGROUND_TOP = new Color(10, 8, 28);
    private final Color BACKGROUND_BOTTOM = new Color(3, 2, 10);
    private final Color GLOW_COLOR = new Color(99, 102, 241, 15);
    private final Color ACCENT_GLOW = new Color(245, 158, 11, 20);
    private final Color CARD_BG = new Color(12, 16, 33, 235);
    private final Color CARD_BORDER = new Color(55, 65, 81, 180);
    private final Color TITLE_PRIMARY = Color.WHITE;
    private final Color TITLE_ACCENT = new Color(245, 158, 11);
    private final Color TITLE_SHADOW = new Color(168, 85, 247, 45);
    private final Color SUBTITLE_COLOR = new Color(156, 163, 175);
    private final Color BUTTON_BG = new Color(17, 24, 39, 190);
    private final Color BUTTON_BG_HOVER = new Color(30, 27, 75, 230);
    private final Color BUTTON_BORDER = new Color(55, 65, 81, 150);
    private final Color BUTTON_BORDER_HOVER = new Color(245, 158, 11);
    private final Color BUTTON_TEXT = new Color(229, 231, 235);

    private final Random RANDOM = new Random();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final Timer particleTimer;
    private final Timer fireworkTimer;
    
    private float titleGlow = 0.0f;
    private boolean titleGlowIncreasing = true;

    private final Rectangle PLAY_AGAIN_BUTTON = new Rectangle(0, 0, 210, 52);
    private final Rectangle GO_HOME_BUTTON = new Rectangle(0, 0, 210, 52);

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
        float gravity = 0.12f;
        float angle;
        float rotationSpeed;

        Particle(float x, float y, Color color, boolean isFirework) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.isFirework = isFirework;
            this.size = isFirework ? 4 + RANDOM.nextInt(4) : 8 + RANDOM.nextInt(8);
            this.maxLife = isFirework ? 35 + RANDOM.nextInt(25) : 100 + RANDOM.nextInt(60);
            this.life = maxLife;
            
            this.angle = RANDOM.nextFloat() * (float) (2 * Math.PI);
            this.rotationSpeed = (RANDOM.nextFloat() - 0.5f) * 0.2f;

            float angleVel = (float) (RANDOM.nextFloat() * 2 * Math.PI);
            float speed = isFirework ? 5 + RANDOM.nextFloat() * 6 : 1.5f + RANDOM.nextFloat() * 3.5f;
            this.vx = (float) (Math.cos(angleVel) * speed);
            this.vy = (float) (Math.sin(angleVel) * speed) - (isFirework ? 3 : 0);
        }

        void update() {
            x += vx;
            y += vy;
            vy += gravity;
            angle += rotationSpeed;
            life--;
        }

        void draw(Graphics2D g2) {
            float alpha = (float) life / maxLife;
            if (alpha < 0) alpha = 0;

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 220)));

            if (isFirework) {
                g2.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 50)));
                g2.fillOval((int) x - size, (int) y - size, size * 2, size * 2);
            } else {
                AffineTransform original = g2.getTransform();
                g2.translate(x, y);
                g2.rotate(angle);
                g2.fillRect(-size / 2, -size / 4, size, size / 2);
                g2.setColor(new Color(255, 255, 255, (int) (alpha * 120)));
                g2.drawLine(-size / 2, -size / 4, size / 2, -size / 4);
                g2.setTransform(original);
            }
        }

        boolean isDead() {
            return life <= 0;
        }
    }

    /**
     * Creates a new Win Screen panel.
     *
     * @param onPlayAgain callback when player chooses to play again (skips intro)
     * @param onGoHome callback when player chooses to go home
     */
    public WinScreenPanel(Runnable onPlayAgain, Runnable onGoHome) {
        this.onPlayAgain = onPlayAgain;
        this.onGoHome = onGoHome;

        setOpaque(false);
        setFocusable(true);
        setLayout(null);
        installListeners();

        particleTimer = new Timer(30, e -> {
            spawnConfettiChance();
            updateParticles();
            updateTitleGlow();
            repaint();
        });
        particleTimer.start();

        fireworkTimer = new Timer(500, e -> {
            spawnFirework();
        });
        fireworkTimer.start();

        for (int i = 0; i < 100; i++) {
            spawnConfetti(true);
        }
        for (int i = 0; i < 6; i++) {
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
                    if (onPlayAgain != null) {
                        onPlayAgain.run();
                    }
                } else if (GO_HOME_BUTTON.contains(p)) {
                    cleanup();
                    if (onGoHome != null) {
                        onGoHome.run();
                    }
                }
            }
        });
    }

    private void updateTitleGlow() {
        if (titleGlowIncreasing) {
            titleGlow += 0.025f;
            if (titleGlow >= 1.0f) {
                titleGlow = 1.0f;
                titleGlowIncreasing = false;
            }
        } else {
            titleGlow -= 0.025f;
            if (titleGlow <= 0.0f) {
                titleGlow = 0.0f;
                titleGlowIncreasing = true;
            }
        }
    }

    private void spawnConfettiChance() {
        if (RANDOM.nextFloat() < 0.6f) {
            spawnConfetti(false);
        }
    }

    private void spawnConfetti(boolean spreadGlobally) {
        Color[] colors = {
                new Color(239, 68, 68), new Color(34, 197, 94), new Color(59, 130, 246),
                new Color(234, 179, 8), new Color(168, 85, 247), new Color(6, 182, 212),
                new Color(249, 115, 22), new Color(244, 63, 94), new Color(245, 158, 11), Color.WHITE
        };

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float x = RANDOM.nextFloat() * width;
        float y = spreadGlobally ? RANDOM.nextFloat() * height : -15;
        Color color = colors[RANDOM.nextInt(colors.length)];
        particles.add(new Particle(x, y, color, false));
    }

    private void spawnFirework() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float x = 80 + RANDOM.nextFloat() * (width - 160);
        float y = 60 + RANDOM.nextFloat() * (height * 0.5f);

        Color[] fireworkColors = {
                new Color(251, 113, 133), new Color(74, 222, 128), new Color(96, 165, 250),
                new Color(253, 224, 71), new Color(192, 132, 252), new Color(45, 212, 191),
                new Color(251, 146, 60), new Color(245, 158, 11)
        };

        int count = 25 + RANDOM.nextInt(20);
        for (int i = 0; i < count; i++) {
            Color color = fireworkColors[RANDOM.nextInt(fireworkColors.length)];
            Particle spark = new Particle(x, y, color, true);
            spark.gravity = 0.04f + RANDOM.nextFloat() * 0.08f;
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

        if (particles.size() > 500) {
            particles.subList(0, particles.size() - 500).clear();
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

            GradientPaint bgGradient = new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM);
            g2.setPaint(bgGradient);
            g2.fillRect(0, 0, w, h);

            Point2D center = new Point2D.Float(w / 2.0f, h / 2.0f);
            float[] dist = { 0.0f, 1.0f };
            Color[] colors = { GLOW_COLOR, new Color(0, 0, 0, 0) };
            g2.setPaint(new java.awt.RadialGradientPaint(center, 600f, dist, colors));
            g2.fillRect(0, 0, w, h);

            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(ACCENT_GLOW);
            g2.drawOval(-170, -170, 540, 540);
            g2.drawOval(w - 390, h - 440, 620, 620);

            for (Particle p : particles) {
                p.draw(g2);
            }

            int cardWidth = Math.min(680, w - 80);
            int cardHeight = 430;
            int cardX = (w - cardWidth) / 2;
            int cardY = (h - cardHeight) / 2 - 10;

            int buttonY = cardY + cardHeight - 85; 
            PLAY_AGAIN_BUTTON.x = cardX + 65;
            PLAY_AGAIN_BUTTON.y = buttonY;
            GO_HOME_BUTTON.x = cardX + cardWidth - 65 - GO_HOME_BUTTON.width;
            GO_HOME_BUTTON.y = buttonY;

            g2.setColor(CARD_BG);
            g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 30, 30);

            Color animatedBorderColor = interpolateColor(CARD_BORDER, BUTTON_BORDER_HOVER, titleGlow * 0.6f);
            g2.setStroke(new BasicStroke(2.0f));
            g2.setColor(animatedBorderColor);
            g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 30, 30);

            FontMetrics fm;
            
            g2.setFont(TITLE_FONT);
            String title = "CONGRATULATIONS!";
            fm = g2.getFontMetrics();
            int titleX = (w - fm.stringWidth(title)) / 2;
            int titleY = cardY + 85;

            g2.setColor(TITLE_SHADOW);
            g2.drawString(title, titleX + 2, titleY + 2);
            g2.setColor(TITLE_PRIMARY);
            g2.drawString(title, titleX, titleY);

            g2.setFont(SUBTITLE_FONT);
            g2.setColor(SUBTITLE_COLOR);
            String subTitle = "YOU ARE A MILLIONAIRE!";
            fm = g2.getFontMetrics();
            int subY = cardY + 135;
            g2.drawString(subTitle, (w - fm.stringWidth(subTitle)) / 2, subY);

            g2.setFont(MONEY_FONT);
            String moneyText = "£1,000,000";
            fm = g2.getFontMetrics();
            int moneyX = (w - fm.stringWidth(moneyText)) / 2;
            int moneyY = cardY + 215;

            int glowIntensity = (int) (titleGlow * 35) + 15;
            g2.setColor(new Color(245, 158, 11, glowIntensity));
            for (int offset = 1; offset <= 6; offset++) {
                g2.drawString(moneyText, moneyX - offset, moneyY);
                g2.drawString(moneyText, moneyX + offset, moneyY);
                g2.drawString(moneyText, moneyX, moneyY - offset);
                g2.drawString(moneyText, moneyX, moneyY + offset);
            }

            g2.setColor(TITLE_ACCENT);
            g2.drawString(moneyText, moneyX, moneyY);

            g2.setFont(BODY_FONT);
            g2.setColor(SUBTITLE_COLOR);
            String message = "You've conquered the mountain and joined the elite club!";
            fm = g2.getFontMetrics();
            int msgY = cardY + 270;
            g2.drawString(message, (w - fm.stringWidth(message)) / 2, msgY);

            int dividerY = cardY + 305;
            g2.setStroke(new BasicStroke(1.0f));
            GradientPaint dividerGradient = new GradientPaint(
                    cardX + 80, dividerY, new Color(55, 65, 81, 0),
                    w / 2.0f, dividerY, new Color(55, 65, 81, 180),
                    true
            );
            g2.setPaint(dividerGradient);
            g2.drawLine(cardX + 80, dividerY, cardX + cardWidth - 80, dividerY);

            drawCustomButton(g2, PLAY_AGAIN_BUTTON, "PLAY AGAIN", hoverPlayAgain);
            drawCustomButton(g2, GO_HOME_BUTTON, "GO HOME", hoverGoHome);

        } finally {
            g2.dispose();
        }
    }

    private void drawCustomButton(Graphics2D g2, Rectangle r, String text, boolean isHovered) {
        if (isHovered) {
            g2.setColor(new Color(245, 158, 11, 20));
            g2.fillRoundRect(r.x - 3, r.y - 3, r.width + 6, r.height + 6, 20, 20);
        }

        g2.setColor(isHovered ? BUTTON_BG_HOVER : BUTTON_BG);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        g2.setStroke(new BasicStroke(isHovered ? 2.0f : 1.2f));
        g2.setColor(isHovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        g2.setFont(BUTTON_FONT);
        g2.setColor(isHovered ? TITLE_ACCENT : BUTTON_TEXT);
        
        FontMetrics fm = g2.getFontMetrics();
        int x = r.x + (r.width - fm.stringWidth(text)) / 2;
        int y = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, x, y);
    }

    private Color interpolateColor(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() + ratio * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + ratio * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + ratio * (c2.getBlue() - c1.getBlue()));
        int a = (int) (c1.getAlpha() + ratio * (c2.getAlpha() - c1.getAlpha()));
        return new Color(r, g, b, a);
    }
}