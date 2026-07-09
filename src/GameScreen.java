import javax.swing.JFrame;
import javax.swing.JSplitPane;

/**
 * Main gameplay window frame layout managing both the interactive question board 
 * panel on the left and the money ladder graphics rendering sideboard on the right.
 */
public class GameScreen extends JFrame {

    private JSplitPane splitPane;
    private GameScreenPanel gamePanel;
    private MoneyLadder moneyLadder;
    private GameSession session;

    /**
     * Missing default no-argument constructor called by GameOverScreen's playAgain button.
     * Forwards directly to primary initializer with a fresh session context tracking instance.
     */
    public GameScreen() {
        this(new GameSession());
    }

    /**
     * Primary window initialization layout wrapper setup.
     *
     * @param session the current active game state container instance
     */
    public GameScreen(GameSession session) {
        super("Money Ladder Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760); // Set a reasonable window size for better visibility
        setLocationRelativeTo(null);
        setResizable(false);

        this.session = session;
        this.gamePanel = new GameScreenPanel(this, session);
        
        // Instantiate display sub-components
        moneyLadder = new MoneyLadder();

        // Unify split layout structure to keep Money Ladder locked onto the right sidebar
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gamePanel, moneyLadder);
        splitPane.setDividerLocation(820); // Leaves ample room for the sideboard canvas layout
        splitPane.setEnabled(false);       // Disables divider resizing mechanics
        splitPane.setBorder(null);

        add(splitPane);
    }
}