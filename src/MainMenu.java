import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import javax.swing.*;

/**
 * MainMenu manages the main window frame for the Millionaire application.
 * It houses the primary custom drawing canvas (MenuPanel).
 */
public class MainMenu extends JFrame {

    /** Creates the main menu window and installs the menu panel. */
    public MainMenu() {
        setTitle("FINAL ANSWER?");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        add(new MenuPanel());
    }

    /**
     * MenuPanel handles the custom rendering of the user interface backgrounds,
     * decorative visual geometry, fonts, titles, and dynamic text buttons.
     */
    class MenuPanel extends JPanel {
        private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 52);
        private final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 15);
        private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 18);

        private final Color BACKGROUND_TOP = new Color(10, 8, 28);
        private final Color BACKGROUND_BOTTOM = new Color(3, 2, 10);
        private final Color GLOW_COLOR = new Color(99, 102, 241, 25);
        private final Color ACCENT_GLOW = new Color(245, 158, 11, 18);
        private final Color TITLE_PRIMARY = Color.WHITE;
        private final Color TITLE_ACCENT = new Color(245, 158, 11);
        private final Color TITLE_SHADOW = new Color(168, 85, 247, 40);
        private final Color SUBTITLE_COLOR = new Color(156, 163, 175);
        private final Color BUTTON_BG = new Color(17, 24, 39, 150);
        private final Color BUTTON_BG_HOVER = new Color(30, 27, 75, 200);
        private final Color BUTTON_BORDER = new Color(55, 65, 81, 100);
        private final Color BUTTON_BORDER_HOVER = new Color(245, 158, 11);
        private final Color BUTTON_TEXT = new Color(209, 213, 219);

        // --- Adjusted Button Bounds for 3 buttons (removed Settings) ---
        private final Rectangle play = new Rectangle(300, 350, 300, 50);
        private final Rectangle credits = new Rectangle(300, 420, 300, 50);
        private final Rectangle quit = new Rectangle(300, 490, 300, 50);

        // Track currently hovered menu item
        private String hovered = "";

        /**
         * Constructor initializing input listeners for interaction and hit detection.
         */
        MenuPanel() {
            setFocusable(true);

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    handleMouseMoved(e.getPoint());
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMousePressed(e.getPoint());
                }
            });
        }

        /** Updates the hover state based on the current pointer position. */
        private void handleMouseMoved(Point point) {
            if (play.contains(point)) {
                hovered = "PLAY";
            } else if (credits.contains(point)) {
                hovered = "CREDITS";
            } else if (quit.contains(point)) {
                hovered = "QUIT";
            } else {
                hovered = "";
            }
            repaint();
        }

        /** Routes a press event to the selected menu action immediately. */
        private void handleMousePressed(Point point) {
            if (play.contains(point)) {
                // Instantly inject and run the cinematic intro animation panel first!
                IntroCutscene introView = new IntroCutscene(() -> {
                    // Callback loop triggers instantly when the cutscene completes or gets skipped
                    GameScreen game = new GameScreen();
                    game.setVisible(true);
                    MainMenu.this.dispose();
                });

                MainMenu.this.getContentPane().removeAll();
                MainMenu.this.add(introView);
                MainMenu.this.revalidate();
                MainMenu.this.repaint();
                introView.requestFocusInWindow();
            } else if (credits.contains(point)) {
                Credits creditsView = new Credits();
                MainMenu.this.getContentPane().removeAll();
                MainMenu.this.add(creditsView);
                MainMenu.this.revalidate();
                MainMenu.this.repaint();
            } else if (quit.contains(point)) {
                MainMenu.this.dispose();
                System.exit(0);
            }
        }

        /**
         * Overridden graphics layer painting all custom colors, visual assets,
         * and font strings cleanly onto the panel canvas.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            GradientPaint background = new GradientPaint(
                    0, 0, BACKGROUND_TOP,
                    0, getHeight(), BACKGROUND_BOTTOM);
            g2.setPaint(background);
            g2.fillRect(0, 0, getWidth(), getHeight());

            Point2D center = new Point2D.Float(getWidth() / 2.0f, getHeight() / 2.0f);
            float radius = 500f;
            float[] dist = { 0.0f, 1.0f };
            Color[] colors = { GLOW_COLOR, new Color(0, 0, 0, 0) };
            RadialGradientPaint glow = new RadialGradientPaint(center, radius, dist, colors);
            g2.setPaint(glow);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setStroke(new BasicStroke(1f));
            g2.setColor(ACCENT_GLOW);
            g2.drawOval(-200, -200, 600, 600);
            g2.drawOval(getWidth() - 400, getHeight() - 400, 600, 600);

            g2.setFont(TITLE_FONT);

            g2.setColor(TITLE_SHADOW);
            drawCentered(g2, "FINAL ANSWER?", 172);

            g2.setColor(TITLE_PRIMARY);
            drawCentered(g2, "FINAL ANSWER?", 170);

            FontMetrics titleMetrics = g2.getFontMetrics();
            int titleWidth = titleMetrics.stringWidth("FINAL ANSWER?");
            int titleX = (getWidth() - titleWidth) / 2;
            g2.setColor(TITLE_ACCENT);
            g2.fillRoundRect(titleX + 40, 188, titleWidth - 80, 4, 4, 4);

            g2.setFont(SUBTITLE_FONT);
            g2.setColor(SUBTITLE_COLOR);
            drawCentered(g2, "ONE MILLION REASONS TO PLAY", 255);

            // --- Draw Navigation Buttons ---
            drawButton(g2, play, "PLAY", hovered.equals("PLAY"));
            drawButton(g2, credits, "CREDITS", hovered.equals("CREDITS"));
            drawButton(g2, quit, "QUIT", hovered.equals("QUIT"));
        }

        private void drawButton(Graphics2D g2, Rectangle r, String text, boolean hover) {
            if (hover) {
                g2.setColor(BUTTON_BG_HOVER);
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);

                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(BUTTON_BORDER_HOVER);
                g2.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);
            } else {
                g2.setColor(BUTTON_BG);
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);

                g2.setStroke(new BasicStroke(1f));
                g2.setColor(BUTTON_BORDER);
                g2.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);
            }

            g2.setColor(hover ? TITLE_PRIMARY : BUTTON_TEXT);
            g2.setFont(BUTTON_FONT);
            FontMetrics fm = g2.getFontMetrics();
            int x = r.x + (r.width - fm.stringWidth(text)) / 2;
            int y = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();

            g2.drawString(text, x, y);
        }

        private void drawCentered(Graphics2D g2, String text, int y) {
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            g2.drawString(text, x, y);
        }
    }
}