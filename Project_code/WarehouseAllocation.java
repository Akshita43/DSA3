import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * WarehouseAllocation
 * -------------------
 * Applies Max-Flow (Edmonds-Karp) to a real "Smart Shopping" problem:
 * given the items and quantities in a customer's cart, and several
 * warehouses each holding limited stock of those items and having a
 * limited total daily dispatch capacity, how many units of the order can
 * actually be fulfilled, and which warehouse should supply what?
 *
 * Graph construction:
 *   source -> item_i        (capacity = quantity requested in the cart)
 *   item_i -> warehouse_j   (capacity = that warehouse's stock of item i)
 *   warehouse_j -> sink     (capacity = that warehouse's total dispatch capacity)
 *
 * The max flow from source to sink is exactly the maximum number of cart
 * units that can be dispatched given both per-item stock limits AND each
 * warehouse's overall capacity limit - a direct instance of bipartite
 * flow / assignment, the same pattern used for order-fulfillment and
 * resource-allocation problems in real e-commerce systems.
 */
public class WarehouseAllocation {

    private static final String[] WAREHOUSE_NAMES = {"Central Warehouse", "North Warehouse", "South Warehouse"};
    private static final int[] WAREHOUSE_CAPACITY = {12, 8, 6}; // total units each can dispatch

    /**
     * Runs the allocation for the given cart (product name -> quantity)
     * and prints a full breakdown: per-item fulfillment and per-warehouse
     * contribution, using simulated stock levels.
     */
    public static void run(Map<String, Integer> cartItems) {
        List<String> items = new ArrayList<>(cartItems.keySet());
        int numItems = items.size();
        int numWarehouses = WAREHOUSE_NAMES.length;

        // Node layout: 0 = source, 1..numItems = items,
        // numItems+1..numItems+numWarehouses = warehouses,
        // last node = sink.
        int source = 0;
        int sink = numItems + numWarehouses + 1;
        int totalNodes = sink + 1;

        MaxFlowEdmondsKarp flow = new MaxFlowEdmondsKarp(totalNodes);

        // Simulated stock per (item, warehouse) - deterministic pseudo-random
        // based on the item name and warehouse index, so results are
        // reproducible across runs for the same cart.
        int[][] stock = new int[numItems][numWarehouses];

        for (int i = 0; i < numItems; i++) {
            String item = items.get(i);
            int requested = cartItems.get(item);
            flow.addEdge(source, i + 1, requested);

            for (int w = 0; w < numWarehouses; w++) {
                int simulatedStock = simulateStock(item, w);
                stock[i][w] = simulatedStock;
                flow.addEdge(i + 1, numItems + 1 + w, simulatedStock);
            }
        }

        for (int w = 0; w < numWarehouses; w++) {
            flow.addEdge(numItems + 1 + w, sink, WAREHOUSE_CAPACITY[w]);
        }

        int maxFulfillable = flow.computeMaxFlow(source, sink);
        int totalRequested = cartItems.values().stream().mapToInt(Integer::intValue).sum();

        System.out.println("\n--- Warehouse Fulfillment Check (Max-Flow / Edmonds-Karp) ---");
        System.out.println("Total units requested : " + totalRequested);
        System.out.println("Total units fulfillable: " + maxFulfillable);
        if (maxFulfillable < totalRequested) {
            System.out.println("(Bottleneck: warehouse capacity or item stock limits some units - this");
            System.out.println(" shortfall corresponds to the min-cut in the flow network.)");
        }

        System.out.println("\nPer-item fulfillment:");
        for (int i = 0; i < numItems; i++) {
            String item = items.get(i);
            int requested = cartItems.get(item);
            int fulfilled = 0;
            for (int w = 0; w < numWarehouses; w++) {
                fulfilled += flow.flowOn(i + 1, numItems + 1 + w);
            }
            System.out.printf("  %-20s requested %-3d  fulfilled %-3d%n", item, requested, fulfilled);
        }

        System.out.println("\nPer-warehouse contribution:");
        for (int w = 0; w < numWarehouses; w++) {
            int dispatched = flow.flowOn(numItems + 1 + w, sink);
            System.out.printf("  %-20s dispatching %-3d / %-3d capacity%n",
                    WAREHOUSE_NAMES[w], dispatched, WAREHOUSE_CAPACITY[w]);
        }
    }

    /** Deterministic pseudo-random stock level so the same item/warehouse
     *  combination always gives the same simulated stock in a given run. */
    private static int simulateStock(String item, int warehouseIndex) {
        int hash = Math.abs((item + warehouseIndex).hashCode());
        return 2 + (hash % 6); // stock between 2 and 7 units
    }
}