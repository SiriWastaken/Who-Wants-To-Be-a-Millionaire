import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;

public class ModeSelector extends JPanel {

    // The @FunctionalInterface annotation guarantees this can be used as a lambda
    public interface ModeSelectionListener {
        void onModeSelected(GameSession.GameMode selectedMode);

        default void onClosed() {
        }
    }

    // --- Typography (Clean & Modern) ---
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 42);
    private final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private final Font CARD_TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private final Font CARD_DESC_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);

    // --- Modern Dark Theme Palette ---
    private final Color BACKGROUND_TOP = new Color(11, 11, 22);
    private final Color BACKGROUND_BOTTOM = new Color(5, 5, 12);

    private final Color TITLE_PRIMARY = Color.WHITE;
    private final Color ACCENT_COLOR = new Color(245, 158, 11); // Warm Amber
    private final Color SUBTITLE_COLOR = new Color(148, 163, 184); // Cool Slate

    private final Color MAIN_CARD_BG = new Color(17, 24, 39, 240); // Tailored dark slate
    private final Color MAIN_CARD_BORDER = new Color(31, 41, 55, 255);

    private final Color CARD_BG_UNSELECTED = new Color(31, 41, 55, 120);
    private final Color CARD_BG_HOVER = new Color(55, 65, 81, 150);
    private final Color CARD_BG_SELECTED = new Color(245, 158, 11, 15);

    private final Color CARD_BORDER_UNSELECTED = new Color(75, 85, 99, 100);
    private final Color CARD_BORDER_HOVER = new Color(156, 163, 175, 200);
    private final Color CARD_BORDER_SELECTED = ACCENT_COLOR;

    private final Color START_BTN_BG = ACCENT_COLOR;
    private final Color START_BTN_BG_HOVER = new Color(251, 191, 36);
    private final Color START_BTN_TEXT = new Color(15, 23, 42);

    // --- Hitboxes (Structured & Dynamic) ---
    private final Rectangle timedMode = new Rectangle(0, 0, 210, 150);
    private final Rectangle untimedMode = new Rectangle(0, 0, 210, 150);
    private final Rectangle startButton = new Rectangle(0, 0, 220, 46);
    private final Rectangle closeButton = new Rectangle(0, 0, 32, 32);

    private GameSession.GameMode selectedMode = GameSession.GameMode.TIMED;
    private final ModeSelectionListener listener;

    private boolean hoveredTimedCard = false;
    private boolean hoveredUntimedCard = false;
    private boolean hoveredStartBtn = false;
    private boolean hoveredCloseBtn = false;

    public ModeSelector(ModeSelectionListener listener) {
        this.listener = listener;
        setFocusable(true);
        setOpaque(false);
        installListeners();
    }

    private void installListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point point = e.getPoint();
                boolean hTimed = timedMode.contains(point);
                boolean hUntimed = untimedMode.contains(point);
                boolean hStart = startButton.contains(point);
                boolean hClose = closeButton.contains(point);

                if (hTimed != hoveredTimedCard || hUntimed != hoveredUntimedCard ||
                        hStart != hoveredStartBtn || hClose != hoveredCloseBtn) {
                    hoveredTimedCard = hTimed;
                    hoveredUntimedCard = hUntimed;
                    hoveredStartBtn = hStart;
                    hoveredCloseBtn = hClose;
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                if (closeButton.contains(point)) {
                    // Hide this selector first
                    setVisible(false);

                    // Alert the parent frame immediately to bring back the Main Menu
                    if (listener != null) {
                        listener.onClosed();
                    }
                } else if (timedMode.contains(point)) {
                    selectedMode = GameSession.GameMode.TIMED;
                    repaint();
                } else if (untimedMode.contains(point)) {
                    selectedMode = GameSession.GameMode.UNTIMED;
                    repaint();
                } else if (startButton.contains(point)) {
                    if (listener != null) {
                        listener.onModeSelected(selectedMode);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredTimedCard = false;
                hoveredUntimedCard = false;
                hoveredStartBtn = false;
                hoveredCloseBtn = false;
                repaint();
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

        // 1. Sleek Gradient Background
        g2.setPaint(new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM));
        g2.fillRect(0, 0, w, h);

        // 2. Dynamic Layout Math (Centered & Spaced)
        int cardWidth = 560;
        int cardHeight = 320;
        int cardX = (w - cardWidth) / 2;
        int cardY = (h - cardHeight) / 2 + 30;

        int titleY = cardY - 60;

        timedMode.setBounds(cardX + 45, cardY + 50, timedMode.width, timedMode.height);
        untimedMode.setBounds(cardX + cardWidth - 45 - untimedMode.width, cardY + 50, untimedMode.width,
                untimedMode.height);
        startButton.setBounds(cardX + (cardWidth - startButton.width) / 2, cardY + cardHeight - 75, startButton.width,
                startButton.height);
        closeButton.setBounds(cardX + cardWidth - 44, cardY + 16, closeButton.width, closeButton.height);

        // 3. Crisp Unified Header
        g2.setFont(TITLE_FONT);
        g2.setColor(TITLE_PRIMARY);
        drawCentered(g2, "FINAL ANSWER?", titleY);

        g2.setFont(SUBTITLE_FONT);
        g2.setColor(SUBTITLE_COLOR);
        drawCentered(g2, "Choose your ruleset to begin", titleY + 28);

        // 4. Main Container Card
        g2.setColor(MAIN_CARD_BG);
        g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 24, 24);

        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(MAIN_CARD_BORDER);
        g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 24, 24);

        // 5. Draw Selection Cards
        drawModeCard(g2, timedMode, "⏱", "Timed Mode", "Race against the countdown.",
                selectedMode == GameSession.GameMode.TIMED, hoveredTimedCard);
        drawModeCard(g2, untimedMode, "♾", "Untimed Mode", "Take all the time you need.",
                selectedMode == GameSession.GameMode.UNTIMED, hoveredUntimedCard);

        // 6. Flat Start Button (Modern Pill Shape)
        g2.setColor(hoveredStartBtn ? START_BTN_BG_HOVER : START_BTN_BG);
        g2.fillRoundRect(startButton.x, startButton.y, startButton.width, startButton.height, 24, 24);

        g2.setFont(BUTTON_FONT);
        g2.setColor(START_BTN_TEXT);
        drawCenteredInRect(g2, "START GAME", startButton);

        // 7. Standardized Minimalist Close Button (Clean 'X')
        if (hoveredCloseBtn) {
            g2.setColor(new Color(239, 68, 68, 40));
            g2.fillOval(closeButton.x, closeButton.y, closeButton.width, closeButton.height);
        }

        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(hoveredCloseBtn ? Color.WHITE : SUBTITLE_COLOR);

        int cx = closeButton.x + closeButton.width / 2;
        int cy = closeButton.y + closeButton.height / 2;
        int offset = 6;
        g2.drawLine(cx - offset, cy - offset, cx + offset, cy + offset);
        g2.drawLine(cx + offset, cy - offset, cx - offset, cy + offset);
    }

    private void drawModeCard(Graphics2D g2, Rectangle r, String icon, String title, String description,
            boolean selected, boolean hover) {

        Color bgColor = selected ? CARD_BG_SELECTED : (hover ? CARD_BG_HOVER : CARD_BG_UNSELECTED);
        Color borderColor = selected ? CARD_BORDER_SELECTED : (hover ? CARD_BORDER_HOVER : CARD_BORDER_UNSELECTED);
        float borderStroke = selected ? 2.0f : 1.2f;

        g2.setColor(bgColor);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);
        g2.setStroke(new BasicStroke(borderStroke));
        g2.setColor(borderColor);
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        FontMetrics fmIcon = g2.getFontMetrics(new Font("SansSerif", Font.PLAIN, 32));
        FontMetrics fmTitle = g2.getFontMetrics(CARD_TITLE_FONT);
        FontMetrics fmDesc = g2.getFontMetrics(CARD_DESC_FONT);

        int spacing = 12;
        int totalHeight = fmIcon.getAscent() + spacing + fmTitle.getAscent() + spacing + fmDesc.getAscent();
        int currentY = r.y + (r.height - totalHeight) / 2 + fmIcon.getAscent() - 5;

        g2.setFont(new Font("SansSerif", Font.PLAIN, 32));
        g2.setColor(selected ? ACCENT_COLOR : TITLE_PRIMARY);
        drawCenteredInRectX(g2, icon, r, currentY);

        currentY += fmTitle.getAscent() + spacing + 10;
        g2.setFont(CARD_TITLE_FONT);
        g2.setColor(selected ? ACCENT_COLOR : TITLE_PRIMARY);
        drawCenteredInRectX(g2, title, r, currentY);

        currentY += fmDesc.getAscent() + spacing;
        g2.setFont(CARD_DESC_FONT);
        g2.setColor(SUBTITLE_COLOR);
        drawCenteredInRectX(g2, description, r, currentY);
    }

    private void drawCentered(Graphics2D g2, String text, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, y);
    }

    private void drawCenteredInRect(Graphics2D g2, String text, Rectangle r) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, r.x + (r.width - fm.stringWidth(text)) / 2,
                r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent());
    }

    private void drawCenteredInRectX(Graphics2D g2, String text, Rectangle r, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, r.x + (r.width - fm.stringWidth(text)) / 2, y);
    }
}