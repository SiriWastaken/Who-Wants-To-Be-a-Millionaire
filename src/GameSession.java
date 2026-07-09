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

    /** Builds a fresh session and loads a new deck of questions. */
    public GameSession() {
        restart();
    }

    /** Reloads the deck, resets score and timer state, and starts from the first question. */
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

    public boolean shouldShowTimer() {
        return !finished
                && !highStakesDecisionPending
                && getCurrentQuestion() != null
                && score < QuestionBank.getMoneyForQuestion(8);
    }

    public boolean hasReachedHighStakes() {
        return score >= QuestionBank.getMoneyForQuestion(8);
    }

    public int getCurrentQuestionTimeLimit() {
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

        return matchesCorrectAnswer(answerIndex,
                currentQuestion.getCorrectAnswer());
    }
    

    /**
     * Returns the correct answer index (0=A, 1=B, 2=C, 3=D).
     */
    public int getCorrectAnswerIndex() {

        Question currentQuestion = getCurrentQuestion();

        if (currentQuestion == null) {
            return -1;
        }

        return currentQuestion.getCorrectAnswer()
                .trim()
                .toUpperCase()
                .charAt(0) - 'A';
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

        if (finished || getCurrentQuestion() == null) {
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
        statusType = newStatusType == null
                ? StatusType.NEUTRAL
                : newStatusType;
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

            timeRemaining = getCurrentQuestion() == null
                    ? 0
                    : getCurrentQuestion().getTimeLimit();

            clearStatusMessage();

            return true;
        }

        finished = true;
        timeRemaining = 0;

        statusMessage = "You walked away with £"
                + String.format("%,d", score) + ".";

        statusType = StatusType.COMPLETE;

        return true;
    }

    /** Advances to the next question or completes the game. */
    private void advanceToNextQuestion(String message,
                                       StatusType nextStatusType) {

        statusMessage = message;
        statusType = nextStatusType;

        currentIndex++;

        if (currentIndex >= questionDeck.size()) {

            finished = true;
            timeRemaining = 0;

            score = QuestionBank.getMoneyForQuestion(
                    questionDeck.size() - 1);

            lastSafeMoney = score;

            statusMessage = "You cleared the board.";
            statusType = StatusType.COMPLETE;

            return;
        }

        questionSerial++;

        if (hasReachedHighStakes()) {

            highStakesDecisionPending = true;

            timeRemaining = 0;

            statusMessage =
                    "Would you like to play this round or walk away?";

            statusType = StatusType.NEUTRAL;

            return;
        }

        timeRemaining = getCurrentQuestion().getTimeLimit();
    }

    /** Ends the run with a failure. */
    private void finishWithFailure(String message) {

        finished = true;
        timeRemaining = 0;

        statusMessage = message;
        statusType = StatusType.FAILURE;
    }

    /** Finds a suitable replacement question for Swap. */
    private int findSwapIndex() {

        Question currentQuestion = getCurrentQuestion();

        if (currentQuestion == null) {
            return -1;
        }

        for (int i = currentIndex + 1;
             i < questionDeck.size();
             i++) {

            if (questionDeck.get(i).getDifficulty()
                    == currentQuestion.getDifficulty()) {

                return i;
            }
        }

        if (currentIndex + 1 < questionDeck.size()) {
            return currentIndex + 1;
        }

        return -1;
    }

    /** Compares an answer index against the stored answer label. */
    private boolean matchesCorrectAnswer(int answerIndex,
                                         String correctAnswer) {

        String selectedLabel =
                String.valueOf((char) ('A' + answerIndex));

        return selectedLabel.equals(
                correctAnswer.trim().toUpperCase());
    }

}