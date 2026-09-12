import java.util.*;

/**
 * Recommender.java  (NEW FEATURE)
 * ------------------------------------------------------------
 * "You might also like...": given one catalog product, tokenizes
 * every catalog entry into a word-set and scores the others by
 * how many words they share with it (simple word-overlap /
 * Jaccard-style similarity). Returns the closest matches, excluding
 * the product itself.
 * ------------------------------------------------------------
 */
public class Recommender {

    private final List<String> catalog;
    private final List<Set<String>> tokenized;

    public Recommender(List<String> catalog) {
        this.catalog = catalog;
        this.tokenized = new ArrayList<>();
        for (String product : catalog) {
            tokenized.add(new HashSet<>(Arrays.asList(product.split("\\s+"))));
        }
    }

    /** Returns up to topN catalog entries most similar to catalog.get(index). */
    public List<String> recommend(int index, int topN) {
        Set<String> base = tokenized.get(index);
        List<int[]> scored = new ArrayList<>(); // [catalogIndex, overlapScore]
        for (int i = 0; i < catalog.size(); i++) {
            if (i == index) continue;
            Set<String> other = new HashSet<>(tokenized.get(i));
            other.retainAll(base);
            int overlap = other.size();
            if (overlap > 0) scored.add(new int[]{i, overlap});
        }
        scored.sort((a, b) -> Integer.compare(b[1], a[1])); // highest overlap first

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, scored.size()); i++) {
            result.add(catalog.get(scored.get(i)[0]));
        }
        return result;
    }

    /** Finds the first catalog entry that equals or contains the given text (case-insensitive). */
    public int findProductIndex(String text) {
        String needle = text.toLowerCase().trim();
        for (int i = 0; i < catalog.size(); i++) {
            if (catalog.get(i).equals(needle)) return i;
        }
        for (int i = 0; i < catalog.size(); i++) {
            if (catalog.get(i).contains(needle)) return i;
        }
        return -1;
    }
}
