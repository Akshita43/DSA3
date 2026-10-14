import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ShoppingCart
 * ------------
 * Simple cart backed by a HashMap (LinkedHashMap to preserve insertion
 * order for display) mapping product name -> quantity. Demonstrates a
 * classic use of a hash-based structure: O(1) add/update/lookup by key.
 */
public class ShoppingCart {

    private final Map<String, Integer> items = new LinkedHashMap<>();

    public void addItem(String productName, int quantity) {
        if (quantity <= 0) return;
        items.merge(productName.toLowerCase(), quantity, Integer::sum);
    }

    public boolean removeItem(String productName) {
        return items.remove(productName.toLowerCase()) != null;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    /** Prints the cart contents and returns the total price. */
    public double printCartAndGetTotal(ProductCatalog catalog) {
        double total = 0.0;
        System.out.println("\n--- Your Cart ---");
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            Product product = catalog.getProduct(entry.getKey());
            if (product == null) continue; // safety check
            int qty = entry.getValue();
            double subtotal = product.price * qty;
            total += subtotal;
            System.out.printf("  %-15s x%-3d  Rs.%-10.2f Subtotal: Rs.%.2f%n",
                    capitalize(entry.getKey()), qty, product.price, subtotal);
        }
        System.out.printf("  ----------------------------------------%n");
        System.out.printf("  Total: Rs.%.2f%n", total);
        return total;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}