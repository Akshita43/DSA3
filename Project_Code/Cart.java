import java.util.*;

/**
 * Cart.java  (NEW FEATURE)
 * ------------------------------------------------------------
 * A simple shopping cart: add a catalog product (with quantity),
 * remove one, view everything currently in it, and clear it out.
 * Keeps insertion order so the printed cart matches how items
 * were added.
 * ------------------------------------------------------------
 */
public class Cart {

    private final LinkedHashMap<String, Integer> items = new LinkedHashMap<>();

    public void add(String product, int quantity) {
        items.merge(product, quantity, Integer::sum);
    }

    public boolean remove(String product) {
        return items.remove(product) != null;
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int totalItemCount() {
        int total = 0;
        for (int qty : items.values()) total += qty;
        return total;
    }

    public void print() {
        if (items.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        System.out.println("\n--- Your Cart ---");
        int line = 1;
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            System.out.println(line + ". " + e.getKey() + "  (qty: " + e.getValue() + ")");
            line++;
        }
        System.out.println("Total items: " + totalItemCount());
    }
}
