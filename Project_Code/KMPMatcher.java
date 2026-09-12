import java.util.*;

/**
 * KMPMatcher.java
 * ------------------------------------------------------------
 * FEATURE 2: Exact Phrase Search in Catalog (Knuth-Morris-Pratt).
 * Standard LPS (longest-prefix-suffix) table + linear scan,
 * so matching stays O(n + m) per catalog entry.
 * ------------------------------------------------------------
 */
public class KMPMatcher {

    private static int[] computeLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0, i = 1;
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    /** Returns the 0-based starting index of every occurrence of pattern in text. */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0) return result;
        int[] lps = computeLPS(pattern);
        int i = 0, j = 0;
        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++; j++;
                if (j == m) { result.add(i - j); j = lps[j - 1]; }
            } else if (j != 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return result;
    }
}
