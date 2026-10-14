import java.util.ArrayList;
import java.util.List;

/**
 * KMP (Knuth-Morris-Pratt)
 * ------------------------
 * Exact phrase search: finds every position at which `pattern` occurs
 * inside `text`, in O(n + m) time (n = text length, m = pattern length).
 *
 * Unlike a naive scan, which can re-compare characters it has already
 * seen after a mismatch (worst case O(n*m)), KMP pre-computes an LPS
 * (Longest Proper Prefix which is also a Suffix) array for the pattern.
 * On a mismatch, the LPS array tells the algorithm exactly how far it
 * can safely shift the pattern without re-checking characters that are
 * guaranteed to match - so the text pointer never moves backwards.
 *
 * SmartCart uses this for "does this exact phrase appear in this product
 * description" (menu option 5b / used by the algorithm comparison), as
 * opposed to Aho-Corasick, which finds many DIFFERENT patterns at once,
 * and Rabin-Karp, which finds ONE pattern using hashing instead of a
 * failure function.
 */
public class KMP {

    /** Builds the LPS ("failure function") array for `pattern`. */
    public static int[] computeLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0; // length of the previous longest prefix-suffix
        int i = 1;
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    /**
     * Returns every starting index in `text` at which `pattern` occurs.
     * Empty list if there is no occurrence.
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0 || m > n) return matches;

        int[] lps = computeLPS(pattern);
        int i = 0; // index into text
        int j = 0; // index into pattern

        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    matches.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j != 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return matches;
    }

    /** Convenience check: does `pattern` occur anywhere in `text`? */
    public static boolean contains(String text, String pattern) {
        return !search(text, pattern).isEmpty();
    }
}
