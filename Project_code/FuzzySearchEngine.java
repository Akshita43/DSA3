import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FuzzySearchEngine {

    private final Trie dictionary;

    public FuzzySearchEngine(Trie dictionary) {
        this.dictionary = dictionary;
    }

    public List<Suggestion> search(String query, int maxDistance) {
        List<Suggestion> results = new ArrayList<>();

        int[] initialRow = new int[query.length() + 1];
        for (int i = 0; i <= query.length(); i++) {
            initialRow[i] = i;
        }

        StringBuilder currentWord = new StringBuilder();
        dfs(dictionary.getRoot(), query, initialRow, currentWord, maxDistance, results);

        Collections.sort(results);
        return results;
    }

    private void dfs(TrieNode node, String query, int[] previousRow,
                      StringBuilder currentWord, int maxDistance,
                      List<Suggestion> results) {

        int currentDistance = previousRow[query.length()];
        if (node.isEndOfWord && currentDistance <= maxDistance) {
            results.add(new Suggestion(currentWord.toString(), currentDistance));
        }

        if (min(previousRow) > maxDistance) {
            return;
        }

        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            char ch = entry.getKey();
            TrieNode child = entry.getValue();
            int[] currentRow = buildNextRow(previousRow, query, ch);

            currentWord.append(ch);
            dfs(child, query, currentRow, currentWord, maxDistance, results);
            currentWord.deleteCharAt(currentWord.length() - 1);
        }
    }

    private int[] buildNextRow(int[] previousRow, String query, char ch) {
        int[] currentRow = new int[query.length() + 1];
        currentRow[0] = previousRow[0] + 1;

        for (int i = 1; i <= query.length(); i++) {
            int insertCost = currentRow[i - 1] + 1;
            int deleteCost = previousRow[i] + 1;
            int substituteCost = previousRow[i - 1] + (query.charAt(i - 1) == ch ? 0 : 1);
            currentRow[i] = Math.min(Math.min(insertCost, deleteCost), substituteCost);
        }
        return currentRow;
    }

    private int min(int[] arr) {
        int m = arr[0];
        for (int v : arr) if (v < m) m = v;
        return m;
    }

    public static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                                   Math.min(dp[i][j - 1], dp[i - 1][j]));
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}