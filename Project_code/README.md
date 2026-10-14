# SmartCart — build & run

## Compile
```
javac -encoding UTF-8 *.java
```

## Run
```
java Main
```
(Make sure `products.csv` and `dictionary.txt` are in the same folder you run this from —
the program looks for them in the current directory.)

## What's in this build

Algorithms implemented, matching the ones named in the abstract/report:

| Algorithm              | File                     | Used for |
|-------------------------|--------------------------|----------|
| Trie                     | `Trie.java`              | Storing catalog names / dictionary words |
| Aho-Corasick             | `AhoCorasick.java`       | Multi-keyword search (menu 6) |
| KMP                      | `KMP.java`               | Exact phrase search in descriptions (menu 13) |
| Rabin-Karp               | `RabinKarp.java`         | Substring/keyword search (menu 5) |
| Wagner-Fischer (Levenshtein DP) | `FuzzySearchEngine.java` | Spell correction / fuzzy search |
| Damerau-Levenshtein      | `DamerauLevenshtein.java`| Spell correction with transposition typos (menu 14) |
| Edmonds-Karp max-flow    | `MaxFlowEdmondsKarp.java`| Warehouse fulfillment (menu 11) |
| Binary search            | `PriceSearch.java`       | Price range filter (menu 8) |
| Heap / PriorityQueue     | `TopNFinder.java`        | Top-N cheapest/priciest (menu 9) |

### What was added in this pass
1. **`KMP.java`** — the abstract described KMP for exact phrase search, but no
   implementation existed. Added with the standard LPS-array construction and
   linear-time search; wired in as menu option **13**.
2. **`DamerauLevenshtein.java`** — the abstract also described this for
   transposition-typo correction; added as a standalone DP class and wired
   into a new **menu option 14** that compares it side-by-side with
   Wagner-Fischer on the same query.
3. **`dictionary.txt`** — a ~1,700-word shopping-domain dictionary (catalog
   vocabulary + common brand/color/material/category terms), loaded through
   the `Trie.loadFromFile(...)` method that already existed in `Trie.java`
   but was never actually called anywhere. This is what the report's
   "948 unique shopping terms" / "1,753-word dictionary" claim describes —
   previously the program only ever built its dictionary from the ~350
   words that appear in the 2,175 product *names*, ignoring descriptions
   entirely and never touching a real dictionary file.
4. **Menu option 15 — algorithm benchmark** — times Rabin-Karp vs KMP vs
   Aho-Corasick on the same keyword across all descriptions, and prints
   each algorithm's asymptotic complexity next to its measured time, plus
   a similar complexity comparison for Wagner-Fischer vs Damerau-Levenshtein.
   This is the "comparison of algorithms" piece for the review.

### Note on compiling
This was written and checked by hand (brace-matched, syntax-reviewed) but
**not run through an actual `javac`**, since no JDK was available in this
tool environment. Please compile it yourself before your review — if
anything doesn't compile, the error message will point straight at the
line to fix, and the new code is short enough (three small files plus a
few added methods in `Main.java`) that it should be quick to patch.
