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
import javax.swing.*;

/** Handles gameplay input, timers, and small UI state for the game screen. */
public class GameScreenPanel extends JPanel {

    private final GameSession SESSION = new GameSession();
    private final GameScreenRenderer RENDERER = new GameScreenRenderer();
    private final GameSession.GameMode gameMode;

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

    /**
     * Creates the gameplay panel with the specified game mode.
     *
     * @param mode the game mode (TIMED or UNTIMED)
     */
    public GameScreenPanel(GameSession.GameMode mode) {
        this.gameMode = mode;
        SESSION.setGameMode(mode);

        setFocusable(true);
        setPreferredSize(new Dimension(1100, 760));
        setLayout(null);
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
                if (isPlayOrWalkShowing) {
                    return;
                }
                handleMouseMoved(event.getPoint());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
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
     * Lifelines are disabled during answer animation.
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

        // If an answer has been selected (animation running), block ALL interactions
        if (answerAnimationRunning || selectedAnswer != -1) {
            return;
        }

        // Check answer buttons
        for (int i = 0; i < ANSWER_BOUNDS.length; i++) {
            if (ANSWER_BOUNDS[i].contains(point) && !ANSWER_LOCKS[i]) {
                beginAnswerAnimation(i);
                return;
            }
        }

        // Check lifeline buttons (only if no answer has been selected)
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
        // Double-check: lifelines cannot be used if an answer has been selected
        if (answerAnimationRunning || selectedAnswer != -1) {
            JOptionPane.showMessageDialog(this, "You cannot use lifelines after selecting an answer.");
            return;
        }

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
        clearLifelineDisplay();
        Arrays.fill(ANSWER_LOCKS, false);
        JOptionPane.showMessageDialog(this, "Swap used. The current question has been refreshed.");
    }

    /**
     * Shows the epic win screen when the player reaches £1,000,000.
     */
    private void showWinScreen() {
        if (playOrWalkPanel != null) {
            remove(playOrWalkPanel);
            playOrWalkPanel.cleanup();
            playOrWalkPanel = null;
        }
        isPlayOrWalkShowing = false;
        COUNTDOWN_TIMER.stop();
        clearLifelineDisplay();

        WinScreenPanel winScreen = new WinScreenPanel(
                () -> {
                    SwingUtilities.invokeLater(() -> {
                        SESSION.RESTART();
                        SESSION.setGameMode(gameMode);
                        java.awt.Container parent = this.getParent();
                        if (parent != null) {
                            GameScreenPanel newGamePanel = new GameScreenPanel(gameMode);
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
                    SwingUtilities.invokeLater(() -> {
                        returnToMenu();
                    });
                });

        java.awt.Container parent = this.getParent();
        if (parent != null) {
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
     * @param correctAnsText the text of the correct answer
     */
    private void showGameOverPanel(String selectedAnsText, String correctAnsText) {
        java.awt.Container parent = this.getParent();
        if (parent != null) {
            final MoneyLadder ladderRef = this.moneyLadder;
            final GameSession.GameMode mode = this.gameMode;
            final GameOverPanel[] gameOverHolder = new GameOverPanel[1];

            GameOverPanel gameOver = new GameOverPanel(
                    selectedAnsText,
                    correctAnsText,
                    SESSION.getLastSafeMoney(),
                    () -> {
                        SESSION.RESTART();
                        SESSION.setGameMode(mode);
                        GameScreenPanel newGamePanel = new GameScreenPanel(mode);
                        if (ladderRef != null) {
                            newGamePanel.setMoneyLadder(ladderRef);
                        }
                        parent.add(newGamePanel);
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

            gameOverHolder[0] = gameOver;
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

        audiencePollPercentages = calculateAudiencePercentages(question);
        repaint();

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

        int correctPercent = 40 + (int) (Math.random() * 15);
        result[correctIdx] = correctPercent;

        int remaining = 100 - correctPercent;
        for (int i = 0; i < 4; i++) {
            if (i != correctIdx && remaining > 0) {
                int share = (int) (remaining * (0.15 + Math.random() * 0.25));
                result[i] = Math.min(share, remaining);
                remaining -= result[i];
            }
        }

        if (remaining > 0) {
            result[correctIdx] += remaining;
        }

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

        phoneAFriendSuggestedIndex = correctAnswer.charAt(0) - 'A';
        phoneAFriendSuggestion = friendName + " thinks this is the answer!";
        repaint();

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

        clearLifelineDisplay();

        int safeMoney = SESSION.getLastSafeMoney();
        int currentIndex = SESSION.getCurrentQuestionNumber() - 1;
        int nextMoney = QuestionBank.getMoneyForQuestion(currentIndex + 1);

        COUNTDOWN_TIMER.stop();

        playOrWalkPanel = new PlayOrWalkPanel(
                safeMoney,
                nextMoney,
                () -> {
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
        if (playOrWalkPanel != null) {
            remove(playOrWalkPanel);
            playOrWalkPanel.cleanup();
            playOrWalkPanel = null;
        }
        isPlayOrWalkShowing = false;
        revalidate();

        boolean failed = SESSION.getStatusType() == GameSession.StatusType.FAILURE;
        String title = failed ? "Game Over" : "Game Complete";

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
            SESSION.setGameMode(gameMode);
            completionDialogShowing = false;
            lastQuestionSerial = -1;
            Arrays.fill(ANSWER_LOCKS, false);
            clearLifelineDisplay();
            isPlayOrWalkShowing = false;
            playOrWalkPanel = null;

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
        if (isPlayOrWalkShowing) {
            return;
        }

        if (SESSION.getQuestionSerial() != lastQuestionSerial) {
            clearLifelineDisplay();
            Arrays.fill(ANSWER_LOCKS, false);
            lastQuestionSerial = SESSION.getQuestionSerial();
        }

        if (moneyLadder != null) {
            moneyLadder.setCurrentLevel(SESSION.getCurrentQuestionNumber());
        }

        if (SESSION.isHighStakesDecisionPending() && !SESSION.isFinished() &&
                SESSION.getStatusType() != GameSession.StatusType.COMPLETE) {
            showPlayOrWalkPanel();
            return;
        }

        if (SESSION.isFinished() && SESSION.getStatusType() == GameSession.StatusType.FAILURE) {
            COUNTDOWN_TIMER.stop();
            completionDialogShowing = true;

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

                if (selectedAnswer >= 0 && selectedAnswer < 4) {
                    selectedAnsText = (char) ('A' + selectedAnswer) + ": " + choices[selectedAnswer];
                }

                int cIdx = q.getCorrectAnswer().toUpperCase().trim().charAt(0) - 'A';
                if (cIdx >= 0 && cIdx < 4) {
                    correctAnsText = q.getCorrectAnswer().toUpperCase().trim() + ": " + choices[cIdx];
                }
            }

            showGameOverPanel(selectedAnsText, correctAnsText);
            return;
        }

        if (SESSION.isFinished() && SESSION.getStatusType() == GameSession.StatusType.COMPLETE) {
            String status = SESSION.getStatusMessage();
            if (status.contains("walked away")) {
                completionDialogShowing = true;
                COUNTDOWN_TIMER.stop();
                repaint();
                showCompletionDialog();
                return;
            }

            if (SESSION.getScore() >= 1000000) {
                showWinScreen();
                return;
            }

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
        // Prevent animation if already running or if PlayOrWalk is showing
        if (answerAnimationRunning || selectedAnswer != -1 || isPlayOrWalkShowing)
            return;

        // In UNTIMED mode, we don't stop the timer (it's not running anyway)
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
                        // Only restart timer if in TIMED mode
                        if (gameMode == GameSession.GameMode.TIMED) {
                            COUNTDOWN_TIMER.restart();
                        }
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