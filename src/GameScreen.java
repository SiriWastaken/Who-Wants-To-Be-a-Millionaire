import java.awt.BorderLayout;
import javax.swing.JFrame;

/** Main window that hosts the Lock In gameplay panel. */
public class GameScreen extends JFrame {

    /** Creates the game window and installs the gameplay panel.
     *
     * @param none no parameters are required
     * @return void
     */
    public GameScreen() {
        setTitle("LOCK IN");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        GameScreenPanel gamePanel = new GameScreenPanel();
        MoneyLadder moneyLadder = new MoneyLadder();

        gamePanel.setMoneyLadder(moneyLadder);

        getContentPane().setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(moneyLadder, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }
}
