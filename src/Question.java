public class Question {

    private final String question;
    private final String answerA;
    private final String answerB;
    private final String answerC;
    private final String answerD;
    private final String correctAnswer;
    private final int difficulty;
    private final int timeLimit;
    private final String category;

    /** Creates a question record for the game database.
     *
     * @param question the question text shown to the player
     * @param answerA the A answer choice
     * @param answerB the B answer choice
     * @param answerC the C answer choice
     * @param answerD the D answer choice
     * @param correctAnswer the correct answer label, such as A or B
     * @param difficulty the question difficulty tier
     * @param timeLimit the number of seconds allowed for this question
     * @param category the category label displayed in the UI
     * @return void
     */
    public Question(String question, String answerA, String answerB,
                    String answerC, String answerD,
                    String correctAnswer, int difficulty,
                    int timeLimit, String category) {

        this.question = question;
        this.answerA = answerA;
        this.answerB = answerB;
        this.answerC = answerC;
        this.answerD = answerD;
        this.correctAnswer = correctAnswer;
        this.difficulty = difficulty;
        this.timeLimit = timeLimit;
        this.category = category;
    }

    /** Returns the question text.
     *
     * @param none no parameters are required
     * @return the question text
     */
    public String getQuestion() {
        return question;
    }

    /** Returns the A answer choice.
     *
     * @param none no parameters are required
     * @return the A answer choice
     */
    public String getAnswerA() {
        return answerA;
    }

    /** Returns the B answer choice.
     *
     * @param none no parameters are required
     * @return the B answer choice
     */
    public String getAnswerB() {
        return answerB;
    }

    /** Returns the C answer choice.
     *
     * @param none no parameters are required
     * @return the C answer choice
     */
    public String getAnswerC() {
        return answerC;
    }

    /** Returns the D answer choice.
     *
     * @param none no parameters are required
     * @return the D answer choice
     */
    public String getAnswerD() {
        return answerD;
    }

    /** Returns the correct answer label.
     *
     * @param none no parameters are required
     * @return the correct answer label
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    /** Returns the difficulty tier.
     *
     * @param none no parameters are required
     * @return the difficulty tier
     */
    public int getDifficulty() {
        return difficulty;
    }

    /** Returns the time limit in seconds.
     *
     * @param none no parameters are required
     * @return the time limit in seconds
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    /** Returns the category label.
     *
     * @param none no parameters are required
     * @return the category label
     */
    public String getCategory() {
        return category;
    }
}