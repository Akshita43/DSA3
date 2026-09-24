# SmartCart – A Dictionary-Based Fuzzy Search and Spell Correction Engine for Smart Shopping

A console-based shopping assistant in Java. It finds products even when the user makes spelling mistakes.

## Features

- Search with spell correction
- Autocomplete
- Keyword and exact phrase search
- Price range search and Top-N cheapest / costliest
- Trending searches
- Shopping cart
- Warehouse fulfillment check

## How to Run

javac -encoding UTF-8 *.java
java Main

Keep products.csv and dictionary.txt in the same folder.

## Menu and Algorithms

| Menu | What it does | Algorithm |
|---|---|---|
| 1 | Search with spell correction | Trie + Wagner-Fischer |
| 2 | Browse all items | Simple loop |
| 3 | Browse by category | HashMap + Trie |
| 4 | Autocomplete | Trie |
| 5 | Keyword search | Rabin-Karp |
| 6 | Many-keyword search | Aho-Corasick |
| 7 | Trending searches | HashMap |
| 8 | Price range | Binary Search |
| 9 | Top-N cheapest / costliest | Heap |
| 10 | Shopping cart | HashMap |
| 11 | Warehouse check | Edmonds-Karp (Max-Flow) |
| 12 | View catalog | Trie |
| 13 | Exact phrase search | KMP |
| 14 | Spell-check comparison | Wagner-Fischer vs Damerau-Levenshtein |
| 15 | Benchmark | Rabin-Karp vs KMP vs Aho-Corasick |
| 16 | Exit | None |

## Course Outcomes

| CO | What we did | Menu |
|---|---|---|
| CO1 | Picked the right algorithm for each problem, and compared them in a benchmark | 15 |
| CO2 | KMP and Rabin-Karp (Aho-Corasick is extra) | 13, 5, 6 |
| CO3 | Edit distance DP: Wagner-Fischer and Damerau-Levenshtein | 1, 14 |
| CO4 | Edmonds-Karp max-flow for warehouse fulfillment | 11 |

