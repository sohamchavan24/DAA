import java.util.*;

public class MultistageGraphDP {
    static final int INF = 1_000_000;
    static int n;
    static String[] label;
    static int[] stageOf;
    static List<List<int[]>> graph = new ArrayList<>();

    static void init(String[] names, int[] stages) {
        n = names.length;
        label = names;
        stageOf = stages;
        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
    }

    static void addEdge(int u, int v, int weight) {
        if (stageOf[v] != stageOf[u] + 1)
            throw new IllegalArgumentException("Edge must go from stage i to stage i+1");
        graph.get(u).add(new int[]{v, weight});
    }

    static void updateWeight(int u, int v, int newWeight) {
        for (int[] e : graph.get(u)) if (e[0] == v) e[1] = newWeight;
    }

    static void closeRoad(int u, int v) {
        updateWeight(u, v, INF);
    }

    // Backward DP: nodes are numbered stage by stage, so going from n-1 down to 0
    // processes stages K-1, K-2, ..., 1.
    static void solve(int sink, int[] cost, int[] next) {
        Arrays.fill(cost, INF);
        Arrays.fill(next, -1);
        cost[sink] = 0;
        for (int i = sink - 1; i >= 0; i--) {
            for (int[] e : graph.get(i)) {
                int j = e[0], w = e[1];
                if (cost[j] < INF && w < INF && w + cost[j] < cost[i]) {
                    cost[i] = w + cost[j];
                    next[i] = j;
                }
            }
        }
    }

    static String path(int src, int[] next, int sink) {
        StringBuilder sb = new StringBuilder();
        int i = src;
        while (i != -1) {
            sb.append(label[i]);
            if (i == sink) break;
            sb.append(" -> ");
            i = next[i];
        }
        return sb.toString();
    }

    static void report(String title, int source, int sink) {
        int[] cost = new int[n];
        int[] next = new int[n];
        solve(sink, cost, next);
        System.out.println(title);
        System.out.println("  Minimum cost from " + label[source] + " to " + label[sink] + " : " + cost[source]);
        System.out.println("  Optimal route : " + path(source, next, sink));
    }

    public static void main(String[] args) {
        // Stages: V1={A}, V2={B,C,D,E}, V3={F,G,H}, V4={I,J,K}, V5={L}
        String[] names = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};
        int[] stages = {1, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5};
        init(names, stages);
        int A = 0, B = 1, C = 2, D = 3, E = 4, F = 5, G = 6, H = 7, I = 8, J = 9, K = 10, L = 11;

        addEdge(A, B, 9);  addEdge(A, C, 7);  addEdge(A, D, 3);  addEdge(A, E, 2);
        addEdge(B, F, 4);  addEdge(B, G, 2);  addEdge(B, H, 1);
        addEdge(C, F, 2);  addEdge(C, G, 7);
        addEdge(D, G, 11); addEdge(D, H, 11);
        addEdge(E, G, 8);  addEdge(E, H, 11);
        addEdge(F, I, 6);  addEdge(F, J, 5);
        addEdge(G, I, 4);  addEdge(G, J, 3);  addEdge(G, K, 6);
        addEdge(H, J, 6);  addEdge(H, K, 4);
        addEdge(I, L, 4);  addEdge(J, L, 2);  addEdge(K, L, 7);

        int[] cost = new int[n];
        int[] next = new int[n];
        solve(L, cost, next);

        System.out.println("Backward DP table (cost to reach sink L from each node):\n");
        for (int i = 0; i < n; i++) {
            String nx = next[i] == -1 ? "-" : label[next[i]];
            System.out.println("Node " + label[i] + " (Stage " + stages[i] + ") : cost = " + cost[i] + ", next = " + nx);
        }
        System.out.println("\nMinimum delivery cost from A to L: " + cost[A]);
        System.out.println("Optimal Route: " + path(A, next, L));

        System.out.println("\n--- Real-time update 1: traffic jam on road G -> J (3 to 12) ---");
        updateWeight(G, J, 12);
        report("After traffic update:", A, L);

        System.out.println("\n--- Real-time update 2: road closure F -> J and H -> K ---");
        closeRoad(F, J);
        closeRoad(H, K);
        report("After road closures:", A, L);

        System.out.println("\n--- Batch processing: several delivery requests, one DP pass ---");
        solve(L, cost, next);
        int[] sources = {A, B, C, D, E, F};
        for (int s : sources) {
            System.out.println("Request from " + label[s] + " to " + label[L] + " : cost = " + cost[s]
                    + ", route = " + path(s, next, L));
        }
    }
}
