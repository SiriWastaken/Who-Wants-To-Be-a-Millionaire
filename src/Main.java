import javax.swing.SwingUtilities;

/** Entry point for the Lock In application. */
public class Main {
    /** Launches the main menu on the Swing event thread.
     *
     * @param args command-line arguments passed to the program
     * @return void
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
        
    }
}