import java.util.*;

/**
 * TrieNode.java
 * ------------------------------------------------------------
 * A single node of a 26-letter (a-z) Trie. Reused by:
 *   - AhoCorasick.java  (adds "fail" links for multi-pattern search)
 *   - Autocomplete.java (plain prefix trie, no fail links needed)
 * ------------------------------------------------------------
 */
public class TrieNode {
    public static final int ALPHA = 26;

    public TrieNode[] children = new TrieNode[ALPHA];
    public TrieNode fail;                 // used only by Aho-Corasick
    public List<String> output = new ArrayList<>(); // completed words at/behind this node
    public boolean isWord = false;        // used only by Autocomplete
    public String word = null;            // full word stored at a leaf (Autocomplete)

    /** Inserts a lowercase a-z word into the trie rooted at this node. */
    public void insert(String word) {
        TrieNode node = this;
        for (char c : word.toCharArray()) {
            int idx = c - 'a';
            if (idx < 0 || idx >= ALPHA) continue; // ignore non a-z characters
            if (node.children[idx] == null) node.children[idx] = new TrieNode();
            node = node.children[idx];
        }
        node.isWord = true;
        node.word = word;
        if (!node.output.contains(word)) node.output.add(word);
    }
}
