import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Main
 * ----
 * "A Dictionary-Based Fuzzy Search and Spell Correction Engine
 *  for Smart Shopping"
 *
 * Combines, across the four algorithm modules:
 *   Module 2 (String Algorithms) - Rabin-Karp rolling hash (keyword search),
 *                                   Aho-Corasick multi-pattern matching (multi-keyword search)
 *   Module 3 (Advanced DP)       - Trie + Levenshtein DP (Wagner-Fischer) fuzzy search/spell correction
 *   Module 4 (Network Flow)      - Edmonds-Karp max-flow (warehouse fulfillment for the cart)
 *   Plus: Binary Search (price range), Heap/PriorityQueue (top-N), HashMap (cart, trending, word index)
 */
public class Main {

    private static final SearchHistory history = new SearchHistory();
    private static final ShoppingCart cart = new ShoppingCart();

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        ProductCatalog catalog = new ProductCatalog();
        catalog.loadFromCsv(args.length > 0 ? args[0] : "products.csv");

        // Word-level index: fixes fuzzy search / autocomplete working only
        // on WHOLE product names by also indexing each individual word.
        // wordEngine corrects against this catalog-derived vocabulary, so
        // every suggestion it returns is guaranteed to map back to a real
        // product (needed by the product-lookup logic below).
        WordIndex wordIndex = new WordIndex();
        wordIndex.build(catalog);
        FuzzySearchEngine wordEngine = new FuzzySearchEngine(wordIndex.getWordTrie());

        // Dictionary Trie: a broader shopping-domain vocabulary (catalog
        // words plus common brand/color/material/category terms), loaded
        // from a plain word list the way Trie.loadFromFile is designed for.
        // Used for the standalone spell-correction comparison (option 14),
        // where suggestions don't need to resolve to a specific product.
        Trie dictionaryTrie = new Trie();
        try {
            dictionaryTrie.loadFromFile("dictionary.txt");
        } catch (IOException e) {
            System.out.println("(dictionary.txt not found - falling back to catalog-only vocabulary)");
        }
        if (dictionaryTrie.getWordCount() == 0) {
            for (String w : wordIndex.getWordTrie().getAllWords()) dictionaryTrie.insert(w);
        }
        FuzzySearchEngine dictionaryEngine = new FuzzySearchEngine(dictionaryTrie);

        // Category spell correction
        Trie categoryTrie = new Trie();
        Map<String, String> categoryByLower = new LinkedHashMap<>();
        for (String c : catalog.getAllCategories()) {
            categoryTrie.insert(c.toLowerCase());
            categoryByLower.put(c.toLowerCase(), c);
        }
        FuzzySearchEngine categoryEngine = new FuzzySearchEngine(categoryTrie);

        System.out.println("=================================================");
        System.out.println(" Dictionary-Based Fuzzy Search & Spell Correction");
        System.out.println("          Engine for Smart Shopping");
        System.out.println("=================================================");
        System.out.println("Catalog loaded: " + catalog.size() + " items across "
                + catalog.getAllCategories().size() + " categories");
        System.out.println("Dictionary loaded: " + dictionaryTrie.getWordCount() + " shopping-domain terms");

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        while (true) {
            printMenu();
            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "16";

            switch (choice) {
                case "1" -> searchItem(scanner, catalog, wordIndex, wordEngine);
                case "2" -> browseAll(catalog);
                case "3" -> browseByCategory(scanner, catalog, categoryEngine, categoryByLower);
                case "4" -> autocomplete(scanner, catalog, wordIndex);
                case "5" -> keywordSearch(scanner, catalog);
                case "6" -> multiKeywordSearch(scanner, catalog);
                case "7" -> trendingSearches();
                case "8" -> priceRangeSearch(scanner, catalog);
                case "9" -> topNSearch(scanner, catalog);
                case "10" -> cartMenu(scanner, catalog);
                case "11" -> warehouseCheck(catalog);
                case "12" -> {
                    System.out.println("\n--- Catalog Structure (Trie) ---");
                    catalog.getNameTrie().printTree();
                }
                case "13" -> exactPhraseSearch(scanner, catalog);
                case "14" -> spellCorrectionComparison(scanner, dictionaryEngine);
                case "15" -> algorithmBenchmark(scanner, catalog);
                case "16" -> {
                    scanner.close();
                    System.out.println("Thank you for shopping with us!");
                    return;
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1.  Search for an item (with spell correction)");
        System.out.println("2.  Browse all items");
        System.out.println("3.  Browse items by category");
        System.out.println("4.  Autocomplete (type a prefix)");
        System.out.println("5.  Keyword / substring search (Rabin-Karp)");
        System.out.println("6.  Multi-keyword search (Aho-Corasick)");
        System.out.println("7.  Trending searches");
        System.out.println("8.  Search by price range (Binary Search)");
        System.out.println("9.  Top-N cheapest / most expensive (Heap)");
        System.out.println("10. Shopping cart");
        System.out.println("11. Check warehouse fulfillment for cart (Max-Flow)");
        System.out.println("12. View catalog structure (Trie)");
        System.out.println("13. Exact phrase search in descriptions (KMP)");
        System.out.println("14. Spell-check comparison: Wagner-Fischer vs Damerau-Levenshtein");
        System.out.println("15. Algorithm comparison / benchmark");
        System.out.println("16. Exit");
        System.out.print("Choose an option: ");
    }

    // ---------- 1. Search with word-level spell correction ----------
    private static void searchItem(Scanner scanner, ProductCatalog catalog,
                                    WordIndex wordIndex, FuzzySearchEngine wordEngine) {
        System.out.print("Enter item name: ");
        String query = scanner.nextLine().trim().toLowerCase();
        if (query.isEmpty()) return;

        history.record(query);

        // Fast path: query is already the exact full product name
        if (catalog.getNameTrie().containsExact(query)) {
            System.out.println("\nItem found:");
            System.out.println(catalog.getProduct(query).toDetailString());
            return;
        }

        // Otherwise, correct each WORD independently (fixes "opa chrger"
        // style typos that don't resemble any single full product name).
        // For each token, gather a few close candidates rather than just
        // the single closest one, then try combinations to find one that
        // actually matches real products - this avoids picking a
        // technically-closer word (by tie-break) that leads nowhere.
        List<String> tokens = WordIndex.tokenize(query);
        List<List<String>> candidatesPerToken = new ArrayList<>();
        boolean anyTokenNeededCorrection = false;

        for (String token : tokens) {
            if (wordIndex.getWordTrie().containsExact(token)) {
                candidatesPerToken.add(List.of(token));
            } else {
                int maxDistance = maxDistanceFor(token);
                List<Suggestion> suggestions = wordEngine.search(token, maxDistance);
                if (!suggestions.isEmpty()) {
                    anyTokenNeededCorrection = true;
                    List<String> top = new ArrayList<>();
                    for (int i = 0; i < Math.min(6, suggestions.size()); i++) {
                        top.add(suggestions.get(i).word);
                    }
                    candidatesPerToken.add(top);
                } else {
                    candidatesPerToken.add(List.of(token)); // no correction found, keep as-is
                }
            }
        }

        // Try combinations of candidates (best-first) to find one that
        // actually returns matching products. Capped to avoid blow-up on
        // long multi-word queries with many ambiguous tokens.
        List<String> correctedTokens = null;
        List<String> matches = new ArrayList<>();
        int combosTried = 0;
        for (List<String> combo : cartesianProduct(candidatesPerToken)) {
            if (++combosTried > 500) break;
            List<String> found = wordIndex.getProductsContainingAll(combo);
            if (!found.isEmpty()) {
                correctedTokens = combo;
                matches = found;
                break;
            }
        }

        boolean corrected = anyTokenNeededCorrection;
        boolean partial = false;
        if (correctedTokens == null) {
            // No combination matched all tokens - fall back to the single
            // closest candidate per token, and match ANY of them instead.
            correctedTokens = new ArrayList<>();
            for (List<String> candidates : candidatesPerToken) {
                correctedTokens.add(candidates.get(0));
            }
            matches = wordIndex.getProductsContainingAny(correctedTokens);
            partial = true;
        }


        if (matches.isEmpty()) {
            System.out.println("  No item found matching \"" + query + "\", and no close spelling match either.");
            return;
        }

        if (corrected) {
            System.out.println("  Did you mean: \"" + String.join(" ", correctedTokens) + "\"?");
        }

        System.out.println("\n" + (partial ? "Closest matches" : "Matching items") + " (" + matches.size() + "):");
        int shown = 0;
        for (String name : matches) {
            if (shown >= 10) {
                System.out.println("  ... and " + (matches.size() - shown) + " more");
                break;
            }
            System.out.println("  " + catalog.getProduct(name));
            shown++;
        }

        if (matches.size() == 1) {
            System.out.println();
            System.out.println(catalog.getProduct(matches.get(0)).toDetailString());
        }
    }

    // ---------- 2. Browse everything ----------
    private static void browseAll(ProductCatalog catalog) {
        System.out.println("\n--- All Items (" + catalog.size() + ") ---");
        for (Product p : catalog.getAllProducts()) {
            System.out.println("  " + p);
        }
    }

    // ---------- 3. Browse by category (with spell correction) ----------
    private static void browseByCategory(Scanner scanner, ProductCatalog catalog,
                                          FuzzySearchEngine categoryEngine, Map<String, String> categoryByLower) {
        TreeSet<String> categories = catalog.getAllCategories();
        System.out.println("\nAvailable categories:");
        int i = 1;
        for (String c : categories) {
            System.out.println("  " + (i++) + ". " + c);
        }
        System.out.print("Type a category name: ");
        String input = scanner.nextLine().trim().toLowerCase();

        String resolved = categoryByLower.get(input);

        if (resolved == null) {
            int maxDistance = maxDistanceFor(input);
            List<Suggestion> suggestions = categoryEngine.search(input, maxDistance);
            if (!suggestions.isEmpty()) {
                String correctedLower = suggestions.get(0).word;
                resolved = categoryByLower.get(correctedLower);
                System.out.println("  Did you mean category: \"" + resolved + "\"?");
            }
        }

        if (resolved == null) {
            System.out.println("  No category found with that name.");
            return;
        }

        List<Product> items = catalog.getCategoryProductObjects(resolved);
        System.out.println("\n--- " + resolved + " (" + items.size() + " items) ---");
        for (Product p : items) {
            System.out.println("  " + p);
        }
    }

    // ---------- 4. Autocomplete (word-level) ----------
    private static void autocomplete(Scanner scanner, ProductCatalog catalog, WordIndex wordIndex) {
        System.out.print("Start typing an item name: ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.isEmpty()) return;

        List<String> tokens = WordIndex.tokenize(input);
        String lastToken = tokens.get(tokens.size() - 1);
        List<String> precedingTokens = tokens.subList(0, tokens.size() - 1);

        List<String> completions = wordIndex.getWordTrie().getWordsWithPrefix(lastToken, 15);
        if (completions.isEmpty()) {
            System.out.println("  No items start with \"" + input + "\"");
            return;
        }

        Set<String> shown = new LinkedHashSet<>();
        System.out.println("  Suggestions:");
        for (String completion : completions) {
            List<String> requiredWords = new ArrayList<>(precedingTokens);
            requiredWords.add(completion);

            for (String name : wordIndex.getProductsContainingAll(requiredWords)) {
                if (shown.add(name)) {
                    System.out.println("    - " + catalog.getProduct(name));
                }
                if (shown.size() >= 10) break;
            }
            if (shown.size() >= 10) break;
        }

        if (shown.isEmpty()) {
            System.out.println("  No items match \"" + input + "\" with any completion.");
        }
    }

    // ---------- 5. Keyword / substring search (Rabin-Karp) ----------
    private static void keywordSearch(Scanner scanner, ProductCatalog catalog) {
        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();
        if (keyword.isEmpty()) return;

        List<String> allNames = catalog.getAllNames();
        List<String> matches = RabinKarp.searchDictionary(allNames, keyword);

        if (matches.isEmpty()) {
            System.out.println("  No items contain \"" + keyword + "\"");
        } else {
            System.out.println("  Items containing \"" + keyword + "\":");
            int shown = 0;
            for (String name : matches) {
                if (shown++ >= 15) {
                    System.out.println("  ... and " + (matches.size() - shown + 1) + " more");
                    break;
                }
                System.out.println("    - " + catalog.getProduct(name));
            }
        }
    }

    // ---------- 6. Multi-keyword search (Aho-Corasick) ----------
    private static void multiKeywordSearch(Scanner scanner, ProductCatalog catalog) {
        System.out.print("Enter keywords, comma-separated (e.g. shoes, black, nike): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.isEmpty()) return;

        List<String> keywords = new ArrayList<>();
        for (String k : input.split(",")) {
            String trimmed = k.trim();
            if (!trimmed.isEmpty()) keywords.add(trimmed);
        }
        if (keywords.isEmpty()) return;

        AhoCorasick automaton = new AhoCorasick();
        for (String k : keywords) automaton.addPattern(k);
        automaton.build();

        System.out.println("  Items matching any of " + keywords + ":");
        int shown = 0;
        for (String name : catalog.getAllNames()) {
            Set<String> hits = automaton.search(name);
            if (!hits.isEmpty()) {
                if (shown++ >= 15) {
                    System.out.println("  ... more matches exist, refine your keywords to narrow down");
                    break;
                }
                System.out.println("    - " + catalog.getProduct(name) + "   [matched: " + hits + "]");
            }
        }
        if (shown == 0) {
            System.out.println("  No items matched any of the given keywords.");
        }
    }

    // ---------- 7. Trending searches ----------
    private static void trendingSearches() {
        List<Map.Entry<String, Integer>> top = history.topSearches(5);
        if (top.isEmpty()) {
            System.out.println("  No searches recorded yet this session.");
        } else {
            System.out.println("\n--- Trending Searches ---");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : top) {
                System.out.println("  " + rank++ + ". " + entry.getKey() + "  (" + entry.getValue() + " searches)");
            }
        }
    }

    // ---------- 8. Price range search (Binary Search) ----------
    private static void priceRangeSearch(Scanner scanner, ProductCatalog catalog) {
        try {
            System.out.print("Minimum price: ");
            double min = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Maximum price: ");
            double max = Double.parseDouble(scanner.nextLine().trim());

            List<Product> sorted = catalog.getSortedByPrice();
            List<Product> results = PriceSearch.searchByRange(sorted, min, max);

            if (results.isEmpty()) {
                System.out.println("  No items found between Rs." + min + " and Rs." + max);
            } else {
                System.out.println("\n--- Items between Rs." + min + " and Rs." + max + " ---");
                int shown = 0;
                for (Product p : results) {
                    if (shown++ >= 20) {
                        System.out.println("  ... and " + (results.size() - shown + 1) + " more");
                        break;
                    }
                    System.out.println("  " + p);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("  Please enter valid numbers.");
        }
    }

    // ---------- 9. Top-N cheapest / most expensive (Heap) ----------
    private static void topNSearch(Scanner scanner, ProductCatalog catalog) {
        TreeSet<String> categories = catalog.getAllCategories();
        System.out.println("\nAvailable categories:");
        for (String c : categories) {
            System.out.println("  - " + c);
        }
        System.out.print("Category (or 'all' for entire catalog): ");
        String categoryInput = scanner.nextLine().trim();

        List<Product> pool = categoryInput.equalsIgnoreCase("all")
                ? catalog.getAllProducts()
                : catalog.getCategoryProductObjects(resolveCategoryCaseInsensitive(categoryInput, categories));

        if (pool.isEmpty()) {
            System.out.println("  No items found for that category.");
            return;
        }

        System.out.print("Cheapest or most expensive? (cheap/expensive): ");
        String mode = scanner.nextLine().trim().toLowerCase();
        System.out.print("How many items (N)? ");
        int n;
        try {
            n = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Please enter a valid number.");
            return;
        }

        List<Product> result = mode.startsWith("cheap")
                ? TopNFinder.cheapest(pool, n)
                : TopNFinder.mostExpensive(pool, n);

        System.out.println("\n--- Top " + result.size() + " (" + mode + ") ---");
        for (Product p : result) {
            System.out.println("  " + p);
        }
    }

    // ---------- 10. Shopping cart ----------
    private static void cartMenu(Scanner scanner, ProductCatalog catalog) {
        while (true) {
            System.out.println("\n--- Shopping Cart ---");
            System.out.println("1. Add item");
            System.out.println("2. View cart & total");
            System.out.println("3. Remove item");
            System.out.println("4. Back to main menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.print("Item name (exact, use option 1 to find it first): ");
                    String name = scanner.nextLine().trim().toLowerCase();
                    if (!catalog.getNameTrie().containsExact(name)) {
                        System.out.println("  No such item in catalog. Try searching first (option 1 in main menu).");
                        break;
                    }
                    System.out.print("Quantity: ");
                    try {
                        int qty = Integer.parseInt(scanner.nextLine().trim());
                        cart.addItem(name, qty);
                        System.out.println("  Added " + qty + " x " + name + " to cart.");
                    } catch (NumberFormatException e) {
                        System.out.println("  Please enter a valid quantity.");
                    }
                }
                case "2" -> {
                    if (cart.isEmpty()) {
                        System.out.println("  Your cart is empty.");
                    } else {
                        cart.printCartAndGetTotal(catalog);
                    }
                }
                case "3" -> {
                    System.out.print("Item name to remove: ");
                    String name = scanner.nextLine().trim().toLowerCase();
                    System.out.println(cart.removeItem(name) ? "  Removed." : "  Item not in cart.");
                }
                case "4" -> {
                    return;
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }

    // ---------- 11. Warehouse fulfillment check (Max-Flow) ----------
    private static void warehouseCheck(ProductCatalog catalog) {
        if (cart.isEmpty()) {
            System.out.println("  Your cart is empty. Add items first (option 10).");
            return;
        }
        WarehouseAllocation.run(cart.getItems());
    }

    // ---------- 13. Exact phrase search in descriptions (KMP) ----------
    private static void exactPhraseSearch(Scanner scanner, ProductCatalog catalog) {
        System.out.print("Enter exact phrase to find inside product descriptions: ");
        String phrase = scanner.nextLine().trim().toLowerCase();
        if (phrase.isEmpty()) return;

        int shown = 0;
        int totalMatches = 0;
        for (Product p : catalog.getAllProducts()) {
            List<Integer> positions = KMP.search(p.description.toLowerCase(), phrase);
            if (!positions.isEmpty()) {
                totalMatches++;
                if (shown++ < 15) {
                    System.out.println("    - " + p + "   [found at char index " + positions.get(0) + "]");
                }
            }
        }
        if (totalMatches == 0) {
            System.out.println("  No description contains the exact phrase \"" + phrase + "\"");
        } else if (totalMatches > 15) {
            System.out.println("  ... and " + (totalMatches - 15) + " more descriptions match");
        }
    }

    // ---------- 14. Spell-check comparison: Wagner-Fischer vs Damerau-Levenshtein ----------
    private static void spellCorrectionComparison(Scanner scanner, FuzzySearchEngine dictionaryEngine) {
        System.out.print("Enter a (possibly misspelled) word to compare correction methods: ");
        String query = scanner.nextLine().trim().toLowerCase();
        if (query.isEmpty()) return;

        int maxDistance = maxDistanceFor(query) + 1;
        List<Suggestion> wagnerFischerHits = dictionaryEngine.search(query, maxDistance);

        // Re-rank the same candidate pool using Damerau-Levenshtein so the
        // two algorithms are compared on identical candidates.
        List<Suggestion> damerauHits = new ArrayList<>();
        for (Suggestion s : wagnerFischerHits) {
            damerauHits.add(new Suggestion(s.word, DamerauLevenshtein.distance(query, s.word)));
        }
        java.util.Collections.sort(damerauHits);

        System.out.println("\n  Query: \"" + query + "\"");
        System.out.println("  ---------------------------------------------------------------");
        System.out.printf("  %-20s %-22s %-22s%n", "Candidate", "Wagner-Fischer dist.", "Damerau-Lev. dist.");
        System.out.println("  ---------------------------------------------------------------");
        int shown = 0;
        for (Suggestion s : wagnerFischerHits) {
            if (shown++ >= 8) break;
            int dl = DamerauLevenshtein.distance(query, s.word);
            String flag = dl < s.distance ? "  <- transposition detected" : "";
            System.out.printf("  %-20s %-22d %-22d%s%n", s.word, s.distance, dl, flag);
        }
        if (wagnerFischerHits.isEmpty()) {
            System.out.println("  No dictionary word found within edit distance " + maxDistance + ".");
        } else {
            System.out.println("\n  Best Wagner-Fischer suggestion : " + wagnerFischerHits.get(0));
            System.out.println("  Best Damerau-Levenshtein sugg. : " + damerauHits.get(0));
            System.out.println("  (Damerau-Levenshtein additionally credits swapped-adjacent-letter typos,");
            System.out.println("   e.g. \"form\"<->\"from\", as a single edit instead of two.)");
        }
    }

    // ---------- 15. Algorithm comparison / benchmark ----------
    private static void algorithmBenchmark(Scanner scanner, ProductCatalog catalog) {
        List<Product> products = catalog.getAllProducts();
        List<String> descriptions = new ArrayList<>();
        for (Product p : products) descriptions.add(p.description.toLowerCase());

        System.out.print("Enter a keyword to benchmark exact-match algorithms on (e.g. cotton): ");
        String keyword = scanner.hasNextLine() ? scanner.nextLine().trim().toLowerCase() : "cotton";
        if (keyword.isEmpty()) keyword = "cotton";

        // --- Rabin-Karp: one pattern, rolling hash, scans each description ---
        long t0 = System.nanoTime();
        int rkHits = 0;
        for (String d : descriptions) {
            if (RabinKarp.containsSubstring(d, keyword)) rkHits++;
        }
        long rkTime = System.nanoTime() - t0;

        // --- KMP: one pattern, LPS-based, scans each description ---
        t0 = System.nanoTime();
        int kmpHits = 0;
        for (String d : descriptions) {
            if (KMP.contains(d, keyword)) kmpHits++;
        }
        long kmpTime = System.nanoTime() - t0;

        // --- Aho-Corasick: built for many patterns, used here with one to
        //     keep the comparison fair; its real advantage is multi-keyword
        //     search in a single pass (see option 6). ---
        t0 = System.nanoTime();
        AhoCorasick automaton = new AhoCorasick();
        automaton.addPattern(keyword);
        automaton.build();
        int acHits = 0;
        for (String d : descriptions) {
            if (!automaton.search(d).isEmpty()) acHits++;
        }
        long acTime = System.nanoTime() - t0;

        System.out.println("\n  Exact-match algorithm comparison over " + descriptions.size() + " descriptions");
        System.out.println("  keyword = \"" + keyword + "\"");
        System.out.println("  -------------------------------------------------------------------------------");
        System.out.printf("  %-14s %-10s %-14s %-30s%n", "Algorithm", "Matches", "Time (ms)", "Time complexity");
        System.out.println("  -------------------------------------------------------------------------------");
        System.out.printf("  %-14s %-10d %-14.3f %-30s%n", "Rabin-Karp", rkHits, rkTime / 1_000_000.0, "O(n+m) avg, O(n*m) worst");
        System.out.printf("  %-14s %-10d %-14.3f %-30s%n", "KMP", kmpHits, kmpTime / 1_000_000.0, "O(n+m) worst-case");
        System.out.printf("  %-14s %-10d %-14.3f %-30s%n", "Aho-Corasick", acHits, acTime / 1_000_000.0, "O(n+m+z), best for MANY patterns");
        System.out.println();
        System.out.println("  Note: for a single keyword, KMP and Rabin-Karp are typically fastest.");
        System.out.println("  Aho-Corasick's advantage only shows up when searching for many keywords");
        System.out.println("  at once (option 6), since it scans the text ONCE regardless of pattern count,");
        System.out.println("  while KMP/Rabin-Karp would need one full pass PER keyword.");

        // --- Spell-correction distance comparison summary ---
        System.out.println("\n  Edit-distance algorithm comparison (spell correction):");
        System.out.println("  -------------------------------------------------------------------------------");
        System.out.printf("  %-22s %-16s %-40s%n", "Algorithm", "Complexity", "Notes");
        System.out.printf("  %-22s %-16s %-40s%n", "Wagner-Fischer", "O(n*m)", "insert/delete/substitute only");
        System.out.printf("  %-22s %-16s %-40s%n", "Damerau-Levenshtein", "O(n*m)", "also credits adjacent transpositions");
        System.out.println("  See option 14 to compare both on a specific misspelled word.");
    }

    // ---------- Helpers ----------
    /** Generates every combination of one item from each list, e.g.
     *  [[a,b],[c,d]] -> [a,c],[a,d],[b,c],[b,d]. Used to try candidate
     *  spelling corrections in combination until one actually matches. */
    private static List<List<String>> cartesianProduct(List<List<String>> lists) {
        List<List<String>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (List<String> options : lists) {
            List<List<String>> next = new ArrayList<>();
            for (List<String> partial : result) {
                for (String option : options) {
                    List<String> extended = new ArrayList<>(partial);
                    extended.add(option);
                    next.add(extended);
                }
            }
            result = next;
        }
        return result;
    }

    private static String resolveCategoryCaseInsensitive(String input, TreeSet<String> categories) {
        for (String c : categories) {
            if (c.equalsIgnoreCase(input)) return c;
        }
        return input;
    }

    private static int maxDistanceFor(String query) {
        if (query.length() <= 4) return 2;
        if (query.length() <= 8) return 3;
        return 4;
    }
}