/**
 * Represents a trivia question record with choices, answers, and game metadata.
 */
public class Question {

    private final String QUESTION;
    private final String ANSWER_A;
    private final String ANSWER_B;
    private final String ANSWER_C;
    private final String ANSWER_D;
    private final String CORRECT_ANSWER;
    private final int DIFFICULTY;
    private final int TIME_LIMIT;
    private final String CATEGORY;

    /**
     * Creates a question record for the game database.
     *
     * @param question      the question text shown to the player
     * @param answerA       the A answer choice
     * @param answerB       the B answer choice
     * @param answerC       the C answer choice
     * @param answerD       the D answer choice
     * @param correctAnswer the correct answer label, such as A or B
     * @param difficulty    the question difficulty tier
     * @param timeLimit     the number of seconds allowed for this question
     * @param category      the category label displayed in the UI
     */
    public Question(String question, String answerA, String answerB,
            String answerC, String answerD,
            String correctAnswer, int difficulty,
            int timeLimit, String category) {

        this.QUESTION = question;
        this.ANSWER_A = answerA;
        this.ANSWER_B = answerB;
        this.ANSWER_C = answerC;
        this.ANSWER_D = answerD;
        this.CORRECT_ANSWER = correctAnswer;
        this.DIFFICULTY = difficulty;
        this.TIME_LIMIT = timeLimit;
        this.CATEGORY = category;
    }

    /**
     * Returns the question text.
     *
     * @param none no parameters are required
     * @return the question text
     */
    public String getQuestion() {
        return QUESTION;
    }

    /**
     * Returns the A answer choice.
     *
     * @param none no parameters are required
     * @return the A answer choice
     */
    public String getAnswerA() {
        return ANSWER_A;
    }

    /**
     * Returns the B answer choice.
     *
     * @param none no parameters are required
     * @return the B answer choice
     */
    public String getAnswerB() {
        return ANSWER_B;
    }

    /**
     * Returns the C answer choice.
     *
     * @param none no parameters are required
     * @return the C answer choice
     */
    public String getAnswerC() {
        return ANSWER_C;
    }

    /**
     * Returns the D answer choice.
     *
     * @param none no parameters are required
     * @return the D answer choice
     */
    public String getAnswerD() {
        return ANSWER_D;
    }

    /**
     * Returns the correct answer label.
     *
     * @param none no parameters are required
     * @return the correct answer label
     */
    public String getCorrectAnswer() {
        return CORRECT_ANSWER;
    }

    /**
     * Returns the difficulty tier.
     *
     * @param none no parameters are required
     * @return the difficulty tier
     */
    public int getDifficulty() {
        return DIFFICULTY;
    }

    /**
     * Returns the time limit in seconds.
     *
     * @param none no parameters are required
     * @return the time limit in seconds
     */
    public int getTimeLimit() {
        return TIME_LIMIT;
    }

    /**
     * Returns the category label.
     *
     * @param none no parameters are required
     * @return the category label
     */
    public String getCategory() {
        return CATEGORY;
    }
}