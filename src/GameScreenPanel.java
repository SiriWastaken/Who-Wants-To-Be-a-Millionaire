import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Handles gameplay input, timers, and small UI state for the game screen. */
public class GameScreenPanel extends JPanel {

    private final GameSession SESSION = new GameSession();
    private final GameScreenRenderer RENDERER = new GameScreenRenderer();

    private MoneyLadder moneyLadder;

    private final Rectangle BACK_BUTTON = new Rectangle(34, 26, 128, 38);
    private final Rectangle[] ANSWER_BOUNDS = {
            new Rectangle(90, 390, 420, 88),
            new Rectangle(590, 390, 420, 88),
            new Rectangle(90, 495, 420, 88),
            new Rectangle(590, 495, 420, 88)
    };
    private final Rectangle[] LIFELINE_BOUNDS = {
            new Rectangle(90, 620, 200, 42),
            new Rectangle(305, 620, 200, 42),
            new Rectangle(520, 620, 200, 42),
            new Rectangle(735, 620, 200, 42)
    };
    private final boolean[] ANSWER_LOCKS = new boolean[4];
    private final Timer COUNTDOWN_TIMER;
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

    // Lifeline display state
    private int[] audiencePollPercentages = null;
    private String phoneAFriendSuggestion = null;
    private int phoneAFriendSuggestedIndex = -1;
    private Timer lifelineDisplayTimer = null;

    // Play or Walk Away state
    private PlayOrWalkPanel playOrWalkPanel = null;
    private boolean isPlayOrWalkShowing = false;

    /** Creates the gameplay panel and starts the countdown timer. */
    public GameScreenPanel() {
        setFocusable(true);
        setPreferredSize(new Dimension(1100, 760));
        setLayout(null); // Use null layout so we can position the overlay
        COUNTDOWN_TIMER = new Timer(1000, event -> onTick());
        installListeners();
        syncQuestionState();
        if (!SESSION.isFinished()) {
            COUNTDOWN_TIMER.start();
        }
    }

    /**
     * Registers the MoneyLadder reference so this panel can push live step
     * highlights.
     * * @param ladder the instantiated MoneyLadder UI instance
     */
    public void setMoneyLadder(MoneyLadder ladder) {
        this.moneyLadder = ladder;
        // Seed initial level placement matching active state
        if (this.moneyLadder != null) {
            this.moneyLadder.setCurrentMoney(SESSION.getScore());
        }
    }

    /**
     * Returns the active game session used by the renderer.
     *
     * @param none no parameters are required
     * @return the active game session used by the renderer
     */
    GameSession getSession() {
        return SESSION;
    }

    /**
     * Returns the menu-button bounds used for hit testing and drawing.
     *
     * @param none no parameters are required
     * @return the menu-button bounds used for hit testing and drawing
     */
    Rectangle getBackButtonBounds() {
        return BACK_BUTTON;
    }

    /**
     * Returns the four answer-button bounds used for hit testing and drawing.
     *
     * @param none no parameters are required
     * @return the four answer-button bounds used for hit testing and drawing
     */
    Rectangle[] getAnswerBounds() {
        return ANSWER_BOUNDS;
    }

    /**
     * Returns the lifeline-button bounds used for hit testing and drawing.
     *
     * @param none no parameters are required
     * @return the lifeline-button bounds used for hit testing and drawing
     */
    Rectangle[] getLifelineBounds() {
        return LIFELINE_BOUNDS;
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
        return ANSWER_LOCKS[index];
    }

    /**
     * Returns the audience poll percentage for a given answer index.
     *
     * @param index the answer index (0-3)
     * @return the percentage, or -1 if not available
     */
    public int getAudiencePollPercentage(int index) {
        if (audiencePollPercentages != null && index >= 0 && index < audiencePollPercentages.length) {
            return audiencePollPercentages[index];
        }
        return -1;
    }

    /**
     * Returns the phone a friend suggestion text.
     *
     * @return the suggestion text, or null if not available
     */
    public String getPhoneAFriendSuggestion() {
        return phoneAFriendSuggestion;
    }

    /**
     * Returns the index of the answer suggested by phone a friend.
     *
     * @return the suggested index, or -1 if not available
     */
    public int getPhoneAFriendSuggestedIndex() {
        return phoneAFriendSuggestedIndex;
    }

    /**
     * Returns whether the Play or Walk Away panel is currently showing.
     *
     * @return true if showing, false otherwise
     */
    public boolean isPlayOrWalkShowing() {
        return isPlayOrWalkShowing;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Only render the main game - the overlay is now a child component
        RENDERER.paint((Graphics2D) g, this);
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
                // If Play or Walk is showing, don't process game mouse events
                if (isPlayOrWalkShowing) {
                    return;
                }
                handleMouseMoved(event.getPoint());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                // If Play or Walk is showing, don't process game mouse events
                if (isPlayOrWalkShowing) {
                    return;
                }
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
        if (BACK_BUTTON.contains(point)) {
            returnToMenu();
            return;
        }

        if (SESSION.isFinished() || SESSION.getCurrentQuestion() == null) {
            return;
        }

        for (int i = 0; i < ANSWER_BOUNDS.length; i++) {
            if (ANSWER_BOUNDS[i].contains(point) && !ANSWER_LOCKS[i]) {
                beginAnswerAnimation(i);
                return;
            }
        }

        for (int i = 0; i < LIFELINE_BOUNDS.length; i++) {
            if (LIFELINE_BOUNDS[i].contains(point)) {
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
        hoveringBack = BACK_BUTTON.contains(point);

        hoveredAnswerIndex = -1;
        for (int i = 0; i < ANSWER_BOUNDS.length; i++) {
            if (ANSWER_BOUNDS[i].contains(point)) {
                hoveredAnswerIndex = i;
                break;
            }
        }

        hoveredLifelineIndex = -1;
        for (int i = 0; i < LIFELINE_BOUNDS.length; i++) {
            if (LIFELINE_BOUNDS[i].contains(point)) {
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
        // Don't tick timer if Play or Walk is showing
        if (isPlayOrWalkShowing) {
            return;
        }
        SESSION.tick();
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
            case 2 -> useTwentyFiveSeventyFiveLifeline();
            case 3 -> usePhoneAFriendLifeline();
            default -> {
            }
        }
    }

    /**
     * Locks two incorrect answers, leaving the correct answer and one wrong option
     * available.
     *
     * @param none no parameters are required
     * @return void
     */
    private void useTwentyFiveSeventyFiveLifeline() {
        if (!SESSION.useFiftyFifty()) {
            JOptionPane.showMessageDialog(this, "25/75 has already been used.");
            return;
        }

        Question question = SESSION.getCurrentQuestion();
        if (question == null) {
            return;
        }

        Arrays.fill(ANSWER_LOCKS, false);
        int[] eliminatedIndices = Lifelines.getFiftyFiftyEliminatedIndices(question);
        for (int index : eliminatedIndices) {
            ANSWER_LOCKS[index] = true;
        }
        repaint();
    }

    /**
     * Uses Swap and clears any answer locks so the replacement question is
     * interactive.
     *
     * @param none no parameters are required
     * @return void
     */
    private void useSwapLifeline() {
        if (!SESSION.useSwap()) {
            JOptionPane.showMessageDialog(this, "Swap is unavailable right now.");
            return;
        }
        // Clear any lifeline displays
        clearLifelineDisplay();

        Arrays.fill(ANSWER_LOCKS, false);
        JOptionPane.showMessageDialog(this, "Swap used. The current question has been refreshed.");
    }

    /**
     * Shows the epic win screen when the player reaches £1,000,000.
     */
    private void showWinScreen() {
        // Clean up any Play or Walk panel
        if (playOrWalkPanel != null) {
            remove(playOrWalkPanel);
            playOrWalkPanel.cleanup();
            playOrWalkPanel = null;
        }
        isPlayOrWalkShowing = false;

        // Stop the timer
        COUNTDOWN_TIMER.stop();

        // Clear any lifeline displays
        clearLifelineDisplay();

        // Create and show the win screen
        WinScreenPanel winScreen = new WinScreenPanel(
                () -> {
                    // Play Again
                    SwingUtilities.invokeLater(() -> {
                        SESSION.RESTART();
                        // Replace this panel with a fresh game panel
                        java.awt.Container parent = this.getParent();
                        if (parent != null) {
                            GameScreenPanel newGamePanel = new GameScreenPanel();
                            if (moneyLadder != null) {
                                newGamePanel.setMoneyLadder(moneyLadder);
                            }
                            parent.add(newGamePanel);
                            parent.remove(this);
                            parent.revalidate();
                            parent.repaint();
                        }
                    });
                },
                () -> {
                    // Go Home
                    SwingUtilities.invokeLater(() -> {
                        returnToMenu();
                    });
                });

        // Replace this panel with the win screen
        java.awt.Container parent = this.getParent();
        if (parent != null) {
            // Add the win screen and remove this panel
            parent.add(winScreen);
            parent.remove(this);
            parent.revalidate();
            parent.repaint();
        }
    }

    /**
     * Shows the game over panel when the player loses.
     *
     * @param selectedAnsText the text of the selected answer
     * @param correctAnsText  the text of the correct answer
     */
    private void showGameOverPanel(String selectedAnsText, String correctAnsText) {
        // Swap panels gracefully inside the immediate parent container
        java.awt.Container parent = this.getParent();
        if (parent != null) {
            // Keep a reference to the ladder to pass back on restart
            final MoneyLadder ladderRef = this.moneyLadder;

            // FIX: Use a final single-element array to bypass the lambda initialization
            // scope trap
            final GameOverPanel[] gameOverHolder = new GameOverPanel[1];

            GameOverPanel gameOver = new GameOverPanel(
                    selectedAnsText,
                    correctAnsText,
                    SESSION.getLastSafeMoney(),
                    () -> {
                        // Play Again Action
                        SESSION.RESTART();
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
                    });

            // Assign to the wrapper array so the lambda closure can access it later when
            // invoked
            gameOverHolder[0] = gameOver;

            // Add the game over panel to the layout and remove this game screen
            parent.add(gameOver);
            parent.remove(this);
            parent.revalidate();
            parent.repaint();
        }
    }

    /**
     * Shows the audience poll results as percentages under each answer option.
     *
     * @param none no parameters are required
     * @return void
     */
    private void useAudiencePollLifeline() {
        if (!SESSION.useAudiencePoll()) {
            JOptionPane.showMessageDialog(this, "Audience Poll has already been used.");
            return;
        }

        Question question = SESSION.getCurrentQuestion();
        if (question == null) {
            return;
        }

        // Calculate percentages
        audiencePollPercentages = calculateAudiencePercentages(question);
        repaint();

        // Auto-remove after 8 seconds
        if (lifelineDisplayTimer != null) {
            lifelineDisplayTimer.stop();
        }
        lifelineDisplayTimer = new Timer(8000, e -> {
            audiencePollPercentages = null;
            repaint();
            lifelineDisplayTimer = null;
        });
        lifelineDisplayTimer.setRepeats(false);
        lifelineDisplayTimer.start();
    }

    /**
     * Calculates audience poll percentages.
     */
    private int[] calculateAudiencePercentages(Question question) {
        int[] result = new int[4];
        int correctIdx = question.getCorrectAnswer().trim().toUpperCase().charAt(0) - 'A';

        // Correct answer gets 40-55%
        int correctPercent = 40 + (int) (Math.random() * 15);
        result[correctIdx] = correctPercent;

        // Distribute remaining among wrong answers
        int remaining = 100 - correctPercent;
        for (int i = 0; i < 4; i++) {
            if (i != correctIdx && remaining > 0) {
                int share = (int) (remaining * (0.15 + Math.random() * 0.25));
                result[i] = Math.min(share, remaining);
                remaining -= result[i];
            }
        }

        // Add any leftover to correct answer
        if (remaining > 0) {
            result[correctIdx] += remaining;
        }

        // Ensure all values sum to 100
        int total = 0;
        for (int i = 0; i < 4; i++) {
            total += result[i];
        }
        if (total != 100 && total > 0) {
            result[correctIdx] += (100 - total);
        }

        return result;
    }

    /**
     * Shows the Phone a Friend suggestion under the suggested answer option.
     *
     * @param none no parameters are required
     * @return void
     */
    private void usePhoneAFriendLifeline() {
        if (!SESSION.usePhoneAFriend()) {
            JOptionPane.showMessageDialog(this, "Phone a Friend has already been used.");
            return;
        }

        Question question = SESSION.getCurrentQuestion();
        if (question == null) {
            return;
        }

        String[] friendNames = { "Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Avery", "Quinn" };
        String friendName = friendNames[(int) (Math.random() * friendNames.length)];
        String correctAnswer = question.getCorrectAnswer().trim().toUpperCase();

        // Convert the answer letter to an index
        phoneAFriendSuggestedIndex = correctAnswer.charAt(0) - 'A';
        phoneAFriendSuggestion = friendName + " thinks this is the answer!";
        repaint();

        // Auto-remove after 8 seconds
        if (lifelineDisplayTimer != null) {
            lifelineDisplayTimer.stop();
        }
        lifelineDisplayTimer = new Timer(8000, e -> {
            phoneAFriendSuggestion = null;
            phoneAFriendSuggestedIndex = -1;
            repaint();
            lifelineDisplayTimer = null;
        });
        lifelineDisplayTimer.setRepeats(false);
        lifelineDisplayTimer.start();
    }

    /**
     * Clears any lifeline display state.
     */
    private void clearLifelineDisplay() {
        audiencePollPercentages = null;
        phoneAFriendSuggestion = null;
        phoneAFriendSuggestedIndex = -1;
        if (lifelineDisplayTimer != null) {
            lifelineDisplayTimer.stop();
            lifelineDisplayTimer = null;
        }
    }

    /**
     * Shows the Play or Walk Away panel when the player reaches a safe point.
     */
    private void showPlayOrWalkPanel() {
        if (isPlayOrWalkShowing) {
            return;
        }

        // Clear any lifeline displays
        clearLifelineDisplay();

        int safeMoney = SESSION.getLastSafeMoney();
        int currentIndex = SESSION.getCurrentQuestionNumber() - 1;
        int nextMoney = QuestionBank.getMoneyForQuestion(currentIndex + 1);

        // Stop the timer while player decides
        COUNTDOWN_TIMER.stop();

        // Create the panel
        playOrWalkPanel = new PlayOrWalkPanel(
                safeMoney,
                nextMoney,
                () -> {
                    // Walk Away
                    SwingUtilities.invokeLater(() -> {
                        if (playOrWalkPanel != null) {
                            remove(playOrWalkPanel);
                            playOrWalkPanel.cleanup();
                            playOrWalkPanel = null;
                        }
                        isPlayOrWalkShowing = false;
                        SESSION.chooseHighStakesDecision(false);
                        showCompletionDialog();
                        repaint();
                        revalidate();
                    });
                },
                () -> {
                    // Continue Playing
                    SwingUtilities.invokeLater(() -> {
                        if (playOrWalkPanel != null) {
                            remove(playOrWalkPanel);
                            playOrWalkPanel.cleanup();
                            playOrWalkPanel = null;
                        }
                        isPlayOrWalkShowing = false;
                        SESSION.chooseHighStakesDecision(true);
                        if (!SESSION.isFinished()) {
                            COUNTDOWN_TIMER.start();
                        }
                        syncQuestionState();
                        repaint();
                        revalidate();
                    });
                });

        playOrWalkPanel.setBounds(0, 0, getWidth(), getHeight());
        playOrWalkPanel.setOpaque(false);
        add(playOrWalkPanel);
        isPlayOrWalkShowing = true;
        revalidate();
        repaint();
    }

    /**
     * Prompts the player to replay or return to the menu after the run ends.
     *
     * @param none no parameters are required
     * @return void
     */
    private void showCompletionDialog() {
        // Clean up any Play or Walk panel
        if (playOrWalkPanel != null) {
            remove(playOrWalkPanel);
            playOrWalkPanel.cleanup();
            playOrWalkPanel = null;
        }
        isPlayOrWalkShowing = false;
        revalidate();

        boolean failed = SESSION.getStatusType() == GameSession.StatusType.FAILURE;
        String title = failed ? "Game Over" : "Game Complete";

        // Check if this was a walk-away
        String statusMsg = SESSION.getStatusMessage();
        boolean walkedAway = statusMsg != null && statusMsg.contains("walked away");

        String message;
        if (walkedAway) {
            message = "You walked away with " + String.format("£%,d", SESSION.getScore()) +
                    ".\n\nWould you like to play again?";
        } else if (failed) {
            message = "GAME OVER: You earned a grand total of " + String.format("£%,d", SESSION.getLastSafeMoney()) +
                    ".\n\nWould you like to play again?";
        } else {
            message = "You cleared Final Answer? with a score of " + String.format("£%,d", SESSION.getScore()) +
                    ".\n\nWould you like to play again?";
        }

        int choice = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            SESSION.RESTART();
            completionDialogShowing = false;
            lastQuestionSerial = -1;
            Arrays.fill(ANSWER_LOCKS, false);
            clearLifelineDisplay();
            isPlayOrWalkShowing = false;
            playOrWalkPanel = null;

            // Re-sync the level capsule back to level 1 on restart
            if (moneyLadder != null) {
                moneyLadder.setCurrentMoney(SESSION.getScore());
            }

            COUNTDOWN_TIMER.start();
            repaint();
            return;
        }

        returnToMenu();
    }

    /**
     * Resets local interaction state when the session advances, finishes, or
     * restarts.
     */
    private void syncQuestionState() {
        // If Play or Walk is showing, don't update state
        if (isPlayOrWalkShowing) {
            return;
        }

        if (SESSION.getQuestionSerial() != lastQuestionSerial) {
            // Clear any lifeline display state when question changes
            clearLifelineDisplay();
            Arrays.fill(ANSWER_LOCKS, false);
            lastQuestionSerial = SESSION.getQuestionSerial();
        }

        // Highlight the ladder step matching the question the player is facing!
        if (moneyLadder != null) {
            moneyLadder.setCurrentLevel(SESSION.getCurrentQuestionNumber());
        }

        // Check if the panel is pending - triggered from GameSession
        if (SESSION.isHighStakesDecisionPending() && !SESSION.isFinished() &&
                SESSION.getStatusType() != GameSession.StatusType.COMPLETE) {
            showPlayOrWalkPanel();
            return;
        }

        // Handle game failure (incorrect answer or time expired)
        if (SESSION.isFinished() && SESSION.getStatusType() == GameSession.StatusType.FAILURE) {
            COUNTDOWN_TIMER.stop();
            completionDialogShowing = true;

            // Clean up any Play or Walk panel if it's still showing
            if (playOrWalkPanel != null) {
                remove(playOrWalkPanel);
                playOrWalkPanel.cleanup();
                playOrWalkPanel = null;
                isPlayOrWalkShowing = false;
                revalidate();
            }

            Question q = SESSION.getCurrentQuestion();
            String selectedAnsText = "Time Ran Out!";
            String correctAnsText = "Not found";

            if (q != null) {
                String[] choices = { q.getAnswerA(), q.getAnswerB(), q.getAnswerC(), q.getAnswerD() };

                // Read from our selectedAnswer before it is reset
                if (selectedAnswer >= 0 && selectedAnswer < 4) {
                    selectedAnsText = (char) ('A' + selectedAnswer) + ": " + choices[selectedAnswer];
                }

                int cIdx = q.getCorrectAnswer().toUpperCase().trim().charAt(0) - 'A';
                if (cIdx >= 0 && cIdx < 4) {
                    correctAnsText = q.getCorrectAnswer().toUpperCase().trim() + ": " + choices[cIdx];
                }
            }

            // Show game over using the existing game over panel logic
            showGameOverPanel(selectedAnsText, correctAnsText);
            return;
        }

        // Handle game completion - WIN!
        if (SESSION.isFinished() && SESSION.getStatusType() == GameSession.StatusType.COMPLETE) {
            // Check if this was a walk-away
            String status = SESSION.getStatusMessage();
            if (status.contains("walked away")) {
                completionDialogShowing = true;
                COUNTDOWN_TIMER.stop();
                repaint();
                showCompletionDialog();
                return;
            }

            // Check if the player won £1,000,000
            if (SESSION.getScore() >= 1000000) {
                // Show the epic win screen!
                showWinScreen();
                return;
            }

            // Normal completion
            completionDialogShowing = true;
            COUNTDOWN_TIMER.stop();
            repaint();
            showCompletionDialog();
            return;
        }

        if (!SESSION.isFinished()) {
            completionDialogShowing = false;
            repaint();
            return;
        }
        if (completionDialogShowing) {
            repaint();
            return;
        }
        completionDialogShowing = true;
        COUNTDOWN_TIMER.stop();
        repaint();
        showCompletionDialog();
    }

    /**
     * Opens a fresh main menu and closes the current game window.
     *
     * @param none no parameters are required
     * @return void
     */
    private void returnToMenu() {
        clearLifelineDisplay();
        isPlayOrWalkShowing = false;
        if (playOrWalkPanel != null) {
            remove(playOrWalkPanel);
            playOrWalkPanel.cleanup();
            playOrWalkPanel = null;
            revalidate();
        }

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
     * It waits 2 seconds (steady orange), then pulses for 2 seconds (orange/green
     * or orange/red).
     * If correct, it restarts the timer and advances. If incorrect, it triggers
     * game over.
     */
    private void beginAnswerAnimation(int answerIndex) {
        if (answerAnimationRunning || selectedAnswer != -1 || isPlayOrWalkShowing)
            return;

        COUNTDOWN_TIMER.stop();

        selectedAnswer = answerIndex;
        lastAnswerCorrect = SESSION.isAnswerCorrect(answerIndex);
        flashCount = 0;
        flashState = false;

        // Wait 2 seconds (Orange answer lock)
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

                    SESSION.submitAnswer(selectedAnswer);

                    // STAGE 3: Call sync first so it captures the active selectedAnswer
                    syncQuestionState();

                    // Clean up variables AFTER sync has run
                    answerAnimationRunning = false;
                    selectedAnswer = -1;
                    flashState = false;

                    if (!SESSION.isFinished() && !isPlayOrWalkShowing) {
                        COUNTDOWN_TIMER.restart();
                    }
                }
            });
            flashTimer.start();
        });

        delayTimer.start();
        repaint();
    }

    /**
     * Adds a window listener to clean up resources when the window closes.
     *
     * @param window the window to attach the listener to
     * @return void
     */
    public void addWindowListenerForAudio(java.awt.Window window) {
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                clearLifelineDisplay();
                if (playOrWalkPanel != null) {
                    remove(playOrWalkPanel);
                    playOrWalkPanel.cleanup();
                    playOrWalkPanel = null;
                }
                isPlayOrWalkShowing = false;
                revalidate();
            }
        });
    }
}