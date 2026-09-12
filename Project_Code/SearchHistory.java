import java.util.*;

/**
 * SearchHistory.java  (NEW FEATURE)
 * ------------------------------------------------------------
 * Remembers the last N things the user searched for, tagged by
 * which feature they used (keyword filter, phrase search, typo
 * search, autocomplete...). Lets the user glance back at what
 * they already tried this session.
 * ------------------------------------------------------------
 */
public class SearchHistory {

    private static class Entry {
        final String type;
        final String query;
        Entry(String type, String query) { this.type = type; this.query = query; }
    }

    private final Deque<Entry> history = new ArrayDeque<>();
    private final int capacity;

    public SearchHistory(int capacity) {
        this.capacity = capacity;
    }

    public void record(String type, String query) {
        if (history.size() == capacity) history.removeLast();
        history.addFirst(new Entry(type, query));
    }

    public void print() {
        if (history.isEmpty()) {
            System.out.println("No searches yet this session.");
            return;
        }
        System.out.println("\n--- Recent Searches (most recent first) ---");
        int i = 1;
        for (Entry e : history) {
            System.out.println(i + ". [" + e.type + "] \"" + e.query + "\"");
            i++;
        }
    }
}
