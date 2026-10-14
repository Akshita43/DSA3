import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * WordIndex
 * ---------
 * Product names are multi-word strings (e.g. "samsung charger black").
 * Matching the WHOLE name against a typo like "opa chrger" fails, because
 * Levenshtein distance is computed over the entire string, and "opa chrger"
 * is nowhere close to the full string "samsung charger black" - just to one
 * word inside it.
 *
 * WordIndex fixes this by tokenizing every product name into individual
 * words and indexing them separately:
 *   - wordTrie:      a Trie of every distinct word across all product names,
 *                     used for per-word fuzzy correction and autocomplete.
 *   - productsByWord: an inverted index (word -> product names containing it),
 *                     used to look up which products a (corrected) word
 *                     actually belongs to.
 */
public class WordIndex {

    private final Trie wordTrie = new Trie();
    private final Map<String, List<String>> productsByWord = new HashMap<>();

    public void build(ProductCatalog catalog) {
        for (String productName : catalog.getAllNames()) {
            for (String token : tokenize(productName)) {
                wordTrie.insert(token);
                productsByWord
                    .computeIfAbsent(token, k -> new ArrayList<>())
                    .add(productName);
            }
        }
    }

    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        for (String t : text.trim().toLowerCase().split("\\s+")) {
            if (!t.isEmpty()) tokens.add(t);
        }
        return tokens;
    }

    public Trie getWordTrie() {
        return wordTrie;
    }

    public List<String> getProductsContaining(String word) {
        return productsByWord.getOrDefault(word.toLowerCase(), new ArrayList<>());
    }

    /** Returns product names containing ALL given words (set intersection). */
    public List<String> getProductsContainingAll(List<String> words) {
        Set<String> result = null;
        for (String word : words) {
            List<String> matches = getProductsContaining(word);
            if (result == null) {
                result = new LinkedHashSet<>(matches);
            } else {
                result.retainAll(matches);
            }
            if (result.isEmpty()) break;
        }
        return result == null ? new ArrayList<>() : new ArrayList<>(result);
    }

    /** Returns product names containing ANY of the given words, ranked by
     *  how many of the words each product matches (most matches first). */
    public List<String> getProductsContainingAny(List<String> words) {
        Map<String, Integer> matchCount = new HashMap<>();
        for (String word : words) {
            for (String product : getProductsContaining(word)) {
                matchCount.merge(product, 1, Integer::sum);
            }
        }
        List<String> results = new ArrayList<>(matchCount.keySet());
        results.sort((a, b) -> matchCount.get(b) - matchCount.get(a));
        return results;
    }
}
