import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * TopNFinder
 * ----------
 * Finds the N cheapest or N most expensive products in a list, using a
 * PriorityQueue (heap) instead of sorting the entire list. This is the
 * classic heap-based "top-K" pattern: for the N most expensive items, keep
 * a MIN-heap of size N as we scan every product - any new item bigger than
 * the heap's smallest gets swapped in, and the smallest is evicted. This
 * costs O(P log N) for P products, cheaper than sorting all P items
 * (O(P log P)) when N is small.
 */
public class TopNFinder {

    /** Returns the N cheapest products, cheapest first. */
    public static List<Product> cheapest(List<Product> products, int n) {
        PriorityQueue<Product> maxHeap = new PriorityQueue<>(
                Comparator.comparingDouble((Product p) -> p.price).reversed());

        for (Product p : products) {
            maxHeap.offer(p);
            if (maxHeap.size() > n) {
                maxHeap.poll(); // evict the most expensive of the current top-N
            }
        }
        List<Product> result = new ArrayList<>(maxHeap);
        result.sort(Comparator.comparingDouble(p -> p.price)); // ascending: cheapest first
        return result;
    }

    /** Returns the N most expensive products, priciest first. */
    public static List<Product> mostExpensive(List<Product> products, int n) {
        PriorityQueue<Product> minHeap = new PriorityQueue<>(
                Comparator.comparingDouble(p -> p.price));

        for (Product p : products) {
            minHeap.offer(p);
            if (minHeap.size() > n) {
                minHeap.poll(); // evict the cheapest of the current top-N
            }
        }
        List<Product> result = new ArrayList<>(minHeap);
        result.sort(Collections.reverseOrder(Comparator.comparingDouble(p -> p.price))); // descending
        return result;
    }
}