import java.util.ArrayList;
import java.util.List;

/**
 * RabinKarp
 * ---------
 * Implements the Rabin-Karp string-matching algorithm using a rolling hash,
 * to find every dictionary word that CONTAINS a given substring/pattern.
 *
 * This is separate from the fuzzy (typo-tolerant) search: it does exact
 * substring matching, e.g. searching for "put" finds "computer" and
 * "computation" because they contain "put" as a substring.
 *
 * Why Rabin-Karp instead of a plain nested loop?
 *   A naive check ("does this word contain this substring") compares the
 *   pattern against every position in every word, costing O(text_length *
 *   pattern_length) per word. Rabin-Karp instead computes a rolling hash of
 *   the pattern once, and a rolling hash of each window of the text, sliding
 *   the window one character at a time in O(1) per shift (instead of
 *   recomputing the whole substring hash from scratch). This brings a single
 *   word's search down to O(text_length + pattern_length) on average, only
 *   falling back to a direct character check when hashes collide.
 */
public class RabinKarp {

    // Large prime modulus keeps hash values bounded and reduces collisions.
    private static final long MOD = 1_000_000_007L;
    // Base for polynomial hashing (must be bigger than the alphabet size;
    // 256 comfortably covers standard Unicode BMP characters used here).
    private static final long BASE = 256L;

    /**
     * Returns true if `text` contains `pattern` as a substring, computed
     * using the Rabin-Karp rolling hash technique.
     */
    public static boolean containsSubstring(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();
        if (m == 0) return true;
        if (m > n) return false;

        long patternHash = 0;
        long windowHash = 0;
        long highOrder = 1; // BASE^(m-1) % MOD, used to remove the leaving character

        for (int i = 0; i < m - 1; i++) {
            highOrder = (highOrder * BASE) % MOD;
        }

        // Initial hash of the pattern and of the first window of the text
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % MOD;
            windowHash = (windowHash * BASE + text.charAt(i)) % MOD;
        }

        for (int i = 0; ; i++) {
            // Hash matched: confirm with a direct character check
            // (guards against the rare hash collision).
            if (windowHash == patternHash && text.regionMatches(i, pattern, 0, m)) {
                return true;
            }
            if (i == n - m) break;

            // Roll the hash forward by one character: remove text[i],
            // shift left, add text[i + m].
            windowHash = (windowHash - text.charAt(i) * highOrder % MOD + MOD) % MOD;
            windowHash = (windowHash * BASE + text.charAt(i + m)) % MOD;
        }
        return false;
    }

    /**
     * Scans every word in `words` and returns those that contain `pattern`
     * as a substring, using the Rabin-Karp check above.
     */
    public static List<String> searchDictionary(List<String> words, String pattern) {
        List<String> matches = new ArrayList<>();
        for (String word : words) {
            if (containsSubstring(word, pattern)) {
                matches.add(word);
            }
        }
        return matches;
    }
}