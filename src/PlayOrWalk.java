import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * Displays the £16,000 high-stakes decision screen.
 *
 * Allows the player to either continue playing or walk away
 * with their current winnings.
 */
public class PlayOrWalk extends JPanel {

    private final GameSession session;
    private final DecisionListener listener;

    private Rectangle playButton;
    private Rectangle walkButton;

    private boolean hoverPlay;
    private boolean hoverWalk;

    public PlayOrWalk(GameSession session, DecisionListener listener) {

        this.session = session;
        this.listener = listener;

        setPreferredSize(new Dimension(900, 700));
        setBackground(new Color(6, 6, 16));

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                if (playButton != null && playButton.contains(e.getPoint())) {

                    session.chooseHighStakesDecision(true);

                    if (listener != null) {
                        listener.decisionMade(true);
                    }

                }

                else if (walkButton != null && walkButton.contains(e.getPoint())) {

                    session.chooseHighStakesDecision(false);

                    if (listener != null) {
                        listener.decisionMade(false);
                    }

                }

            }

        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {

                hoverPlay = playButton != null &&
                        playButton.contains(e.getPoint());

                hoverWalk = walkButton != null &&
                        walkButton.contains(e.getPoint());

                repaint();

            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0,
                0,
                new Color(8, 8, 25),
                0,
                getHeight(),
                new Color(20, 10, 45));

        g2.setPaint(gradient);
        g2.fillRect(
                0,
                0,
                getWidth(),
                getHeight());

        g2.setColor(new Color(245, 179, 92, 40));

        g2.fillOval(
                getWidth() / 2 - 250,
                80,
                500,
                250);

        drawCenteredText(
                g2,
                "£16,000 REACHED!",
                130,
                new Font("Serif", Font.BOLD, 60),
                new Color(246, 230, 194));

        drawCenteredText(
                g2,
                "You have secured £16,000.",
                210,
                new Font("SansSerif", Font.BOLD, 28),
                Color.WHITE);

        drawCenteredText(
                g2,
                "Do you want to risk it for the next question?",
                260,
                new Font("SansSerif", Font.PLAIN, 24),
                Color.WHITE);

        int buttonWidth = 280;
        int buttonHeight = 90;

        int center = getWidth() / 2;

        playButton = new Rectangle(
                center - buttonWidth - 30,
                370,
                buttonWidth,
                buttonHeight);

        walkButton = new Rectangle(
                center + 30,
                370,
                buttonWidth,
                buttonHeight);

        drawButton(
                g2,
                playButton,
                "PLAY ROUND",
                hoverPlay);

        drawButton(
                g2,
                walkButton,
                "WALK AWAY",
                hoverWalk);

        g2.dispose();

    }

    private void drawButton(Graphics2D g2,
            Rectangle box,
            String text,
            boolean hovered) {

        if (hovered) {

            g2.setColor(new Color(255, 170, 36));

        }

        else {

            g2.setColor(new Color(57, 146, 224));

        }

        g2.fillRoundRect(
                box.x,
                box.y,
                box.width,
                box.height,
                30,
                30);

        g2.setColor(Color.WHITE);

        g2.setStroke(new BasicStroke(3));

        g2.drawRoundRect(
                box.x,
                box.y,
                box.width,
                box.height,
                30,
                30);

        Font font = new Font("SansSerif", Font.BOLD, 28);

        g2.setFont(font);

        FontMetrics fm = g2.getFontMetrics();

        int x = box.x +
                (box.width - fm.stringWidth(text)) / 2;

        int y = box.y +
                (box.height + fm.getAscent()) / 2 - 5;

        g2.drawString(text, x, y);

    }

    private void drawCenteredText(Graphics2D g2,
            String text,
            int y,
            Font font,
            Color color) {

        g2.setFont(font);
        g2.setColor(color);

        FontMetrics fm = g2.getFontMetrics();

        int x = (getWidth() - fm.stringWidth(text)) / 2;

        g2.drawString(
                text,
                x,
                y);

    }

    public interface DecisionListener {

        void decisionMade(boolean continuePlaying);

    }

}