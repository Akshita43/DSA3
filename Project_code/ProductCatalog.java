import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ProductCatalog {

    private final Trie nameTrie = new Trie();
    private final Map<String, Product> productsByName = new HashMap<>();
    private final Map<String, List<String>> productsByCategory = new HashMap<>();

    public void loadFromCsv(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("name,")) continue;
                }

                String[] parts = line.split(",", 4);
                if (parts.length < 3) continue;

                String name = parts[0].trim().toLowerCase();
                String category = parts[1].trim();
                double price;
                try {
                    price = Double.parseDouble(parts[2].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                String description = parts.length >= 4 ? parts[3].trim() : "No description available.";
                if (description.startsWith("\"") && description.endsWith("\"") && description.length() > 1) {
                    description = description.substring(1, description.length() - 1);
                }

                nameTrie.insert(name);
                productsByName.put(name, new Product(name, category, price, description));
                productsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(name);
            }
        }
    }

    public Trie getNameTrie() {
        return nameTrie;
    }

    public int size() {
        return productsByName.size();
    }

    public Product getProduct(String name) {
        return productsByName.get(name.toLowerCase());
    }

    public List<String> getAllNames() {
        return nameTrie.getAllWords();
    }

    public List<String> getCategoryProducts(String category) {
        return productsByCategory.getOrDefault(category, new ArrayList<>());
    }

    public TreeSet<String> getAllCategories() {
        return new TreeSet<>(productsByCategory.keySet());
    }

    public List<Product> getAllProducts() {
        List<Product> all = new ArrayList<>();
        for (String name : getAllNames()) {
            all.add(productsByName.get(name));
        }
        return all;
    }

    public List<Product> getCategoryProductObjects(String category) {
        List<Product> result = new ArrayList<>();
        for (String name : getCategoryProducts(category)) {
            result.add(productsByName.get(name));
        }
        return result;
    }

    public List<Product> getSortedByPrice() {
        List<Product> all = getAllProducts();
        all.sort((a, b) -> Double.compare(a.price, b.price));
        return all;
    }
}