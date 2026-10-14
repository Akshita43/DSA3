import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * AhoCorasick
 * -----------
 * Multi-pattern string matcher: given several search keywords, builds a
 * single automaton (a Trie of all keywords plus "failure links") and scans
 * each text ONCE to find every keyword that appears in it - instead of
 * scanning the text once per keyword (which is what repeating Rabin-Karp
 * for each keyword would require).
 *
 * How it works:
 *   1. Insert every pattern into a Trie, same as Trie.java, but each
 *      end-of-word node also remembers WHICH pattern(s) end there.
 *   2. Build failure links with a BFS over the Trie (like a Trie-based
 *      KMP): a node's failure link points to the longest proper suffix
 *      of its path that is also a prefix of some pattern - so that when
 *      a character mismatches, we can jump to that suffix instead of
 *      restarting from the root. This is what makes the scan linear in
 *      the text length regardless of how many patterns there are.
 *   3. Searching: walk the text character by character; on a mismatch,
 *      follow failure links until a match is found or we reach the root.
 *      At every node, all patterns ending there (its own outputs plus
 *      everything inherited via failure links) are reported.
 */
public class AhoCorasick {

    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node fail;
        List<String> outputs = new ArrayList<>();
    }

    private final Node root = new Node();
    private boolean built = false;

    public void addPattern(String pattern) {
        Node current = root;
        for (char ch : pattern.toCharArray()) {
            current = current.children.computeIfAbsent(ch, c -> new Node());
        }
        current.outputs.add(pattern);
        built = false;
    }

    /** Must be called once after all patterns are added, before search(). */
    public void build() {
        Queue<Node> queue = new LinkedList<>();
        root.fail = root;
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();

                Node f = current.fail;
                while (f != root && !f.children.containsKey(ch)) {
                    f = f.fail;
                }
                Node candidate = f.children.get(ch);
                child.fail = (candidate != null && candidate != child) ? candidate : root;
                child.outputs.addAll(child.fail.outputs);
                queue.add(child);
            }
        }
        built = true;
    }

    /** Returns every pattern found anywhere in `text`, in one linear pass. */
    public Set<String> search(String text) {
        if (!built) build();
        Set<String> found = new LinkedHashSet<>();
        Node current = root;
        for (char ch : text.toCharArray()) {
            while (current != root && !current.children.containsKey(ch)) {
                current = current.fail;
            }
            current = current.children.getOrDefault(ch, root);
            found.addAll(current.outputs);
        }
        return found;
    }
}