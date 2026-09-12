import java.util.*;

/**
 * Autocomplete.java  (NEW FEATURE)
 * ------------------------------------------------------------
 * "As-you-type" suggestions: given a prefix like "lap", returns
 * every dictionary word that starts with it (e.g. "laptop",
 * "laptopbag", "laptopsleeve"...). Built on a plain TrieNode
 * (no fail links needed - that's only for Aho-Corasick).
 * ------------------------------------------------------------
 */
public class Autocomplete {

    private final TrieNode root = new TrieNode();

    public Autocomplete(List<String> dictionary) {
        for (String word : dictionary) {
            root.insert(word);
        }
    }

    /** Returns up to maxResults dictionary words that start with prefix. */
    public List<String> suggest(String prefix, int maxResults) {
        List<String> results = new ArrayList<>();
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            int idx = c - 'a';
            if (idx < 0 || idx >= TrieNode.ALPHA || node.children[idx] == null) {
                return results; // no words with this prefix
            }
            node = node.children[idx];
        }
        collectWords(node, results, maxResults);
        return results;
    }

    private void collectWords(TrieNode node, List<String> results, int maxResults) {
        if (results.size() >= maxResults) return;
        if (node.isWord) results.add(node.word);
        for (int c = 0; c < TrieNode.ALPHA && results.size() < maxResults; c++) {
            if (node.children[c] != null) collectWords(node.children[c], results, maxResults);
        }
    }
}
