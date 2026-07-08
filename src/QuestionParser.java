import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionParser {

    private static final String[] SEARCH_PATHS = {
            "src/Assets/QuestionsList.csv",
            "bin/Assets/QuestionsList.csv"
    };
    private static final ArrayList<Question> questions = new ArrayList<>();
    private static final Random random = new Random();
    private static boolean loaded = false;

    /** Loads all questions from the CSV source if they have not already been loaded.
     *
     * @param none no parameters are required
     * @return void
     */
    public static synchronized void loadQuestions() {
        if (loaded) {
            return;
        }

        for (String path : SEARCH_PATHS) {
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }

            try (BufferedReader input = new BufferedReader(new FileReader(file))) {
                input.readLine();

                String line;
                while ((line = input.readLine()) != null) {
                    // The CSV parser preserves quoted commas so question text stays intact.
                    List<String> columns = parseCsvLine(line);
                    if (columns.size() < 10) {
                        continue;
                    }

                    try {
                        String questionText = columns.get(1).trim();
                        String answerA = columns.get(2).trim();
                        String answerB = columns.get(3).trim();
                        String answerC = columns.get(4).trim();
                        String answerD = columns.get(5).trim();
                        String correctAnswer = columns.get(6).trim();
                        int difficulty = Integer.parseInt(columns.get(7).trim());
                        int timeLimit = Integer.parseInt(columns.get(8).trim());
                        String category = columns.get(9).trim();

                        questions.add(new Question(questionText, answerA, answerB, answerC, answerD,
                                correctAnswer, difficulty, timeLimit, category));
                    } catch (NumberFormatException ignored) {
                        // Skip malformed rows and keep loading the rest of the database.
                    }
                }

                if (!questions.isEmpty()) {
                    loaded = true;
                    return;
                }
            } catch (IOException ignored) {
                // Try the next candidate path.
            }
        }

        loaded = true;
    }

    /** Returns a copy of all loaded questions.
     *
     * @param none no parameters are required
     * @return a copy of all loaded questions
     */
    public static synchronized List<Question> getAllQuestions() {
        loadQuestions();
        return new ArrayList<>(questions);
    }

    /** Returns all loaded questions that match the requested difficulty.
     *
     * @param difficulty the difficulty tier to filter by
     * @return the matching questions
     */
    public static synchronized List<Question> getQuestionsByDifficulty(int difficulty) {
        loadQuestions();

        ArrayList<Question> matchingQuestions = new ArrayList<>();
        for (Question question : questions) {
            if (question.getDifficulty() == difficulty) {
                matchingQuestions.add(question);
            }
        }
        return matchingQuestions;
    }

    /** Returns a random question of the requested difficulty.
     *
     * @param difficulty the desired difficulty level
     * @return a random question object or null if none exist
     */
    public static synchronized Question getRandomQuestion(int difficulty) {
        List<Question> matchingQuestions = getQuestionsByDifficulty(difficulty);

        if (matchingQuestions.isEmpty()) {
            return null;
        }

        return matchingQuestions.get(random.nextInt(matchingQuestions.size()));
    }

    /** Parses a CSV line while preserving quoted values.
     *
     * @param line the raw CSV row to parse
     * @return the parsed columns
     */
    private static List<String> parseCsvLine(String line) {
        ArrayList<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        columns.add(current.toString());
        return columns;
    }
}