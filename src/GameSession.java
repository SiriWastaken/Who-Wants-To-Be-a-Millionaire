import java.util.ArrayList;
import java.util.Collections;

/** Maintains the game state, question deck, score, timer, and lifeline usage. */
public class GameSession {

    /** Represents the latest state change for the status banner. */
    public enum StatusType {
        NEUTRAL,
        SUCCESS,
        FAILURE,
        COMPLETE
    }

    private final ArrayList<Question> questionDeck = new ArrayList<>();
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

    /** Builds a fresh session and loads a new deck of questions.
     *
     * @param none no parameters are required
     * @return void
     */
    public GameSession() {
        restart();
    }

    /** Reloads the deck, resets score and timer state, and starts from the first question.
     *
     * @param none no parameters are required
     * @return void
     */
    public final void restart() {
        questionDeck.clear();
        questionDeck.addAll(QuestionBank.buildQuestionDeck());
        currentIndex = 0;
        score = 0;
        finished = false;
        swapUsed = false;
        audiencePollUsed = false;
        fiftyFiftyUsed = false;
        phoneAFriendUsed = false;
        highStakesDecisionPending = false;
        questionSerial = 1;
        statusType = StatusType.NEUTRAL;
        statusMessage = "";
        lastSafeMoney = 0;

        if (getCurrentQuestion() != null) {
            timeRemaining = getCurrentQuestion().getTimeLimit();
        } else {
            timeRemaining = 0;
            finished = true;
            statusMessage = "No questions are available.";
            statusType = StatusType.COMPLETE;
        }
    }

    /** Returns the question currently on screen, or null when the run is complete.
     *
     * @param none no parameters are required
     * @return the active question, or null when the run has ended
     */
    public Question getCurrentQuestion() {
        if (finished || currentIndex < 0 || currentIndex >= questionDeck.size()) {
            return null;
        }
        return questionDeck.get(currentIndex);
    }

    /** Returns the one-based question number currently being shown.
     *
     * @param none no parameters are required
     * @return the one-based question number currently being shown
     */
    public int getCurrentQuestionNumber() {
        return Math.min(currentIndex + 1, questionDeck.size());
    }

    /** Returns the total number of questions selected for this run.
     *
     * @param none no parameters are required
     * @return the total number of questions selected for this run
     */
    public int getTotalQuestions() {
        return questionDeck.size();
    }

    /** Returns the last guaranteed payout reached by the player.
     *
     * @param none no parameters are required
     * @return the last guaranteed payout reached by the player
     */
    public int getLastSafeMoney() {
        return lastSafeMoney;
    }

    /** Returns the current score shown in the money ladder card.
     *
     * @param none no parameters are required
     * @return the current score shown in the money ladder card
     */
    public int getScore() { return score; }

    /** Returns the seconds left for the active question.
     *
     * @param none no parameters are required
     * @return the seconds left for the active question
     */
    public int getTimeRemaining() { return timeRemaining; }

    /** Returns whether the run has ended.
     *
     * @param none no parameters are required
     * @return true when the run has ended, otherwise false
     */
    public boolean isFinished() { return finished; }

    /** Returns the latest banner message, or an empty string when nothing should be shown.
     *
     * @param none no parameters are required
     * @return the latest banner message, or an empty string when nothing should be shown
     */
    public String getStatusMessage() { return statusMessage == null ? "" : statusMessage; }

    /** Returns the banner style used by the renderer.
     *
     * @param none no parameters are required
     * @return the banner style used by the renderer
     */
    public StatusType getStatusType() { return statusType; }

    /** Returns the serial number for the active question so listeners can detect swaps and advances.
     *
     * @param none no parameters are required
     * @return the serial number for the active question
     */
    public int getQuestionSerial() { return questionSerial; }

    /** Returns whether Swap has already been consumed.
     *
     * @param none no parameters are required
     * @return true when Swap has already been consumed, otherwise false
     */
    public boolean isSwapUsed() { return swapUsed; }

    /** Returns whether Audience Poll has already been consumed.
     *
     * @param none no parameters are required
     * @return true when Audience Poll has already been consumed, otherwise false
     */
    public boolean isAudiencePollUsed() { return audiencePollUsed; }

    /** Returns whether 25/75 has already been consumed.
     *
     * @param none no parameters are required
     * @return true when 25/75 has already been consumed, otherwise false
     */
    public boolean isFiftyFiftyUsed() { return fiftyFiftyUsed; }

    /** Returns whether Phone a Friend has already been consumed.
     *
     * @param none no parameters are required
     * @return true when Phone a Friend has already been consumed, otherwise false
     */
    public boolean isPhoneAFriendUsed() { return phoneAFriendUsed; }

    /** Returns whether the game is waiting for the high-stakes play or walk-away decision.
     *
     * @param none no parameters are required
     * @return true when the next question is gated by the high-stakes choice
     */
    public boolean isHighStakesDecisionPending() { return highStakesDecisionPending; }

    /** Returns whether the countdown timer should be visible for the current question.
     *
     * @param none no parameters are required
     * @return true when the countdown should be shown, otherwise false
     */
    public boolean shouldShowTimer() {
        return !finished && !highStakesDecisionPending && getCurrentQuestion() != null && score < QuestionBank.getMoneyForQuestion(8);
    }

    /** Returns whether the game has crossed into the high-stakes portion of the ladder.
     *
     * @param none no parameters are required
     * @return true when the game is at or beyond £16,000
     */
    public boolean hasReachedHighStakes() {
        return score >= QuestionBank.getMoneyForQuestion(8);
    }

    /** Returns the time limit for the current question, or zero when there is no active question.
     *
     * @param none no parameters are required
     * @return the current question time limit, or zero when none is active
     */
    public int getCurrentQuestionTimeLimit() {
        Question currentQuestion = getCurrentQuestion();
        return currentQuestion == null ? 0 : currentQuestion.getTimeLimit();
    }

    /** Submits an answer and advances to the next question immediately.
     *
     * @param answerIndex the selected answer index, from 0 to 3
     * @return true if the answer was correct, otherwise false
     */
    public boolean submitAnswer(int answerIndex) {
        if (finished) {
            return false;
        }

        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return false;
        }

        // The answer check only compares the selected index against the stored label.
        boolean correct = matchesCorrectAnswer(answerIndex, currentQuestion.getCorrectAnswer());
        if (correct) {
            // The player keeps the prize for the question they just cleared.
            score = QuestionBank.getMoneyForQuestion(currentIndex);
            if (QuestionBank.isSafeMoney(score)) {
                lastSafeMoney = score;
            }
            advanceToNextQuestion("", StatusType.NEUTRAL);
        } else {
            finishWithFailure("Incorrect answer.");
        }
        return correct;
    }

    /** Decrements the timer and advances the run if time expires.
     *
     * @param none no parameters are required
     * @return void
     */
    public void tick() {
        if (finished || getCurrentQuestion() == null) {
            return;
        }

        // Clamp the timer so it never goes negative if the event fires late.
        timeRemaining = Math.max(0, timeRemaining - 1);
        if (timeRemaining == 0) {
            finishWithFailure("Time expired.");
        }
    }

    /** Swaps the current question with a later question of the same difficulty when possible.
     *
     * @param none no parameters are required
     * @return true when the swap succeeds, otherwise false
     */
    public boolean useSwap() {
        if (finished || swapUsed || questionDeck.size() < 2 || getCurrentQuestion() == null) {
            return false;
        }

        // Swap prefers a question at the same difficulty so the ladder stays balanced.
        int swapIndex = findSwapIndex();
        if (swapIndex < 0) {
            return false;
        }

        Collections.swap(questionDeck, currentIndex, swapIndex);
        swapUsed = true;
        questionSerial++;
        timeRemaining = getCurrentQuestion().getTimeLimit();
        statusMessage = "Swap used. The question has been replaced.";
        statusType = StatusType.NEUTRAL;
        return true;
    }

    /** Marks Audience Poll as used.
     *
     * @param none no parameters are required
     * @return true when the lifeline was consumed, otherwise false
     */
    public boolean useAudiencePoll() {
        if (finished || audiencePollUsed) {
            return false;
        }

        audiencePollUsed = true;
        statusMessage = "Audience Poll used.";
        statusType = StatusType.NEUTRAL;
        return true;
    }

    /** Marks 25/75 as used.
     *
     * @param none no parameters are required
     * @return true when the lifeline was consumed, otherwise false
     */
    public boolean useFiftyFifty() {
        if (finished || fiftyFiftyUsed) {
            return false;
        }

        fiftyFiftyUsed = true;
        statusMessage = "25/75 used.";
        statusType = StatusType.NEUTRAL;
        return true;
    }

    /** Marks Phone a Friend as used.
     *
     * @param none no parameters are required
     * @return true when the lifeline was consumed, otherwise false
     */
    public boolean usePhoneAFriend() {
        if (finished || phoneAFriendUsed) {
            return false;
        }

        phoneAFriendUsed = true;
        statusMessage = "Phone a Friend used.";
        statusType = StatusType.NEUTRAL;
        return true;
    }

    /** Updates the banner message and style for transient UI feedback.
     *
     * @param message the message to display
     * @param newStatusType the banner style to use
     * @return void
     */
    public void setStatusMessage(String message, StatusType newStatusType) {
        statusMessage = message == null ? "" : message;
        statusType = newStatusType == null ? StatusType.NEUTRAL : newStatusType;
    }

    /** Clears any transient banner message.
     *
     * @param none no parameters are required
     * @return void
     */
    public void clearStatusMessage() {
        statusMessage = "";
        statusType = StatusType.NEUTRAL;
    }

    /** Resolves the player's choice on the high-stakes transition screen.
     *
     * @param playRound true to continue to the next question, false to walk away
     * @return true when the choice was accepted, otherwise false
     */
    public boolean chooseHighStakesDecision(boolean playRound) {
        if (finished || !highStakesDecisionPending) {
            return false;
        }

        highStakesDecisionPending = false;
        if (playRound) {
            timeRemaining = getCurrentQuestion() == null ? 0 : getCurrentQuestion().getTimeLimit();
            clearStatusMessage();
            return true;
        }

        finished = true;
        timeRemaining = 0;
        statusMessage = "You walked away with £" + String.format("%,d", score) + ".";
        statusType = StatusType.COMPLETE;
        return true;
    }

    /** Advances to the next question and resolves completion state.
     *
     * @param message the status message to show for the transition
     * @param nextStatusType the banner style to use for the transition
     * @return void
     */
    private void advanceToNextQuestion(String message, StatusType nextStatusType) {
        statusMessage = message;
        statusType = nextStatusType;

        currentIndex++;
        if (currentIndex >= questionDeck.size()) {
            // Finishing the final question should resolve the whole run as a completion.
            finished = true;
            timeRemaining = 0;
            score = QuestionBank.getMoneyForQuestion(questionDeck.size() - 1);
            lastSafeMoney = score;
            statusMessage = "You cleared the board.";
            statusType = StatusType.COMPLETE;
            return;
        }

        questionSerial++;
        if (hasReachedHighStakes()) {
            highStakesDecisionPending = true;
            timeRemaining = 0;
            statusMessage = "Would you like to play this round or walk away?";
            statusType = StatusType.NEUTRAL;
            return;
        }

        timeRemaining = getCurrentQuestion().getTimeLimit();
    }

    /** Ends the run with a failure state.
     *
     * @param message the failure message to show
     * @return void
     */
    private void finishWithFailure(String message) {
        // Failure ends the run immediately and freezes the timer display.
        finished = true;
        timeRemaining = 0;
        statusMessage = message;
        statusType = StatusType.FAILURE;
    }

    /** Finds a later question that can replace the current one for Swap.
     *
     * @param none no parameters are required
     * @return the index to swap with, or -1 when none is available
     */
    private int findSwapIndex() {
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return -1;
        }

        for (int i = currentIndex + 1; i < questionDeck.size(); i++) {
            if (questionDeck.get(i).getDifficulty() == currentQuestion.getDifficulty()) {
                return i;
            }
        }

        return currentIndex + 1 < questionDeck.size() ? currentIndex + 1 : -1;
    }

    /** Compares a selected answer index against the correct answer label.
     *
     * @param answerIndex the selected answer index
     * @param correctAnswer the correct answer label
     * @return true when the selected answer matches the correct label, otherwise false
     */
    private boolean matchesCorrectAnswer(int answerIndex, String correctAnswer) {
        String selectedLabel = String.valueOf((char) ('A' + answerIndex));
        return selectedLabel.equals(correctAnswer.trim().toUpperCase());
    }

}
