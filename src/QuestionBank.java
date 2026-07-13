import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    /**
     * Builds a 16-question deck ordered by difficulty when possible.
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

        // Shuffle first so replay runs are not just the same ordered slice of the
        // database.
        Collections.shuffle(availableQuestions, RANDOM);

        for (int difficulty = 1; difficulty <= 16 && !availableQuestions.isEmpty(); difficulty++) {
            // Pull one question per difficulty tier when the source data supports it.
            Question chosenQuestion = pickQuestionForDifficulty(availableQuestions, difficulty);
            if (chosenQuestion != null) {
                selectedQuestions.add(chosenQuestion);
                availableQuestions.remove(chosenQuestion);
            }
        }

        // If the CSV does not contain all difficulty tiers, top up the deck from what
        // remains.
        while (selectedQuestions.size() < 16 && !availableQuestions.isEmpty()) {
            selectedQuestions.add(availableQuestions.remove(0));
        }

        if (selectedQuestions.size() < 16) {
            for (Question fallbackQuestion : createFallbackQuestions()) {
                if (selectedQuestions.size() >= 16) {
                    break;
                }
                if (!selectedQuestions.contains(fallbackQuestion)) {
                    selectedQuestions.add(fallbackQuestion);
                }
            }
        }

        return selectedQuestions.isEmpty() ? createFallbackQuestions() : selectedQuestions;
    }

    /**
     * Builds a 16-question deck and returns both the deck and a set of used
     * questions
     * for tracking purposes (prevents Swap from reusing questions).
     *
     * @return an array where index 0 is the deck and index 1 is the set of used
     *         questions
     */
    public static Object[] buildQuestionDeckWithTracking() {
        List<Question> deck = buildQuestionDeck();
        Set<Question> usedQuestions = new HashSet<>(deck);
        return new Object[] { deck, usedQuestions };
    }

    /**
     * Finds a replacement question for Swap that hasn't been used yet.
     *
     * @param currentDifficulty the difficulty of the question to replace
     * @param usedQuestions     a set of questions already used in this game
     * @param currentDeck       the current deck of questions
     * @param currentIndex      the index of the question to replace
     * @return a replacement question, or null if none available
     */
    public static Question findSwapReplacement(int currentDifficulty, Set<Question> usedQuestions,
            List<Question> currentDeck, int currentIndex) {
        // Get all questions from the database
        List<Question> allQuestions = QuestionParser.getAllQuestions();
        if (allQuestions.isEmpty()) {
            return null;
        }

        // Shuffle to get random selection
        Collections.shuffle(allQuestions, RANDOM);

        // Try to find a question of the same difficulty that hasn't been used
        for (Question q : allQuestions) {
            if (q.getDifficulty() == currentDifficulty && !usedQuestions.contains(q)) {
                // Make sure it's not already in the deck (except the one being replaced)
                boolean inDeck = false;
                for (int i = 0; i < currentDeck.size(); i++) {
                    if (i != currentIndex && currentDeck.get(i).equals(q)) {
                        inDeck = true;
                        break;
                    }
                }
                if (!inDeck) {
                    return q;
                }
            }
        }

        // If no exact difficulty match, try any question not used
        for (Question q : allQuestions) {
            if (!usedQuestions.contains(q)) {
                boolean inDeck = false;
                for (int i = 0; i < currentDeck.size(); i++) {
                    if (i != currentIndex && currentDeck.get(i).equals(q)) {
                        inDeck = true;
                        break;
                    }
                }
                if (!inDeck) {
                    return q;
                }
            }
        }

        // If all questions are used, try any question (including used ones) except the
        // current one
        for (Question q : allQuestions) {
            if (!q.equals(currentDeck.get(currentIndex))) {
                return q;
            }
        }

        return null;
    }

    /**
     * Returns the prize for a 0-based question index.
     *
     * @param questionIndex the zero-based index of the question
     * @return the prize amount for that index
     */
    public static int getMoneyForQuestion(int questionIndex) {
        return MONEY_LADDER[Math.min(questionIndex, MONEY_LADDER.length - 1)];
    }

    /**
     * Returns true when the provided amount is a safe point.
     *
     * @param money the prize amount to inspect
     * @return true when the amount is a safe point, otherwise false
     */
    public static boolean isSafeMoney(int money) {
        return money == 1000 || money == 32000 || money == 1000000;
    }

    /**
     * Picks the best available question for a requested difficulty.
     *
     * @param availableQuestions the candidate questions that can still be selected
     * @param difficulty         the requested difficulty tier
     * @return the selected question, or null when no candidate is available
     */
    private static Question pickQuestionForDifficulty(List<Question> availableQuestions, int difficulty) {
        Question exactMatch = null;
        Question closestMatch = null;
        int closestDistance = Integer.MAX_VALUE;

        for (Question question : availableQuestions) {
            if (question.getDifficulty() == difficulty) {
                // Prefer an exact difficulty match to preserve the intended progression.
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

    /**
     * Creates a small fallback deck when the CSV file cannot be loaded.
     *
     * @param none no parameters are required
     * @return the fallback question deck
     */
    private static List<Question> createFallbackQuestions() {
        ArrayList<Question> fallback = new ArrayList<>();
        fallback.add(new Question("What color is the sky on a clear day?", "Blue", "Green", "Red", "Purple", "A", 1, 10,
                "Science"));
        fallback.add(
                new Question("How many days are in one week?", "5", "6", "7", "8", "C", 1, 10, "General Knowledge"));
        fallback.add(new Question("Which animal says meow?", "Dog", "Cat", "Cow", "Horse", "B", 1, 10, "Animals"));
        fallback.add(new Question("What is 5 + 3?", "6", "7", "8", "9", "C", 1, 10, "Math"));
        fallback.add(
                new Question("What planet do we live on?", "Mars", "Venus", "Earth", "Jupiter", "C", 2, 10, "Science"));
        fallback.add(new Question("What is the capital of Canada?", "Toronto", "Ottawa", "Montreal", "Vancouver", "B",
                2, 10, "Geography"));
        fallback.add(new Question("Which season comes after spring?", "Winter", "Summer", "Autumn", "Monsoon", "B", 2,
                10, "General Knowledge"));
        fallback.add(new Question("What is 12 divided by 3?", "2", "3", "4", "5", "C", 2, 10, "Math"));
        fallback.add(new Question("How many letters are in the English alphabet?", "24", "25", "26", "27", "C", 3, 10,
                "General Knowledge"));
        fallback.add(new Question("Which gas do humans breathe in?", "Oxygen", "Helium", "Nitrogen", "Carbon Dioxide",
                "A", 3, 10, "Science"));
        fallback.add(new Question("What is the largest ocean on Earth?", "Atlantic", "Indian", "Arctic", "Pacific", "D",
                4, 10, "Geography"));
        fallback.add(new Question("What is 9 times 9?", "72", "81", "99", "108", "B", 4, 10, "Math"));
        fallback.add(new Question("Which instrument has 88 keys?", "Guitar", "Piano", "Violin", "Drums", "B", 5, 10,
                "Music"));
        fallback.add(new Question("Who wrote Romeo and Juliet?", "Shakespeare", "Dickens", "Austen", "Orwell", "A", 5,
                10, "Literature"));
        fallback.add(new Question("Which continent is Egypt in?", "Asia", "Africa", "Europe", "South America", "B", 6,
                10, "Geography"));
        fallback.add(new Question("What is 15 squared?", "200", "225", "250", "275", "B", 6, 10, "Math"));
        return fallback;
    }
}