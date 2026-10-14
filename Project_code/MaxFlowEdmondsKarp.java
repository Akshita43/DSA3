import java.util.LinkedList;
import java.util.Queue;

/**
 * MaxFlowEdmondsKarp
 * ------------------
 * Generic max-flow solver over a graph given as a capacity matrix, using
 * the Edmonds-Karp method: repeatedly find an augmenting path from source
 * to sink using BFS (guaranteeing the SHORTEST augmenting path each time,
 * which is what separates Edmonds-Karp from plain Ford-Fulkerson and
 * bounds it at O(V * E^2)), push as much flow as the path's bottleneck
 * edge allows, and update the residual graph (subtract flow forward, add
 * flow backward so it can be "undone" later if a better routing exists).
 * Stops when no augmenting path remains - at that point, by the max-flow
 * min-cut theorem, the total flow pushed equals the min-cut capacity
 * separating source from sink.
 */
public class MaxFlowEdmondsKarp {

    private final int n;
    private final int[][] residual;   // capacity remaining on each edge
    private final int[][] original;   // original capacities, to recover flow later

    public MaxFlowEdmondsKarp(int nodeCount) {
        this.n = nodeCount;
        this.residual = new int[n][n];
        this.original = new int[n][n];
    }

    /** Adds a directed edge u -> v with the given capacity. */
    public void addEdge(int u, int v, int capacity) {
        residual[u][v] += capacity;
        original[u][v] += capacity;
    }

    /** Runs Edmonds-Karp and returns the maximum flow from source to sink. */
    public int computeMaxFlow(int source, int sink) {
        int totalFlow = 0;
        int[] parent = new int[n];

        while (bfsFindPath(source, sink, parent)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residual[u][v]);
            }
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow; // reverse edge, allows flow to be rerouted
            }
            totalFlow += pathFlow;
        }
        return totalFlow;
    }

    /** BFS over the residual graph looking for any path with spare capacity. */
    private boolean bfsFindPath(int source, int sink, int[] parent) {
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < n; v++) {
                if (!visited[v] && residual[u][v] > 0) {
                    parent[v] = u;
                    visited[v] = true;
                    if (v == sink) return true;
                    queue.add(v);
                }
            }
        }
        return false;
    }

    /** After computeMaxFlow() has run, returns how much flow actually went
     *  through edge u -> v (original capacity minus what's left). */
    public int flowOn(int u, int v) {
        return original[u][v] - residual[u][v];
    }
}