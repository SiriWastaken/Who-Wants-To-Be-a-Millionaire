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

    // Scaled-down fonts to ensure perfect fit on smaller resolutions
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 38);
    private final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 18);
    private final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private final Font ITALIC_FONT = new Font("SansSerif", Font.ITALIC, 12);
    private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);

    private final Color BACKGROUND_TOP = new Color(10, 8, 28);
    private final Color BACKGROUND_BOTTOM = new Color(3, 2, 10);
    private final Color GLOW_COLOR = new Color(99, 102, 241, 15);
    private final Color TITLE_PRIMARY = Color.WHITE;
    private final Color TITLE_ACCENT = new Color(245, 158, 11);
    private final Color SUBTITLE_COLOR = new Color(156, 163, 175);
    private final Color CARD_BG = new Color(17, 24, 39, 195);
    private final Color CARD_BORDER = new Color(55, 65, 81, 140);

    private final Color BUTTON_BG = new Color(17, 24, 39, 170);
    private final Color BUTTON_BG_HOVER = new Color(30, 27, 75, 210);
    private final Color BUTTON_BORDER = new Color(55, 65, 81, 130);
    private final Color BUTTON_BORDER_HOVER = new Color(245, 158, 11);

    private final Rectangle GO_HOME_BUTTON = new Rectangle(0, 0, 180, 42);
    private boolean HOVER_GO_HOME = false;

    public Credits() {
        setPreferredSize(new Dimension(1100, 760));
        setFocusable(true);
        installListeners();
    }

    private void installListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                HOVER_GO_HOME = GO_HOME_BUTTON.contains(e.getPoint());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (GO_HOME_BUTTON.contains(e.getPoint())) {
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

        // Responsive Card Dimensions 
        int cardWidth = Math.min(840, w - 80);
        int cardHeight = 440; // Reduced height to keep layout compact
        
        // Dynamically scale vertical positioning relative to screen height
        int titleY = Math.max(50, (int) (h * 0.10));
        int cardY = titleY + 40; 
        int cardX = (w - cardWidth) / 2; // Fixed: Now explicitly declared and in scope

        // Position bottom menu navigation button dynamically below the card layout
        GO_HOME_BUTTON.x = (w - GO_HOME_BUTTON.width) / 2;
        GO_HOME_BUTTON.y = cardY + cardHeight + 20;

        // Gradient Background Paint Mesh
        GradientPaint background = new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM);
        g2.setPaint(background);
        g2.fillRect(0, 0, w, h);

        Point2D center = new Point2D.Float(w / 2.0f, h / 2.0f);
        float[] dist = { 0.0f, 1.0f };
        Color[] colors = { GLOW_COLOR, new Color(0, 0, 0, 0) };
        g2.setPaint(new java.awt.RadialGradientPaint(center, Math.max(w, h) * 0.6f, dist, colors));
        g2.fillRect(0, 0, w, h);

        // Credits Panel Container Slate
        g2.setColor(CARD_BG);
        g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 20, 20);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 20, 20);

        // Header Text Display (Main Title above the Card)
        g2.setFont(TITLE_FONT);
        g2.setColor(TITLE_ACCENT);
        drawCenteredText(g2, "Final Answer?", titleY);

        // Section 1: Lead Developer & Design
        g2.setFont(SUBTITLE_FONT);
        g2.setColor(TITLE_PRIMARY);
        drawCenteredText(g2, "DEVELOPMENT & VISUAL DESIGN", cardY + 35);

        g2.setFont(BODY_FONT);
        g2.setColor(SUBTITLE_COLOR);
        drawCenteredText(g2, "Game Design, Programming, and Logo Art by Sri Ganty", cardY + 60);
        drawCenteredText(g2, "for Mr. Nucci's ICS3U - Intro to Computer Science Course Final", cardY + 82);

        // Decorative Divider Stroke Line 1
        g2.setColor(new Color(55, 65, 81, 80));
        g2.drawLine(cardX + 120, cardY + 105, cardX + cardWidth - 120, cardY + 105);

        // Section 2: Historical/Original Creators & Inspiration
        g2.setFont(SUBTITLE_FONT);
        g2.setColor(TITLE_PRIMARY);
        drawCenteredText(g2, "CREATIVE CONCEPT & INSPIRATION", cardY + 135);

        g2.setFont(BODY_FONT);
        g2.setColor(SUBTITLE_COLOR);
        drawCenteredText(g2, "Original Game Format Concept by Celador, the BBC,", cardY + 160);
        drawCenteredText(g2, "David Briggs, Mike Whitehill, and Steven Knight.", cardY + 182);
        drawCenteredText(g2, "Inspired by Kaun Banega Crorepati (the Indian adaptation).", cardY + 204);

        // Decorative Divider Stroke Line 2
        g2.setColor(new Color(55, 65, 81, 80));
        g2.drawLine(cardX + 120, cardY + 225, cardX + cardWidth - 120, cardY + 225);

        // Section 3: Audio Design / Soundtrack
        g2.setFont(SUBTITLE_FONT);
        g2.setColor(TITLE_PRIMARY);
        drawCenteredText(g2, "SOUNDTRACK", cardY + 255);

        g2.setFont(BODY_FONT);
        g2.setColor(SUBTITLE_COLOR);
        drawCenteredText(g2, "Iconic Game Theme and Audio Scoring Composed by", cardY + 280);
        drawCenteredText(g2, "Keith Strachan and Matthew Strachan.", cardY + 302);

        // Section 4: Legal IP Disclaimer (Anchored near the bottom inside-edge of the card)
        g2.setFont(ITALIC_FONT);
        g2.setColor(new Color(107, 114, 128)); 
        drawCenteredText(g2, "\"Who Wants to Be a Millionaire\" IP owned by Sony Pictures Television.", cardY + cardHeight - 25);

        // Draw Home Navigation Button
        drawCustomButton(g2, GO_HOME_BUTTON, "GO HOME", HOVER_GO_HOME);
    }

    private void drawCustomButton(Graphics2D g2, Rectangle r, String text, boolean isHovered) {
        g2.setColor(isHovered ? BUTTON_BG_HOVER : BUTTON_BG);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(isHovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        g2.setFont(BUTTON_FONT);
        g2.setColor(isHovered ? TITLE_PRIMARY : TITLE_ACCENT);
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