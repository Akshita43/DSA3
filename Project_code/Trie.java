import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trie {
    private final TrieNode root;
    private int wordCount = 0;

    public Trie() {
        root = new TrieNode();
    }

    public TrieNode getRoot() {
        return root;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void insert(String word) {
        if (word == null || word.isEmpty()) return;
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            current = current.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        if (!current.isEndOfWord) {
            current.isEndOfWord = true;
            wordCount++;
        }
    }

    public void loadFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty() && !word.startsWith("#")) {
                    insert(word);
                }
            }
        }
    }

    public boolean containsExact(String word) {
        TrieNode node = walkTo(word);
        return node != null && node.isEndOfWord;
    }

    /** Walks the trie following `word`'s characters, returns the node
     *  reached, or null if the path breaks before the word ends. */
    private TrieNode walkTo(String word) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            current = current.children.get(ch);
            if (current == null) return null;
        }
        return current;
    }

    /**
     * Autocomplete: returns up to `limit` dictionary words that start with
     * `prefix`. This is a natural extension of the Trie - since all words
     * sharing a prefix live under the same node, we just walk to that node
     * once (O(prefix length)) and then collect everything below it.
     */
    public List<String> getWordsWithPrefix(String prefix, int limit) {
        List<String> results = new ArrayList<>();
        TrieNode node = walkTo(prefix);
        if (node == null) return results;
        collectWords(node, new StringBuilder(prefix), results, limit);
        return results;
    }

    public void printTree() {
        System.out.println("(root)");
        printNode(root, "");
    }

    private void printNode(TrieNode node, String prefix) {
        int i = 0;
        int size = node.children.size();
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            i++;
            boolean isLast = (i == size);
            char ch = entry.getKey();
            TrieNode child = entry.getValue();

            System.out.println(prefix + (isLast ? "+-- " : "|-- ") + ch + (child.isEndOfWord ? "  [word end]" : ""));
            printNode(child, prefix + (isLast ? "    " : "|   "));
        }
    }

    public List<String> getAllWords() {
        List<String> words = new ArrayList<>();
        collectWords(root, new StringBuilder(), words, Integer.MAX_VALUE);
        return words;
    }

    private void collectWords(TrieNode node, StringBuilder prefix, List<String> words, int limit) {
        if (words.size() >= limit) return;
        if (node.isEndOfWord) {
            words.add(prefix.toString());
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            if (words.size() >= limit) return;
            prefix.append(entry.getKey());
            collectWords(entry.getValue(), prefix, words, limit);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }
}