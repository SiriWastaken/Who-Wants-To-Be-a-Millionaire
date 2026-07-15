import java.awt.BorderLayout;
import javax.swing.JFrame;

/** Main window that hosts the Lock In gameplay panel. */
public class GameScreen extends JFrame {

    /** Creates the game window with default timed mode. */
    public GameScreen() {
        this(GameSession.GameMode.TIMED);
    }

    /**
     * Creates the game window with the specified game mode.
     *
     * @param mode the game mode to use (TLE (TIMED) or UNTIMED)
     */
    public GameScreen(GameSession.GameMode mode) {
        setTitle("FINAL ANSWER?");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        GameScreenPanel gamePanel = new GameScreenPanel(mode);
        MoneyLadder moneyLadder = new MoneyLadder();

        gamePanel.setMoneyLadder(moneyLadder);

        getContentPane().setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(moneyLadder, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }
}