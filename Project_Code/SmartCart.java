import java.util.*;

/**
 * SmartCart.java
 * ------------------------------------------------------------
 * Main entry point / menu. Everything else lives in its own file:
 *
 *   FileLoader.java     - reads data/dictionary.txt and data/catalog.txt
 *   TrieNode.java        - shared trie node (Aho-Corasick + Autocomplete)
 *   AhoCorasick.java      - Feature 1: multi-keyword product filter
 *   KMPMatcher.java       - Feature 2: exact phrase search (KMP)
 *   EditDistance.java     - Feature 3: typo-tolerant search
 *                           (Wagner-Fischer + Damerau-Levenshtein)
 *   Autocomplete.java     - Feature 4 (NEW): prefix / "as you type" suggestions
 *   Recommender.java      - Feature 5 (NEW): "related products" suggestions
 *   Cart.java             - Feature 6 (NEW): add-to-cart / view cart
 *   SearchHistory.java    - Feature 7 (NEW): remembers recent searches
 *
 * DICTIONARY (data/dictionary.txt) = 1000+ real words: brands,
 * categories, descriptors, colors/materials, fabrics, etc.
 *
 * CATALOG (data/catalog.txt) = 300+ full product description
 * sentences built from dictionary-style words.
 *
 * Compile: javac -d out src/*.java
 * Run:     java -cp out SmartCart      (run from the project root,
 *          so the "data" folder is found next to it)
 * ------------------------------------------------------------
 */
public class SmartCart {

    static Scanner sc = new Scanner(System.in);

    static List<String> dictionary;
    static List<String> catalog;
    static Cart cart = new Cart();
    static SearchHistory history = new SearchHistory(15);
    static Autocomplete autocomplete;
    static Recommender recommender;

    public static void main(String[] args) {
        dictionary = FileLoader.loadDictionary();
        catalog = FileLoader.loadCatalog();

        System.out.println("Dictionary loaded with " + dictionary.size() + " words.");
        System.out.println("Catalog loaded with " + catalog.size() + " products.\n");

        autocomplete = new Autocomplete(dictionary);
        recommender = new Recommender(catalog);

        boolean running = true;
        while (running) {
            System.out.println("\n========= SmartCart: Shopping Assistant =========");
            System.out.println(" 1. Multi-Keyword Product Filter (Aho-Corasick)");
            System.out.println(" 2. Exact Phrase Search in Catalog (KMP)");
            System.out.println(" 3. Typo-Tolerant Word Search (Wagner-Fischer + Damerau-Levenshtein)");
            System.out.println(" 4. Autocomplete / Suggest-as-you-type");
            System.out.println(" 5. Related Products (\"You might also like\")");
            System.out.println(" 6. View / Manage Cart");
            System.out.println(" 7. View Recent Search History");
            System.out.println(" 0. Exit");
            System.out.println("===================================================");
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1: multiKeywordFilter(); break;
                case 2: kmpCatalogSearch(); break;
                case 3: typoTolerantSearch(); break;
                case 4: autocompleteMenu(); break;
                case 5: relatedProductsMenu(); break;
                case 6: cartMenu(); break;
                case 7: history.print(); break;
                case 0: running = false; break;
                default: System.out.println("Invalid choice.");
            }
        }
        System.out.println("Thanks for shopping with SmartCart!");
    }

    // ================================================================
    // FEATURE 1: Multi-Keyword Product Filter via Aho-Corasick
    // ================================================================
    static void multiKeywordFilter() {
        System.out.println("\nCatalog has " + catalog.size() + " products. Showing first 15:");
        for (int i = 0; i < Math.min(15, catalog.size()); i++) System.out.println("  - " + catalog.get(i));

        int k = readInt("\nHow many keywords do you want to filter by? ");
        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            keywords.add(readLine("Keyword " + (i + 1) + ": ").toLowerCase());
        }
        history.record("keyword-filter", String.join(", ", keywords));

        AhoCorasick automaton = new AhoCorasick(keywords);

        System.out.println("\n--- Matching Products ---");
        boolean anyMatch = false;
        for (String product : catalog) {
            Set<String> hits = automaton.search(product);
            if (!hits.isEmpty()) {
                System.out.println("\"" + product + "\"  -> matched keywords: " + hits);
                anyMatch = true;
            }
        }
        if (!anyMatch) System.out.println("No products matched any of the given keywords.");
    }

    // ================================================================
    // FEATURE 2: Exact Phrase Search in Catalog via KMP
    // ================================================================
    static void kmpCatalogSearch() {
        String pattern = readLine("\nEnter an exact phrase to search for: ").toLowerCase();
        history.record("phrase-search", pattern);

        boolean anyMatch = false;
        for (String product : catalog) {
            List<Integer> matches = KMPMatcher.search(product, pattern);
            if (!matches.isEmpty()) {
                System.out.println("\"" + product + "\"  -> found at index(es) " + matches);
                anyMatch = true;
            }
        }
        if (!anyMatch) System.out.println("Phrase not found in any catalog entry.");
    }

    // ================================================================
    // FEATURE 3: Typo-Tolerant Word Search (now shows top 3 suggestions)
    // ================================================================
    static void typoTolerantSearch() {
        String query = readLine("\nSearch for a word (e.g. a mistyped brand/category): ").toLowerCase();
        history.record("typo-search", query);

        if (dictionary.contains(query)) {
            System.out.println("Exact match found in dictionary: \"" + query + "\"");
            return;
        }

        List<EditDistance.Suggestion> top = EditDistance.topMatches(query, dictionary, 3);

        System.out.println("No exact match for \"" + query + "\".");
        System.out.println("Did you mean one of these?");
        int rank = 1;
        for (EditDistance.Suggestion s : top) {
            System.out.println("  " + rank + ". \"" + s.word + "\"  " +
                    "(Wagner-Fischer: " + s.wagnerFischerDistance +
                    ", Damerau-Levenshtein: " + s.damerauDistance + ")");
            rank++;
        }
    }

    // ================================================================
    // FEATURE 4 (NEW): Autocomplete / prefix suggestions
    // ================================================================
    static void autocompleteMenu() {
        String prefix = readLine("\nStart typing a word (prefix): ").toLowerCase();
        history.record("autocomplete", prefix);

        List<String> suggestions = autocomplete.suggest(prefix, 10);
        if (suggestions.isEmpty()) {
            System.out.println("No dictionary words start with \"" + prefix + "\".");
        } else {
            System.out.println("Suggestions for \"" + prefix + "\":");
            for (String s : suggestions) System.out.println("  - " + s);
        }
    }

    // ================================================================
    // FEATURE 5 (NEW): Related products ("You might also like")
    // ================================================================
    static void relatedProductsMenu() {
        System.out.println("\nCatalog has " + catalog.size() + " products. Showing first 15:");
        for (int i = 0; i < Math.min(15, catalog.size()); i++) System.out.println("  - " + catalog.get(i));

        String query = readLine("\nEnter a product (or part of one) to get recommendations for: ").toLowerCase();
        history.record("recommend", query);

        int idx = recommender.findProductIndex(query);
        if (idx == -1) {
            System.out.println("Could not find that product in the catalog.");
            return;
        }
        System.out.println("Because you looked at: \"" + catalog.get(idx) + "\"");
        List<String> related = recommender.recommend(idx, 5);
        if (related.isEmpty()) {
            System.out.println("No related products found.");
        } else {
            System.out.println("You might also like:");
            for (String r : related) System.out.println("  - " + r);
        }
    }

    // ================================================================
    // FEATURE 6 (NEW): Cart management
    // ================================================================
    static void cartMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Cart Menu ---");
            System.out.println(" 1. Add product to cart");
            System.out.println(" 2. Remove product from cart");
            System.out.println(" 3. View cart");
            System.out.println(" 4. Clear cart");
            System.out.println(" 0. Back to main menu");
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1: {
                    String query = readLine("Enter a product (or part of one) to add: ").toLowerCase();
                    int idx = recommender.findProductIndex(query);
                    if (idx == -1) {
                        System.out.println("Could not find that product in the catalog.");
                        break;
                    }
                    int qty = readInt("Quantity: ");
                    cart.add(catalog.get(idx), Math.max(1, qty));
                    System.out.println("Added \"" + catalog.get(idx) + "\" to cart.");
                    break;
                }
                case 2: {
                    String query = readLine("Enter the exact product line to remove: ").toLowerCase();
                    System.out.println(cart.remove(query) ? "Removed." : "That item wasn't in your cart.");
                    break;
                }
                case 3: cart.print(); break;
                case 4: cart.clear(); System.out.println("Cart cleared."); break;
                case 0: back = true; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------- input helpers ----------------
    static int readInt(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextInt()) { sc.next(); System.out.print("Enter a valid integer: "); }
        int v = sc.nextInt();
        sc.nextLine();
        return v;
    }

    static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}
