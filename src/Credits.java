import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Handles rendering and interaction for the styled game credits screen. */
public class Credits extends JPanel {

    private final Font titleFont = new Font("SansSerif", Font.BOLD, 46);
    private final Font subtitleFont = new Font("SansSerif", Font.BOLD, 22);
    private final Font bodyFont = new Font("SansSerif", Font.PLAIN, 16);
    private final Font italicFont = new Font("SansSerif", Font.ITALIC, 14);
    private final Font buttonFont = new Font("SansSerif", Font.BOLD, 16);

    private final Color backgroundTop = new Color(10, 8, 28);
    private final Color backgroundBottom = new Color(3, 2, 10);
    private final Color glowColor = new Color(99, 102, 241, 15);
    private final Color titlePrimary = Color.WHITE;
    private final Color titleAccent = new Color(245, 158, 11);
    private final Color subtitleColor = new Color(156, 163, 175);
    private final Color cardBg = new Color(17, 24, 39, 195);
    private final Color cardBorder = new Color(55, 65, 81, 140);

    private final Color buttonBg = new Color(17, 24, 39, 170);
    private final Color buttonBgHover = new Color(30, 27, 75, 210);
    private final Color buttonBorder = new Color(55, 65, 81, 130);
    private final Color buttonBorderHover = new Color(245, 158, 11);

    private final Rectangle goHomeButton = new Rectangle(0, 0, 180, 46);
    private boolean hoverGoHome = false;

    public Credits() {
        setPreferredSize(new Dimension(1100, 760));
        setFocusable(true);
        installListeners();
    }

    private void installListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverGoHome = goHomeButton.contains(e.getPoint());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (goHomeButton.contains(e.getPoint())) {
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

        // Calculate layout proportions
        int cardWidth = Math.min(840, w - 80);
        int cardHeight = 420; // Increased height to allow comfortable vertical breathing room
        int cardX = (w - cardWidth) / 2;
        int cardY = (h - cardHeight) / 2 - 20;

        // Position bottom menu navigation button dynamically below the card layout
        goHomeButton.x = (w - goHomeButton.width) / 2;
        goHomeButton.y = cardY + cardHeight + 30;

        // Gradient Background Paint Mesh
        GradientPaint background = new GradientPaint(0, 0, backgroundTop, 0, h, backgroundBottom);
        g2.setPaint(background);
        g2.fillRect(0, 0, w, h);

        Point2D center = new Point2D.Float(w / 2.0f, h / 2.0f);
        float[] dist = { 0.0f, 1.0f };
        Color[] colors = { glowColor, new Color(0, 0, 0, 0) };
        g2.setPaint(new java.awt.RadialGradientPaint(center, Math.max(w, h) * 0.6f, dist, colors));
        g2.fillRect(0, 0, w, h);

        // Credits Panel Container Slate
        g2.setColor(cardBg);
        g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 24, 24);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(cardBorder);
        g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 24, 24);

        // Header Text Display (Main Title above the Card)
        g2.setFont(titleFont);
        g2.setColor(titleAccent);
        drawCenteredText(g2, "Final Answer?", cardY - 40);

        // Section 1: Lead Developer
        g2.setFont(subtitleFont);
        g2.setColor(titlePrimary);
        drawCenteredText(g2, "DEVELOPMENT", cardY + 45);

        g2.setFont(bodyFont);
        g2.setColor(subtitleColor);
        drawCenteredText(g2, "Designed and Developed by Sri Ganty", cardY + 75);
        drawCenteredText(g2, "for Mr. Nucci's ICS3U - Intro to Computer Science Course Final", cardY + 100);

        // Decorative Divider Stroke Line
        g2.setColor(new Color(55, 65, 81, 100));
        g2.drawLine(cardX + 100, cardY + 130, cardX + cardWidth - 100, cardY + 130);

        // Section 2: Historical/Original Creators & Inspiration
        g2.setFont(subtitleFont);
        g2.setColor(titlePrimary);
        drawCenteredText(g2, "CREATIVE CONCEPT", cardY + 165);

        g2.setFont(bodyFont);
        g2.setColor(subtitleColor);
        // Original creators lines
        drawCenteredText(g2, "Original Game Format Concept by Celador, the BBC,", cardY + 195);
        drawCenteredText(g2, "David Briggs, Mike Whitehill, and Steven Knight.", cardY + 220);

        // Inspiration lines
        drawCenteredText(g2, "Inspiration also taken from Kaun Banega Crorepati,", cardY + 255);
        drawCenteredText(g2, "the Indian adaptation of the original game format.", cardY + 280);

        // Section 3: Legal IP Disclaimer (Perfectly shifted to the bottom section of the card)
        g2.setFont(italicFont);
        g2.setColor(new Color(107, 114, 128)); // Soft, dim gray
        drawCenteredText(g2, "\"Who Wants to Be a Millionaire\" IP owned by Sony Pictures Television.", cardY + 340);

        // Draw Home Navigation Button
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