import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Utility methods for the game's lifeline behavior and messaging. */
public final class Lifelines {

    private static final Random RANDOM = new Random();

    /** Prevents instantiation of this utility class.
     *
     * @param none no parameters are required
     * @return void
     */
    private Lifelines() {
    }

    /** Builds the simulated audience-poll results for the current question.
     *
     * @param question the question to summarize
     * @return the formatted audience-poll text
     */
    public static String buildAudiencePollText(Question question) {
        String correct = question.getCorrectAnswer().trim().toUpperCase();
        StringBuilder poll = new StringBuilder("Audience Poll\n\n");

        for (int i = 0; i < 4; i++) {
            String label = String.valueOf((char) ('A' + i));
            int base = label.equals(correct) ? 40 : 20;
            int variation = (i * 7) % 8;
            poll.append(label).append(": ").append(base + variation).append("%\n");
        }

        return poll.toString();
    }

    /** Returns one incorrect answer index that should be hidden by 25/75.
     *
     * @param question the question used to determine the correct answer
     * @return the answer index that should be locked
     */
    public static int[] getFiftyFiftyEliminatedIndices(Question question) {
        int correctIndex = question.getCorrectAnswer().trim().toUpperCase().charAt(0) - 'A';
        List<Integer> eliminated = new ArrayList<>(1);

        for (int i = 0; i < 4; i++) {
            if (i != correctIndex) {
                eliminated.add(i);
            }
        }

        if (eliminated.isEmpty()) {
            return new int[0];
        }

        int chosenIndex = eliminated.get(RANDOM.nextInt(eliminated.size()));

        return new int[] { chosenIndex };
    }

    /** Builds the phone-a-friend hint string for the current question.
     *
     * @param question the question to reveal
     * @return the formatted phone-a-friend hint
     */
    public static String buildPhoneAFriendHint(Question question) {
        return "Your friend thinks the answer is " + question.getCorrectAnswer().trim().toUpperCase() + ".";
    }
}