import java.util.ArrayList;
import java.util.List;

/**
 * PriceSearch
 * -----------
 * Finds all products whose price falls within [minPrice, maxPrice], using
 * Binary Search on a price-sorted product list instead of scanning every
 * item (linear search).
 *
 * Approach: binary search for the FIRST index whose price >= minPrice
 * (lower bound), and the LAST index whose price <= maxPrice (upper bound).
 * Everything between those two indices (inclusive) is the answer -
 * O(log n) to find each boundary instead of O(n) to scan the whole list.
 */
public class PriceSearch {

    /** Returns products priced between minPrice and maxPrice (inclusive),
     *  given a list already sorted ascending by price. */
    public static List<Product> searchByRange(List<Product> sortedByPrice, double minPrice, double maxPrice) {
        List<Product> results = new ArrayList<>();
        if (sortedByPrice.isEmpty() || minPrice > maxPrice) return results;

        int lower = lowerBound(sortedByPrice, minPrice);
        int upper = upperBound(sortedByPrice, maxPrice);

        for (int i = lower; i <= upper && i < sortedByPrice.size(); i++) {
            results.add(sortedByPrice.get(i));
        }
        return results;
    }

    /** Binary search: first index whose price >= target. */
    private static int lowerBound(List<Product> list, double target) {
        int lo = 0, hi = list.size(); // hi = "not found yet" sentinel
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (list.get(mid).price < target) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    /** Binary search: last index whose price <= target. */
    private static int upperBound(List<Product> list, double target) {
        int lo = 0, hi = list.size();
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (list.get(mid).price <= target) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo - 1;
    }
}