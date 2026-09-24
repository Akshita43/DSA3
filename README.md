# 🛒 SmartCart – A Dictionary-Based Fuzzy Search and Spell Correction Engine for Smart Shopping

SmartCart is a **console-based e-commerce shopping assistant** that demonstrates how classic string-matching and edit-distance algorithms can be used to build a smart product search system.

The system allows users to search products using **multiple keywords, exact phrases, and misspelled words**. It also provides autocomplete suggestions, related-product recommendations, cart management, and recent search history.

The project is implemented in **Java** and organized into separate files for better readability, testing, and maintenance.

---

## ✨ Features

### 1. Multi-Keyword Product Filter
Uses the **Aho-Corasick algorithm** to search for multiple keywords simultaneously in product descriptions and display matching products.

### 2. Exact Phrase Search
Uses the **Knuth-Morris-Pratt (KMP) algorithm** to search for an exact phrase within the product catalog.

### 3. Typo-Tolerant Search
Uses:
- **Wagner-Fischer Edit Distance**
- **Damerau-Levenshtein Edit Distance**

to identify spelling mistakes and provide the **top 3 closest suggestions**.

### 4. Autocomplete / Suggest-as-you-type
Uses a **Trie** to suggest dictionary words that match the prefix entered by the user.

### 5. Related Products
Recommends products that share similar descriptive words with the selected product.

### 6. Shopping Cart
Allows users to:
- Add products
- Specify quantities
- View cart contents
- Remove products
- Clear the cart

### 7. Search History
Stores the user's **last 15 searches** across the different search features.

---

## 🧠 Algorithms and Data Structures

| Algorithm / Data Structure | Purpose |
|---|---|
| **Trie** | Stores dictionary words and supports autocomplete |
| **Aho-Corasick** | Multi-keyword product filtering |
| **KMP** | Exact phrase searching |
| **Wagner-Fischer** | Standard edit-distance calculation |
| **Damerau-Levenshtein** | Handles adjacent character transpositions |
| **ArrayList / Collections** | Stores and manages catalog, cart, and search data |

---

## 📁 Project Structure

```text
SmartCart/
│
├── data/
│   ├── dictionary.txt
│   └── catalog.txt
│
├── src/
│   ├── SmartCart.java
│   ├── FileLoader.java
│   ├── TrieNode.java
│   ├── AhoCorasick.java
│   ├── KMPMatcher.java
│   ├── EditDistance.java
│   ├── Autocomplete.java
│   ├── Recommender.java
│   ├── Cart.java
│   └── SearchHistory.java
│
└── README.md


Menu	What it does	Algorithm
1	Search with spell correction	Trie + Wagner-Fischer
2	Browse all items	Simple loop
3	Browse by category	HashMap + Trie
4	Autocomplete	Trie
5	Keyword search	Rabin-Karp
6	Many-keyword search	Aho-Corasick
7	Trending searches	HashMap
8	Price range	Binary Search
9	Top-N cheapest / costliest	Heap
10	Shopping cart	HashMap
11	Warehouse check	Edmonds-Karp (Max-Flow)
12	View catalog	Trie
13	Exact phrase search	KMP
14	Spell-check comparison	Wagner-Fischer vs Damerau-Levenshtein
15	Benchmark	Rabin-Karp vs KMP vs Aho-Corasick
16	Exit	None
