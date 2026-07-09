import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Handles gameplay input, timers, and small UI state for the game screen. */
public class GameScreenPanel extends JPanel {

    private final GameSession session = new GameSession();
    private final GameScreenRenderer renderer = new GameScreenRenderer();

    private MoneyLadder moneyLadder;

    private final Rectangle backButton = new Rectangle(34, 26, 128, 38);
    private final Rectangle[] answerBounds = {
            new Rectangle(90, 390, 420, 88),
            new Rectangle(590, 390, 420, 88),
            new Rectangle(90, 495, 420, 88),
            new Rectangle(590, 495, 420, 88)
    };
    private final Rectangle[] lifelineBounds = {
            new Rectangle(90, 620, 200, 42),
            new Rectangle(305, 620, 200, 42),
            new Rectangle(520, 620, 200, 42),
            new Rectangle(735, 620, 200, 42)
    };
    private final boolean[] answerLocks = new boolean[4];
    private final Timer countdownTimer;
    private int hoveredAnswerIndex = -1;
    private int hoveredLifelineIndex = -1;
    private boolean hoveringBack;
    private int lastQuestionSerial = -1;
    private boolean completionDialogShowing;
    private boolean answerAnimationRunning = false;
    private int selectedAnswer = -1;
    private int flashCount = 0;
    private boolean flashState = false;
    private boolean lastAnswerCorrect = false;

    /** Creates the gameplay panel and starts the countdown timer. */
    public GameScreenPanel() {
        setFocusable(true);
        setPreferredSize(new Dimension(1100, 760));
        countdownTimer = new Timer(1000, event -> onTick());
        installListeners();
        syncQuestionState();
        if (!session.isFinished()) {
            countdownTimer.start();
        }
    }

    /**
     * * Registers the MoneyLadder reference so this panel can push live step
     * highlights.
     * * @param ladder the instantiated MoneyLadder UI instance
     */
    public void setMoneyLadder(MoneyLadder ladder) {
        this.moneyLadder = ladder;
        // Seed initial level placement matching active state
        if (this.moneyLadder != null) {
            this.moneyLadder.setCurrentMoney(session.getScore());
        }
    }

    /**
     * Returns the active game session used by the renderer.
     *
     * @param none no parameters are required
     * @return the active game session used by the renderer
     */
    GameSession getSession() {
        return session;
    }

    /**
     * Returns the menu-button bounds used for hit testing and drawing.
     *
     * @param none no parameters are required
     * @return the menu-button bounds used for hit testing and drawing
     */
    Rectangle getBackButtonBounds() {
        return backButton;
    }

    /**
     * Returns the four answer-button bounds used for hit testing and drawing.
     *
     * @param none no parameters are required
     * @return the four answer-button bounds used for hit testing and drawing
     */
    Rectangle[] getAnswerBounds() {
        return answerBounds;
    }

    /**
     * Returns the lifeline-button bounds used for hit testing and drawing.
     *
     * @param none no parameters are required
     * @return the lifeline-button bounds used for hit testing and drawing
     */
    Rectangle[] getLifelineBounds() {
        return lifelineBounds;
    }

    /**
     * Returns whether the cursor is over the menu button.
     *
     * @param none no parameters are required
     * @return true when the cursor is over the menu button, otherwise false
     */
    boolean isHoveringBack() {
        return hoveringBack;
    }

    /**
     * Returns the currently hovered answer index, or -1 when none is hovered.
     *
     * @param none no parameters are required
     * @return the currently hovered answer index, or -1 when none is hovered
     */
    int getHoveredAnswerIndex() {
        return hoveredAnswerIndex;
    }

    /**
     * Returns the currently hovered lifeline index, or -1 when none is hovered.
     *
     * @param none no parameters are required
     * @return the currently hovered lifeline index, or -1 when none is hovered
     */
    int getHoveredLifelineIndex() {
        return hoveredLifelineIndex;
    }

    /**
     * Returns whether the answer at the given index is currently locked.
     *
     * @param index the answer slot to inspect
     * @return true when the answer is locked, otherwise false
     */
    boolean isAnswerLocked(int index) {
        return answerLocks[index];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.paint((Graphics2D) g, this);
    }

    /**
     * Installs mouse listeners for hover state, single-press answer selection, and
     * navigation.
     *
     * @param none no parameters are required
     * @return void
     */
    private void installListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                handleMouseMoved(event.getPoint());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                handleMousePressed(event.getPoint());
            }
        });
    }

    boolean isAnswerAnimationRunning() {
        return answerAnimationRunning;
    }

    int getSelectedAnswer() {
        return selectedAnswer;
    }

    boolean getFlashState() {
        return flashState;
    }

    boolean wasLastAnswerCorrect() {
        return lastAnswerCorrect;
    }

    /**
     * Handles mouse-move events and refreshes hover feedback.
     *
     * @param point the pointer location to evaluate
     * @return void
     */
    private void handleMouseMoved(Point point) {
        updateHoverState(point);
    }

    /**
     * Handles mouse-press events and routes them through the click dispatcher.
     *
     * @param point the pointer location to evaluate
     * @return void
     */
    private void handleMousePressed(Point point) {
        handleClick(point);
    }

    /**
     * Routes a pointer press to the back button, answer grid, or lifelines.
     *
     * @param point the pointer location to evaluate
     * @return void
     */
    private void handleClick(Point point) {
        if (backButton.contains(point)) {
            returnToMenu();
            return;
        }

        if (session.isFinished() || session.getCurrentQuestion() == null) {
            return;
        }

        for (int i = 0; i < answerBounds.length; i++) {
            if (answerBounds[i].contains(point) && !answerLocks[i]) {
                beginAnswerAnimation(i);
                return;
            }
        }

        for (int i = 0; i < lifelineBounds.length; i++) {
            if (lifelineBounds[i].contains(point)) {
                useLifeline(i);
                syncQuestionState();
                return;
            }
        }
    }

    /**
     * Recomputes hover state so the renderer can highlight the current target.
     *
     * @param point the pointer location to evaluate
     * @return void
     */
    private void updateHoverState(Point point) {
        hoveringBack = backButton.contains(point);

        hoveredAnswerIndex = -1;
        for (int i = 0; i < answerBounds.length; i++) {
            if (answerBounds[i].contains(point)) {
                hoveredAnswerIndex = i;
                break;
            }
        }

        hoveredLifelineIndex = -1;
        for (int i = 0; i < lifelineBounds.length; i++) {
            if (lifelineBounds[i].contains(point)) {
                hoveredLifelineIndex = i;
                break;
            }
        }

        repaint();
    }

    /**
     * Advances the countdown and refreshes the display.
     *
     * @param none no parameters are required
     * @return void
     */
    private void onTick() {
        session.tick();
        syncQuestionState();
    }

    /**
     * Dispatches lifeline actions by button index.
     *
     * @param index the lifeline button index
     * @return void
     */
    private void useLifeline(int index) {
        switch (index) {
            case 0 -> useSwapLifeline();
            case 1 -> useAudiencePollLifeline();
            case 2 -> useFiftyFiftyLifeline();
            case 3 -> usePhoneAFriendLifeline();
            default -> {
            }
        }
    }

    /**
     * Uses Swap and clears any answer locks so the replacement question is
     * interactive.
     *
     * @param none no parameters are required
     * @return void
     */
    private void useSwapLifeline() {
        if (!session.useSwap()) {
            JOptionPane.showMessageDialog(this, "Swap is unavailable right now.");
            return;
        }
        Arrays.fill(answerLocks, false);
        JOptionPane.showMessageDialog(this, "Swap used. The current question has been refreshed.");
    }

    /**
     * Shows a lightweight audience-poll summary for the active question.
     *
     * @param none no parameters are required
     * @return void
     */
    private void useAudiencePollLifeline() {
        if (!session.useAudiencePoll()) {
            JOptionPane.showMessageDialog(this, "Audience Poll has already been used.");
            return;
        }

        Question question = session.getCurrentQuestion();
        if (question == null) {
            return;
        }

        JOptionPane.showMessageDialog(this, Lifelines.buildAudiencePollText(question), "Audience Poll",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Locks two incorrect answers, leaving the correct answer and one wrong option
     * available.
     *
     * @param none no parameters are required
     * @return void
     */
    private void useFiftyFiftyLifeline() {
        if (!session.useFiftyFifty()) {
            JOptionPane.showMessageDialog(this, "25/75 has already been used.");
            return;
        }

        Question question = session.getCurrentQuestion();
        if (question == null) {
            return;
        }

        Arrays.fill(answerLocks, false);
        int[] eliminatedIndices = Lifelines.getFiftyFiftyEliminatedIndices(question);
        for (int index : eliminatedIndices) {
            answerLocks[index] = true;
        }
        repaint();
    }

    /**
     * Shows the active question's correct answer as the simulated phone-a-friend
     * hint.
     *
     * @param none no parameters are required
     * @return void
     */
    private void usePhoneAFriendLifeline() {
        if (!session.usePhoneAFriend()) {
            JOptionPane.showMessageDialog(this, "Phone a Friend has already been used.");
            return;
        }

        Question question = session.getCurrentQuestion();
        if (question == null) {
            return;
        }

        JOptionPane.showMessageDialog(this, Lifelines.buildPhoneAFriendHint(question), "Phone a Friend",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Resets local interaction state when the session advances, finishes, or
     * restarts.
     */
    private void syncQuestionState() {
        if (session.getQuestionSerial() != lastQuestionSerial) {
            Arrays.fill(answerLocks, false);
            lastQuestionSerial = session.getQuestionSerial();
        }

        // Highlight the ladder step matching the question the player is facing!
        if (moneyLadder != null) {
            moneyLadder.setCurrentLevel(session.getCurrentQuestionNumber());
        }

        if (session.isFinished() && session.getStatusType() == GameSession.StatusType.FAILURE) {
            countdownTimer.stop();
            completionDialogShowing = true;

            Question q = session.getCurrentQuestion();
            String selectedAnsText = "Time Ran Out!";
            String correctAnsText = "Not found";

            if (q != null) {
                String[] choices = { q.getAnswerA(), q.getAnswerB(), q.getAnswerC(), q.getAnswerD() };
                
                // Read from our selectedAnswer before it is reset
                if (selectedAnswer >= 0 && selectedAnswer < 4) {
                    selectedAnsText = (char)('A' + selectedAnswer) + ": " + choices[selectedAnswer];
                }

                int cIdx = q.getCorrectAnswer().toUpperCase().trim().charAt(0) - 'A';
                if (cIdx >= 0 && cIdx < 4) {
                    correctAnsText = q.getCorrectAnswer().toUpperCase().trim() + ": " + choices[cIdx];
                }
            }

            // Swap panels gracefully inside the immediate parent container
            java.awt.Container parent = this.getParent();
            if (parent != null) {
                // Keep a reference to the ladder to pass back on restart
                final MoneyLadder ladderRef = this.moneyLadder;

                // FIX: Use a final single-element array to bypass the lambda initialization scope trap
                final GameOverPanel[] gameOverHolder = new GameOverPanel[1];

                GameOverPanel gameOver = new GameOverPanel(
                    selectedAnsText, 
                    correctAnsText, 
                    session.getLastSafeMoney(), 
                    () -> {
                        // Play Again Action
                        session.restart();
                        GameScreenPanel newGamePanel = new GameScreenPanel();
                        if (ladderRef != null) {
                            newGamePanel.setMoneyLadder(ladderRef);
                        }

                        // Swap the fresh gameplay panel directly back into the primary layout
                        parent.add(newGamePanel);
                        
                        // Safely remove the game over panel using our wrapper holder reference
                        if (gameOverHolder[0] != null && gameOverHolder[0].getParent() != null) {
                            java.awt.Container goParent = gameOverHolder[0].getParent();
                            goParent.remove(gameOverHolder[0]);
                            goParent.revalidate();
                            goParent.repaint();
                        }
                        
                        parent.revalidate();
                        parent.repaint();
                        newGamePanel.requestFocusInWindow();
                    }
                );

                // Assign to the wrapper array so the lambda closure can access it later when invoked
                gameOverHolder[0] = gameOver;

                // Add the game over panel to the layout and remove this game screen
                parent.add(gameOver);
                parent.remove(this);
                parent.revalidate();
                parent.repaint();
            }
            return;
        }

        if (!session.isFinished()) {
            completionDialogShowing = false;
            repaint();
            return;
        }
        if (completionDialogShowing) {
            repaint();
            return;
        }
        completionDialogShowing = true;
        countdownTimer.stop();
        repaint();
        showCompletionDialog();
    }

    /**
     * Prompts the player to replay or return to the menu after the run ends.
     *
     * @param none no parameters are required
     * @return void
     */
    private void showCompletionDialog() {
        boolean failed = session.getStatusType() == GameSession.StatusType.FAILURE;
        String title = failed ? "Game Over" : "Game Complete";
        String message = failed
                ? "GAME OVER: You earned a grand total of " + String.format("%,d", session.getLastSafeMoney())
                        + ".\n\nWould you like to play again?"
                : "You cleared Lock In with a score of $" + String.format("%,d", session.getScore())
                        + ".\n\nWould you like to play again?";
        int choice = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            session.restart();
            completionDialogShowing = false;
            lastQuestionSerial = -1;
            Arrays.fill(answerLocks, false);

            // Re-sync the level capsule back to level 1 on restart
            if (moneyLadder != null) {
                moneyLadder.setCurrentMoney(session.getScore());
            }

            countdownTimer.start();
            repaint();
            return;
        }

        returnToMenu();
    }

    /**
     * Opens a fresh main menu and closes the current game window.
     *
     * @param none no parameters are required
     * @return void
     */
    private void returnToMenu() {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });

        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    /**
     * Initiates and conducts the answer animation timeline.
     * It waits 2 seconds (steady orange), then pulses for 2 seconds (orange/green or orange/red).
     * If correct, it restarts the timer and advances. If incorrect, it triggers game over.
     */
    private void beginAnswerAnimation(int answerIndex) {
        if (answerAnimationRunning || selectedAnswer != -1)
            return;

        countdownTimer.stop();

        selectedAnswer = answerIndex;
        lastAnswerCorrect = session.isAnswerCorrect(answerIndex);
        flashCount = 0;
        flashState = false;

        // STAGE 1: Wait 2 seconds (Steady Orange Lock)
        Timer delayTimer = new Timer(2000, null);
        delayTimer.setRepeats(false);
        delayTimer.addActionListener(delayEvent -> {
            
            // STAGE 2: Start Pulsing
            answerAnimationRunning = true;

            Timer flashTimer = new Timer(250, null);
            flashTimer.addActionListener(flashEvent -> {
                flashState = !flashState;
                repaint();
                flashCount++;

                if (flashCount >= 8) {
                    flashTimer.stop();

                    session.submitAnswer(selectedAnswer);

                    // STAGE 3: Call sync first so it captures the active selectedAnswer
                    syncQuestionState();

                    // Clean up variables AFTER sync has run
                    answerAnimationRunning = false;
                    selectedAnswer = -1;
                    flashState = false;

                    if (!session.isFinished()) {
                        countdownTimer.restart();
                    }
                }
            });
            flashTimer.start();
        });
        
        delayTimer.start();
        repaint(); 
    }

}