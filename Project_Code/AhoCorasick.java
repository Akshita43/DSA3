import java.util.*;

/**
 * AhoCorasick.java
 * ------------------------------------------------------------
 * FEATURE 1: Multi-Keyword Product Filter.
 * Builds a Trie of the chosen keywords, links "fail" pointers
 * (the Aho-Corasick automaton), then scans catalog text in a
 * single pass to report every keyword that occurs in it.
 * ------------------------------------------------------------
 */
public class AhoCorasick {

    private final TrieNode root;

    public AhoCorasick(List<String> patterns) {
        this.root = new TrieNode();
        for (String p : patterns) {
            root.insert(p);
        }
        buildFailureLinks();
    }

    private void buildFailureLinks() {
        Queue<TrieNode> queue = new LinkedList<>();
        for (int c = 0; c < TrieNode.ALPHA; c++) {
            if (root.children[c] != null) {
                root.children[c].fail = root;
                queue.add(root.children[c]);
            }
        }
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            for (int c = 0; c < TrieNode.ALPHA; c++) {
                TrieNode child = current.children[c];
                if (child == null) continue;
                TrieNode failNode = current.fail;
                while (failNode != null && failNode.children[c] == null) failNode = failNode.fail;
                child.fail = (failNode == null) ? root : failNode.children[c];
                if (child.fail == null) child.fail = root;
                child.output.addAll(child.fail.output);
                queue.add(child);
            }
        }
    }

    /** Returns every pattern that occurs anywhere inside the given text. */
    public Set<String> search(String text) {
        Set<String> found = new LinkedHashSet<>();
        TrieNode node = root;
        for (int i = 0; i < text.length(); i++) {
            int c = text.charAt(i) - 'a';
            if (c < 0 || c >= TrieNode.ALPHA) { node = root; continue; }
            while (node != root && node.children[c] == null) node = node.fail;
            if (node.children[c] != null) node = node.children[c];
            found.addAll(node.output);
        }
        return found;
    }
}
