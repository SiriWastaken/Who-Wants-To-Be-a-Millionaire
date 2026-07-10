import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Custom dialog for displaying lifeline views.
 * Opens as a separate window and can be closed with ESC or click.
 */
public class LifelineDialog extends JDialog {

    private final JPanel lifelinePanel;

    /**
     * Creates a new LifelineDialog with the given panel.
     *
     * @param owner the parent frame
     * @param title the dialog title
     * @param panel the lifeline panel to display
     */
    public LifelineDialog(JFrame owner, String title, JPanel panel) {
        super(owner, title, true);
        this.lifelinePanel = panel;

        setUndecorated(true);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(520, 480));
        pack();
        setLocationRelativeTo(owner);

        // Close on ESC
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        // Close on click anywhere
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dispose();
            }
        });

        setFocusable(true);
        requestFocusInWindow();
    }

    @Override
    public void dispose() {
        if (lifelinePanel instanceof AudiencePollPanel) {
            ((AudiencePollPanel) lifelinePanel).stopAnimation();
        } else if (lifelinePanel instanceof PhoneAFriendPanel) {
            ((PhoneAFriendPanel) lifelinePanel).stopAnimation();
        }
        super.dispose();
    }
}