import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Builds question decks and provides the money ladder for the game session. */
public final class QuestionBank {

    private static final Random RANDOM = new Random();

    private static final int[] MONEY_LADDER = {
            100, 200, 300, 500, 1000, 2000, 4000, 8000,
            16000, 32000, 64000, 125000, 250000, 500000,
            750000, 1000000
    };

    private QuestionBank() {
    }

    /** Builds a 16-question deck ordered by difficulty when possible.
     *
     * @param none no parameters are required
     * @return the 16-question deck used by a game session
     */
    public static List<Question> buildQuestionDeck() {
        ArrayList<Question> availableQuestions = new ArrayList<>(QuestionParser.getAllQuestions());
        ArrayList<Question> selectedQuestions = new ArrayList<>();

        if (availableQuestions.isEmpty()) {
            return createFallbackQuestions();
        }

        // Shuffle first so replay runs are not just the same ordered slice of the database.
        Collections.shuffle(availableQuestions, RANDOM);

        for (int difficulty = 1; difficulty <= 16 && !availableQuestions.isEmpty(); difficulty++) {
            // Pull one question per difficulty tier when the source data supports it.
            Question chosenQuestion = pickQuestionForDifficulty(availableQuestions, difficulty);
            if (chosenQuestion != null) {
                selectedQuestions.add(chosenQuestion);
                availableQuestions.remove(chosenQuestion);
            }
        }

        // If the CSV does not contain all difficulty tiers, top up the deck from what remains.
        while (selectedQuestions.size() < 16 && !availableQuestions.isEmpty()) {
            selectedQuestions.add(availableQuestions.remove(0));
        }

        return selectedQuestions.isEmpty() ? createFallbackQuestions() : selectedQuestions;
    }

    /** Returns the prize for a 0-based question index.
     *
     * @param questionIndex the zero-based index of the question
     * @return the prize amount for that index
     */
    public static int getMoneyForQuestion(int questionIndex) {
        return MONEY_LADDER[Math.min(questionIndex, MONEY_LADDER.length - 1)];
    }

    /** Returns true when the provided amount is a safe point.
     *
     * @param money the prize amount to inspect
     * @return true when the amount is a safe point, otherwise false
     */
    public static boolean isSafeMoney(int money) {
        return money == 1000 || money == 32000 || money == 1000000;
    }

    /** Picks the best available question for a requested difficulty.
     *
     * @param availableQuestions the candidate questions that can still be selected
     * @param difficulty the requested difficulty tier
     * @return the selected question, or null when no candidate is available
     */
    private static Question pickQuestionForDifficulty(List<Question> availableQuestions, int difficulty) {
        Question exactMatch = null;
        Question closestMatch = null;
        int closestDistance = Integer.MAX_VALUE;

        for (Question question : availableQuestions) {
            if (question.getDifficulty() == difficulty) {
                // Exact matches keep the ladder feeling consistent with the real show.
                exactMatch = question;
                break;
            }

            int distance = Math.abs(question.getDifficulty() - difficulty);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestMatch = question;
            }
        }

        return exactMatch != null ? exactMatch : closestMatch;
    }

    /** Creates a small fallback deck when the CSV file cannot be loaded.
     *
     * @param none no parameters are required
     * @return the fallback question deck
     */
    private static List<Question> createFallbackQuestions() {
        ArrayList<Question> fallback = new ArrayList<>();
        fallback.add(new Question("What color is the sky on a clear day?", "Blue", "Green", "Red", "Purple", "A", 1, 10, "Science"));
        fallback.add(new Question("How many days are in one week?", "5", "6", "7", "8", "C", 1, 10, "General Knowledge"));
        fallback.add(new Question("Which animal says meow?", "Dog", "Cat", "Cow", "Horse", "B", 1, 10, "Animals"));
        fallback.add(new Question("What is 5 + 3?", "6", "7", "8", "9", "C", 1, 10, "Math"));
        return fallback;
    }
}
