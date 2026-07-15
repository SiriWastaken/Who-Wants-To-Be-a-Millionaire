import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Maintains the game state, question deck, score, timer, and lifeline usage.
 */
public class GameSession {

    /** Represents the latest state change for the status banner. */
    public enum StatusType {
        NEUTRAL,
        SUCCESS,
        FAILURE,
        COMPLETE
    }

    /** Game mode options. */
    public enum GameMode {
        TIMED,      // Original mode with countdown timer
        UNTIMED     // No timer - player can take as long as they want
    }

    private final ArrayList<Question> questionDeck = new ArrayList<>();
    private Set<Question> usedQuestions = new HashSet<>();
    private int currentIndex;
    public int score;
    private int timeRemaining;
    private int questionSerial;
    private boolean finished;
    private boolean swapUsed;
    private boolean audiencePollUsed;
    private boolean fiftyFiftyUsed;
    private boolean phoneAFriendUsed;
    private boolean highStakesDecisionPending;
    private String statusMessage = "";
    private StatusType statusType;
    private int lastSafeMoney;
    private GameMode currentGameMode = GameMode.TIMED;

    /** Builds a fresh session and loads a new deck of questions. */
    public GameSession() {
        RESTART();
    }

    /**
     * Sets the game mode for this session.
     *
     * @param mode the game mode to use
     */
    public void setGameMode(GameMode mode) {
        this.currentGameMode = mode;
    }

    /**
     * Returns the current game mode.
     *
     * @return the current game mode
     */
    public GameMode getGameMode() {
        return currentGameMode;
    }

    /**
     * Reloads the deck, resets score and timer state, and starts from the first
     * question.
     */
    public final void RESTART() {
        questionDeck.clear();
        usedQuestions.clear();

        // Build deck with tracking
        Object[] result = QuestionBank.buildQuestionDeckWithTracking();
        @SuppressWarnings("unchecked")
        List<Question> deck = (List<Question>) result[0];
        questionDeck.addAll(deck);
        usedQuestions = (Set<Question>) result[1];

        currentIndex = 0;
        score = 0;
        finished = false;

        swapUsed = false;
        audiencePollUsed = false;
        fiftyFiftyUsed = false;
        phoneAFriendUsed = false;

        highStakesDecisionPending = false;

        questionSerial = 1;

        statusMessage = "";
        statusType = StatusType.NEUTRAL;

        lastSafeMoney = 0;

        if (getCurrentQuestion() != null) {
            timeRemaining = getCurrentQuestion().getTimeLimit();
        } else {
            finished = true;
            timeRemaining = 0;
            statusMessage = "No questions are available.";
            statusType = StatusType.COMPLETE;
        }
    }

    /** Returns the active question. */
    public Question getCurrentQuestion() {
        if (finished || currentIndex < 0 || currentIndex >= questionDeck.size()) {
            return null;
        }
        return questionDeck.get(currentIndex);
    }

    /** Returns the current question number (1-based). */
    public int getCurrentQuestionNumber() {
        return Math.min(currentIndex + 1, questionDeck.size());
    }

    /** Returns the total number of questions. */
    public int getTotalQuestions() {
        return questionDeck.size();
    }

    /** Returns the last guaranteed payout. */
    public int getLastSafeMoney() {
        return lastSafeMoney;
    }

    /** Returns the current score. */
    public int getScore() {
        return score;
    }

    /** Returns seconds remaining. */
    public int getTimeRemaining() {
        return timeRemaining;
    }

    /** Returns whether the game has ended. */
    public boolean isFinished() {
        return finished;
    }

    /** Returns the current status message. */
    public String getStatusMessage() {
        return statusMessage == null ? "" : statusMessage;
    }

    /** Returns the banner status. */
    public StatusType getStatusType() {
        return statusType;
    }

    /** Returns the question serial number. */
    public int getQuestionSerial() {
        return questionSerial;
    }

    public boolean isSwapUsed() {
        return swapUsed;
    }

    public boolean isAudiencePollUsed() {
        return audiencePollUsed;
    }

    public boolean isFiftyFiftyUsed() {
        return fiftyFiftyUsed;
    }

    public boolean isPhoneAFriendUsed() {
        return phoneAFriendUsed;
    }

    public boolean isHighStakesDecisionPending() {
        return highStakesDecisionPending;
    }

    /**
     * Returns whether the timer should be shown.
     * In UNTIMED mode, the timer is always hidden.
     */
    public boolean shouldShowTimer() {
        if (currentGameMode == GameMode.UNTIMED) {
            return false;
        }
        return !finished
                && !highStakesDecisionPending
                && getCurrentQuestion() != null
                && score < QuestionBank.getMoneyForQuestion(8);
    }

    /**
     * Returns the time limit for the current question.
     * In UNTIMED mode, returns a very large number (effectively infinite).
     */
    public int getCurrentQuestionTimeLimit() {
        if (currentGameMode == GameMode.UNTIMED) {
            return 99999; // Effectively infinite
        }
        Question currentQuestion = getCurrentQuestion();
        return currentQuestion == null ? 0 : currentQuestion.getTimeLimit();
    }

    /**
     * Checks whether the selected answer is correct WITHOUT changing the game
     * state.
     */
    public boolean isAnswerCorrect(int answerIndex) {
        if (finished) {
            return false;
        }
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return false;
        }
        return matchesCorrectAnswer(answerIndex, currentQuestion.getCorrectAnswer());
    }

    /**
     * Returns the correct answer index (0=A, 1=B, 2=C, 3=D).
     */
    public int getCorrectAnswerIndex() {
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return -1;
        }
        return currentQuestion.getCorrectAnswer().trim().toUpperCase().charAt(0) - 'A';
    }

    /**
     * Commits the player's answer AFTER the animation has completed.
     */
    public void submitAnswer(int answerIndex) {
        if (finished) {
            return;
        }
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return;
        }

        if (isAnswerCorrect(answerIndex)) {
            score = QuestionBank.getMoneyForQuestion(currentIndex);
            // Update safe money if this is a safe point
            if (QuestionBank.isSafeMoney(score)) {
                lastSafeMoney = score;
            }
            advanceToNextQuestion("", StatusType.NEUTRAL);
        } else {
            finishWithFailure("Incorrect answer.");
        }
    }

    /** Decrements the timer once per second. */
    public void tick() {
        if (finished || getCurrentQuestion() == null || highStakesDecisionPending) {
            return;
        }
        // In UNTIMED mode, the timer never ticks down
        if (currentGameMode == GameMode.UNTIMED) {
            return;
        }
        timeRemaining = Math.max(0, timeRemaining - 1);
        if (timeRemaining == 0) {
            finishWithFailure("Time expired.");
        }
    }

    /** Uses the Swap lifeline. */
    public boolean useSwap() {
        if (finished || swapUsed || questionDeck.size() < 2 || getCurrentQuestion() == null) {
            return false;
        }

        Question currentQuestion = getCurrentQuestion();
        int currentDifficulty = currentQuestion.getDifficulty();

        // Find a replacement question that hasn't been used
        Question replacement = QuestionBank.findSwapReplacement(
                currentDifficulty, usedQuestions, questionDeck, currentIndex);

        if (replacement == null) {
            // No replacement found
            return false;
        }

        // Add the current question to used set (it won't be used again)
        usedQuestions.add(currentQuestion);

        // Replace the question in the deck
        questionDeck.set(currentIndex, replacement);
        usedQuestions.add(replacement);

        swapUsed = true;
        questionSerial++;
        timeRemaining = getCurrentQuestion().getTimeLimit();
        statusMessage = "Swap used. The question has been replaced.";
        statusType = StatusType.NEUTRAL;
        return true;
    }

    public boolean useAudiencePoll() {
        if (finished || audiencePollUsed) {
            return false;
        }
        audiencePollUsed = true;
        statusMessage = "Audience Poll used.";
        statusType = StatusType.NEUTRAL;
        return true;
    }

    public boolean useFiftyFifty() {
        if (finished || fiftyFiftyUsed) {
            return false;
        }
        fiftyFiftyUsed = true;
        statusMessage = "25/75 used.";
        statusType = StatusType.NEUTRAL;
        return true;
    }

    public boolean usePhoneAFriend() {
        if (finished || phoneAFriendUsed) {
            return false;
        }
        phoneAFriendUsed = true;
        statusMessage = "Phone a Friend used.";
        statusType = StatusType.NEUTRAL;
        return true;
    }

    /** Updates the banner message and style for transient UI feedback. */
    public void setStatusMessage(String message, StatusType newStatusType) {
        statusMessage = message == null ? "" : message;
        statusType = newStatusType == null ? StatusType.NEUTRAL : newStatusType;
    }

    /** Clears any transient banner message. */
    public void clearStatusMessage() {
        statusMessage = "";
        statusType = StatusType.NEUTRAL;
    }

    /** Resolves the player's choice on the high-stakes transition screen. */
    public boolean chooseHighStakesDecision(boolean playRound) {
        if (finished || !highStakesDecisionPending) {
            return false;
        }

        highStakesDecisionPending = false;

        if (playRound) {
            timeRemaining = getCurrentQuestion() == null ? 0 : getCurrentQuestion().getTimeLimit();
            clearStatusMessage();
        } else {
            finished = true;
            timeRemaining = 0;
            statusMessage = "You walked away with £" + String.format("%,d", lastSafeMoney) + ".";
            statusType = StatusType.COMPLETE;
        }

        return true;
    }

    /** Advances to the next question or completes the game. */
    private void advanceToNextQuestion(String message, StatusType nextStatusType) {
        statusMessage = message;
        statusType = nextStatusType;

        // Add the current question to used set before moving on
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion != null) {
            usedQuestions.add(currentQuestion);
        }

        currentIndex++;

        if (currentIndex >= questionDeck.size()) {
            finished = true;
            timeRemaining = 0;
            score = QuestionBank.getMoneyForQuestion(questionDeck.size() - 1);
            lastSafeMoney = score;
            statusMessage = "You cleared the board!";
            statusType = StatusType.COMPLETE;
            return;
        }

        questionSerial++;

        // Show Play or Walk before EVERY question if score >= 32000
        if (score >= 32000 && !finished) {
            highStakesDecisionPending = true;
            timeRemaining = 0; // Stop timer while player decides
            statusMessage = "Would you like to continue or walk away?";
            statusType = StatusType.NEUTRAL;
            return;
        }

        // Otherwise, start the timer for the next question
        timeRemaining = getCurrentQuestion().getTimeLimit();
    }

    /** Ends the run with a failure. */
    private void finishWithFailure(String message) {
        finished = true;
        timeRemaining = 0;
        statusMessage = message;
        statusType = StatusType.FAILURE;
    }

    /** Compares an answer index against the stored answer label. */
    private boolean matchesCorrectAnswer(int answerIndex, String correctAnswer) {
        String selectedLabel = String.valueOf((char) ('A' + answerIndex));
        return selectedLabel.equals(correctAnswer.trim().toUpperCase());
    }
}