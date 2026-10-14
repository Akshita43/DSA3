import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * SearchHistory
 * -------------
 * Tracks how many times each (corrected) search term has been looked up,
 * using a HashMap as a frequency counter. Powers a simple "Trending
 * Searches" feature, similar to what shopping sites show under their
 * search bar.
 */
public class SearchHistory {

    private final Map<String, Integer> frequency = new HashMap<>();

    public void record(String term) {
        if (term == null || term.isEmpty()) return;
        frequency.merge(term.toLowerCase(), 1, Integer::sum);
    }

    /** Returns the top `limit` most-searched terms, most frequent first. */
    public List<Map.Entry<String, Integer>> topSearches(int limit) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(frequency.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        return entries.subList(0, Math.min(limit, entries.size()));
    }
}