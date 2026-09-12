import java.util.*;

/**
 * EditDistance.java
 * ------------------------------------------------------------
 * FEATURE 3: Typo-Tolerant Word Search.
 *   - wagnerFischer(): classic Levenshtein edit distance
 *     (insert / delete / substitute).
 *   - damerauLevenshtein(): same, but also allows a transposition
 *     of two adjacent letters as a single edit (handles swapped
 *     letters like "wolrd" -> "world").
 *   - topMatches(): ranks the whole dictionary against a typed
 *     query and returns the best N suggestions (an added "suggest"
 *     feature instead of only ever showing a single best guess).
 * ------------------------------------------------------------
 */
public class EditDistance {

    public static int wagnerFischer(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[n][m];
    }

    public static int damerauLevenshtein(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
                if (i > 1 && j > 1
                        && a.charAt(i - 1) == b.charAt(j - 2)
                        && a.charAt(i - 2) == b.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
                }
            }
        }
        return dp[n][m];
    }

    /** One ranked suggestion: the dictionary word plus both distance metrics. */
    public static class Suggestion {
        public final String word;
        public final int wagnerFischerDistance;
        public final int damerauDistance;

        public Suggestion(String word, int wf, int dam) {
            this.word = word;
            this.wagnerFischerDistance = wf;
            this.damerauDistance = dam;
        }
    }

    /**
     * Ranks the full dictionary against "query" by Damerau-Levenshtein
     * distance (ties broken by Wagner-Fischer distance, then alphabetically)
     * and returns the top N closest words. This backs the "Did you mean...?"
     * suggestion list (multiple options instead of a single guess).
     */
    public static List<Suggestion> topMatches(String query, List<String> dictionary, int topN) {
        List<Suggestion> all = new ArrayList<>();
        for (String word : dictionary) {
            int dam = damerauLevenshtein(query, word);
            int wf = wagnerFischer(query, word);
            all.add(new Suggestion(word, wf, dam));
        }
        all.sort((s1, s2) -> {
            if (s1.damerauDistance != s2.damerauDistance) return Integer.compare(s1.damerauDistance, s2.damerauDistance);
            if (s1.wagnerFischerDistance != s2.wagnerFischerDistance) return Integer.compare(s1.wagnerFischerDistance, s2.wagnerFischerDistance);
            return s1.word.compareTo(s2.word);
        });
        return all.subList(0, Math.min(topN, all.size()));
    }
}
