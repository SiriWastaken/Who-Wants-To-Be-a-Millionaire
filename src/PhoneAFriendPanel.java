import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Displays the Phone a Friend lifeline with a simulated phone call interface.
 * Matches the game's dark theme and color scheme.
 */
public class PhoneAFriendPanel extends JPanel {

    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 26);
    private final Font NAME_FONT = new Font("SansSerif", Font.BOLD, 22);
    private final Font ADVICE_FONT = new Font("SansSerif", Font.ITALIC, 18);
    private final Font STATUS_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 12);

    // Match your game's colors
    private final Color BACKGROUND_TOP = new Color(10, 8, 28);
    private final Color BACKGROUND_BOTTOM = new Color(3, 2, 10);
    private final Color GLOW_COLOR = new Color(99, 102, 241, 15);
    private final Color CARD_BG = new Color(17, 24, 39, 230);
    private final Color CARD_BORDER = new Color(55, 65, 81, 200);
    private final Color PHONE_BG = new Color(17, 24, 39, 220);
    private final Color ACCENT_GOLD = new Color(245, 158, 11);
    private final Color SUCCESS = new Color(34, 197, 94);
    private final Color SUBTITLE_COLOR = new Color(156, 163, 175);

    private final String[] FRIEND_NAMES = {
        "Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Avery", "Quinn"
    };

    private final String friendName;
    private final String adviceText;
    private boolean callConnected = false;
    private boolean adviceRevealed = false;
    private int dotCount = 0;
    private final Timer animationTimer;

    /**
     * Creates a new Phone a Friend panel.
     *
     * @param question the current question to get advice on
     */
    public PhoneAFriendPanel(Question question) {
        setOpaque(false);
        setFocusable(true);

        friendName = FRIEND_NAMES[(int)(Math.random() * FRIEND_NAMES.length)];
        adviceText = "I'm fairly confident the answer is " + 
                     question.getCorrectAnswer().trim().toUpperCase() + ".";

        animationTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!callConnected) {
                    dotCount = (dotCount + 1) % 4;
                    if (dotCount == 0) {
                        callConnected = true;
                    }
                } else if (!adviceRevealed) {
                    adviceRevealed = true;
                }
                repaint();
            }
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background gradient matching your game
        GradientPaint background = new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM);
        g2.setPaint(background);
        g2.fillRoundRect(0, 0, w, h, 24, 24);

        // Background glow
        g2.setColor(GLOW_COLOR);
        g2.fillOval(w/2 - 150, -50, 300, 300);
        g2.fillOval(w/2 - 200, h - 150, 400, 400);

        // Card border
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(CARD_BORDER);
        g2.drawRoundRect(0, 0, w, h, 24, 24);

        // Title with gold accent
        g2.setFont(TITLE_FONT);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String title = "Phone a Friend";
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, 50);

        // Gold underline
        g2.setColor(ACCENT_GOLD);
        g2.fillRoundRect(w/2 - 60, 58, 120, 3, 3, 3);

        // Phone container
        int phoneX = w / 2 - 150;
        int phoneY = 85;
        int phoneW = 300;
        int phoneH = 240;

        g2.setColor(PHONE_BG);
        g2.fillRoundRect(phoneX, phoneY, phoneW, phoneH, 30, 30);
        g2.setColor(CARD_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(phoneX, phoneY, phoneW, phoneH, 30, 30);

        // Phone icon (simple)
        g2.setColor(ACCENT_GOLD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(phoneX + phoneW/2 - 20, phoneY + 10, 40, 40);
        g2.drawLine(phoneX + phoneW/2 - 15, phoneY + 55, phoneX + phoneW/2 + 15, phoneY + 55);
        g2.drawLine(phoneX + phoneW/2, phoneY + 15, phoneX + phoneW/2, phoneY + 45);

        // Friend name
        g2.setFont(NAME_FONT);
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        g2.drawString(friendName, (w - fm.stringWidth(friendName)) / 2, phoneY + 85);

        // Status
        g2.setFont(STATUS_FONT);
        if (!callConnected) {
            g2.setColor(ACCENT_GOLD);
            String status = "Calling" + ".".repeat(dotCount);
            fm = g2.getFontMetrics();
            g2.drawString(status, (w - fm.stringWidth(status)) / 2, phoneY + 120);
        } else if (!adviceRevealed) {
            g2.setColor(SUCCESS);
            String status = "Connected - Thinking" + ".".repeat(dotCount);
            fm = g2.getFontMetrics();
            g2.drawString(status, (w - fm.stringWidth(status)) / 2, phoneY + 120);
        } else {
            g2.setColor(SUCCESS);
            String status = "Connected - Advice given";
            fm = g2.getFontMetrics();
            g2.drawString(status, (w - fm.stringWidth(status)) / 2, phoneY + 120);
        }

        // Advice text (revealed after call connects)
        if (adviceRevealed) {
            g2.setFont(ADVICE_FONT);
            g2.setColor(SUBTITLE_COLOR);
            
            // Draw with "speech bubble" style - centered wrapping
            String[] words = adviceText.split(" ");
            StringBuilder line = new StringBuilder();
            int lineY = phoneY + 160;
            int maxWidth = phoneW - 50;
            int lineHeight = 30;

            for (String word : words) {
                String testLine = line.length() == 0 ? word : line + " " + word;
                if (g2.getFontMetrics().stringWidth(testLine) > maxWidth && line.length() > 0) {
                    fm = g2.getFontMetrics();
                    g2.drawString(line.toString(), (w - fm.stringWidth(line.toString())) / 2, lineY);
                    lineY += lineHeight;
                    line = new StringBuilder(word);
                } else {
                    if (line.length() > 0) line.append(' ');
                    line.append(word);
                }
            }
            if (line.length() > 0) {
                fm = g2.getFontMetrics();
                g2.drawString(line.toString(), (w - fm.stringWidth(line.toString())) / 2, lineY);
            }
        }

        // Subtitle
        g2.setFont(SUBTITLE_FONT);
        g2.setColor(SUBTITLE_COLOR);
        String subtitle = "Press ESC or click anywhere to return to the game";
        fm = g2.getFontMetrics();
        g2.drawString(subtitle, (w - fm.stringWidth(subtitle)) / 2, h - 25);
    }

    /**
     * Stops the animation and cleans up resources.
     */
    public void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    /**
     * Returns whether the advice has been revealed.
     *
     * @return true if advice is revealed
     */
    public boolean isAdviceRevealed() {
        return adviceRevealed;
    }
}