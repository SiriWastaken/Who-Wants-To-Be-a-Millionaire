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
        setSize(1100, 760);
        setLocationRelativeTo(null);
        setResizable(false);
        add(new GameScreenPanel());
    }
}
