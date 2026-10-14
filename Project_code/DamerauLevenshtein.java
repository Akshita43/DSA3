/**
 * DamerauLevenshtein
 * ------------------
 * Extends the standard Wagner-Fischer edit distance (see
 * FuzzySearchEngine.levenshteinDistance) by also allowing the
 * transposition of two ADJACENT characters as a single edit, in
 * addition to insertion, deletion, and substitution.
 *
 * This matters for spell correction because swapped-adjacent-letter
 * typos ("form" typed as "from", "the" typed as "teh") are extremely
 * common - especially on mobile keyboards - and are exactly one edit
 * away from the correct word under Damerau-Levenshtein, but TWO edits
 * away (a delete + an insert, or two substitutions) under plain
 * Wagner-Fischer/Levenshtein. A smaller distance means the correct
 * word ranks higher as a spelling suggestion.
 *
 * This is the classic "optimal string alignment" (restricted edit
 * distance) variant: it runs in O(n*m) time and space using a full DP
 * table, same asymptotic cost as Wagner-Fischer, just with one extra
 * transposition case checked per cell.
 */
public class DamerauLevenshtein {

    public static int distance(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;

                int deletion = dp[i - 1][j] + 1;
                int insertion = dp[i][j - 1] + 1;
                int substitution = dp[i - 1][j - 1] + cost;
                int best = Math.min(Math.min(deletion, insertion), substitution);

                // Adjacent transposition: swapping the last two characters
                // (e.g. "ab" -> "ba") counts as a single edit, provided
                // both strings are long enough and the swapped pair matches.
                if (i > 1 && j > 1
                        && a.charAt(i - 1) == b.charAt(j - 2)
                        && a.charAt(i - 2) == b.charAt(j - 1)) {
                    best = Math.min(best, dp[i - 2][j - 2] + 1);
                }

                dp[i][j] = best;
            }
        }
        return dp[n][m];
    }
}
